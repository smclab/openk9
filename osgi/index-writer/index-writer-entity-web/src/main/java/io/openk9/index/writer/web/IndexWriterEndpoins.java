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
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
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
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Component(immediate = true, service = IndexWriterEndpoins.class)
public class IndexWriterEndpoins extends BaseEndpointRegister {

	public static final String AND = "_AND";

	public static final String EXACT = "_EXACT";

	@Activate
	public void activate(BundleContext bundleContext) {

		setBundleContext(bundleContext);

		this.registerEndpoint(
			HttpHandler.delete("/clean-orphan-entities/{tenantId}", this::_cleanOrphanEntities),
			HttpHandler.post("/get-entities/{tenantId}", this::_getEntities),
			HttpHandler.post("/", this::_insertEntity)
		);

	}

	private Publisher<Void> _cleanOrphanEntities(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		Mono<Object> cleanOrphanEntities =
			Mono.create(sink -> {

			long tenantId = Long.parseLong(httpRequest.pathParam("tenantId"));

			RestHighLevelClient client =
				_restHighLevelClientProvider.get();

			try {

				final Scroll scroll =
					new Scroll(TimeValue.timeValueMinutes(1L));

				String entityIndexName = tenantId + "-entity";

				SearchRequest searchRequest =
					new SearchRequest(entityIndexName);
				searchRequest.scroll(scroll);
				SearchSourceBuilder searchSourceBuilder =
					new SearchSourceBuilder();
				searchRequest.source(searchSourceBuilder);

				SearchResponse searchResponse =
					client.search(searchRequest, RequestOptions.DEFAULT);

				String scrollId = searchResponse.getScrollId();

				SearchHit[] searchHits = searchResponse.getHits().getHits();

				Collection<String> entitiesToDelete = new HashSet<>();
				Collection<String> entityNames = new ArrayList<>();

				while (searchHits != null && searchHits.length > 0) {

					for (SearchHit searchHit : searchHits) {

						Map<String, Object> source = searchHit.getSourceAsMap();

						Object id = source.get("id");
						Object type = source.get("type");
						String name = String.valueOf(source.get("name"));

						String nestEntityPath = "entities." + type;

						String nestIdPath = nestEntityPath + ".id";

						CountRequest countRequest =
							new CountRequest(tenantId + "-*-data");

						countRequest.query(
							QueryBuilders
								.nestedQuery(
									nestEntityPath,
									matchQuery(nestIdPath, id),
									ScoreMode.Max)
								.ignoreUnmapped(true)
							);

						CountResponse countResponse =
							client.count(countRequest, RequestOptions.DEFAULT);

						if (countResponse.getCount() == 0) {

							entitiesToDelete.add(searchHit.getId());
							entityNames.add(name);

						}

					}

					SearchScrollRequest scrollRequest =
						new SearchScrollRequest(scrollId);

					scrollRequest.scroll(scroll);

					searchResponse =
						client.scroll(scrollRequest, RequestOptions.DEFAULT);

					scrollId = searchResponse.getScrollId();

					searchHits = searchResponse.getHits().getHits();

				}

				ClearScrollRequest clearScrollRequest =
					new ClearScrollRequest();
				clearScrollRequest.addScrollId(scrollId);
				ClearScrollResponse clearScrollResponse =
					client.clearScroll(
						clearScrollRequest, RequestOptions.DEFAULT);

				boolean succeeded = clearScrollResponse.isSucceeded();

				if (!entitiesToDelete.isEmpty()) {

					BulkRequest bulkRequest = new BulkRequest();

					bulkRequest.add(
						entitiesToDelete
							.stream()
							.map(id -> new DeleteRequest(entityIndexName, id))
							.collect(Collectors.toList())
					);

					BulkResponse bulkResponse =
						client.bulk(bulkRequest, RequestOptions.DEFAULT);

				}

				sink.success(entityNames);

			}
			catch (Exception e) {
				sink.error(e);
			}
		});

		return _httpResponseWriter.write(
			httpResponse,
			cleanOrphanEntities.publishOn(Schedulers.single())
		);

	}

	private Publisher<Void> _insertEntity(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		RestHighLevelClient restHighLevelClient =
			_restHighLevelClientProvider.get();

		Mono<List<DocumentEntityRequest>> request =
			Mono
				.from(httpRequest.aggregateBodyToByteArray())
				.map(json -> _jsonFactory.fromJsonList(json, DocumentEntityRequest.class));

		Mono<BulkResponse> elasticResponse =
			request
				.flatMapIterable(Function.identity())
				.map(entity -> {

					IndexRequest indexRequest =
						new IndexRequest(entity.getTenantId() + "-entity");

					return indexRequest.source(
						_jsonFactory.toJson(entity), XContentType.JSON);

				})
				.reduce(new BulkRequest(), BulkRequest::add)
				.flatMap(bulkRequest ->
					Mono.create(sink -> {

						bulkRequest.setRefreshPolicy(
							WriteRequest.RefreshPolicy.WAIT_UNTIL);

						Cancellable cancellable =
							restHighLevelClient
								.bulkAsync(
									bulkRequest, RequestOptions.DEFAULT,
									new ReactorActionListener<>(sink));

						sink.onCancel(cancellable::cancel);

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
					matchQuery(
							fieldAndOperator,
							entry.getValue())
						.operator(Operator.AND);

			}
			else if (fieldName.endsWith(EXACT)) {

				String fieldAndOperator =
					fieldName.substring(0, fieldName.length() - EXACT.length());

				matchQueryBuilder =
					matchQuery(
						fieldAndOperator,
						entry.getValue())
						.operator(Operator.AND);
			}
			else {
				matchQueryBuilder =
					matchQuery(
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