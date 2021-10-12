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

import io.openk9.common.api.constant.Strings;
import io.openk9.datasource.client.api.DatasourceClient;
import io.openk9.http.util.HttpUtil;
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.Datasource;
import io.openk9.model.Tenant;
import io.openk9.plugin.driver.manager.client.api.PluginDriverManagerClient;
import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTOList;
import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchRequest;
import io.openk9.search.api.query.SearchTokenizer;
import io.openk9.search.client.api.Search;
import io.openk9.search.query.internal.response.Response;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
		int aggregationSize() default 20;
	}
	@Activate
	@Modified
	void activate(Config config) {
		_datasourceFieldAggregations = config.datasourceFieldAggregations();
		_aggregationSize = config.aggregationSize();
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
	protected void customizeSearchSourceBuilder(
		Tenant tenant, List<Datasource> datasources, SearchRequest searchRequest,
		List<PluginDriverDTO> documentTypeList,
		SearchSourceBuilder searchSourceBuilder,
		org.elasticsearch.action.search.SearchRequest elasticSearchQuery) {

		documentTypeList
			.stream()
			.map(PluginDriverDTO::getDocumentTypes)
			.flatMap(Collection::stream)
			.map(DocumentTypeDTO::getName)
			.distinct()
			.flatMap(name ->
				Arrays
					.stream(_datasourceFieldAggregations)
					.map(suffix -> name + "." + suffix)
			)
			.map(nameField ->
				AggregationBuilders
					.terms(nameField)
					.field(nameField)
					.size(_aggregationSize)
			)
			.forEach(searchSourceBuilder::aggregation);

		searchSourceBuilder.aggregation(
			AggregationBuilders
				.nested("entitiesNested", "entities")
				.subAggregation(
					AggregationBuilders
						.terms("entities.id")
						.field("entities.id")
						.subAggregation(
							AggregationBuilders
								.terms("entities.context")
								.field("entities.context")
						)
						.size(_aggregationSize)
				)
		);

		searchSourceBuilder.aggregation(
			AggregationBuilders
				.terms("datasourceId")
				.field("datasourceId")
				.size(_aggregationSize)
		);

		searchSourceBuilder.aggregation(
			AggregationBuilders
				.terms("documentTypes")
				.field("documentTypes")
				.size(_aggregationSize)
		);

		searchSourceBuilder.from(0);
		searchSourceBuilder.size(0);

	}

	@Override
	protected Mono<Response> searchHitToResponseMono(
		Tenant tenant, List<Datasource> datasourceList,
		PluginDriverDTOList pluginDriverDTOList,
		HttpServerRequest httpServerRequest, SearchRequest searchRequest,
		SearchResponse searchResponse) {

		return _search.search(factory -> {

			org.elasticsearch.action.search.SearchRequest
				searchRequestEntity =
				factory.createSearchRequestEntity(tenant.getTenantId());

			Aggregations aggregations = searchResponse.getAggregations();

			CompositeAggregation entitiesAggr = aggregations.get("entitiesNested");

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

			for (CompositeAggregation.Bucket bucket : entitiesAggr.getBuckets()) {

				boolQueryBuilder.should(
					QueryBuilders.matchQuery(
						"id", bucket.getKey().get("entities.id"))
				);

			}

			SearchSourceBuilder ssb = new SearchSourceBuilder();

			ssb.query(boolQueryBuilder);
			ssb.size(1000);
			ssb.fetchSource(new String[]{"name", "id", "type"}, null);

			return searchRequestEntity.source(ssb);
		})
			.map(entityResponse -> {

				Map<String, String[]> entityMap = new HashMap<>();

				for (SearchHit hit : entityResponse.getHits()) {
					Map<String, Object> sourceAsMap = hit.getSourceAsMap();
					String name =(String)sourceAsMap.get("name");
					String type =(String)sourceAsMap.get("type");
					String entityId =
						Integer.toString((Integer)sourceAsMap.get("id"));
					entityMap.put(entityId, new String[]{name, type});
				}

				Aggregations aggregations = searchResponse.getAggregations();

				List<Map<String, Object>> list = new ArrayList<>();

				for (Map.Entry<String, Aggregation> aggr :
					aggregations.getAsMap().entrySet()) {

					Aggregation aggregation = aggr.getValue();

					String key = aggr.getKey();

					if (key.equals("entitiesNested")) {
						Nested nestedAggregator = (Nested)aggregation;
						for (Aggregation nestedEntitiesAggr :
							nestedAggregator.getAggregations()) {

							Terms terms = (Terms)nestedEntitiesAggr;

							for (Terms.Bucket bucket : terms.getBuckets()) {

								String entityId = bucket.getKeyAsString();

								Aggregations subAggr = bucket.getAggregations();

								Iterator<Aggregation> iterator =
									subAggr.iterator();

								String[] strings = entityMap.getOrDefault(
									entityId, _EMPTY_ARRAY);

								if (!iterator.hasNext()) {
									list.add(
										Map.of(
											"tokenType", "ENTITY",
											"keywordKey", "",
											"entityName", strings[0],
											"value", entityId,
											"entityType", strings[1],
											"count", bucket.getDocCount()
										)
									);
								}
								else {
									while (iterator.hasNext()) {

										Terms entityContextTerms =
											(Terms)iterator.next();

										for (Terms.Bucket b : entityContextTerms.getBuckets()) {
											list.add(
												Map.of(
													"tokenType", "ENTITY",
													"keywordKey", b.getKeyAsString(),
													"entityName", strings[0],
													"value", entityId,
													"entityType", strings[1],
													"count", bucket.getDocCount()
												)
											);
										}

									}
								}

							}

						}

					}
					else if (key.equals("datasourceId")) {

						Terms terms =(Terms)aggr.getValue();

						for (Terms.Bucket bucket : terms.getBuckets()) {

							long datasourceId = NumberUtils.toLong(
								bucket.getKeyAsString());

							String datasourceName = Strings.BLANK;

							for (Datasource datasource : datasourceList) {
								if (datasource.getDatasourceId() == datasourceId) {
									datasourceName = datasource.getName();
								}
							}

							list.add(
								Map.of(
									"tokenType", "DATASOURCE",
									"value", datasourceName,
									"datasourceId", datasourceId,
									"count", bucket.getDocCount()
								)
							);
						}

					}
					else if (key.equals("documentTypes")) {

						Terms terms =(Terms)aggr.getValue();

						for (Terms.Bucket bucket : terms.getBuckets()) {
							list.add(
								Map.of(
									"tokenType", "DOCTYPE",
									"value", bucket.getKey(),
									"count", bucket.getDocCount()
								)
							);
						}

					}
					else {

						Terms terms =(Terms)aggr.getValue();

						for (Terms.Bucket bucket : terms.getBuckets()) {
							list.add(
								Map.of(
									"tokenType", "TEXT",
									"keywordKey", terms.getName(),
									"value", bucket.getKey(),
									"count", bucket.getDocCount()
								)
							);
						}

					}

				}

				return new Response(list, list.size());

			});
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

	private String[] _datasourceFieldAggregations;
	private int _aggregationSize;

	private static final String[] _EMPTY_ARRAY = new String[]{"", ""};

}
