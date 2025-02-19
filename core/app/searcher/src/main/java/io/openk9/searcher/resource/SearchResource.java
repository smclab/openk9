/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.searcher.resource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import io.openk9.searcher.client.dto.ParserSearchToken;
import io.openk9.searcher.client.dto.SearchRequest;
import io.openk9.searcher.client.mapper.SearcherMapper;
import io.openk9.searcher.grpc.QueryAnalysisRequest;
import io.openk9.searcher.grpc.QueryAnalysisResponse;
import io.openk9.searcher.grpc.QueryAnalysisSearchToken;
import io.openk9.searcher.grpc.QueryAnalysisTokens;
import io.openk9.searcher.grpc.QueryParserRequest;
import io.openk9.searcher.grpc.QueryParserResponse;
import io.openk9.searcher.grpc.Searcher;
import io.openk9.searcher.grpc.Sort;
import io.openk9.searcher.grpc.TokenType;
import io.openk9.searcher.grpc.Value;
import io.openk9.searcher.mapper.InternalSearcherMapper;
import io.openk9.searcher.payload.response.Response;
import io.openk9.searcher.payload.response.SuggestionsResponse;
import io.openk9.searcher.queryanalysis.QueryAnalysisToken;

import com.google.protobuf.ByteString;
import com.google.protobuf.ProtocolStringList;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.lucene.search.TotalHits;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.logging.Logger;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.ShardSearchFailure;
import org.opensearch.client.ResponseException;
import org.opensearch.client.ResponseListener;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.CheckedFunction;
import org.opensearch.core.common.text.Text;
import org.opensearch.core.xcontent.DeprecationHandler;
import org.opensearch.core.xcontent.MediaTypeRegistry;
import org.opensearch.core.xcontent.NamedXContentRegistry;
import org.opensearch.core.xcontent.XContentParser;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.fetch.subphase.highlight.HighlightField;

@Path("/v1")
@RequestScoped
public class SearchResource {

	private static final Logger log = Logger.getLogger(SearchResource.class);
	private static final Object namedXContentRegistryKey = new Object();
	private static final Pattern i18nHighlithKeyPattern = Pattern.compile(
		"\\.i18n\\..{5,}$|\\.base$");
	private static final int INTERNAL_SERVER_ERROR = 500;
	private final Map<Object, NamedXContentRegistry> namedXContentRegistryMap =
		Collections.synchronizedMap(new IdentityHashMap<>());
	@GrpcClient("searcher")
	Searcher searcherClient;
	@Inject
	@Claim(standard = Claims.raw_token)
	String rawToken;
	@Inject
	SearcherMapper searcherMapper;
	@Inject
	InternalSearcherMapper internalSearcherMapper;
	@Inject
	RestHighLevelClient restHighLevelClient;
	@Context
	HttpServerRequest request;
	@Context
	HttpHeaders
		headers;
	@ConfigProperty(name = "openk9.searcher.supported.headers.name", defaultValue = "OPENK9_ACL")
	List<String> supportedHeadersName;
	@ConfigProperty(name = "openk9.searcher.total-result-limit", defaultValue = "10000")
	Integer totalResultLimit;

	@POST
	@Path("/search-query")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<String> searchQuery(SearchRequest searchRequest) {

		QueryParserRequest queryParserRequest =
			getQueryParserRequest(searchRequest);

		Uni<QueryParserResponse> queryParserResponseUni =
			searcherClient.queryParser(queryParserRequest);

		return queryParserResponseUni.map(queryParserResponse -> {

			ByteString query = queryParserResponse.getQuery();

			return query.toStringUtf8();

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

				String searchRequestBody = query.toStringUtf8();

				ProtocolStringList indexNameList =
					queryParserResponse.getIndexNameList();

				if (indexNameList == null || indexNameList.isEmpty()) {
					return Uni.createFrom().item(Response.EMPTY);
				}

				String indexNames =
					String.join(",", indexNameList);

				var queryParams = queryParserResponse.getQueryParametersMap();

				org.opensearch.client.Request request =
					new org.opensearch.client.Request(
						"GET", "/" + indexNames + "/_search");

				request.addParameters(queryParams);

				request.setJsonEntity(searchRequestBody);

				return Uni.createFrom().<SearchResponse>emitter((sink) -> restHighLevelClient
						.getLowLevelClient()
						.performRequestAsync(request, new ResponseListener() {
							@Override
							public void onSuccess(
								org.opensearch.client.Response response) {
								try {
									SearchResponse searchResponse =
										parseEntity(
											response.getEntity(),
											SearchResponse::fromXContent
										);

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
					.map(this::toSearchResponse);
			})
			.onFailure()
			.transform(throwable -> {
				return new WebApplicationException(jakarta.ws.rs.core.Response
					.status(getResponseStatus(throwable))
						.entity(JsonObject.of(
							"details", "Unable to serve search request"))
					.build());
				}
			);

	}

	private int getResponseStatus(Throwable throwable) {

		int statusCode = INTERNAL_SERVER_ERROR;

		if (throwable instanceof ResponseException responseException) {
			statusCode = responseException.getResponse()
				.getStatusLine()
				.getStatusCode();
		}

		if (statusCode == INTERNAL_SERVER_ERROR) {
			log.error("Search request failed", throwable);
		}
		else {
			log.warn("Search request failed", throwable);
		}

		return statusCode;
	}

	@POST
	@Path("/query-analysis")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<io.openk9.searcher.queryanalysis.QueryAnalysisResponse> queryAnalysis(
		io.openk9.searcher.queryanalysis.QueryAnalysisRequest searchRequest) {

		QueryAnalysisRequest queryAnalysisRequest =
			getQueryAnalysisRequest(searchRequest, "query-analysis");

		return searcherClient
			.queryAnalysis(queryAnalysisRequest)
			.map(this::toQueryAnalysisResponse);

	}

	@POST
	@Path("/semantic-autocomplete")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<io.openk9.searcher.queryanalysis.QueryAnalysisResponse> semanticAutocomplete(
		io.openk9.searcher.queryanalysis.QueryAnalysisRequest searchRequest) {

		QueryAnalysisRequest queryAnalysisRequest =
			getQueryAnalysisRequest(searchRequest, "semantic-autocomplete");

		return searcherClient
			.queryAnalysis(queryAnalysisRequest)
			.map(this::toQueryAnalysisResponse);

	}

	protected static void mapI18nFields(Map<String, Object> sourceAsMap) {

		for (Map.Entry<String, Object> entry : sourceAsMap.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map) {
				Map<String, Object> objectMap = (Map<String, Object>) value;
				if (objectMap.containsKey("i18n")) {

					Map<String, Object> i18nMap =
						(Map<String, Object>) objectMap.get("i18n");
					var item = i18nMap.values().iterator().next();
					if (!i18nMap.isEmpty()) {
						if (item instanceof String i18nString) {
							entry.setValue(i18nString);
						}
						else if (item instanceof List<?> i18nList) {
							var i18nListString = i18nList
								.stream()
								.map(String::valueOf)
								.toList();

							entry.setValue(i18nListString);
						}
						else {
							log.warn("The object i18nList is not a String or a List<String>");
						}
					}

				}
				else if (objectMap.containsKey("base")) {
					entry.setValue(objectMap.get("base"));
				}
				else {
					mapI18nFields((Map<String, Object>) value);
				}
			}
			if (value instanceof Iterable) {
				for (Object item : (Iterable<?>) value) {
					if (item instanceof Map) {
						mapI18nFields((Map<String, Object>) item);
					}
				}
			}
		}

	}

	private static Iterable<Integer> toList(Integer[] pos) {
		if (pos == null || pos.length == 0) {
			return List.of();
		}
		return List.of(pos);
	}

	private static QueryAnalysisSearchToken.Builder createQastBuilder(
		QueryAnalysisToken token) {
		Map<String, Object> tokenMap = token.getToken();
		QueryAnalysisSearchToken.Builder qastBuilder =
			QueryAnalysisSearchToken.newBuilder();

		for (Map.Entry<String, Object> entry : tokenMap.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value == null) {
				continue;
			}
			switch (key) {
				case "tokenType" -> qastBuilder.setTokenType(TokenType.valueOf((String) value));
				case "value" -> qastBuilder.setValue((String) value);
				case "score" -> qastBuilder.setScore(((Number) value).floatValue());
				case "keywordKey" -> qastBuilder.setKeywordKey((String) value);
				case "keywordName" -> qastBuilder.setKeywordName((String) value);
				case "entityType" -> qastBuilder.setEntityType((String) value);
				case "entityName" -> qastBuilder.setEntityName((String) value);
				case "tenantId" -> qastBuilder.setTenantId((String) value);
				case "label" -> qastBuilder.setLabel((String) value);
			}
		}

		return qastBuilder;
	}

	private QueryParserRequest getQueryParserRequest(SearchRequest searchRequest) {

		var requestBuilder = searcherMapper
			.toQueryParserRequest(searchRequest)
			.toBuilder();

		setVectorIndices(searchRequest, requestBuilder);

		Map<String, Value> extra = new HashMap<>();

		for (String headerName : supportedHeadersName) {
			List<String> requestHeader = headers.getRequestHeader(headerName);
			if (requestHeader != null && !requestHeader.isEmpty()) {
				extra.put(headerName, Value.newBuilder().addAllValue(requestHeader).build());
			}
		}

		String sortAfterKey = searchRequest.getSortAfterKey();
		String language = searchRequest.getLanguage();

		return requestBuilder
			.setVirtualHost(request.host())
			.setJwt(rawToken == null ? "" : rawToken)
			.putAllExtra(extra)
			.addAllSort(mapToGrpc(searchRequest.getSort()))
			.setSortAfterKey(sortAfterKey == null ? "" : sortAfterKey)
			.setLanguage(language == null ? "" : language)
			.build();

	}

	private Iterable<Sort> mapToGrpc(
		List<Map<String, Map<String, String>>> sort) {

		if (sort == null || sort.isEmpty()) {
			return List.of();
		}

		Set<Sort> sortList = new HashSet<>(sort.size());

		for (Map<String, Map<String, String>> map : sort) {

			for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {

				String fieldName = entry.getKey();

				Sort.Builder builder =
					Sort
						.newBuilder()
						.setField(fieldName);

				Map<String, String> value = entry.getValue();

				if (value != null && !value.isEmpty()) {
					builder.putAllExtras(value);
				}

				sortList.add(builder.build());

			}

		}

		return sortList;

	}

	private static void setVectorIndices(
		SearchRequest searchRequest,
		QueryParserRequest.Builder requestBuilder) {
		var searchTokens = searchRequest.getSearchQuery();

		for (ParserSearchToken token : searchTokens) {

			var tokenType = token.getTokenType();

			if (tokenType != null
				&& (tokenType.equalsIgnoreCase("knn")
					|| tokenType.equalsIgnoreCase("hybrid"))) {

				requestBuilder.setVectorIndices(true);
				break;
			}

		}
	}

	private static String getHighlightName(String highlightName) {
		Matcher matcher = i18nHighlithKeyPattern.matcher(highlightName);
		if (matcher.find()) {
			return matcher.replaceFirst("");
		}
		else {
			return highlightName;
		}
	}

	private io.openk9.searcher.queryanalysis.QueryAnalysisResponse toQueryAnalysisResponse(
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

				if (StringUtils.isNotBlank(queryAnalysisSearchToken.getKeywordKey())) {
					token.put("keywordKey", queryAnalysisSearchToken.getKeywordKey());
				}
				if (StringUtils.isNotBlank(queryAnalysisSearchToken.getKeywordName())) {
					token.put("keywordName", queryAnalysisSearchToken.getKeywordName());
				}
				if (StringUtils.isNotBlank(queryAnalysisSearchToken.getEntityType())) {
					token.put("entityType", queryAnalysisSearchToken.getEntityType());
				}
				if (StringUtils.isNotBlank(queryAnalysisSearchToken.getEntityName())) {
					token.put("entityName", queryAnalysisSearchToken.getEntityName());
				}
				if (StringUtils.isNotBlank(queryAnalysisSearchToken.getTenantId())) {
					token.put("tenantId", queryAnalysisSearchToken.getTenantId());
				}

				if (StringUtils.isNotBlank(queryAnalysisSearchToken.getLabel())) {
					token.put("label", queryAnalysisSearchToken.getLabel());
				}

				if (!queryAnalysisSearchToken.getExtraMap().isEmpty()) {
					token.put("extra", queryAnalysisSearchToken.getExtraMap());
				}

				tokens.add(token);

			}

			queryAnalysisTokens.setTokens(tokens);

			queryAnalysisTokensList.add(queryAnalysisTokens);

		}

		return builder.analysis(queryAnalysisTokensList).build();

	}

	private NamedXContentRegistry getNamedXContentRegistry() {

		return namedXContentRegistryMap.computeIfAbsent(
			namedXContentRegistryKey, o -> {
				try {
					Field registry =
						RestHighLevelClient.class.getDeclaredField("registry");

					registry.setAccessible(true);

					return (NamedXContentRegistry) registry.get(restHighLevelClient);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			});

	}

	private QueryAnalysisRequest getQueryAnalysisRequest(
		io.openk9.searcher.queryanalysis.QueryAnalysisRequest searchRequest, String mode) {

		QueryAnalysisRequest.Builder builder =
			QueryAnalysisRequest.newBuilder();

		builder.setSearchText(searchRequest.getSearchText());
		builder.setVirtualHost(request.host());
		builder.setJwt(rawToken == null ? "" : rawToken);
		builder.setMode(mode);

		if (searchRequest.getTokens() != null) {

			for (QueryAnalysisToken token : searchRequest.getTokens()) {
				QueryAnalysisSearchToken.Builder qastBuilder =
					createQastBuilder(token);
				builder
					.addTokens(
						io.openk9.searcher.grpc.QueryAnalysisToken
							.newBuilder()
							.setText(token.getText())
							.setEnd(token.getEnd())
							.setStart(token.getStart())
							.addAllPos(toList(token.getPos()))
							.setToken(qastBuilder));
			}

		}

		return builder.build();

	}

	private <Resp> Resp parseEntity(
		final HttpEntity entity,
		final CheckedFunction<XContentParser, Resp, IOException> entityParser)
	throws IOException {

		if (entity == null) {
			throw new IllegalStateException("Response body expected but not returned");
		}

		if (entity.getContentType() == null) {
			throw new IllegalStateException(
				"Opensearch didn't return the [Content-Type] header, unable to parse response body");
		}

		var mediaTypeValue = entity.getContentType().getValue();
		if (mediaTypeValue != null &&
			(mediaTypeValue = mediaTypeValue.toLowerCase(Locale.ROOT)).contains("vnd.opensearch")) {
			mediaTypeValue = mediaTypeValue.replaceAll("vnd.opensearch\\+", "").replaceAll(
				"\\s*;\\s*compatible-with=\\d+",
				""
			);
		}

		org.opensearch.core.xcontent.MediaType mediaType = MediaTypeRegistry.fromMediaType(
			mediaTypeValue);
		if (mediaType == null) {
			throw new IllegalStateException("Unsupported Content-Type: " + mediaTypeValue);
		}

		try (XContentParser parser = mediaType.xContent().createParser(
			getNamedXContentRegistry(),
			DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
			entity.getContent()
		)
		) {

			return entityParser.apply(parser);
		}
	}

	private Response toSearchResponse(SearchResponse searchResponse) {
		printShardFailures(searchResponse);

		SearchHits hits = searchResponse.getHits();

		List<Map<String, Object>> result = new ArrayList<>();

		for (SearchHit hit : hits.getHits()) {

			Map<String, Object> sourceAsMap = hit.getSourceAsMap();

			Map<String, Object> sourceMap = new HashMap<>(
				sourceAsMap.size() + 1, 1);

			sourceMap.putAll(sourceAsMap);

			mapI18nFields(sourceMap);

			sourceMap.put("id", hit.getId());

			Map<String, HighlightField> highlightFields =
				hit.getHighlightFields();

			Map<String, Object> highlightMap = new HashMap<>(
				highlightFields.size(), 1);

			for (HighlightField value : highlightFields.values()) {
				highlightMap.put(
					getHighlightName(value.getName()),
					Arrays
						.stream(value.getFragments())
						.map(Text::string)
						.toArray(String[]::new)
				);
			}

			Map<String, Object> hitMap = new HashMap<>(2, 1);

			hitMap.put("source", sourceMap);
			hitMap.put("highlight", highlightMap);
			hitMap.put("score", hit.getScore());

			Object[] sortValues = hit.getSortValues();

			if (sortValues != null && sortValues.length > 0) {
				hitMap.put(
					"sortAfterKey",
					Base64.getEncoder().encodeToString(
						JsonArray.of(sortValues).toBuffer().getBytes()
					)
				);
			}

			result.add(hitMap);

		}

		TotalHits totalHits = hits.getTotalHits();
		var totalHitsValue = totalHits != null ? totalHits.value : totalResultLimit;

		return new Response(result, Math.min(totalHitsValue, totalResultLimit));
	}


	private void printShardFailures(SearchResponse searchResponse) {
		if (searchResponse.getShardFailures() != null) {
			for (ShardSearchFailure failure : searchResponse.getShardFailures()) {
				log.warn(failure.reason());
			}
		}
	}

}
