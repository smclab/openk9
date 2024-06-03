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

import com.google.protobuf.ByteString;
import com.google.protobuf.ProtocolStringList;
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
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.lucene.search.TotalHits;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
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
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Path("/v1")
@RequestScoped
public class SearchResource {

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

				ProtocolStringList indexNameList =
					queryParserResponse.getIndexNameList();

				if (indexNameList == null || indexNameList.isEmpty()) {
					return Uni.createFrom().item(Response.EMPTY);
				}

				String indexNames =
					String.join(",", indexNameList);

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


	@POST
	@Path("/suggerimenti")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<io.openk9.searcher.queryanalysis.QueryAnalysisResponse> suggerimenti(
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

	private QueryAnalysisRequest getQueryAnalysisRequest(
		io.openk9.searcher.queryanalysis.QueryAnalysisRequest searchRequest) {

		QueryAnalysisRequest.Builder builder =
			QueryAnalysisRequest.newBuilder();

		builder.setSearchText(searchRequest.getSearchText());
		builder.setVirtualHost(request.host());
		builder.setJwt(rawToken == null ? "" : rawToken);

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
							.addAllPos(_toList(token.getPos()))
							.setToken(qastBuilder));
			}

		}

		return builder.build();

	}

	private static Iterable<Integer> _toList(Integer[] pos) {
		if (pos == null || pos.length == 0) {
			return List.of();
		}
		return List.of(pos);
	}

	private static QueryAnalysisSearchToken.Builder _createQastBuilder(
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
				case "score" -> qastBuilder.setScore(((Number)value).floatValue());
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

		Map<String, Value> extra = new HashMap<>();

		for (String headerName : supportedHeadersName) {
			List<String> requestHeader = headers.getRequestHeader(headerName);
			if (requestHeader != null && !requestHeader.isEmpty()) {
				extra.put(headerName, Value.newBuilder().addAllValue(requestHeader).build());
			}
		}

		String sortAfterKey = searchRequest.getSortAfterKey();
		String language = searchRequest.getLanguage();

		return searcherMapper
			.toQueryParserRequest(searchRequest)
			.toBuilder()
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

		return new Response(result, totalHits.value);
	}

	protected static void mapI18nFields(Map<String, Object> sourceAsMap) {

		for (Map.Entry<String, Object> entry : sourceAsMap.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof Map) {
				Map<String, Object> objectMap = (Map<String, Object>) value;
				if (objectMap.containsKey("i18n")) {

					Map<String, Object> i18nMap =
						(Map<String, Object>) objectMap.get("i18n");

					if (!i18nMap.isEmpty()) {
						if (i18nMap.values().iterator().next() instanceof String) {
							String i18nString =
								(String) i18nMap.values().iterator().next();
							entry.setValue(i18nString);
						}
						else if (i18nMap.values().iterator().next() instanceof List<?>) {
							List i18nList = ((List<Object>) i18nMap.values().iterator().next())
								.stream()
								.map(object -> String.valueOf(object))
								.toList();
							entry.setValue(i18nList);
						}
						else {
							logger.warn("The object i18nList is not a String or a List<String>");
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
				for (Object item: (Iterable<?>) value) {
					if (item instanceof Map) {
						mapI18nFields((Map<String, Object>) item);
					}
				}
			}
		}

	}

	private static String getHighlightName(String highlightName) {
		Matcher matcher = i18nHighlithKeyPattern.matcher(highlightName);
		if (matcher.find()) {
			return matcher.replaceFirst("");
		}
		else  {
			return highlightName;
		}
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


	static Logger logger = Logger.getLogger(SearchResource.class);

	@Inject
	@Claim(standard = Claims.raw_token)
	String rawToken;

	@Context
	HttpServerRequest request;

	@Context
	HttpHeaders
	headers;

	@ConfigProperty(name = "openk9.searcher.supported.headers.name", defaultValue = "OPENK9_ACL")
	List<String> supportedHeadersName;

	private final Map<Object, NamedXContentRegistry> namedXContentRegistryMap =
		Collections.synchronizedMap(new IdentityHashMap<>());

	private static final Object namedXContentRegistryKey = new Object();
	private static final Pattern i18nHighlithKeyPattern = Pattern.compile("\\.i18n\\..{5,}$|\\.base$");

}
