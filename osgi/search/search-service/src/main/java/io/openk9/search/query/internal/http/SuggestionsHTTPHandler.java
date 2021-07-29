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
import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchRequest;
import io.openk9.search.api.query.SearchTokenizer;
import io.openk9.search.client.api.Search;
import io.openk9.search.query.internal.response.Response;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
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
import java.util.List;
import java.util.Map;
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
			.get("/v1/suggestions", this)
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
		List<PluginDriverDTO> documentTypeList, SearchSourceBuilder searchSourceBuilder) {

		super.customizeSearchSourceBuilder(
			tenant, datasources, searchRequest, documentTypeList,
			searchSourceBuilder);

		Stream<String> datasourceFieldStream =
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

		Stream<String> concat = Stream
			.concat(
				Arrays.stream(_rootFieldAggregations),
				datasourceFieldStream
			);

		concat
			.map(nameField ->
				AggregationBuilders
					.terms(nameField)
					.field(nameField)
					.size(_aggregationSize)
			)
			.forEach(searchSourceBuilder::aggregation);

		searchSourceBuilder.from(0);
		searchSourceBuilder.size(0);

	}

	@Override
	protected Response searchHitToResponse(SearchResponse searchResponse) {

		Aggregations aggregations = searchResponse.getAggregations();

		List<Map<String, Object>> list = new ArrayList<>();

		for (Map.Entry<String, Aggregation> aggr :
			aggregations.getAsMap().entrySet()) {

			Aggregation aggregation = aggr.getValue();

			if (aggregation instanceof Terms) {
				Terms terms = (Terms)aggregation;
				for (Terms.Bucket bucket : terms.getBuckets()) {
					list.add(
						Map.of(
							"tokenType", "TEXT",
							"keywordKey", aggr.getKey(),
							"value", bucket.getKey(),
							"count", bucket.getDocCount()
						)
					);
				}
			}
		}

		return new Response(list, list.size(), true);

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

	private String[] _rootFieldAggregations;
	private String[] _datasourceFieldAggregations;
	private int _aggregationSize;

}
