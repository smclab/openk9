package io.openk9.search.query.internal.websocket;

import io.openk9.datasource.client.api.DatasourceClient;
import io.openk9.http.socket.WebSocketHandler;
import io.openk9.http.socket.WebSocketSession;
import io.openk9.http.util.HttpUtil;
import io.openk9.http.web.Endpoint;
import io.openk9.http.web.HttpRequest;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.Datasource;
import io.openk9.model.Tenant;
import io.openk9.plugin.driver.manager.client.api.PluginDriverManagerClient;
import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.SearchKeywordDTO;
import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchToken;
import io.openk9.search.client.api.Search;
import io.openk9.search.client.api.util.SearchUtil;
import io.openk9.search.query.internal.response.Response;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
	immediate = true,
	service = Endpoint.class
)
public class SearchWebSocketHandler implements WebSocketHandler {

	@Override
	public String getPath() {
		return "/v1/ws/search";
	}

	@Override
	public int getMaxFramePayloadLength() {
		return Integer.MAX_VALUE;
	}

	@Override
	public Publisher<Void> apply(
		WebSocketSession webSocketSession) {

		return Flux
			.from(webSocketSession.receive())
			.flatMap(webSocketMessage ->
				_handleMessage(
					webSocketSession,
					_jsonFactory.fromJson(
						webSocketMessage.getPayloadAsString(),
						WSSearchMessage.class)));
	}

	private Publisher<Void> _handleMessage(
		WebSocketSession webSocketSession, WSSearchMessage wsSearchMessage) {

		HttpRequest request = webSocketSession.getRequest();

		String hostName = HttpUtil.getHostName(request);

		return _getTenant(hostName)
			.flatMap(tenant ->
				_findDatasourceByTenantIdAndIsActive(tenant.getTenantId())
					.flatMap(datasources -> _toQuerySearchRequest(
						tenant, datasources, wsSearchMessage, request)
						.map(this::_searchHitToResponse)
						.map(_jsonFactory::toJson)
					)
			)
			.map(webSocketSession::textMessage)
			.transform(webSocketSession::send);
	}

	private Mono<Tenant> _getTenant(String hostName) {
		return _datasourceClient
			.findTenantByVirtualHost(hostName)
			.next()
			.switchIfEmpty(
				Mono.error(
					() -> new RuntimeException(
						"tenant not found for virtualhost: " + hostName)));
	}

	private Mono<List<Datasource>> _findDatasourceByTenantIdAndIsActive(
		long tenantId) {

		return _datasourceClient
			.findDatasourceByTenantIdAndIsActive(tenantId)
			.collectList();

	}

	private Mono<SearchResponse> _toQuerySearchRequest(
		Tenant tenant, List<Datasource> datasources, WSSearchMessage searchRequest,
		HttpRequest httpRequest) {

		return Mono.defer(() -> {

			List<String> serviceDriverNames =
				new ArrayList<>(datasources.size());

			for (Datasource datasource : datasources) {

				String driverServiceName = datasource.getDriverServiceName();

				if (!serviceDriverNames.contains(driverServiceName)) {
					serviceDriverNames.add(driverServiceName);
				}

			}

			return _pluginDriverManagerClient.getPluginDriverList(serviceDriverNames);

		}).flatMap(pluginDriverList -> {

			List<PluginDriverDTO> pluginDriverDTOList =
				pluginDriverList.getPluginDriverDTOList();

			Map<String, List<SearchToken>> tokenTypeGroup =
				searchRequest
					.getSearchQuery()
					.stream()
					.collect(Collectors.groupingBy(SearchToken::getTokenType));

			List<SearchToken> datasource = tokenTypeGroup.get("DATASOURCE");

			Stream<PluginDriverDTO> documentTypeStream =
				pluginDriverDTOList.stream();

			if (datasource != null) {

				List<String> datasourceValues = datasource
					.stream()
					.map(SearchToken::getValues)
					.flatMap(Arrays::stream)
					.distinct()
					.collect(Collectors.toList());

				documentTypeStream =
					documentTypeStream
						.filter(entry ->
							datasourceValues.contains(entry.getName()));

			}

			List<PluginDriverDTO> documentTypeList =
				documentTypeStream
					.collect(Collectors.toList());

			QueryParser queryParser =
				_queryParsers
					.stream()
					.reduce(QueryParser.NOTHING, QueryParser::andThen);

			return queryParser.apply(
				QueryParser.Context.of(
					tenant,
					datasources,
					documentTypeList,
					tokenTypeGroup,
					httpRequest
				)
			).flatMap(boolQueryBuilderConsumer ->
				_search.search(factory -> {

					long tenantId = tenant.getTenantId();

					if (documentTypeList.isEmpty()) {
						return SearchUtil.EMPTY_SEARCH_REQUEST;
					}

					BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

					boolQueryBuilderConsumer.accept(boolQuery);

					org.elasticsearch.action.search.SearchRequest elasticSearchQuery;

					if (datasource != null) {

						String[] indexNames = documentTypeList
							.stream()
							.map(PluginDriverDTO::getName)
							.distinct()
							.toArray(String[]::new);

						elasticSearchQuery =
							factory.createSearchRequestData(
								tenantId, indexNames);
					}
					else {
						elasticSearchQuery =
							factory.createSearchRequestData(tenantId, "*");
					}

					SearchSourceBuilder searchSourceBuilder =
						new SearchSourceBuilder();

					searchSourceBuilder.query(boolQuery);

					customizeSearchSourceBuilder(
						tenant, datasources, searchRequest, documentTypeList,
						searchSourceBuilder);

					if (_log.isDebugEnabled()) {
						_log.debug(searchSourceBuilder.toString());
					}

					return elasticSearchQuery.source(searchSourceBuilder);

				}));

		});

	}

	private void customizeSearchSourceBuilder(
		Tenant tenant, List<Datasource> datasources, WSSearchMessage searchRequest,
		List<PluginDriverDTO> documentTypeList, SearchSourceBuilder searchSourceBuilder) {

		int[] range = searchRequest.getRange();

		searchSourceBuilder.from(range[0]);
		searchSourceBuilder.size(range[1]);

		HighlightBuilder highlightBuilder = new HighlightBuilder();

		documentTypeList
			.stream()
			.map(PluginDriverDTO::getDocumentTypes)
			.flatMap(Collection::stream)
			.map(DocumentTypeDTO::getSearchKeywords)
			.flatMap(Collection::stream)
			.filter(SearchKeywordDTO::isText)
			.map(SearchKeywordDTO::getKeyword)
			.distinct()
			.forEach(highlightBuilder::field);

		highlightBuilder.forceSource(true);

		highlightBuilder.tagsSchema("default");

		searchSourceBuilder.highlighter(highlightBuilder);

	}

	private Response _searchHitToResponse(SearchResponse searchResponse) {

		SearchHits hits = searchResponse.getHits();

		List<Map<String, Object>> result = new ArrayList<>();

		for (SearchHit hit : hits.getHits()) {

			Map<String, Object> sourceAsMap = hit.getSourceAsMap();

			Map<String, Object> sourceMap = new HashMap<>(
				sourceAsMap.size() + 1, 1);

			sourceMap.putAll(sourceAsMap);

			sourceMap.put("id", hit.getId());

			Map<String, HighlightField> highlightFields =
				hit.getHighlightFields();

			Map<String, Object> highlightMap = new HashMap<>(
				highlightFields.size(), 1);

			for (HighlightField value : highlightFields.values()) {
				highlightMap.put(
					value.getName(),
					Arrays
						.stream(value.getFragments())
						.map(Text::string)
						.toArray(String[]::new)
				);
			}

			Map<String, Object> hitMap = new HashMap<>(2, 1);

			hitMap.put("source", sourceMap);
			hitMap.put("highlight", highlightMap);

			result.add(hitMap);

		}

		TotalHits totalHits = hits.getTotalHits();

		return new Response(
			result,
			totalHits.value,
			totalHits.relation == TotalHits.Relation.EQUAL_TO
		);
	}

	@Reference(
		service = QueryParser.class,
		bind = "addQueryParser",
		unbind = "removeQueryParser",
		cardinality = ReferenceCardinality.MULTIPLE,
		policyOption = ReferencePolicyOption.GREEDY,
		policy = ReferencePolicy.DYNAMIC
	)
	private void addQueryParser(QueryParser queryParser) {
		_queryParsers.add(queryParser);
	}

	private void removeQueryParser(QueryParser queryParser) {
		_queryParsers.remove(queryParser);
	}

	private final List<QueryParser> _queryParsers = new ArrayList<>();

	@Reference
	private DatasourceClient _datasourceClient;

	@Reference
	private Search _search;

	@Reference
	private PluginDriverManagerClient _pluginDriverManagerClient;

	@Reference
	private JsonFactory _jsonFactory;

	private static final Logger _log = LoggerFactory.getLogger(
		SearchWebSocketHandler.class);

}
