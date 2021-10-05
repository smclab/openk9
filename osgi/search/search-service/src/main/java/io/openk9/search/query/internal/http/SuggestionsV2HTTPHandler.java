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
import io.openk9.search.client.api.RestHighLevelClientProvider;
import io.openk9.search.client.api.Search;
import io.openk9.search.client.api.SearchRequestFactory;
import io.openk9.search.query.internal.response.Response;
import io.openk9.search.query.internal.response.SuggestionsDTO;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
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
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class SuggestionsV2HTTPHandler extends BaseSearchHTTPHandler {

	@interface Config {
		String[] rootFieldAggregations() default {"type"};
		String[] datasourceFieldAggregations() default {"topic", "documentType"};
		int aggregationSize() default 20;
	}
	@Activate
	@Modified
	void activate(Config config) {
		_rootFieldAggregations = config.rootFieldAggregations();
		_datasourceFieldAggregations = config.datasourceFieldAggregations();
		_aggregationSize = config.aggregationSize();
	}

	@Override
	public HttpServerRoutes handle(
		HttpServerRoutes router) {
		return router
			.post("/v2/suggestions", this);
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
		List<PluginDriverDTO> documentTypeList, SearchSourceBuilder searchSourceBuilder) {

		super.customizeSearchSourceBuilder(
			tenant, datasources, searchRequest, documentTypeList,
			searchSourceBuilder);

		/* <BLE> */

		org.elasticsearch.action.search.SearchRequest searchRequestEntity =
			_searchRequestFactory.createSearchRequestEntity(
				tenant.getTenantId());

		SearchSourceBuilder searchSourceBuilderEntity =
			new SearchSourceBuilder();

		searchSourceBuilderEntity
			.aggregation(
				AggregationBuilders
					.terms("type.keyword")
					.field("type.keyword")
					.size(_aggregationSize)
		);

		searchSourceBuilderEntity.size(0);

		searchRequestEntity.source(searchSourceBuilderEntity);

		Stream<TermsAggregationBuilder> termsAggregationBuilderEntities;

		try {
			SearchResponse search =
				_restHighLevelClientProvider
					.get()
					.search(searchRequestEntity, RequestOptions.DEFAULT);

			Stream.Builder<TermsAggregationBuilder> builder = Stream.builder();

			for (Aggregation aggregation : search.getAggregations()) {
				Terms terms = (Terms)aggregation;

				for (Terms.Bucket bucket : terms.getBuckets()) {
					builder.add(
						AggregationBuilders
							.terms("entities." + bucket.getKeyAsString() + ".id")
							.field("entities." + bucket.getKeyAsString() + ".id")
							.size(_aggregationSize)
					);
				}

			}

			termsAggregationBuilderEntities = builder.build();

		}
		catch (IOException e) {
			throw Exceptions.bubble(e);
		}

		/* </BLE> */

		List<String> documentTypeNameList =
			documentTypeList
				.stream()
				.map(PluginDriverDTO::getDocumentTypes)
				.flatMap(Collection::stream)
				.map(DocumentTypeDTO::getName)
				.distinct()
				.collect(Collectors.toList());

		Stream<String> datasourceFieldStream =
			documentTypeNameList
				.stream()
				.flatMap(name ->
					Arrays
						.stream(_datasourceFieldAggregations)
						.map(suffix -> name + "." + suffix)
				);

		Stream<String> concat =
			Stream
				.of(
					Arrays.stream(_rootFieldAggregations),
					datasourceFieldStream
				)
				.flatMap(Function.identity());

		Stream.of(
			concat
				.map(nameField ->
					AggregationBuilders
						.terms(nameField)
						.field(nameField)
						.size(_aggregationSize)
				),
				Stream.of(
					AggregationBuilders
						.terms("datasourceId")
						.field("datasourceId")
						.size(_aggregationSize)
				),
				termsAggregationBuilderEntities
			)
			.flatMap(Function.identity())
			.forEach(searchSourceBuilder::aggregation);

		searchSourceBuilder.from(0);
		searchSourceBuilder.size(0);

	}

	@Override
	protected Mono<Response> searchHitToResponseMono(
		Tenant tenant, List<Datasource> datasourceList,
		PluginDriverDTOList pluginDriverDTOList,
		HttpServerRequest httpServerRequest, SearchRequest searchRequest,
		SearchResponse searchResponse) {

		return Mono.defer(() -> {

			Aggregations aggregations = searchResponse.getAggregations();

			Map<String, List<String>> aggregationsResult =
				aggregations
					.asList()
					.stream()
					.map(aggregation -> (Terms)aggregation)
					.filter(terms -> !terms.getBuckets().isEmpty())
					.map(terms -> {

						String aggregationName = terms.getName();

						if (_log.isDebugEnabled()) {
							_log.debug("aggregationName: " + aggregationName);
						}

						if (aggregationName.startsWith("entities.")) {
							if (_log.isDebugEnabled()) {
								_log.debug("startWith: " + aggregationName);
							}
							aggregationName = "entities";
						}

						List<? extends Terms.Bucket> buckets =
							terms.getBuckets();

						return Tuples.of(
							aggregationName, buckets.stream()
								.map(Terms.Bucket::getKeyAsString)
								.collect(Collectors.toList())
						);


					})
					.collect(
						Collectors.toMap(
							Tuple2::getT1, Tuple2::getT2, (l1, l2) -> {
								List<String> merge = new ArrayList<>();
								merge.addAll(l1);
								merge.addAll(l2);
								return merge;
							})
					);

			Mono<List<Map<String, Object>>> datasourcesMono =
				_getDatasourcesMono(
					datasourceList, pluginDriverDTOList, aggregationsResult);

			Mono<List<Map<String, Object>>> entitiesMono =
				_getEntitiesMono(tenant, searchRequest, aggregationsResult);

			Mono<Map<String, Map<String, Object>>> typesMono =
				_getTypesMono(aggregationsResult);

			return Mono.zip(arr -> SuggestionsDTO.of(
				(List<Map<String, Object>>)arr[0],
				(List<Map<String, Object>>)arr[1],
				(Map<String, Map<String, Object>>)arr[2]
			), entitiesMono, datasourcesMono, typesMono)
				.map(suggestionsDTO -> new Response(suggestionsDTO, 0));

		});

	}

	private Mono<Map<String, Map<String, Object>>> _getTypesMono(
		Map<String, List<String>> aggregationsResult) {
		return Mono.fromSupplier(() -> {

			Map<String, Map<String, Object>> types =
				aggregationsResult
					.entrySet()
					.stream()
					.filter(entry ->
						!entry.getKey().equals("datasourceId") &&
						!entry.getKey().equals("entities"))
					.map(entry -> {

						String key = entry.getKey();

						int ind = key.indexOf(".");

						String type;
						String field;

						if (ind == -1) {
							type = "ROOT";
							field = key;
						}
						else {
							type = key.substring(0, ind);
							field = key.substring(ind + 1);
						}

						return Map.entry(
							type, Map.entry(field, entry.getValue()));

					})
					.collect(
						Collectors.groupingBy(
							Map.Entry::getKey,
							Collectors.mapping(
								Map.Entry::getValue,
								Collectors.toMap(
									Map.Entry::getKey,
									Map.Entry::getValue
								)
							)
						)
					);

			return types;
		});
	}

	private Mono<List<Map<String, Object>>> _getEntitiesMono(
		Tenant tenant, SearchRequest searchRequest,
		Map<String, List<String>> aggregationsResult) {
		return Mono.defer(() -> {
			List<String> entities = aggregationsResult.get("entities");

			if (_log.isDebugEnabled()) {
				if (entities != null) {
					_log.debug(entities.toString());
				}
				else {
					_log.debug("entities is null");
				}
			}

			if (entities != null && !entities.isEmpty()) {

				BoolQueryBuilder boolQueryBuilder =
					QueryBuilders.boolQuery();

				for (String entity : entities) {
					boolQueryBuilder.should(
						QueryBuilders.matchQuery("id", entity)
					);
				}

				if (_log.isDebugEnabled()) {
					_log.debug(boolQueryBuilder.toString());
				}

				org.elasticsearch.action.search.SearchRequest
					searchRequestEntity =
					_searchRequestFactory.createSearchRequestEntity(
						tenant.getTenantId());

				SearchSourceBuilder source = new SearchSourceBuilder();

				source.query(boolQueryBuilder);

				int[] range = searchRequest.getRange();

				if (range != null && range.length == 2) {
					source.size(range[1]);
				}
				else {
					source.size(16);
				}

				searchRequestEntity.source(source);

				return _search
					.search(searchRequestEntity)
					.map(searchResponseEntity -> {

						List<Map<String, Object>> resultEntity =
							new ArrayList<>();

						for (SearchHit hit : searchResponseEntity.getHits()) {
							resultEntity.add(hit.getSourceAsMap());
						}

						return resultEntity;

					});

			}
			else {
				return Mono.just(List.of());
			}
		});
	}

	private Mono<List<Map<String, Object>>> _getDatasourcesMono(
		List<Datasource> datasourceList,
		PluginDriverDTOList pluginDriverDTOList,
		Map<String, List<String>> aggregationsResult) {

		return Mono.fromSupplier(() -> {

			List<Map<String, Object>> datasources = new ArrayList<>();

			List<String> datasourceIdList =
				aggregationsResult.get("datasourceId");

			List<PluginDriverDTO> listPluginDriverDTO =
				pluginDriverDTOList.getPluginDriverDTOList();

			for (String e : datasourceIdList) {
				long datasourceId = NumberUtils.toLong(e);

				Map<String, Object> datasourceMap = new HashMap<>();

				for (Datasource datasource : datasourceList) {
					if (datasourceId == datasource.getDatasourceId()) {
						boolean active = datasource.getActive();
						String name = datasource.getName();
						datasourceMap.put("name", name);
						datasourceMap.put("active", active);
						List<Map<String, Object>> documentTypes = new ArrayList<>();
						for (PluginDriverDTO pluginDriverDTO : listPluginDriverDTO) {
							if (pluginDriverDTO.getDriverServiceName().equals(
								datasource.getDriverServiceName())) {

								for (DocumentTypeDTO documentType
									: pluginDriverDTO.getDocumentTypes()) {

									Map<String, Object> map = new HashMap<>();

									map.put("name", documentType.getName());
									map.put("icon", documentType.getIcon());

									documentTypes.add(map);

								}

							}
							datasourceMap.put("documentTypes", documentTypes);
						}
					}
				}

				datasources.add(datasourceMap);

			}

			return datasources;
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

	@Reference
	private SearchRequestFactory _searchRequestFactory;

	@Reference
	private Search _search;

	@Reference
	private RestHighLevelClientProvider _restHighLevelClientProvider;

	private String[] _rootFieldAggregations;
	private String[] _datasourceFieldAggregations;
	private int _aggregationSize;

	private static final Logger _log = LoggerFactory.getLogger(
		SuggestionsV2HTTPHandler.class);

}
