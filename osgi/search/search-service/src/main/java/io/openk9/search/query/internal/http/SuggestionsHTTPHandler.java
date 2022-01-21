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
import io.openk9.http.util.HttpUtil;
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.Datasource;
import io.openk9.model.SuggestionCategoryField;
import io.openk9.model.Tenant;
import io.openk9.plugin.driver.manager.client.api.PluginDriverManagerClient;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTOList;
import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchRequest;
import io.openk9.search.api.query.SearchToken;
import io.openk9.search.api.query.SearchTokenizer;
import io.openk9.search.client.api.Search;
import io.openk9.search.query.internal.response.SuggestionsResponse;
import io.openk9.search.query.internal.response.suggestions.Suggestions;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregation;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.util.function.Tuple2;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = RouterHandler.class,
	property = {
		"base.path=/v1/suggestions"
	}
)
public class SuggestionsHTTPHandler extends BaseSearchHTTPHandler {

	@interface Config {
		String[] datasourceFieldAggregations() default {"topic", "category"};
		boolean enableEntityAggregation() default false;
	}
	@Activate
	@Modified
	void activate(Config config) {
		_datasourceFieldAggregations = config.datasourceFieldAggregations();
		_enableEntityAggregation = config.enableEntityAggregation();
	}

	@Override
	public HttpServerRoutes handle(
		HttpServerRoutes router) {
		return router
			.post("/v1/suggestions", this);
	}

	@Override
	protected Mono<Tuple2<Tenant, List<Datasource>>> _getTenantAndDatasourceList(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {
		String hostName = HttpUtil.getHostName(httpRequest);

		return _datasourceClient
			.findTenantByVirtualHost(hostName)
			.next()
			.switchIfEmpty(
				Mono.error(
					() -> new RuntimeException(
						"tenant not found for virtualhost: " + hostName)))
			.zipWhen(tenant -> _datasourceClient
				.findDatasourceByTenantIdAndIsActive(tenant.getTenantId())
				.collectList());
	}

	@Override
	protected Mono<org.elasticsearch.action.search.SearchRequest> customizeSearchSourceBuilderMono(
		Tenant tenant, List<Datasource> datasources,
		SearchRequest searchRequest, List<PluginDriverDTO> documentTypeList,
		SearchSourceBuilder searchSourceBuilder,
		org.elasticsearch.action.search.SearchRequest elasticSearchQuery) {

		return Mono.defer(() -> {

			Long suggestionCategoryId =
				searchRequest.getSuggestionCategoryId();

			if (suggestionCategoryId == null) {
				return _datasourceClient
					.findSuggestionCategoryFields();
			}
			else {
				return _datasourceClient
					.findSuggestionCategoryFieldsByCategoryId(
						suggestionCategoryId);
			}
		})
			.map(fields -> {

				if (!(fields == null || fields.isEmpty())) {

					Function<String, CompositeValuesSourceBuilder<?>>
						fieldToTerms =
						nameField ->
							new TermsValuesSourceBuilder(nameField)
								.field(nameField)
								.missingBucket(true);

					CompositeAggregationBuilder compositeAggregation =
						fields.stream()
							.map(SuggestionCategoryField::getFieldName)
							.map(fieldToTerms)
							.collect(
								Collectors.collectingAndThen(
									Collectors.toList(),
									list -> AggregationBuilders
										.composite("composite", list)
								)
							);

					String afterKey = searchRequest.getAfterKey();

					if (afterKey != null) {
						byte[] afterKeyDecoded =
							Base64.getDecoder().decode(afterKey);

						Map<String, Object> map =
							_jsonFactory.fromJsonMap(
								new String(afterKeyDecoded), Object.class);

						compositeAggregation.aggregateAfter(map);
					}

					int[] range = searchRequest.getRange();

					if (range != null && range.length == 2) {
						int size = range[1];
						compositeAggregation.size(size);
					}

					searchSourceBuilder.aggregation(compositeAggregation);

				}

				searchSourceBuilder.from(0);
				searchSourceBuilder.size(0);

				searchSourceBuilder.highlighter(null);

				return elasticSearchQuery.source(searchSourceBuilder);

			});

	}

	@Override
	protected Mono<Object> searchHitToResponseMono(
		Tenant tenant, List<Datasource> datasourceList,
		PluginDriverDTOList pluginDriverDTOList,
		HttpServerRequest httpServerRequest, SearchRequest searchRequest,
		SearchResponse searchResponse) {

		if (_enableEntityAggregation) {
			return _search
				.search(factory -> {

					org.elasticsearch.action.search.SearchRequest
						searchRequestEntity =
						factory.createSearchRequestEntity(tenant.getTenantId());

					Aggregations aggregations = searchResponse.getAggregations();

					CompositeAggregation compositeAggregation =
						aggregations.get("composite");

					List<? extends CompositeAggregation.Bucket> buckets =
						compositeAggregation.getBuckets();

					BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

					buckets
						.stream()
						.map(bucket -> (String)bucket.getKey().get("entities.id"))
						.filter(Objects::nonNull)
						.distinct()
						.forEach(entityId -> boolQueryBuilder.should(
							QueryBuilders.matchQuery("id", entityId)
						));

					if (_log.isDebugEnabled()) {
						_log.debug("entities query: " + boolQueryBuilder);
					}

					SearchSourceBuilder ssb = new SearchSourceBuilder();

					ssb.query(boolQueryBuilder);
					ssb.size(1000);
					ssb.fetchSource(new String[]{"name", "id", "type"}, null);

					return searchRequestEntity.source(ssb);
				})
				.flatMap(entityResponse ->
					_datasourceClient
						.findSuggestionCategoryFields()
						.map(fields -> {

							Map<String, String[]> entityMap = new HashMap<>();

							for (SearchHit hit : entityResponse.getHits()) {
								Map<String, Object> sourceAsMap = hit.getSourceAsMap();
								String name =(String)sourceAsMap.get("name");
								String type =(String)sourceAsMap.get("type");
								String entityId =(String)sourceAsMap.get("id");
								entityMap.put(entityId, new String[]{type, name});
							}

							return _getSuggestionsResponse(
								datasourceList, pluginDriverDTOList,
								searchRequest, searchResponse, fields,
								entityMap);

						}));
		}
		else {
			return _datasourceClient
				.findSuggestionCategoryFields()
				.map(fields ->
					_getSuggestionsResponse(
						datasourceList, pluginDriverDTOList,
						searchRequest, searchResponse, fields,
						Map.of()));
		}

	}

	private SuggestionsResponse _getSuggestionsResponse(
		List<Datasource> datasourceList,
		PluginDriverDTOList pluginDriverDTOList, SearchRequest searchRequest,
		SearchResponse searchResponse, List<SuggestionCategoryField> fields,
		Map<String, String[]> entityMap) {

		Aggregations aggregations = searchResponse.getAggregations();

		if (aggregations == null) {
			return SuggestionsResponse.of(List.of(), null);
		}

		Map<String, Aggregation> aggregationMap = aggregations.asMap();

		if (!aggregationMap.containsKey("composite")) {
			return SuggestionsResponse.of(List.of(), null);
		}

		CompositeAggregation compositeAggregation =
			(CompositeAggregation)aggregationMap.get("composite");

		Map<String, Long> fieldNameCategoryIdMap =
			fields
				.stream()
				.collect(
					Collectors.toMap(
						SuggestionCategoryField::getFieldName,
						SuggestionCategoryField::getCategoryId,
						(a1, a2) -> a2));

		Long datasourceIdCategoryId =
			fieldNameCategoryIdMap.getOrDefault(
				"datasourceId", 1L);
		Long entityIdCategoryId =
			fieldNameCategoryIdMap.getOrDefault(
				"entities.id", 2L);
		Long entitiesContextCategoryId =
			fieldNameCategoryIdMap.getOrDefault(
				"entities.context", entityIdCategoryId);
		Long documentTypesCategoryId =
			fieldNameCategoryIdMap.getOrDefault(
				"documentTypes", 4L);

		List<? extends CompositeAggregation.Bucket> buckets =
			compositeAggregation.getBuckets();

		LinkedList<Suggestions> suggestions =
			new LinkedList<>();

		String suggestKeyword =
			searchRequest.getSuggestKeyword();

		BiConsumer<String, Suggestions> addSuggestions;

		if (suggestKeyword != null) {
			addSuggestions = (key, sugg) -> {

				if (!suggestions.contains(sugg)) {
					if (key.contains(suggestKeyword)) {
						suggestions.addFirst(sugg);
					}
					else {
						suggestions.addLast(sugg);
					}
				}
			};
		}
		else {
			addSuggestions = (key, sugg) -> {
				if (!suggestions.contains(sugg)) {
					suggestions.add(sugg);
				}
			};
		}


		for (CompositeAggregation.Bucket bucket : buckets) {

			Map<String, Object> keys = new HashMap<>(bucket.getKey());

			for (Map.Entry<String, Object> entry : keys.entrySet()) {

				String key = entry.getKey();
				String value = (String)entry.getValue();

				if (value == null) {
					continue;
				}

				switch (key) {
					case "datasourceId":

						long datasourceIdL = Long.parseLong(value);

						_datasource(
							datasourceList,
							pluginDriverDTOList,
							datasourceIdCategoryId,
							addSuggestions, datasourceIdL);

						break;
					case "entities.context":
						break;
					case "entities.id":
						String[] typeName = entityMap.get(value);

						if (typeName != null) {
							String type = typeName[0];
							String name = typeName[1];

							String entitiesContext =
								(String)keys.get("entities.context");

							if (entitiesContext != null) {
								addSuggestions.accept(
									name,
									Suggestions.entity(
										value,
										entitiesContextCategoryId,
										type, name, entitiesContext)
								);
							}
							else {
								addSuggestions.accept(
									name,
									Suggestions.entity(
										value, entityIdCategoryId,
										type, name)
								);
							}
						}
						break;
					case "documentTypes":
						addSuggestions.accept(
							value,
							Suggestions.docType(
								value,
								documentTypesCategoryId)
						);
						break;
					default:
						Long textCategoryId =
							fieldNameCategoryIdMap.getOrDefault(
								key, 5L);

						addSuggestions.accept(
							value, Suggestions.text(
								value, textCategoryId, key)
						);

				}
			}
		}

		Map<String, Object> map = compositeAggregation.afterKey();
		String afterKey = null;

		if (map != null) {
			afterKey = _jsonFactory.toJson(map);
			afterKey = Base64.getEncoder().encodeToString(
				afterKey.getBytes(StandardCharsets.UTF_8));
		}

		return SuggestionsResponse.of(suggestions, afterKey);
	}

	@Override
	protected QueryParser.Context createQueryParserContext(
		Tenant tenant, List<Datasource> datasources,
		HttpServerRequest httpRequest,
		Map<String, List<SearchToken>> tokenTypeGroup,
		List<PluginDriverDTO> documentTypeList) {
		return QueryParser.Context.of(
			tenant,
			datasources,
			documentTypeList,
			tokenTypeGroup,
			httpRequest,
			QueryParser.QueryCondition.MUST
		);
	}

	private void _datasource(
		List<Datasource> datasourceList,
		PluginDriverDTOList pluginDriverDTOList, Long datasourceIdCategoryId,
		BiConsumer<String, Suggestions> addSuggestions, long datasourceIdL) {

		for1:for (Datasource datasource : datasourceList) {

			if (datasource
				.getDatasourceId()
				.equals(datasourceIdL)) {

				for (PluginDriverDTO pluginDriverDTO :
					pluginDriverDTOList
						.getPluginDriverDTOList()) {

					if (
						pluginDriverDTO
							.getDriverServiceName()
							.equals(
								datasource
									.getDriverServiceName())
					) {
						addSuggestions.accept(
							pluginDriverDTO.getName(),
							Suggestions.datasource(
								pluginDriverDTO.getName(),
								datasourceIdCategoryId));

						break for1;

					}

				}

			}
		}
	}

	@Reference(
		service = QueryParser.class,
		bind = "addQueryParser",
		unbind = "removeQueryParser",
		cardinality = ReferenceCardinality.MULTIPLE,
		policyOption = ReferencePolicyOption.GREEDY,
		policy = ReferencePolicy.DYNAMIC
	)
	@Override
	protected void addQueryParser(QueryParser queryParser) {
		super.addQueryParser(queryParser);
	}

	@Override
	protected void removeQueryParser(QueryParser queryParser) {
		super.removeQueryParser(queryParser);
	}

	@Reference
	@Override
	protected void setDatasourceClient(
		DatasourceClient datasourceClient) {
		super.setDatasourceClient(datasourceClient);
	}

	@Reference
	@Override
	protected void setSearch(Search search) {
		super.setSearch(search);
	}

	@Reference
	@Override
	protected void setSearchTokenizer(
		SearchTokenizer searchTokenizer) {
		super.setSearchTokenizer(searchTokenizer);
	}

	@Reference
	@Override
	protected void setPluginDriverManagerClient(
		PluginDriverManagerClient pluginDriverManagerClient) {
		super.setPluginDriverManagerClient(pluginDriverManagerClient);
	}

	@Reference
	@Override
	protected void setJsonFactory(JsonFactory jsonFactory) {
		super.setJsonFactory(jsonFactory);
	}

	@Override
	@Reference
	protected void setHttpResponseWriter(
		HttpResponseWriter httpResponseWriter) {
		super.setHttpResponseWriter(httpResponseWriter);
	}

	private String[] _datasourceFieldAggregations;

	private boolean _enableEntityAggregation;

	private static final Logger _log = LoggerFactory.getLogger(
		SuggestionsHTTPHandler.class);

}
