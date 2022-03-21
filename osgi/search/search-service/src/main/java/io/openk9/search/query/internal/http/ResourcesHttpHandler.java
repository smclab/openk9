package io.openk9.search.query.internal.http;

import io.openk9.datasource.client.api.DatasourceClient;
import io.openk9.http.util.HttpUtil;
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.BinaryPayload;
import io.openk9.model.Datasource;
import io.openk9.model.ResourcesPayload;
import io.openk9.model.Tenant;
import io.openk9.plugin.driver.manager.client.api.PluginDriverManagerClient;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.search.api.query.QueryParser;
import io.openk9.search.client.api.Search;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class ResourcesHttpHandler implements RouterHandler {

	@Activate
	void activate(BundleContext bundleContext) {
		lastModifiedDate = Instant.ofEpochMilli(
			bundleContext.getBundle().getLastModified());
	}

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router.get(
			"/resources/{datasourceId}/{documentId}/{resourceId}",
			this::_getResources);
	}

	private Publisher<Void> _getResources(
		HttpServerRequest request, HttpServerResponse response) {

		String hostName = HttpUtil.getHostName(request);

		long datasourceId = NumberUtils.toLong(request.param("datasourceId"));
		String documentId = request.param("documentId");
		String resourceId = request.param("resourceId");

		Mono<Tenant> tenantMono =
			_datasourceClient
				.findTenantByVirtualHost(hostName)
				.next()
				.switchIfEmpty(
					Mono.error(
						() -> new RuntimeException(
							"tenant not found with virtualhost: " + hostName)));

		Mono<Datasource> datasourceMono = _datasourceClient
			.findDatasource(datasourceId)
			.switchIfEmpty(
				Mono.error(
					() -> new RuntimeException(
						"datasource not found with datasourceId: "
						+ datasourceId)));

		return tenantMono
			.zipWith(datasourceMono)
			.flatMap(
				t2 -> _pluginDriverManagerClient
					.getPluginDriver(t2.getT2().getDriverServiceName())
					.flatMap(pd -> _sendResource(
						t2.getT1(), t2.getT2(), pd, datasourceId, documentId,
						resourceId, request, response))
			);

	}

	private void _manageCache(
		HttpServerRequest request, HttpServerResponse response) {

		Map<String, List<String>> queryParams =
			HttpUtil.getQueryParams(request);

		List<String> modifiedDate = queryParams.get("t");

		Instant lastModifiedDate = this.lastModifiedDate;

		if (modifiedDate != null && !modifiedDate.isEmpty()) {
			long t = NumberUtils.toLong(modifiedDate.get(0));
			lastModifiedDate = Instant.ofEpochMilli(t);
		}

		response.header("Cache-Control", "max-age=31536000, public");
		response.header(
			"Expires",
			DateTimeFormatter
				.RFC_1123_DATE_TIME
				.format(
					ZonedDateTime
						.ofInstant(Instant.now(), ZoneOffset.UTC)
						.plus(10, ChronoUnit.YEARS)
				)
		);
		response.header(
			"Last-Modified",
			DateTimeFormatter.RFC_1123_DATE_TIME.format(
				ZonedDateTime.ofInstant(lastModifiedDate, ZoneOffset.UTC))
		);

	}

	private Mono<Void> _sendResource(
		Tenant tenant, Datasource datasource , PluginDriverDTO pluginDriverDTO,
		long datasourceId, String documentId, String resourceId,
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		return _queryParser.apply(
			QueryParser.Context.of(tenant, null, null, null, httpRequest, QueryParser.QueryCondition.DEFAULT))
			.flatMap(consumer ->

				_search.search(factory -> {

					SearchRequest searchRequest =
						factory.createSearchRequestData(
							tenant.getTenantId(), pluginDriverDTO.getName());

					BoolQueryBuilder boolQueryBuilder =
						QueryBuilders.boolQuery();

					boolQueryBuilder.must(
						QueryBuilders
							.idsQuery()
							.addIds(documentId)
					);

					boolQueryBuilder.must(
						QueryBuilders.nestedQuery(
							_RESOURCES_BINARIES,
							QueryBuilders.matchQuery(
								_RESOURCES_BINARIES_ID, resourceId),
							ScoreMode.Max
						)
					);

					SearchSourceBuilder searchSourceBuilder =
						new SearchSourceBuilder();

					consumer.accept(boolQueryBuilder);

					searchSourceBuilder.query(boolQueryBuilder);

					searchSourceBuilder.fetchSource(
						new String[] {
							_RESOURCES_BINARIES_DATA,
							_RESOURCES_BINARIES_CONTENT_TYPE
						}, null);

					return searchRequest.source(searchSourceBuilder);
			}))
			.flatMap(response -> {

				SearchHits searchHits = response.getHits();

				SearchHit[] hits = searchHits.getHits();

				if (hits.length == 0) {
					return httpResponse.sendNotFound();
				}
				else if (hits.length > 1) {
					_log.warn(
						"found more than one resource (datasourceId: "
						+ datasourceId + " resourceId: "
						+ resourceId + " tenantId: "
						+ tenant.getTenantId()  + " documentId: "
						+ documentId + ")"
					);
				}

				SearchHit hit = hits[0];

				String source = hit.getSourceAsString();

				Resources resources =
					_jsonFactory.fromJson(source, Resources.class);

				ResourcesPayload resourcesPayload = resources.getResources();

				BinaryPayload binaryPayload =
					resourcesPayload.getBinaries().get(0);

				String data = binaryPayload.getData();

				String contentType = binaryPayload.getContentType();

				_manageCache(httpRequest, httpResponse);

				if (contentType != null && !contentType.isBlank()) {
					httpResponse.header(_CONTENT_TYPE, contentType);
				}

				byte[] decode = Base64.getDecoder().decode(data);

				return Mono.from(httpResponse.sendByteArray(Mono.just(decode)));

			});
	}

	private Instant lastModifiedDate;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private DatasourceClient _datasourceClient;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private Search _search;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private PluginDriverManagerClient _pluginDriverManagerClient;

	@Reference(
		target = "(component.name=io.openk9.auth.query.parser.AuthQueryParser)",
		policyOption = ReferencePolicyOption.GREEDY
	)
	private QueryParser _queryParser;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private JsonFactory _jsonFactory;

	private static final String _RESOURCES_BINARIES_CONTENT_TYPE =
		"resources.binaries.contentType";

	private static final String _RESOURCES_BINARIES_DATA =
		"resources.binaries.data";

	private static final String _RESOURCES_BINARIES = "resources.binaries";

	private static final String _RESOURCES_BINARIES_ID =
		"resources.binaries.id";

	private static final String _CONTENT_TYPE = "Content-Type";

	private static final Logger _log = LoggerFactory.getLogger(
		ResourcesHttpHandler.class);

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	public static class Resources {
		private ResourcesPayload resources;
	}

}
