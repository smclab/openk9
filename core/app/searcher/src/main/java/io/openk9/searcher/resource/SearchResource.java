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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import io.openk9.searcher.client.dto.AutocompleteRequestDTO;
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.openk9.searcher.client.dto.SearchRequest;
import io.openk9.searcher.client.mapper.SearcherMapper;
import io.openk9.searcher.grpc.AutocompleteConfigurationsRequest;
import io.openk9.searcher.grpc.AutocompleteConfigurationsResponse;
import io.openk9.searcher.grpc.AutocorrectionConfigurationsRequest;
import io.openk9.searcher.grpc.AutocorrectionConfigurationsResponse;
import io.openk9.searcher.grpc.QueryAnalysisRequest;
import io.openk9.searcher.grpc.QueryAnalysisResponse;
import io.openk9.searcher.grpc.QueryAnalysisSearchToken;
import io.openk9.searcher.grpc.QueryAnalysisTokens;
import io.openk9.searcher.grpc.QueryParserRequest;
import io.openk9.searcher.grpc.QueryParserResponse;
import io.openk9.searcher.grpc.Searcher;
import io.openk9.searcher.grpc.Sort;
import io.openk9.searcher.grpc.SortType;
import io.openk9.searcher.grpc.TokenType;
import io.openk9.searcher.grpc.Value;
import io.openk9.searcher.mapper.InternalSearcherMapper;
import io.openk9.searcher.payload.response.AutocompleteHit;
import io.openk9.searcher.payload.response.AutocorrectionDTO;
import io.openk9.searcher.payload.response.Response;
import io.openk9.searcher.payload.response.SuggestionsResponse;
import io.openk9.searcher.queryanalysis.QueryAnalysisToken;

import com.google.protobuf.ByteString;
import com.google.protobuf.ProtocolStringList;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.instrumentation.annotations.WithSpan;
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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.ShardSearchFailure;
import org.opensearch.client.ResponseException;
import org.opensearch.client.ResponseListener;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.json.PlainJsonSerializable;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.SuggestMode;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Operator;
import org.opensearch.client.opensearch._types.query_dsl.PrefixQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType;
import org.opensearch.client.opensearch.core.search.Highlight;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.Suggest;
import org.opensearch.client.opensearch.core.search.SuggestSort;
import org.opensearch.client.opensearch.core.search.Suggester;
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

	protected static final String SUGGESTIONS = "suggestions";
	private static final String AUTOCORRECTION_SUGGESTION = "autocorrection_suggestion";
	private static final String DETAILS_FIELD = "details";
	private static final int NONE_STATUS_CODE = 0;
	private static final int NOT_FOUND_STATUS_CODE = 404;
	private static final Pattern i18nHighlithKeyPattern = Pattern.compile(
		"\\.i18n\\..{5,}$|\\.base$");
	private static final Logger log = Logger.getLogger(SearchResource.class);
	private static final Object namedXContentRegistryKey = new Object();
	private final Map<Object, NamedXContentRegistry> namedXContentRegistryMap =
		Collections.synchronizedMap(new IdentityHashMap<>());

	@Inject
	OpenSearchAsyncClient client;
	@Context
	HttpHeaders headers;
	@Inject
	InternalSearcherMapper internalSearcherMapper;
	@Inject
	@Claim(standard = Claims.raw_token)
	String rawToken; // it is injected to force authentication.
	@Context
	HttpServerRequest request;
	@Inject
	RestHighLevelClient restHighLevelClient;
	@GrpcClient("searcher")
	Searcher searcherClient;
	@Inject
	SearcherMapper searcherMapper;
	@ConfigProperty(name = "openk9.searcher.supported.headers.name", defaultValue = "OPENK9_ACL")
	List<String> supportedHeadersName;
	@ConfigProperty(name = "openk9.searcher.total-result-limit", defaultValue = "10000")
	Integer totalResultLimit;
	@Inject
	Tracer tracer;

	/**
	 * Generates the corrected text by applying autocorrection suggestions to the original text.
	 * <p>
	 * This method iterates through the suggestions sorted by offset and replaces each incorrect
	 * term with its correction, building the final corrected text.
	 *
	 * @param originalText the original text to be corrected
	 * @param suggestions a list of suggestions produced by OpenSearch
	 * @return the corrected text with all suggestions applied, or the original text if no suggestions exist
	 */
	protected static String _generateAutocorrectionText(
			String originalText, List<AutocorrectionDTO.Suggestion> suggestions) {

		if (suggestions == null || suggestions.isEmpty()) {
			return originalText;
		}

		// Sort suggestions by offset to process them in order
		var sortedSuggestions = suggestions.stream()
			.sorted(Comparator.comparingInt(AutocorrectionDTO.Suggestion::offset))
			.toList();

		StringBuilder correctedText = new StringBuilder();
		int currentPosition = 0;

		for (AutocorrectionDTO.Suggestion suggestion : sortedSuggestions) {
			int offset = suggestion.offset();
			int length = suggestion.length();
			String correction = suggestion.correction();

			// Append text before the current suggestion
			correctedText.append(originalText, currentPosition, offset);

			// Append the correction
			correctedText.append(correction);

			// Update position to after the corrected word
			currentPosition = offset + length;
		}

		// Append remaining text after the last suggestion
		correctedText.append(originalText.substring(currentPosition));

		return correctedText.toString();
	}

	protected static Object _getNestedValue(Map<String, Object> map, String path) {
		if (path == null || path.isEmpty()) {
			return null;
		}

		String[] keys = path.split("\\.");
		Object current = map;

		for (String key : keys) {
			if (current instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> currentMap = (Map<String, Object>) current;
				current = currentMap.get(key);

				// Key not found
				if (current == null) {
					return null;
				}
			}
			else {
				// Expected a Map but got something else
				return null;
			}
		}

		return current;
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

	private static String getHighlightName(String highlightName) {
		Matcher matcher = i18nHighlithKeyPattern.matcher(highlightName);
		if (matcher.find()) {
			return matcher.replaceFirst("");
		}
		else {
			return highlightName;
		}
	}

	protected static String getRawToken(HttpHeaders headers) {
		String rawToken = "";

		var authorization = headers.getRequestHeader("Authorization");

		if (!authorization.isEmpty()) {
			var value = authorization.getFirst();
			if (value != null && !value.isEmpty()) {
				rawToken = value.trim().substring(7);
			}
		}

		return rawToken;
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

	@Operation(operationId = "autocomplete")
	@Tag(
		name = "Autocomplete API",
		description = "Return autocomplete suggestions based on indexed data according to the Autocomplete configurations."
	)
	@APIResponses(value = {
		@APIResponse(responseCode = "200", description = "success"),
		@APIResponse(responseCode = "404", description = "not found"),
		@APIResponse(responseCode = "400", description = "invalid"),
		@APIResponse(
			responseCode = "200",
			description = "Ingestion successful",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = Response.class),
					example = SearchRequestExamples.AUTOCOMPLETE_RESPONSE
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@RequestBody(
		content = {
			@Content(
				mediaType = MediaType.APPLICATION_JSON,
				schema = @Schema(implementation = SearchRequest.class),
				examples = {
					@ExampleObject(
						name = "autocomplete",
						value = SearchRequestExamples.AUTOCOMPLETE_SEARCH_REQUEST
					)
				}
			)
		}
	)
	@POST
	@Path("/autocomplete")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<List<AutocompleteHit>> autocomplete(AutocompleteRequestDTO autocompleteRequest) {
		return _buildAutocompleteContext(autocompleteRequest)
			.flatMap(context ->
				_getAutocompleteSuggest(context.query())
					.map(response ->
						_extractAutocompleteResponse(response, context.fields())
					)
			);
	}

	@Operation(operationId = "autocomplete-query")
	@Tag(
		name = "Autocomplete Query API",
		description = "Transform Openk9 Autocomplete Request in equivalent OpenSearch query configured for autocomplete suggestions"
	)
	@APIResponses(value = {
		@APIResponse(responseCode = "200", description = "success"),
		@APIResponse(responseCode = "404", description = "not found"),
		@APIResponse(responseCode = "400", description = "invalid"),
		@APIResponse(
			responseCode = "200",
			description = "Query generation successful",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = Response.class),
					example = SearchRequestExamples.AUTOCOMPLETE_QUERY_RESPONSE
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@RequestBody(
		content = {
			@Content(
				mediaType = MediaType.APPLICATION_JSON,
				schema = @Schema(implementation = SearchRequest.class),
				examples = {
					@ExampleObject(
						name = "autocomplete",
						value = SearchRequestExamples.AUTOCOMPLETE_SEARCH_REQUEST
					)
				}
			)
		}
	)
	@POST
	@Path("/autocomplete-query")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<String> autocompleteQuery(AutocompleteRequestDTO autocompleteRequest) {
		return _buildAutocompleteContext(autocompleteRequest)
			.map(AutocompleteContext::query)
			.map(PlainJsonSerializable::toJsonString);
	}

	@Operation(operationId = "autocorrection-query")
	@Tag(
		name = "Autocorrection Query API",
		description = "Transform Openk9 Search Request in equivalent OpenSearch query configured for autocorrection suggestions"
	)
	@APIResponses(value = {
		@APIResponse(responseCode = "200", description = "success"),
		@APIResponse(responseCode = "404", description = "not found"),
		@APIResponse(responseCode = "400", description = "invalid"),
		@APIResponse(
			responseCode = "200",
			description = "Ingestion successful",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = Response.class),
					example = SearchRequestExamples.AUTOCORRECTION_QUERY_RESPONSE
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@RequestBody(
		content = {
			@Content(
				mediaType = MediaType.APPLICATION_JSON,
				schema = @Schema(implementation = SearchRequest.class),
				examples = {
					@ExampleObject(
						name = "search",
						value = SearchRequestExamples.AUTOCORRECTION_SEARCH_REQUEST
					)
				}
			)
		}
	)
	@POST
	@Path("/autocorrection-query")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<String> autocorrectionQuery(SearchRequest searchRequest) {
		return _buildAutocorrectionContext(searchRequest)
			.map(autocorrectionContext ->
				autocorrectionContext.query().toJsonString()
			);
	}

	@Operation(operationId = "query-analysis")
	@Tag(name = "Query Analysis API", description = "Performs sematic query analysis on search query, as well as provides autocomplete suggestions.")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "200",
					description = "Ingestion successful",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = io.openk9.searcher.queryanalysis.QueryAnalysisResponse.class),
									example = QueryAnalysisRequestExamples.QUERY_ANALYSIS_RESPONSE
							)
					}
			),
			@APIResponse(ref = "#/components/responses/bad-request"),
			@APIResponse(ref = "#/components/responses/not-found"),
			@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@RequestBody(
			content = {
					@Content(
							mediaType = MediaType.APPLICATION_JSON,
							schema = @Schema(implementation = io.openk9.searcher.queryanalysis.QueryAnalysisRequest.class),
							examples = {
									@ExampleObject(
											name = "simple query analysis request",
											value = QueryAnalysisRequestExamples.SIMPLE_QUERY_ANALYSIS_REQUEST
									),
									@ExampleObject(
											name = "query analysis request with tokens",
											value = QueryAnalysisRequestExamples.QUERY_ANALYSIS_REQUEST_WITH_TOKENS
									)
							}
					)
			}
	)
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

	@Operation(operationId = "search")
	@Tag(name = "Search API", description = "Execute search on indexed data. Returns list of matching results ordered by score.")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "200",
					description = "Ingestion successful",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = Response.class),
									example = SearchRequestExamples.SEARCH_RESPONSE
							)
					}
			),
			@APIResponse(ref = "#/components/responses/bad-request"),
			@APIResponse(ref = "#/components/responses/not-found"),
			@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@RequestBody(
			content = {
					@Content(
							mediaType = MediaType.APPLICATION_JSON,
							schema = @Schema(implementation = SearchRequest.class),
							examples = {
									@ExampleObject(
											name = "text search",
											value = SearchRequestExamples.TEXT_SEARCH_REQUEST
									),
									@ExampleObject(
											name = "hybrid search",
											value = SearchRequestExamples.HYBRID_SEARCH_REQUEST
									),
									@ExampleObject(
											name = "knn search",
											value = SearchRequestExamples.KNN_SEARCH_REQUEST
									)
							}
					)
			}
	)
	@POST
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Response> search(SearchRequest searchRequest) {

		return _buildAutocorrectionContext(searchRequest)
			.flatMap(this::_getAutocorrectionSuggest
			)
			.onFailure()
			.recoverWithItem(failure -> {
				_logMessage("Something went wrong during the autocorrected search.", failure);
				return null;
			})
			.map(autocorrectionResult -> {
				_updateSearchQueryWithCorrection(autocorrectionResult, searchRequest);

				// do search and add autocorrection to the search response
				return _doSearch(searchRequest)
					.invoke(response ->
						response.setAutocorrection(autocorrectionResult)
					);
			})
			.flatMap(responseUni -> responseUni);
	}

	@Operation(operationId = "search-query")
	@Tag(name = "Search Query API", description = "Transform Openk9 Search Request in equivalent Opensearch query")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "200",
					description = "Ingestion successful",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = Response.class),
									example = SearchRequestExamples.SEARCH_RESPONSE
							)
					}
			),
			@APIResponse(ref = "#/components/responses/bad-request"),
			@APIResponse(ref = "#/components/responses/not-found"),
			@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@RequestBody(
			content = {
					@Content(
							mediaType = MediaType.APPLICATION_JSON,
							schema = @Schema(implementation = SearchRequest.class),
							examples = {
									@ExampleObject(
											name = "text search",
											value = SearchRequestExamples.TEXT_SEARCH_REQUEST
									),
									@ExampleObject(
											name = "hybrid search",
											value = SearchRequestExamples.HYBRID_SEARCH_REQUEST
									),
									@ExampleObject(
											name = "knn search",
											value = SearchRequestExamples.KNN_SEARCH_REQUEST
									)
							}
					)
			}
	)
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

	@Operation(operationId = SUGGESTIONS)
	@Tag(name = "Suggestions API", description = "Return filter options for a specific filed based on search results.")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "200",
					description = "Ingestion successful",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = SuggestionsResponse.class),
									example = SearchRequestExamples.SUGGESTIONS_RESPONSE
							)
					}
			),
			@APIResponse(ref = "#/components/responses/bad-request"),
			@APIResponse(ref = "#/components/responses/not-found"),
			@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@RequestBody(
			content = {
					@Content(
							mediaType = MediaType.APPLICATION_JSON,
							schema = @Schema(implementation = SearchRequest.class),
							examples = {
									@ExampleObject(
											name = "suggestions for a specific suggestion category",
											value = SearchRequestExamples.SEARCH_REQUEST_FOR_SUGGESTIONS
									),
									@ExampleObject(
											name = "suggestions for a specific suggestion category and string filter",
											value = SearchRequestExamples.SEARCH_REQUEST_FOR_SUGGESTIONS_WITH_PREFIX_FILTER
									)
							}
					)
			}
	)
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

	/**
	 * Creates an OpenSearch autocomplete query based on the provided search request and
	 * Autocomplete configurations.
	 *
	 * <p>This method retrieves autocomplete configurations for the current virtual host,
	 * validates that autocomplete is enabled, extracts and validates the query text from
	 * the search token, and creates an OpenSearch autocomplete request based on the
	 * configurations.
	 *
	 * <p>The method operates asynchronously and chains multiple validation and transformation steps.
	 * If any validation fails or configurations cannot be retrieved, the returned Uni will emit
	 * the corresponding exception.
	 *
	 * @param autocompleteRequest the search request containing the search query and search tokens
	 * @return a {@link Uni} that emits an OpenSearch SearchRequest configured for autocomplete.
	 *         The Uni will emit an exception if validation fails or configurations are unavailable
	 *
	 * @throws AutocompleteException if autocomplete is disabled or if the query text is invalid
	 *
	 * @see AutocompleteRequestDTO
	 * @see AutocompleteContext
	 * @see AutocompleteConfigurationsRequest
	 * @see ParserSearchToken
	 */
	private Uni<AutocompleteContext> _buildAutocompleteContext(
		AutocompleteRequestDTO autocompleteRequest) {

		var virtualHost = request.authority().toString();
		var autocompleteConfigurationsRequest = AutocompleteConfigurationsRequest.newBuilder()
			.setVirtualHost(virtualHost)
			.build();

		// retrieve Autocomplete configurations
		return _getAutocompleteConfigurations(autocompleteConfigurationsRequest)
			.map(autocompleteConfig -> {

				_validateAutocompleteConfig(autocompleteConfig);

				var queryText = autocompleteRequest.getQueryText();

				// Create the autocomplete request for OpenSearch according to the
				// autocomplete configurations.
				var query = _createAutocompleteRequest(autocompleteConfig, queryText);
				var fields = autocompleteConfig.getFieldList();

				return new AutocompleteContext(query, fields);
			});
	}

	/**
	 * Creates an OpenSearch autocorrection query based on the provided search request and
	 * Autocorrection configurations.
	 *
	 * <p>This method retrieves autocorrection configurations for the current virtual host,
	 * validates that autocorrection is enabled, extracts and validates the query text from
	 * the search token, and creates an OpenSearch autocorrection request based on the
	 * configurations.
	 *
	 * <p>The method operates asynchronously and chains multiple validation and transformation steps.
	 * If any validation fails or configurations cannot be retrieved, the returned Uni will emit
	 * the corresponding exception.
	 *
	 * @param searchRequest the search request containing the search query and search tokens
	 * @return a {@link Uni} that emits an OpenSearch SearchRequest configured for autocorrection.
	 *         The Uni will emit an exception if validation fails or configurations are unavailable
	 *
	 * @throws AutocorrectionException if autocorrection is disabled or if the query text is invalid
	 *
	 * @see SearchRequest
	 * @see org.opensearch.client.opensearch.core.SearchRequest
	 * @see AutocorrectionConfigurationsRequest
	 * @see ParserSearchToken
	 */
	private Uni<AutocorrectionContext> _buildAutocorrectionContext(
			SearchRequest searchRequest) {

		var virtualHost = request.authority().toString();
		var autocorrectionConfigurationsRequest = AutocorrectionConfigurationsRequest.newBuilder()
			.setVirtualHost(virtualHost)
			.build();

		// retrieve Autocorrection configurations
		return _getAutocorrectionConfigurations(autocorrectionConfigurationsRequest)
			.map(autocorrectionConfig -> {

				_validateAutocorrectionConfig(autocorrectionConfig);

				// retrieve the searchToken associated with the text entered by the user in the
				// search input.
				var searchTokenUserInput = searchRequest.getSearchQuery().stream()
					.filter(ParserSearchToken::isSearch)
					.findFirst();

				// extract the query text if present.
				var queryText = searchTokenUserInput
					.map(this::_extractAndValidateQueryText)
					.orElseThrow(() -> new AutocorrectionException(
							"Autocorrection was not performed because no search token with isSearch is present."
						)
					);

				// extract, if present, the field that override the searchWithCorrection configuration
				// otherwise use the Autocorrection configuration value.
				var doSearchWithCorrection = searchTokenUserInput
					.map(ParserSearchToken::getOverrideSearchWithCorrection)
					.orElse(autocorrectionConfig.getEnableSearchWithCorrection());

				// Create the autocorrection request for OpenSearch according to the
				// autocorrection configurations and return its JSON representation
				var autocorrectionQuery =
					_createAutocorrectionRequest(autocorrectionConfig, queryText);

				return new AutocorrectionContext(
					autocorrectionQuery,
					queryText,
					doSearchWithCorrection
				);
			});
	}

	/**
	 * Builds an OpenSearch Highlight configuration for the given fields and query text.
	 * <p>
	 * This method constructs a highlight clause that creates a boolean query combining both prefix
	 * and match queries for each field path. The highlighting is configured to return the entire
	 * field content (numberOfFragments=0) with custom tags wrapping matched terms, and
	 * disables field match requirement to allow highlighting across all specified fields.
	 * </p>
	 * <p>
	 * The highlight clause uses a boolean "should" query that includes prefix queries
	 * (matching terms that start with the query text) and match queries (matching terms
	 * containing the query text using OR operator). This combination ensures that both
	 * exact prefix matches and partial word matches are highlighted in the search results.
	 * </p>
	 * <p>
	 * The highlighting is configured with number of fragments set to 0 (returns entire field
	 * content), and require field match set to false (highlights across all fields regardless of
	 * where match occurred).
	 * </p>
	 *
	 * @param parentPathSet the list of fields to use for highlighting
	 * @param queryText the text to search for and highlight in the results
	 * @return a configured Highlight object ready to be used in an OpenSearch query
	 */
	private static Highlight _buildHighlight(Set<String> parentPathSet, String queryText) {

		// used to collect all queries used in the bool query
		List<Query> boolQueryList = new ArrayList<>();

		// prefix queries used in the bool query
		parentPathSet.stream()
			.map(parent ->
				new PrefixQuery.Builder().field(parent).value(queryText).build()
			)
			.map(prefixQuery ->
				new Query.Builder().prefix(prefixQuery).build()
			)
			.forEach(boolQueryList::add);

		// match queries used in the bool query
		parentPathSet.stream()
			.map(parent ->
				new MatchQuery.Builder()
					.field(parent)
					.query(new FieldValue.Builder().stringValue(queryText).build())
					.operator(Operator.Or)
					.build()
			)
			.map(matchQuery ->
				new Query.Builder().match(matchQuery).build()
			)
			.forEach(boolQueryList::add);

		// highlight
		var highlightBuilder = new Highlight.Builder()
			.requireFieldMatch(false)
			.preTags("")
			.postTags("")
			.numberOfFragments(0)
			.highlightQuery(
				new Query.Builder()
					.bool(new BoolQuery.Builder().should(boolQueryList).build())
					.build()
			);

		// fields
		var highlightField = new org.opensearch.client.opensearch.core.search.HighlightField.Builder()
			.numberOfFragments(0)
			.build();

		for (String parentPath : parentPathSet) {
			highlightBuilder.fields(parentPath, highlightField);
		}

		return highlightBuilder.build();
	}

	/**
	 * Creates an OpenSearch search request for autocomplete based on the provided configurations.
	 * Builds a multi-match query with bool_prefix type that searches the query text across
	 * the configured fields, applying fuzziness, minimum should match, and operator settings.
	 *
	 * @param configurations the autocomplete configurations containing fields, and indices.
	 * @param queryText the text to get the autocorrection for
	 * @return a configured search request sorted by relevance in descending order
	 */
	private org.opensearch.client.opensearch.core.SearchRequest _createAutocompleteRequest(
			AutocompleteConfigurationsResponse configurations, String queryText) {

		var fieldList = configurations.getFieldList();

		var fieldPathList = fieldList.stream()
			.map(field -> field.getFieldPath() + "^" + field.getBoost())
			.toList();
		var parentPathSet = fieldList.stream()
			.map(io.openk9.searcher.grpc.Field::getParentPath)
			.collect(Collectors.toSet());

		Query mainQuery = Query.of(
			q -> q
				.multiMatch(m -> m
					.query(queryText)
					.fields(fieldPathList)
					.type(TextQueryType.BoolPrefix)
					.fuzziness(configurations.getFuzziness())
					.minimumShouldMatch(configurations.getMinimumShouldMatch())
					.operator(_grpcEnumToOperator(configurations.getOperator()))
				)
		);

		var highlight = _buildHighlight(parentPathSet, queryText);

		return org.opensearch.client.opensearch.core.SearchRequest.of(s -> s
			.index(configurations.getIndexNameList())
			.source(src -> src.fetch(false))
			.query(mainQuery)
			.size(configurations.getResultSize())
			.highlight(highlight)
			.sort(sort -> sort
				.score(sc -> sc.order(SortOrder.Desc))
			)
		);
	}

	/**
	 * Creates an OpenSearch search request configured for autocorrection suggestions.
	 * The request uses a term suggester to provide correction suggestions based on the query text,
	 * returning only suggestions without documents (size=0).
	 *
	 * @param autocorrectionConfig the autocorrection configuration containing index names,
	 *                            field, and suggester parameters
	 * @param queryText           the query text to be corrected
	 *
	 * @return a configured SearchRequest with term suggester for autocorrection
	 */
	private org.opensearch.client.opensearch.core.SearchRequest _createAutocorrectionRequest(
		AutocorrectionConfigurationsResponse autocorrectionConfig, String queryText) {

		return org.opensearch.client.opensearch.core.SearchRequest.of(s -> s
			.index(autocorrectionConfig.getIndexNameList())
			// to retrieve only suggestions, not documents
			.size(0)
			.suggest(Suggester.of(sug -> sug
				.text(queryText)
				.suggesters(AUTOCORRECTION_SUGGESTION, fsb ->
					// Term suggest precisely corrects individual misspelled words without altering the entire phrase
					fsb.term(tsb -> tsb
						.field(autocorrectionConfig.getField())
						// max number of suggestions to retrieve for each term
						.size(1)
						.sort(
							_grpcEnumToSuggestSort(
								autocorrectionConfig.getSort())
						)
						.suggestMode(
							_grpcEnumToSuggestMode(
								autocorrectionConfig.getSuggestMode()
							)
						)
						.prefixLength(autocorrectionConfig.getPrefixLength())
						.minWordLength(autocorrectionConfig.getMinWordLength())
						.maxEdits(autocorrectionConfig.getMaxEdit())
					)
				)
			))
		);
	}

	/**
	 * Executes a search request by parsing the query and performing an asynchronous OpenSearch query.
	 *
	 * Converts the search request into a query parser request, retrieves the parsed query and
	 * target indices, then executes the search asynchronously against OpenSearch.
	 * Returns an empty response if no indices are found.
	 *
	 * @param searchRequest the search request to execute
	 * @return a Uni emitting the search response, or a WebApplicationException on failure
	 */
	@WithSpan
	protected Uni<Response> _doSearch(SearchRequest searchRequest) {
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

				if (indexNameList.isEmpty()) {
					return Uni.createFrom().item(Response.EMPTY);
				}

				String indexNames =
					String.join(",", indexNameList);

				var queryParams = queryParserResponse.getQueryParametersMap();

				org.opensearch.client.Request openSearchRequest =
					new org.opensearch.client.Request(
						"GET", "/" + indexNames + "/_search");

				openSearchRequest.addParameters(queryParams);

				openSearchRequest.setJsonEntity(searchRequestBody);

				return Uni.createFrom().<SearchResponse>emitter((sink) -> restHighLevelClient
						.getLowLevelClient()
						.performRequestAsync(openSearchRequest, new ResponseListener() {
							@Override
							public void onFailure(Exception e) {
								sink.fail(e);
							}

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
						}))
					.map(this::toSearchResponse);
			})
			.onFailure()
			.transform(throwable -> new WebApplicationException(
				getErrorResponse(throwable))
			);
	}

	/**
	 * Parses the OpenSearch autocomplete response as an {@link AutocompleteHit}.
	 * <p>
	 * This method extracts term suggestions from the OpenSearch response, filters out empty results,
	 * and constructs an {@link AutocompleteHit} containing autocomplete text, the doc type field
	 * label and the score.
	 *
	 * @param response the OpenSearch search response containing autocomplete suggestions
	 * @param fields the fields used to retrieve the autocomplete suggestions
	 *
	 * @return an {@link List<AutocompleteHit>} containing autocomplete data
	 */
	private List<AutocompleteHit> _extractAutocompleteResponse(
			org.opensearch.client.opensearch.core.SearchResponse<Map> response,
			List<io.openk9.searcher.grpc.Field> fields) {

		List<AutocompleteHit> autocompleteHits = new ArrayList<>();

		for (Hit<Map> hit : response.hits().hits()) {
			var score = hit.score();
			var highlight = hit.highlight();

			if (highlight != null) {
				for (io.openk9.searcher.grpc.Field field : fields) {
					List<String> autocomplete = highlight.get(field.getParentPath());

					if (autocomplete != null && !autocomplete.isEmpty()) {
						autocompleteHits.add(
							new AutocompleteHit(autocomplete.getFirst(), field.getLabel(), score)
						);
					}
				}
			}
		}

		return autocompleteHits;
	}

	/**
	 * Parses the OpenSearch autocorrection response as an {@link AutocorrectionDTO}.
	 * <p>
	 * This method extracts term suggestions from the OpenSearch response, filters out empty results,
	 * and constructs an {@link AutocorrectionDTO} containing the original text, correction details,
	 * and a generated autocorrection text. Each suggestion includes the original term,
	 * its position (offset and length), and the suggested correction.
	 *
	 * @param originalText the original query text entered by the user
	 * @param response the OpenSearch search response containing autocorrection suggestions
	 * @param enableSearchWithCorrection flag indicating whether the search was executed with the
	 *                                   corrected text
	 * @return an {@link AutocorrectionDTO} containing autocorrection data or {@code null} if no
	 *         valid suggestions are found
	 */
	private AutocorrectionDTO _extractAutocorrectionResponse(
		String originalText,
		org.opensearch.client.opensearch.core.SearchResponse<Void> response,
		Boolean enableSearchWithCorrection) {

		var suggestions = response.suggest().get(AUTOCORRECTION_SUGGESTION);

		if (suggestions == null || suggestions.isEmpty()) {
			return null;
		}

		List<AutocorrectionDTO.Suggestion> resultSuggestions = _extractSuggestions(suggestions);

		if (resultSuggestions.isEmpty()) {
			return null;
		}

		String autocorrectionText =
			_generateAutocorrectionText(originalText, resultSuggestions);

		return AutocorrectionDTO.builder()
			.originalText(originalText)
			.autocorrectionText(autocorrectionText)
			.searchedWithCorrectedText(enableSearchWithCorrection)
			.suggestions(resultSuggestions)
			.build();
	}

	/**
	 * Extracts and validates the query text from the provided search token.
	 *
	 * <p>This method performs the following validations:
	 * <ul>
	 *   <li>Ensures the search token contains exactly one value</li>
	 *   <li>Ensures the query text is not null or empty</li>
	 * </ul>
	 *
	 * @param searchTokenUserInput the search token to validate
	 * @return the validated query text entered by the user in the search input
	 * @throws AutocorrectionException if the search token contains 0 or more than 1 values
	 * @throws AutocorrectionException if the query text is null or empty
	 *
	 * @see ParserSearchToken
	 * @see AutocorrectionException
	 */
	private String _extractAndValidateQueryText(ParserSearchToken searchTokenUserInput) {

		var searchTokenUserInputValues = searchTokenUserInput.getValues();

		if (searchTokenUserInputValues.size() != 1) {
			throw new AutocorrectionException(
				"Autocorrection was not performed because the user input search token has 0 or more than 1 values."
			);
		}

		// retrieve the text entered by the user in the search input.
		var queryText = searchTokenUserInputValues.getFirst();

		if (queryText == null || queryText.isEmpty()) {
			throw new AutocorrectionException(
				"Autocorrection was not performed because the user input text is null or empty."
			);
		}

		return queryText;
	}

	/**
	 * Extracts and transforms term suggestions from the OpenSearch suggest response.
	 *
	 * @param suggestions the OpenSearch suggestions list
	 * @return a list of extracted autocorrection suggestions, or an empty list if no valid suggestions exist
	 */
	private List<AutocorrectionDTO.Suggestion> _extractSuggestions(List<Suggest<Void>> suggestions) {
		return suggestions.stream()
			.filter(Suggest::isTerm)
			.map(Suggest::term)
			.filter(termSuggest -> !termSuggest.options().isEmpty())
			.map(termSuggest ->
				new AutocorrectionDTO.Suggestion(
					termSuggest.text(),
					termSuggest.offset(),
					termSuggest.length(),
					termSuggest.options().getFirst().text()
				)
			)
			.toList();
	}

	/**
	 * Retrieves autocomplete configurations from the search service.
	 *
	 * <p>This method sends a request to datasource to fetch autocomplete configurations.
	 * If the configurations are not found (404 response), the method returns null. For any other
	 * failure, the method logs the error and propagates an AutocompleteException.
	 *
	 * <p>Error handling:
	 * <ul>
	 *   <li>404 Not Found: Returns null</li>
	 *   <li>Other failures: Logs the error and throws AutocompleteException</li>
	 * </ul>
	 *
	 * @param request the request containing parameters for fetching autocomplete configurations
	 * @return a {@link Uni} that emits the autocomplete configurations if found, or null if not found.
	 *         In case of unexpected errors, the Uni will emit an AutocompleteException
	 *
	 * @throws AutocompleteException if the retrieval fails for any reason other than 404 Not Found
	 *
	 * @see AutocompleteConfigurationsRequest
	 * @see AutocompleteConfigurationsResponse
	 * @see AutocompleteException
	 */
	private Uni<AutocompleteConfigurationsResponse> _getAutocompleteConfigurations(
			AutocompleteConfigurationsRequest request) {

		return searcherClient.getAutocompleteConfigurations(request)
			.onFailure(this::_isNotFound)
			.recoverWithNull()
			.onFailure()
			.recoverWithUni(throwable -> {
				_logMessage("Retrieve autocomplete configurations failed", throwable);
				return Uni.createFrom().failure(
					new AutocompleteException(
						"Retrieve autocomplete configurations failed", throwable
					)
				);
			});
	}

	/**
	 * Retrieves autocorrection configurations from the search service.
	 *
	 * <p>This method sends a request to datasource to fetch autocorrection configurations.
	 * If the configurations are not found (404 response), the method returns null. For any other
	 * failure, the method logs the error and propagates an AutocorrectionException.
	 *
	 * <p>Error handling:
	 * <ul>
	 *   <li>404 Not Found: Returns null</li>
	 *   <li>Other failures: Logs the error and throws AutocorrectionException</li>
	 * </ul>
	 *
	 * @param autocorrectionConfigurationsRequest the request containing parameters for fetching
	 *                                            autocorrection configurations
	 * @return a {@link Uni} that emits the autocorrection configurations if found, or null if not found.
	 *         In case of unexpected errors, the Uni will emit an AutocorrectionException
	 *
	 * @throws AutocorrectionException if the retrieval fails for any reason other than 404 Not Found
	 *
	 * @see AutocorrectionConfigurationsRequest
	 * @see AutocorrectionConfigurationsResponse
	 * @see AutocorrectionException
	 */
	private Uni<AutocorrectionConfigurationsResponse> _getAutocorrectionConfigurations(
			AutocorrectionConfigurationsRequest autocorrectionConfigurationsRequest) {

		return searcherClient.getAutocorrectionConfigurations(autocorrectionConfigurationsRequest)
			.onFailure(this::_isNotFound)
			.recoverWithNull()
			.onFailure()
			.recoverWithUni(throwable -> {
				_logMessage("Retrieve autocorrection configurations failed", throwable);
				return Uni.createFrom().failure(
					new AutocorrectionException(
						"Retrieve autocorrection configurations failed", throwable
					)
				);
			});
	}

	/**
	 * Retrieves autocomplete suggestions from OpenSearch.
	 *
	 * <p>This method executes an autocomplete query against OpenSearch using the provided query.
	 * The method uses distributed tracing to track the OpenSearch request execution.
	 *
	 * @param autocompleteQuery the OpenSearch query configured according to the Autocomplete configurations.
	 * @return a {@link Uni} that emits an {@link SearchResponse} containing autocomplete suggestions
	 *         and metadata. If the OpenSearch request fails, the Uni emits an AutocompleteException.
	 *
	 * @throws AutocompleteException if the OpenSearch autocomplete request fails or returns an error.
	 *
	 * @see org.opensearch.client.opensearch.core.SearchRequest
	 * @see SearchResponse
	 * @see AutocompleteException
	 */
	@WithSpan
	protected Uni<org.opensearch.client.opensearch.core.SearchResponse<Map>> _getAutocompleteSuggest(
			org.opensearch.client.opensearch.core.SearchRequest autocompleteQuery) {

		// retrieve autocomplete suggestions
		return Uni.createFrom().completionStage(() -> {
				try {
					return client.search(autocompleteQuery, Map.class);
				}
				catch (IOException | OpenSearchException e) {
					return CompletableFuture.failedFuture(e);
				}
			})
			.onFailure()
			.recoverWithUni(throwable -> {
				_logMessage("Autocomplete failed", throwable);
				return Uni.createFrom().failure(
					new AutocompleteException("Autocomplete failed", throwable)
				);
			});
	}

	/**
	 * Retrieves autocorrection suggestions from OpenSearch using the term suggester.
	 *
	 * <p>This method executes an autocorrection query against OpenSearch using the provided context,
	 * which contains the generated query, the text to be corrected, and The configuration that
	 * determines whether the search should use the corrected query.
	 * The method uses distributed tracing to track the OpenSearch request execution.
	 *
	 * @param autocorrectionContext a context object containing the autocorrection configuration,
	 *                              the OpenSearch query, and the text to be corrected
	 * @return a {@link Uni} that emits an {@link AutocorrectionDTO} containing autocorrection suggestions
	 *         and metadata. If the OpenSearch request fails, the Uni emits an AutocorrectionException
	 *
	 * @throws AutocorrectionException if the OpenSearch autocorrection request fails or returns an error
	 *
	 * @see AutocorrectionContext
	 * @see AutocorrectionDTO
	 * @see AutocorrectionException
	 */
	@WithSpan
	protected Uni<AutocorrectionDTO> _getAutocorrectionSuggest(
			AutocorrectionContext autocorrectionContext) {

		var autocorrectionRequest = autocorrectionContext.query();
		var queryText = autocorrectionContext.textToCorrect();

		// retrieve autocorrection suggestions
		return Uni.createFrom().completionStage(() -> {
				Span span = tracer.spanBuilder("opensearch-autocorrection-suggestions")
					.setAttribute("queryText", queryText)
					.startSpan();
				try {
					return client.search(autocorrectionRequest, Void.class)
						.thenApply(response -> {
							span.addEvent("suggestions-obtained");
							return response;
						});
				}
				catch (IOException | OpenSearchException e) {
					span.recordException(e);
					span.setStatus(StatusCode.ERROR);
					return CompletableFuture.failedFuture(e);
				}
				finally {
					span.end();
				}
			})
			.onFailure()
			.recoverWithUni(throwable -> {
				_logMessage("Autocorrection failed", throwable);
				return Uni.createFrom().failure(
					new AutocorrectionException("Autocorrection failed", throwable)
				);
			})
			.map(voidSearchResponse ->
				_extractAutocorrectionResponse(
					queryText,
					voidSearchResponse,
					autocorrectionContext.doSearchWithCorrection()
				)
			);
	}

	/**
	 * Converts a gRPC {@link io.openk9.searcher.grpc.Operator} enum
	 * to the corresponding {@link org.opensearch.client.opensearch._types.query_dsl.Operator} enum.
	 *
	 * @param operator the gRPC Operator to convert
	 * @return the corresponding Operator value (Or for OR/UNRECOGNIZED, And for AND)
	 */
	private Operator _grpcEnumToOperator(io.openk9.searcher.grpc.Operator operator) {
		return switch (operator) {
			case OR, UNRECOGNIZED -> Operator.Or;
			case AND -> Operator.And;
		};
	}

	/**
	 * Converts a gRPC {@link io.openk9.searcher.grpc.SuggestMode} enum
	 * to the corresponding {@link org.opensearch.client.opensearch._types.SuggestMode} enum.
	 *
	 * @param suggestMode the gRPC SuggestMode to convert
	 * @return the corresponding SuggestMode value (Missing for MISSING/UNRECOGNIZED, Popular for POPULAR, Always for ALWAYS)
	 */
	private SuggestMode _grpcEnumToSuggestMode(io.openk9.searcher.grpc.SuggestMode suggestMode) {
		return switch (suggestMode) {
			case MISSING, UNRECOGNIZED -> SuggestMode.Missing;
			case POPULAR -> SuggestMode.Popular;
			case ALWAYS -> SuggestMode.Always;
		};
	}

	/**
	 * Converts a gRPC {@link io.openk9.searcher.grpc.SortType} enum
	 * to the corresponding {@link org.opensearch.client.opensearch.core.search.SuggestSort} enum.
	 *
	 * @param sort the gRPC SortType to convert
	 * @return the corresponding SuggestSort value (Score for SCORE/UNRECOGNIZED, Frequency for FREQUENCY)
	 */
	private SuggestSort _grpcEnumToSuggestSort(SortType sort) {
		return switch (sort) {
			case SCORE, UNRECOGNIZED -> SuggestSort.Score;
			case FREQUENCY -> SuggestSort.Frequency;
		};
	}

	/**
	 * Checks whether the provided exception represents a gRPC NOT_FOUND error.
	 *
	 * <p>This method verifies if the {@link Throwable} is an instance of
	 * {@link StatusRuntimeException} and if the associated status code is
	 * {@link Status.Code#NOT_FOUND}.</p>
	 *
	 * @param failure the exception to check
	 * @return {@code true} if the exception is a {@code StatusRuntimeException}
	 *         with NOT_FOUND status code, {@code false} otherwise
	 *
	 * @see io.grpc.StatusRuntimeException
	 * @see io.grpc.Status.Code#NOT_FOUND
	 */
	private boolean _isNotFound(Throwable failure) {
		return failure instanceof StatusRuntimeException &&
			((StatusRuntimeException) failure).getStatus().getCode() == Status.Code.NOT_FOUND;
	}

	/**
	 * Logs a message with optional throwable information based on debug level.
	 * If debug logging is enabled, logs the message with throwable details at DEBUG level.
	 * Otherwise, logs only the message at WARN level.
	 *
	 * @param throwable the throwable to include in debug logging, ignored if debug is disabled
	 */
	private void _logMessage(Throwable throwable) {
		_logMessage(throwable.getMessage(), throwable);
	}

	/**
	 * Logs a message with optional throwable information based on debug level.
	 * If debug logging is enabled, logs the message with throwable details at DEBUG level.
	 * Otherwise, logs only the message at WARN level.
	 *
	 * @param message the message to log
	 * @param throwable the throwable to include in debug logging, ignored if debug is disabled
	 */
	private void _logMessage(String message, Throwable throwable) {
		if (log.isDebugEnabled()) {
			log.warn(message, throwable);
		}
		else {
			log.warn(message);
		}
	}

	/**
	 * Updates the search query with the corrected text if search with correction was enabled.
	 *
	 * <p>This method extracts the search token from the search request and replaces its value
	 * with the autocorrected text if autocorrection was successfully performed, a search token exists
	 * search with correction was enabled.
	 * If any of the required conditions are not met, the search query remains unchanged.
	 *
	 * @param autocorrectionResult the autocorrection result containing the corrected text and metadata
	 * @param searchRequest the search request whose query will be updated with the corrected text
	 *
	 * @see AutocorrectionDTO
	 * @see SearchRequest
	 * @see ParserSearchToken
	 */
	private void _updateSearchQueryWithCorrection(
			AutocorrectionDTO autocorrectionResult, SearchRequest searchRequest) {

		// retrieve the searchToken associated with the text entered by the user in the search input.
		var searchTokenUserInput = searchRequest.getSearchQuery().stream()
			.filter(ParserSearchToken::isSearch)
			.findFirst();

		if (autocorrectionResult != null
			&& autocorrectionResult.isSearchedWithCorrectedText()
			&& searchTokenUserInput.isPresent()) {

			// replace query text in the searchRequest with the correction
			searchTokenUserInput.get().setValues(List.of(
				autocorrectionResult.getAutocorrectionText())
			);
		}
	}

	/**
	 * Validates that the autocomplete configuration is not null.
	 *
	 * <p>This method ensures that autocomplete is enabled by verifying the presence
	 * of a valid autocomplete configuration. If the configuration is null, it indicates
	 * that autocomplete is disabled.
	 *
	 * @param autocompleteConfig the autocomplete configuration to validate
	 * @throws AutocompleteException if the configuration is null, indicating autocomplete is disabled
	 *
	 * @see AutocompleteConfigurationsResponse
	 * @see AutocompleteException
	 */
	private void _validateAutocompleteConfig(
		AutocompleteConfigurationsResponse autocompleteConfig) {

		if (autocompleteConfig == null) {
			var autocompleteException = new AutocompleteException("Autocomplete is disabled.");
			_logMessage(autocompleteException);
			throw autocompleteException;
		}
	}

	/**
	 * Validates that the autocorrection configuration is not null.
	 *
	 * <p>This method ensures that autocorrection is enabled by verifying the presence
	 * of a valid autocorrection configuration. If the configuration is null, it indicates
	 * that autocorrection is disabled.
	 *
	 * @param autocorrectionConfig the autocorrection configuration to validate
	 * @throws AutocorrectionException if the configuration is null, indicating autocorrection is disabled
	 *
	 * @see AutocorrectionConfigurationsResponse
	 * @see AutocorrectionException
	 */
	private void _validateAutocorrectionConfig(
		AutocorrectionConfigurationsResponse autocorrectionConfig) {

		if (autocorrectionConfig == null) {
			throw new AutocorrectionException("Autocorrection is disabled.");
		}
	}

	private jakarta.ws.rs.core.Response getErrorResponse(Throwable throwable) {

		int statusCode = NONE_STATUS_CODE;
		String reason = "Unable to serve search request";

		if (throwable instanceof ResponseException responseException) {
			statusCode = responseException.getResponse()
				.getStatusLine()
				.getStatusCode();
		}

		jakarta.ws.rs.core.Response.Status responseStatus = null;

		switch (statusCode) {
			case NONE_STATUS_CODE, NOT_FOUND_STATUS_CODE -> {
				responseStatus =
					jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

				log.error(reason, throwable);
			}
			default -> {
				reason = "Invalid search request";
				responseStatus =
					jakarta.ws.rs.core.Response.Status.fromStatusCode(statusCode);

				log.warn(reason, throwable);
			}
		}

		return jakarta.ws.rs.core.Response
			.status(responseStatus)
			.entity(JsonObject
				.of(DETAILS_FIELD, reason))
			.build();
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

		var rawToken = getRawToken(headers);
		builder.setSearchText(searchRequest.getSearchText());
		builder.setVirtualHost(request.authority().toString());
		builder.setJwt(rawToken);
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

	private QueryParserRequest getQueryParserRequest(SearchRequest searchRequest) {

		var requestBuilder = searcherMapper
			.toQueryParserRequest(searchRequest)
			.toBuilder();

		Map<String, Value> extra = new HashMap<>();

		for (String headerName : supportedHeadersName) {
			List<String> requestHeader = headers.getRequestHeader(headerName);
			if (requestHeader != null && !requestHeader.isEmpty()) {
				extra.put(headerName, Value.newBuilder().addAllValue(requestHeader).build());
			}
		}

		var rawToken = getRawToken(headers);

		String sortAfterKey = searchRequest.getSortAfterKey();
		String language = searchRequest.getLanguage();

		return requestBuilder
			.setVirtualHost(request.authority().toString())
			.setJwt(rawToken)
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

	private void printShardFailures(SearchResponse searchResponse) {
		if (searchResponse.getShardFailures() != null) {
			for (ShardSearchFailure failure : searchResponse.getShardFailures()) {
				log.warn(failure.reason());
			}
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
		var totalResult = totalHits != null
			? Math.min(totalHits.value, totalResultLimit)
			: 0;

		return new Response(result, totalResult, null);
	}

	protected record AutocompleteContext(
		org.opensearch.client.opensearch.core.SearchRequest query,
		List<io.openk9.searcher.grpc.Field> fields
	) {}

	protected record AutocorrectionContext(
		org.opensearch.client.opensearch.core.SearchRequest query,
		String textToCorrect,
		boolean doSearchWithCorrection
	) {}
}
