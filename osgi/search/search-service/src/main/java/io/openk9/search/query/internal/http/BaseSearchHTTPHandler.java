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
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.Datasource;
import io.openk9.model.Tenant;
import io.openk9.plugin.driver.manager.client.api.PluginDriverManagerClient;
import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.SearchKeywordDTO;
import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchRequest;
import io.openk9.search.api.query.SearchToken;
import io.openk9.search.api.query.SearchTokenizer;
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
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseSearchHTTPHandler implements HttpHandler {

	@Override
	public abstract String getPath();

	@Override
	public abstract int method();

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		return  Mono.from(httpRequest.aggregateBodyToString())
			.flatMap(body -> _getTenantAndDatasourceList(httpRequest, httpResponse)
				.flatMap(tenantDatasources -> _toQuerySearchRequest(
					tenantDatasources.getT1(),
					tenantDatasources.getT2(),
					_searchTokenizer.parse(body),
					httpRequest
					)
				)
			)
			.map(this::searchHitToResponse)
			.map(_jsonFactory::toJson)
			.transform(httpResponse::sendString);

	}

	protected abstract Mono<Tuple2<Tenant, List<Datasource>>>
		_getTenantAndDatasourceList(
			HttpRequest httpRequest, HttpResponse httpResponse);

	protected Response searchHitToResponse(SearchResponse searchResponse) {

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

	private Mono<SearchResponse> _toQuerySearchRequest(
		Tenant tenant, List<Datasource> datasources, SearchRequest searchRequest,
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

	protected void customizeSearchSourceBuilder(
		Tenant tenant, List<Datasource> datasources, SearchRequest searchRequest,
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

	private final List<QueryParser> _queryParsers = new ArrayList<>();

	protected DatasourceClient _datasourceClient;

	protected Search _search;

	protected SearchTokenizer _searchTokenizer;

	protected PluginDriverManagerClient _pluginDriverManagerClient;

	protected JsonFactory _jsonFactory;

	private static final Logger _log = LoggerFactory.getLogger(
		BaseSearchHTTPHandler.class);

}
