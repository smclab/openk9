package io.openk9.searcher.resource;

import com.google.protobuf.ByteString;
import io.openk9.searcher.dto.SearchRequest;
import io.openk9.searcher.grpc.QueryParserRequest;
import io.openk9.searcher.grpc.QueryParserResponse;
import io.openk9.searcher.grpc.Searcher;
import io.openk9.searcher.mapper.SearcherMapper;
import io.openk9.searcher.payload.response.Response;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.io.stream.InputStreamStreamInput;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/v1/search")
public class SearchResource {

	@Context
	HttpServerRequest request;

	@POST
	public Uni<Response> search(SearchRequest searchRequest) {

		QueryParserRequest queryParserRequest =
			searcherMapper
				.toQueryParserRequest(searchRequest)
				.toBuilder()
				.setVirtualHost(request.host())
				.build();

		logger.info("queryParserRequest: " + queryParserRequest);

		Uni<QueryParserResponse> queryParserResponseUni =
			searcherClient.queryParser(queryParserRequest);

		return queryParserResponseUni
			.flatMap(queryParserResponse -> {

				org.elasticsearch.action.search.SearchRequest searchRequestElastic =
					_decodeElasticSearchRequest(
						queryParserResponse.getQuery(),
						queryParserResponse.getIndexNameList().toArray(String[]::new));

				return Uni.createFrom().<SearchResponse>emitter((sink) ->
					client.searchAsync(
						searchRequestElastic, RequestOptions.DEFAULT,
						new ActionListener<>() {
							@Override
							public void onResponse(
								SearchResponse searchResponse) {
								sink.complete(searchResponse);
							}

							@Override
							public void onFailure(Exception e) {
								sink.fail(e);
							}
						}))
					.map(this::_toSearchResponse);

			});

	}

	private org.elasticsearch.action.search.SearchRequest _decodeElasticSearchRequest(
		ByteString query, String[] indices) {

		try(XContentParser parser = JsonXContent.jsonXContent.createParser(
			NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
			new InputStreamStreamInput(query.newInput()))) {

			SearchSourceBuilder searchSourceBuilder =
				SearchSourceBuilder.fromXContent(parser);

			return new org.elasticsearch.action.search.SearchRequest(indices, searchSourceBuilder);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private Response _toSearchResponse(SearchResponse searchResponse) {
		_printShardFailures(searchResponse);

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

		return new Response(result, totalHits.value);
	}

	private void _printShardFailures(SearchResponse searchResponse) {
		if (searchResponse.getShardFailures() != null) {
			for (ShardSearchFailure failure : searchResponse.getShardFailures()) {
				logger.warn(failure.reason());
			}
		}
	}

	@Inject
	SearcherMapper searcherMapper;

	@Inject
	RestHighLevelClient client;

	@GrpcClient("searcher")
	Searcher searcherClient;

	@Inject
	Logger logger;

}
