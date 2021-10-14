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
import io.openk9.search.client.api.Search;
import io.openk9.search.query.internal.response.SuggestionsResponse;
import io.openk9.search.query.internal.response.suggestions.Suggestions;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregation;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

		Function<String, CompositeValuesSourceBuilder<?>> fieldToTerms =
			nameField ->
				new TermsValuesSourceBuilder(nameField)
					.field(nameField)
					.missingBucket(true);

		Stream<String> datasourceFields =
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
				);

		Stream<String> rest =
			Stream.of(
				"entities.id", "entities.context", "datasourceId",
				"documentTypes"
			);

		CompositeAggregationBuilder compositeAggregation =
			Stream
				.concat(datasourceFields, rest)
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
			byte[] afterKeyDecoded = Base64.getDecoder().decode(afterKey);

			Map<String, Object> map =
				_jsonFactory.fromJsonMap(
					new String(afterKeyDecoded), Object.class);

			compositeAggregation.aggregateAfter(map);
		}

		searchSourceBuilder.aggregation(compositeAggregation);

		searchSourceBuilder.from(0);
		searchSourceBuilder.size(0);

	}

	@Override
	protected Mono<Object> searchHitToResponseMono(
		Tenant tenant, List<Datasource> datasourceList,
		PluginDriverDTOList pluginDriverDTOList,
		HttpServerRequest httpServerRequest, SearchRequest searchRequest,
		SearchResponse searchResponse) {

		return _search.search(factory -> {

			org.elasticsearch.action.search.SearchRequest
				searchRequestEntity =
				factory.createSearchRequestEntity(tenant.getTenantId());

			Aggregations aggregations = searchResponse.getAggregations();

			ParsedNested entitiesAggr = aggregations.get("entitiesNested");

			Terms aggregation =
				entitiesAggr.getAggregations().get("entities.id");

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

			for (Terms.Bucket bucket : aggregation.getBuckets()) {

				boolQueryBuilder.should(
					QueryBuilders.matchQuery(
						"id", bucket.getKey())
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
					entityMap.put(entityId, new String[]{type, name});
				}

				Aggregations aggregations = searchResponse.getAggregations();

				CompositeAggregation compositeAggregation =
					aggregations.get("composite");

				List<? extends CompositeAggregation.Bucket> buckets =
					compositeAggregation.getBuckets();

				Set<Suggestions> suggestions =
					new LinkedHashSet<>(buckets.size());

				for (CompositeAggregation.Bucket bucket : buckets) {
					Map<String, Object> keys = new HashMap<>(bucket.getKey());

					String datasourceId =(String)keys.remove("datasourceId");

					if (datasourceId != null) {
						suggestions.add(Suggestions.datasource(datasourceId));
					}

					String entitiesId =(String)keys.remove("entities.id");
					String entitiesContext =
						(String)keys.remove("entities.context");

					if (entitiesId != null) {

						String[] typeName = entityMap.get(entitiesId);

						if (entitiesContext != null) {
							suggestions.add(
								Suggestions.entity(
									entitiesId, typeName[0], typeName[1],
									entitiesContext)
							);
						}
						else {
							suggestions.add(
								Suggestions.entity(
									entitiesId, typeName[0], typeName[1])
							);
						}
					}

					String documentTypes =(String)keys.remove("documentTypes");

					if (documentTypes != null) {
						suggestions.add(
							Suggestions.docType(documentTypes)
						);
					}

					for (Map.Entry<String, Object> restEntry : keys.entrySet()) {
						String keywordKey = restEntry.getKey();
						String value =(String)restEntry.getValue();

						if (value != null) {
							suggestions.add(
								Suggestions.text(value, keywordKey)
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
