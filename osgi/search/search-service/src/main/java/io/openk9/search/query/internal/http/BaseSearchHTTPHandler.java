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

package io.openk9.search.query.internal.http;

import io.openk9.datasource.client.api.DatasourceClient;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.Datasource;
import io.openk9.model.Tenant;
import io.openk9.plugin.driver.manager.client.api.PluginDriverManagerClient;
import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverContextDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTOList;
import io.openk9.plugin.driver.manager.model.SearchKeywordDTO;
import io.openk9.reactor.netty.util.ReactorNettyUtils;
import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchRequest;
import io.openk9.search.api.query.SearchToken;
import io.openk9.search.api.query.SearchTokenizer;
import io.openk9.search.client.api.Search;
import io.openk9.search.client.api.util.SearchUtil;
import io.openk9.search.query.internal.config.SearchConfig;
import io.openk9.search.query.internal.response.Response;
import io.openk9.search.query.internal.util.MapUtil;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseSearchHTTPHandler
	implements RouterHandler, HttpHandler {

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		return  Mono.from(ReactorNettyUtils.aggregateBodyAsString(httpRequest))
			.flatMap(body -> _getTenantAndDatasourceList(httpRequest, httpResponse)
				.flatMap(tenantDatasources -> _getPluginDriverContextDTO(tenantDatasources.getT2())
					.flatMap(pluginDriverDTOList ->
						_toQuerySearchRequest(
							tenantDatasources.getT1(),
							tenantDatasources.getT2(),
							pluginDriverDTOList,
							_searchTokenizer.parse(body),
							httpRequest
						)
						.map(searchResponse ->
							Tuples.of(
								tenantDatasources.getT1(),
								tenantDatasources.getT2(),
								pluginDriverDTOList,
								_searchTokenizer.parse(body),
								httpRequest,
								searchResponse
							)
						)
					)
				)
			)
			.flatMap(t -> searchHitToResponseMono(
				t.getT1(), t.getT2(), PluginDriverDTOList.of(
					t.getT3().getPluginDriverDTOList()), t.getT5(), t.getT4(),
				t.getT6())
			)
			.transform(stringMono -> _httpResponseWriter.write(
				httpResponse, stringMono))
			.contextWrite(
				c -> c.put("HTTP_REQUEST", httpRequest)
			);

	}

	protected abstract Mono<Tuple2<Tenant, List<Datasource>>>
		_getTenantAndDatasourceList(
			HttpServerRequest httpRequest, HttpServerResponse httpResponse);

	protected Mono<Object> searchHitToResponseMono(
		Tenant tenant, List<Datasource> datasourceList,
		PluginDriverDTOList pluginDriverDTOList,
		HttpServerRequest httpServerRequest, SearchRequest searchRequest,
		SearchResponse searchResponse) {
		return Mono.fromSupplier(() -> searchHitToResponse(searchResponse));
	}

	protected Object searchHitToResponse(SearchResponse searchResponse) {

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

			int maxStringLength = getSearchConfig().maxStringLength();

			if (maxStringLength != -1) {
				MapUtil.deepAbbreviateString(sourceMap, maxStringLength);
			}

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
				_log.warn(failure.reason());
			}
		}
	}

	private Mono<SearchResponse> _toQuerySearchRequest(
		Tenant tenant, List<Datasource> datasources,
		PluginDriverContextDTO pdDTOList, SearchRequest searchRequest,
		HttpServerRequest httpRequest) {

		return Mono.defer(() -> {

			List<PluginDriverDTO> pluginDriverDTOList =
				pdDTOList.getPluginDriverDTOList();

			Map<String, List<SearchToken>> tokenTypeGroup =
				searchRequest
					.getSearchQuery()
					.stream()
					.collect(Collectors.groupingBy(SearchToken::getTokenType));

			String suggestKeyword = searchRequest.getSuggestKeyword();

			if (suggestKeyword != null && !suggestKeyword.isBlank()) {

				List<SearchToken> textTokens = tokenTypeGroup.computeIfAbsent(
					SearchToken.TEXT, k -> new ArrayList<>(1));

				textTokens.add(SearchToken.ofText(suggestKeyword));

			}

			List<SearchToken> datasource = tokenTypeGroup.get(
				SearchToken.DATASOURCE);

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
				createQueryParserContext(
					tenant, datasources, httpRequest, tokenTypeGroup,
					documentTypeList, pdDTOList.getAclQuery())
			).flatMap(boolQueryBuilderConsumer ->
				_search.flatMapSearch(factory -> {

					long tenantId = tenant.getTenantId();

					if (documentTypeList.isEmpty()) {
						return Mono.just(SearchUtil.EMPTY_SEARCH_REQUEST);
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

					searchSourceBuilder.trackTotalHits(true);

					return customizeSearchSourceBuilderMono(
						tenant, datasources, searchRequest, documentTypeList,
						searchSourceBuilder, elasticSearchQuery);

				}));

		});

	}

	private void _includeExcludeFields(
		SearchSourceBuilder searchSourceBuilder,
		List<DocumentTypeDTO> documentTypeList) {

		String[] defaultIncludeFields = includeFields();
		String[] defaultExcludeFields = excludeFields();

		Set<String> includes = new HashSet<>(Arrays.asList(defaultIncludeFields));
		Set<String> excludes = new HashSet<>(Arrays.asList(defaultExcludeFields));

		for (DocumentTypeDTO documentTypeDTO : documentTypeList) {

			List<String> includeFields = documentTypeDTO.getIncludeFields();

			if (includeFields != null) {
				includes.addAll(includeFields);
			}

			List<String> excludeFields = documentTypeDTO.getExcludeFields();

			if (excludeFields != null) {
				excludes.addAll(excludeFields);
			}

		}

		searchSourceBuilder.fetchSource(
			includes.toArray(String[]::new),
			excludes.toArray(String[]::new)
		);

	}

	protected QueryParser.Context createQueryParserContext(
		Tenant tenant, List<Datasource> datasources,
		HttpServerRequest httpRequest,
		Map<String, List<SearchToken>> tokenTypeGroup,
		List<PluginDriverDTO> documentTypeList,
		String aclQuery) {

		return QueryParser.Context.of(
			tenant,
			datasources,
			documentTypeList,
			tokenTypeGroup,
			httpRequest,
			QueryParser.QueryCondition.DEFAULT,
			aclQuery
		);
	}

	private Mono<PluginDriverContextDTO> _getPluginDriverContextDTO(List<Datasource> datasources) {
		return Mono.defer(() -> {

			List<String> serviceDriverNames =
				new ArrayList<>(datasources.size());

			for (Datasource datasource : datasources) {

				String driverServiceName = datasource.getDriverServiceName();

				if (!serviceDriverNames.contains(driverServiceName)) {
					serviceDriverNames.add(driverServiceName);
				}

			}

			return _pluginDriverManagerClient
				.getPluginDriverContextDTO(serviceDriverNames);

		});
	}

	protected String[] excludeFields() {
		return new String[] {
			"resources.binaries.data",
			"rawContent"
		};
	}

	protected String[] includeFields() {
		return _EMPTY_ARRAY;
	}

	protected Mono<org.elasticsearch.action.search.SearchRequest> customizeSearchSourceBuilderMono(
		Tenant tenant, List<Datasource> datasources, SearchRequest searchRequest,
		List<PluginDriverDTO> documentTypeList,
		SearchSourceBuilder searchSourceBuilder,
		org.elasticsearch.action.search.SearchRequest elasticSearchQuery) {

		return Mono.fromSupplier(() -> {
			customizeSearchSourceBuilder(
				tenant, datasources, searchRequest, documentTypeList,
				searchSourceBuilder, elasticSearchQuery);
			return elasticSearchQuery.source(searchSourceBuilder);
		});

	}

	protected void customizeSearchSourceBuilder(
		Tenant tenant, List<Datasource> datasources, SearchRequest searchRequest,
		List<PluginDriverDTO> documentTypeList,
		SearchSourceBuilder searchSourceBuilder,
		org.elasticsearch.action.search.SearchRequest elasticSearchQuery) {

		int[] range = searchRequest.getRange();

		if (range != null) {
			searchSourceBuilder.from(range[0]);
			searchSourceBuilder.size(range[1]);
		}

		HighlightBuilder highlightBuilder = new HighlightBuilder();

		List<DocumentTypeDTO> documentTypes = documentTypeList
			.stream()
			.map(PluginDriverDTO::getDocumentTypes)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());

		documentTypes
			.stream()
			.map(DocumentTypeDTO::getSearchKeywords)
			.flatMap(Collection::stream)
			.filter(SearchKeywordDTO::isText)
			.map(SearchKeywordDTO::getKeyword)
			.distinct()
			.forEach(highlightBuilder::field);

		highlightBuilder.forceSource(true);

		highlightBuilder.tagsSchema("default");

		searchSourceBuilder.highlighter(highlightBuilder);

		List<SearchToken> searchQuery = searchRequest.getSearchQuery();

		if (searchQuery != null
			&& !searchQuery.isEmpty()
			&& searchQuery
				.stream()
				.anyMatch(st -> st.getFilter() == null || !st.getFilter())) {

			searchSourceBuilder.minScore(getSearchConfig().minScore());

		}

		_includeExcludeFields(
			searchSourceBuilder, documentTypes);

	}

	protected void addQueryParser(QueryParser queryParser) {
		_queryParsers.add(queryParser);
	}

	protected void removeQueryParser(QueryParser queryParser) {
		_queryParsers.remove(queryParser);
	}

	protected void setDatasourceClient(
		DatasourceClient datasourceClient) {
		_datasourceClient = datasourceClient;
	}

	protected void setSearch(Search search) {
		_search = search;
	}

	protected void setSearchTokenizer(
		SearchTokenizer searchTokenizer) {
		_searchTokenizer = searchTokenizer;
	}

	protected void setPluginDriverManagerClient(
		PluginDriverManagerClient pluginDriverManagerClient) {
		_pluginDriverManagerClient = pluginDriverManagerClient;
	}

	protected void setJsonFactory(JsonFactory jsonFactory) {
		_jsonFactory = jsonFactory;
	}

	protected void setHttpResponseWriter(HttpResponseWriter httpResponseWriter) {
		_httpResponseWriter = httpResponseWriter;
	}

	protected abstract SearchConfig getSearchConfig();

	private final List<QueryParser> _queryParsers = new ArrayList<>();

	protected DatasourceClient _datasourceClient;

	protected Search _search;

	protected SearchTokenizer _searchTokenizer;

	protected PluginDriverManagerClient _pluginDriverManagerClient;

	protected JsonFactory _jsonFactory;

	protected HttpResponseWriter _httpResponseWriter;

	private static final String[] _EMPTY_ARRAY = {};

	private static final Logger _log = LoggerFactory.getLogger(
		BaseSearchHTTPHandler.class);

}
