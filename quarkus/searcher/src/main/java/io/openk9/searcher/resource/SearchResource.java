package io.openk9.searcher.resource;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import io.openk9.searcher.client.dto.SearchRequest;
import io.openk9.searcher.client.mapper.SearcherMapper;
import io.openk9.searcher.grpc.QueryAnalysisRequest;
import io.openk9.searcher.grpc.QueryAnalysisResponse;
import io.openk9.searcher.grpc.QueryAnalysisSearchToken;
import io.openk9.searcher.grpc.QueryAnalysisTokens;
import io.openk9.searcher.grpc.QueryParserRequest;
import io.openk9.searcher.grpc.QueryParserResponse;
import io.openk9.searcher.grpc.Searcher;
import io.openk9.searcher.mapper.InternalSearcherMapper;
import io.openk9.searcher.payload.response.Response;
import io.openk9.searcher.payload.response.SuggestionsResponse;
import io.openk9.searcher.queryanalysis.QueryAnalysisToken;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import org.apache.http.HttpEntity;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.CheckedFunction;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Path("/v1")
public class SearchResource {

	@Context
	HttpServerRequest request;

	@POST
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> search(SearchRequest searchRequest) {

		QueryParserRequest queryParserRequest =
			getQueryParserRequest(searchRequest);

		Uni<QueryParserResponse> queryParserResponseUni =
			searcherClient.queryParser(queryParserRequest);

		return queryParserResponseUni
			.flatMap(queryParserResponse -> {

				ByteString query = queryParserResponse.getQuery();

				String searchRequestElasticS = query.toStringUtf8();

				String indexNames =
					String.join(",", queryParserResponse.getIndexNameList());

				org.elasticsearch.client.Request request =
					new org.elasticsearch.client.Request(
						"GET", "/" + indexNames + "/_search");

				request.setJsonEntity(searchRequestElasticS);

				return Uni.createFrom().<SearchResponse>emitter((sink) -> client
					.getLowLevelClient()
					.performRequestAsync(request, new ResponseListener() {
						@Override
						public void onSuccess(
							org.elasticsearch.client.Response response) {
							try {
								SearchResponse searchResponse =
									parseEntity(response.getEntity(),
										SearchResponse::fromXContent);

								sink.complete(searchResponse);
							}
							catch (IOException e) {
								sink.fail(e);
							}
						}

						@Override
						public void onFailure(Exception e) {
							sink.fail(e);
						}
					}))
					.map(this::_toSearchResponse);

			});

	}

	@POST
	@Path("/suggestions")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<SuggestionsResponse> suggestions(SearchRequest searchRequest) {

		QueryParserRequest queryParserRequest =
			getQueryParserRequest(searchRequest);

		return searcherClient
			.suggestionsQueryParser(queryParserRequest)
			.map(internalSearcherMapper::toSuggestionsResponse);

	}

	@POST
	@Path("/query-analysis")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<io.openk9.searcher.queryanalysis.QueryAnalysisResponse> queryAnalysis(
		io.openk9.searcher.queryanalysis.QueryAnalysisRequest searchRequest) {

		QueryAnalysisRequest queryAnalysisRequest =
			getQueryAnalysisRequest(searchRequest);

		return searcherClient
			.queryAnalysis(queryAnalysisRequest)
			.map(this::_toQueryAnalysisResponse);

	}

	private io.openk9.searcher.queryanalysis.QueryAnalysisResponse _toQueryAnalysisResponse(
		QueryAnalysisResponse queryAnalysisResponse) {

		io.openk9.searcher.queryanalysis.QueryAnalysisResponse.QueryAnalysisResponseBuilder
			builder =
			io.openk9.searcher.queryanalysis.QueryAnalysisResponse.builder();

		builder.searchText(queryAnalysisResponse.getSearchText());

		List<io.openk9.searcher.queryanalysis.QueryAnalysisTokens> queryAnalysisTokensList =
			new ArrayList<>();

		for (QueryAnalysisTokens queryAnalysisTokensGRPC : queryAnalysisResponse.getAnalysisList()) {

			io.openk9.searcher.queryanalysis.QueryAnalysisTokens queryAnalysisTokens =
				new io.openk9.searcher.queryanalysis.QueryAnalysisTokens();

			queryAnalysisTokens.setText(queryAnalysisTokensGRPC.getText());
			queryAnalysisTokens.setStart(queryAnalysisTokensGRPC.getStart());
			queryAnalysisTokens.setEnd(queryAnalysisTokensGRPC.getEnd());
			queryAnalysisTokens.setPos(queryAnalysisTokensGRPC.getPosList().toArray(Integer[]::new));
			List<QueryAnalysisSearchToken> tokensList =
				queryAnalysisTokensGRPC.getTokensList();

			Collection<Map<String, Object>> tokens = new ArrayList<>(tokensList.size());

			for (QueryAnalysisSearchToken queryAnalysisSearchToken : tokensList) {

				Map<String, Object> token = new HashMap<>();

				token.put("tokenType", queryAnalysisSearchToken.getTokenType());
				token.put("value", queryAnalysisSearchToken.getValue());
				token.put("score", queryAnalysisSearchToken.getScore());
				token.put("keywordKey", queryAnalysisSearchToken.getKeywordKey());
				token.put("keywordName", queryAnalysisSearchToken.getKeywordName());
				token.put("entityType", queryAnalysisSearchToken.getEntityType());
				token.put("entityValue", queryAnalysisSearchToken.getEntityValue());
				token.put("tenantId", queryAnalysisSearchToken.getTenantId());

				tokens.add(token);

			}

			queryAnalysisTokens.setTokens(tokens);

			queryAnalysisTokensList.add(queryAnalysisTokens);

		}

		return builder.analysis(queryAnalysisTokensList).build();

	}

	private QueryAnalysisRequest getQueryAnalysisRequest(
		io.openk9.searcher.queryanalysis.QueryAnalysisRequest searchRequest) {

		QueryAnalysisRequest.Builder builder =
			QueryAnalysisRequest.newBuilder();

		builder.setSearchText(searchRequest.getSearchText());
		builder.setVirtualHost(request.host());

		if (searchRequest.getTokens() != null) {

			for (QueryAnalysisToken token : searchRequest.getTokens()) {
				QueryAnalysisSearchToken.Builder qastBuilder =
					_createQastBuilder(token);
				builder
					.addTokens(
						io.openk9.searcher.grpc.QueryAnalysisToken
							.newBuilder()
							.setText(token.getText())
							.setEnd(token.getEnd())
							.setStart(token.getStart())
							.addAllPos(List.of(token.getPos()))
							.setToken(qastBuilder));
			}

		}

		return builder.build();

	}

	private static QueryAnalysisSearchToken.Builder _createQastBuilder(
		QueryAnalysisToken token) {
		Map<String, Object> tokenMap = token.getToken();
		QueryAnalysisSearchToken.Builder qastBuilder =
			QueryAnalysisSearchToken.newBuilder();
		for (Descriptors.FieldDescriptor field : QueryAnalysisSearchToken.getDescriptor().getFields()) {
			if (tokenMap.containsKey(field.getName())) {
				qastBuilder.setField(field, tokenMap.get(field.getName()));
			}
		}
		return qastBuilder;
	}

	private QueryParserRequest getQueryParserRequest(
		SearchRequest searchRequest) {
		return searcherMapper
			.toQueryParserRequest(searchRequest)
			.toBuilder()
			.setVirtualHost(request.host())
			.build();
	}


	protected final <Resp> Resp parseEntity(final HttpEntity entity,
											final CheckedFunction<XContentParser, Resp, IOException> entityParser) throws IOException {
		if (entity == null) {
			throw new IllegalStateException("Response body expected but not returned");
		}
		if (entity.getContentType() == null) {
			throw new IllegalStateException("Elasticsearch didn't return the [Content-Type] header, unable to parse response body");
		}
		XContentType xContentType = XContentType.fromMediaTypeOrFormat(entity.getContentType().getValue());
		if (xContentType == null) {
			throw new IllegalStateException("Unsupported Content-Type: " + entity.getContentType().getValue());
		}
		try (XContentParser parser = xContentType.xContent().createParser(getNamedXContentRegistry(), DeprecationHandler.THROW_UNSUPPORTED_OPERATION, entity.getContent())) {
			return entityParser.apply(parser);
		}
	}

	private NamedXContentRegistry getNamedXContentRegistry() {

		return namedXContentRegistryMap.computeIfAbsent(
			namedXContentRegistryKey, o -> {
				try {
					Field registry =
						RestHighLevelClient.class.getDeclaredField("registry");

					registry.setAccessible(true);

					return (NamedXContentRegistry)registry.get(client);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			});

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
	InternalSearcherMapper internalSearcherMapper;

	@Inject
	RestHighLevelClient client;

	@GrpcClient("searcher")
	Searcher searcherClient;

	@Inject
	Logger logger;

	private final Map<Object, NamedXContentRegistry> namedXContentRegistryMap =
		Collections.synchronizedMap(new IdentityHashMap<>());

	private static final Object namedXContentRegistryKey = new Object();
}
