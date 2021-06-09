package io.openk9.index.writer.web;


import io.openk9.http.util.BaseEndpointRegister;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.index.writer.entity.model.DocumentEntityRequest;
import io.openk9.json.api.JsonFactory;
import io.openk9.search.client.api.ReactorActionListener;
import io.openk9.search.client.api.RestHighLevelClientProvider;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(immediate = true, service = IndexWriterEndpoins.class)
public class IndexWriterEndpoins extends BaseEndpointRegister {

	public static final String AND = "_AND";

	@Activate
	public void activate(BundleContext bundleContext) {

		setBundleContext(bundleContext);

		this.registerEndpoint(
			HttpHandler.post("/get-entities/{tenantId}", this::_getEntities),
			HttpHandler.post("/", this::_insertEntity)
		);

	}

	private Publisher<Void> _insertEntity(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		RestHighLevelClient restHighLevelClient =
			_restHighLevelClientProvider.get();

		Mono<DocumentEntityRequest> request =
			Mono
				.from(httpRequest.aggregateBodyToByteArray())
				.map(json -> _jsonFactory.fromJson(json, DocumentEntityRequest.class));

		Mono<IndexResponse> elasticResponse =
			request.flatMap(entity ->
				Mono.create(sink -> {

					IndexRequest indexRequest =
						new IndexRequest(entity.getTenantId() + "-entity");

					indexRequest.source(
						_jsonFactory.toJson(entity), XContentType.JSON);

					indexRequest.setRefreshPolicy(
						WriteRequest.RefreshPolicy.WAIT_UNTIL);

					restHighLevelClient
						.indexAsync(
							indexRequest, RequestOptions.DEFAULT,
							new ReactorActionListener<>(sink));
				}));

		return _httpResponseWriter.write(
			httpResponse, elasticResponse.thenReturn("{}"));

	}

	private Publisher<Void> _getEntities(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		long tenantId = Long.parseLong(httpRequest.pathParam("tenantId"));

		Mono<List<Map<String, Object>>> response =
			Mono
				.from(httpRequest.aggregateBodyToString())
				.map(json -> _jsonFactory.fromJsonMap(json, Object.class))
				.map(this::_toQuery)
				.flatMap(queryBuilder -> _executeQuery(tenantId, queryBuilder))
				.flatMapIterable(SearchResponse::getHits)
				.map(this::_hitToResponse)
				.collectList()
				.onErrorResume(throwable -> {
					if (_log.isErrorEnabled()) {
						_log.error(throwable.getMessage(), throwable);
					}
					return Mono.empty();
				})
				.defaultIfEmpty(List.of());

		return _httpResponseWriter.write(httpResponse, response);

	}

	private Map<String, Object> _hitToResponse(SearchHit hit) {

		Map<String, Object> response = new HashMap<>();

		response.put("score", hit.getScore());

		response.putAll(hit.getSourceAsMap());

		return response;
	}

	private Mono<SearchResponse> _executeQuery(
		long tenantId, QueryBuilder queryBuilder) {

		return Mono.create(sink -> {

			RestHighLevelClient restHighLevelClient =
				_restHighLevelClientProvider.get();

			SearchRequest searchRequest = new SearchRequest(tenantId + "-entity");

			SearchSourceBuilder searchSourceBuilder =
				new SearchSourceBuilder();

			searchSourceBuilder.query(queryBuilder);

			searchRequest.source(searchSourceBuilder);

			restHighLevelClient
				.searchAsync(
					searchRequest, RequestOptions.DEFAULT,
					new ReactorActionListener<>(sink));

		});
	}

	private QueryBuilder _toQuery(Map<String, Object> queryObject) {

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		for (Map.Entry<String, Object> entry : queryObject.entrySet()) {

			String fieldName = entry.getKey();

			MatchQueryBuilder matchQueryBuilder;

			if (fieldName.endsWith(AND)) {

				String fieldAndOperator =
					fieldName.substring(0, fieldName.length() - AND.length());

				matchQueryBuilder =
					QueryBuilders
						.matchQuery(
							fieldAndOperator,
							entry.getValue())
						.operator(Operator.AND);

			}
			else {
				matchQueryBuilder =
					QueryBuilders
						.matchQuery(
							fieldName,
							entry.getValue());
			}

			boolQueryBuilder.must(matchQueryBuilder);


		}

		return boolQueryBuilder;
	}

	@Deactivate
	public void deactivate() {
		this.close();
	}


	@Override
	public String getBasePath() {
		return "/v1";
	}

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private HttpResponseWriter _httpResponseWriter;

	@Reference
	private RestHighLevelClientProvider _restHighLevelClientProvider;

	private static final Logger _log = LoggerFactory.getLogger(
		IndexWriterEndpoins.class);

}