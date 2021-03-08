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

import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.repository.DatasourceRepository;
import io.openk9.datasource.repository.TenantRepository;
import io.openk9.http.util.HttpUtil;
import io.openk9.http.web.Endpoint;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.json.api.JsonFactory;
import io.openk9.search.api.query.SearchRequest;
import io.openk9.search.api.query.SearchToken;
import io.openk9.search.api.query.SearchTokenizer;
import io.openk9.search.client.api.Search;
import io.openk9.search.client.api.util.SearchUtil;
import io.openk9.ingestion.driver.manager.api.DocumentType;
import io.openk9.ingestion.driver.manager.api.DocumentTypeProvider;
import io.openk9.ingestion.driver.manager.api.PluginDriver;
import io.openk9.ingestion.driver.manager.api.PluginDriverRegistry;
import io.openk9.ingestion.driver.manager.api.SearchKeyword;
import io.openk9.search.query.internal.response.Response;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
	immediate = true,
	service = Endpoint.class,
	property = {
		"base.path=/v1/search"
	}
)
public class SearchHTTPHandler implements HttpHandler {

	@Override
	public String getPath() {
		return "";
	}

	@Override
	public int method() {
		return HttpHandler.GET + HttpHandler.POST;
	}

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		String hostName = HttpUtil.getHostName(httpRequest);

		return _tenantRepository
			.findByVirtualHost(hostName)
			.switchIfEmpty(
				Mono.error(
					() -> new RuntimeException(
						"tenant not found for virtualhost: " + hostName)))
			.map(Tenant::getTenantId)
			.zipWith(Mono.from(httpRequest.aggregateBodyToString()))
			.flatMap(t2 -> _datasourceRepository.findByTenantIdAndIsActive(t2.getT1())
				.collectList()
				.flatMap(datasources -> _toQuerySearchRequest(
					t2.getT1(), datasources,
					_searchTokenizer.parse(t2.getT2()))))
			.map(SearchResponse::getHits)
			.map(this::_searchHitToResponse)
			.map(_jsonFactory::toJson)
			.transform(httpResponse::sendString);

	}

	private Response _searchHitToResponse(SearchHits hits) {

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
		long tenantId, List<Datasource> datasources, SearchRequest searchRequest) {

		return _search.search(factory -> {

			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

			long[] ids = new long[datasources.size()];

			List<String> serviceDriverNames =
				new ArrayList<>(datasources.size());

			for (int i = 0; i < datasources.size(); i++) {

				Datasource datasource = datasources.get(i);

				ids[i] = datasource.getDatasourceId();

				String driverServiceName = datasource.getDriverServiceName();

				if (!serviceDriverNames.contains(driverServiceName)) {
					serviceDriverNames.add(driverServiceName);
				}

			}

			boolQuery.filter(
				QueryBuilders
					.termsQuery("datasourceId", ids)
			);

			Map<String, List<SearchToken>> tokenTypeGroup =
				searchRequest
					.getSearchQuery()
					.stream()
					.collect(Collectors.groupingBy(SearchToken::getTokenType));

			List<SearchToken> datasource = tokenTypeGroup.get("DATASOURCE");

			Collection<PluginDriver> pluginDriverList =
				_pluginDriverRegistry.getPluginDriverList(serviceDriverNames);

			Map<PluginDriver, List<DocumentType>> pluginDriverListDocumentType =
				pluginDriverList
					.stream()
					.collect(
						Collectors.toMap(
							Function.identity(),
							pd -> {

								List<DocumentType> supportedDocumentTypes =
									_documentTypeProvider.getDocumentTypeList(
										pd.getName());

								if (supportedDocumentTypes.isEmpty()) {
									return Collections.singletonList(
										_documentTypeProvider
											.getDefaultDocumentType(
												pd.getName())
									);
								}

								return supportedDocumentTypes;
							},
							(o1, o2) -> {
								throw new IllegalStateException(
									String.format("Duplicate key %s", o1));
							},
							IdentityHashMap::new
						)
					);


			Stream<Map.Entry<PluginDriver, List<DocumentType>>>
				documentTypeStream =
					pluginDriverListDocumentType.entrySet().stream();

			if (datasource != null) {

				List<String> datasourceValues = datasource
					.stream()
					.map(SearchToken::getValues)
					.flatMap(Arrays::stream)
					.distinct()
					.collect(Collectors.toList());

				documentTypeStream =
					documentTypeStream
						.filter(
							entry ->
								datasourceValues.contains(
									entry.getKey().getName()));

			}

			List<Map.Entry<PluginDriver, List<DocumentType>>> documentTypeList =
				documentTypeStream.collect(Collectors.toList());

			if (documentTypeList.isEmpty()) {
				return SearchUtil.EMPTY_SEARCH_REQUEST;
			}

			Stream.of(
				tokenTypeGroup
					.getOrDefault("ENTITY", Collections.emptyList())
					.stream()
					.map(this::_entityEnrichBoolQuery),
				Stream.of(_docTypeBoolQuery(
					tokenTypeGroup
						.getOrDefault("DOCTYPE", Collections.emptyList()))),
				this._textEntityQuery(
					tokenTypeGroup.getOrDefault(
						"TEXT", Collections.emptyList()), documentTypeList)
				)
					.flatMap(Function.identity())
					.reduce(Consumer::andThen)
					.orElse(_DEFAULT_CONSUMER)
					.accept(boolQuery);

			org.elasticsearch.action.search.SearchRequest elasticSearchQuery;

			if (datasource != null) {

				String[] indexNames = documentTypeList
					.stream()
					.map(Map.Entry::getKey)
					.map(PluginDriver::getName)
					.distinct()
					.toArray(String[]::new);

				elasticSearchQuery =
					factory.createSearchRequestData(tenantId, indexNames);
			}
			else {
				elasticSearchQuery =
					factory.createSearchRequestData(tenantId, "*");
			}

			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

			int[] range = searchRequest.getRange();

			searchSourceBuilder.from(range[0]);
			searchSourceBuilder.size(range[1]);

			searchSourceBuilder.query(boolQuery);

			HighlightBuilder highlightBuilder = new HighlightBuilder();

			documentTypeList
				.stream()
				.map(Map.Entry::getValue)
				.flatMap(Collection::stream)
				.map(DocumentType::getSearchKeywords)
				.flatMap(Collection::stream)
				.filter(SearchKeyword::isText)
				.map(SearchKeyword::getKeyword)
				.distinct()
				.forEach(highlightBuilder::field);

			highlightBuilder.forceSource(true);

			highlightBuilder.tagsSchema("default");

			searchSourceBuilder.highlighter(highlightBuilder);

			if (_log.isDebugEnabled()) {
				_log.debug(searchSourceBuilder.toString());
			}

			return elasticSearchQuery.source(searchSourceBuilder);

		});

	}

	private Consumer<BoolQueryBuilder> _docTypeBoolQuery(
		List<SearchToken> searchTokenList) {

		if (searchTokenList.isEmpty()) {
			return _DEFAULT_CONSUMER;
		}

		String[][] typeArray =
			searchTokenList
				.stream()
				.map(SearchToken::getValues)
				.toArray(String[][]::new);

		return bool -> {

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

			for (String[] types : typeArray) {

				BoolQueryBuilder shouldBool = QueryBuilders.boolQuery();

				for (String type : types) {
					shouldBool
						.should(
							QueryBuilders
								.matchQuery("type", type)
								.operator(Operator.AND)
						);
				}

				boolQueryBuilder.must(shouldBool);

			}

			bool.filter(boolQueryBuilder);

		};
	}

	private Stream<Consumer<BoolQueryBuilder>> _textEntityQuery(
		List<SearchToken> tokenTextList,
		List<Map.Entry<PluginDriver, List<DocumentType>>> entityMapperList) {

		return tokenTextList
			.stream()
			.map(searchToken -> _termQueryPrefixValues(
				searchToken, entityMapperList));
	}

	private Consumer<BoolQueryBuilder> _termQueryPrefixValues(
		SearchToken tokenText,
		List<Map.Entry<PluginDriver, List<DocumentType>>> entityMapperList) {

		return query -> {

			String[] values = tokenText.getValues();

			if (values.length == 0) {
				return;
			}

			String keywordKey = tokenText.getKeywordKey();

			Predicate<SearchKeyword> keywordKeyPredicate =
				searchKeyword -> keywordKey == null || keywordKey.isEmpty() ||
								 searchKeyword.getKeyword().equals(keywordKey);

			Map<String, Float> keywordBoostMap =
				entityMapperList
					.stream()
					.map(Map.Entry::getValue)
					.flatMap(Collection::stream)
					.map(DocumentType::getSearchKeywords)
					.flatMap(Collection::stream)
					.filter(SearchKeyword::isText)
					.distinct()
					.filter(keywordKeyPredicate)
					.map(SearchKeyword::getFieldBoost)
					.collect(
						Collectors.toMap(
							Map.Entry::getKey, Map.Entry::getValue));


			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

			for (String value : values) {

				MultiMatchQueryBuilder multiMatchQueryBuilder =
					new MultiMatchQueryBuilder(value);

				multiMatchQueryBuilder.fields(keywordBoostMap);

				boolQueryBuilder.should(multiMatchQueryBuilder);

				multiMatchQueryBuilder =
					new MultiMatchQueryBuilder(value);

				multiMatchQueryBuilder.fields(keywordBoostMap);

				multiMatchQueryBuilder.type(
					MultiMatchQueryBuilder.Type.PHRASE);

				multiMatchQueryBuilder.slop(2);

				multiMatchQueryBuilder.boost(2.0f);

				boolQueryBuilder.should(multiMatchQueryBuilder);

			}

			query.must(boolQueryBuilder);

		};
	}

	private Consumer<BoolQueryBuilder> _entityEnrichBoolQuery(
		SearchToken searchToken) {

		return boolQueryBuilder -> {

			String[] ids = searchToken.getValues();

			String entityType = searchToken.getEntityType();

			String nestEntityPath = ENTITIES + "." + entityType;

			String nestIdPath = nestEntityPath + ".id";

			BoolQueryBuilder innerNestBoolQuery = QueryBuilders
				.boolQuery()
				.must(_multiMatchValues(nestIdPath, ids));

			String keywordKey = searchToken.getKeywordKey();

			if (keywordKey != null && !keywordKey.isEmpty()) {
				innerNestBoolQuery
					.must(QueryBuilders.matchQuery(
						nestEntityPath + ".context", keywordKey));
			}

			boolQueryBuilder.filter(
				QueryBuilders.nestedQuery(
					nestEntityPath,
					innerNestBoolQuery,
					ScoreMode.Max)
			);

		};
	}

	private QueryBuilder _multiMatchValues(String field, String[] ids) {

		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		for (String id : ids) {
			boolQuery.should(QueryBuilders.termQuery(field, id));
		}

		return boolQuery;

	}

	private static final Consumer<BoolQueryBuilder> _DEFAULT_CONSUMER =
		(bool) -> {};

	private static final String ENTITIES = "entities";

	@Reference
	private TenantRepository _tenantRepository;

	@Reference
	private DatasourceRepository _datasourceRepository;

	@Reference
	private Search _search;

	@Reference
	private SearchTokenizer _searchTokenizer;

	@Reference
	private PluginDriverRegistry _pluginDriverRegistry;

	@Reference
	private DocumentTypeProvider _documentTypeProvider;

	@Reference
	private JsonFactory _jsonFactory;

	private static final Logger _log = LoggerFactory.getLogger(
		SearchHTTPHandler.class);

}
