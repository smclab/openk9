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
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.RouterHandler;
import io.openk9.model.Tenant;
import io.openk9.search.client.api.Search;
import io.openk9.search.client.api.SearchRequestFactory;
import io.openk9.search.query.internal.response.Response;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class EntityNameHTTPHandler
	implements RouterHandler, HttpHandler {

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router.get("/v1/entity/name", this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		String hostName = HttpUtil.getHostName(httpRequest);

		Mono<Response> response = _datasourceClient
			.findTenantByVirtualHost(hostName)
			.next()
			.switchIfEmpty(
				Mono.error(
					() -> new RuntimeException(
						"tenant not found for virtualhost: " + hostName)))
			.map(Tenant::getTenantId)
			.map(this::_toSearchRequest)
			.flatMap(_search::search)
			.map(SearchResponse::getHits)
			.map(this::_searchHitToResponse);

		return _httpResponseWriter.write(httpResponse, response);

	}

	private Response _searchHitToResponse(SearchHits searchHits) {

		SearchHit[] hits = searchHits.getHits();

		List<Map<String, Object>> result = new ArrayList<>(hits.length);

		for (SearchHit hit : hits) {

			Map<String, Object> sourceAsMap = hit.getSourceAsMap();

			result.add(
				Map.of("name", sourceAsMap.get("name"))
			);

		}

		TotalHits totalHits = searchHits.getTotalHits();

		return new Response(
			result,
			totalHits.value,
			totalHits.relation == TotalHits.Relation.EQUAL_TO
		);
	}

	private SearchRequest _toSearchRequest(Long tenantId) {

		SearchRequest searchRequest =
			_searchRequestFactory.createSearchRequestEntity(tenantId);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.fetchSource("name", null);

		searchSourceBuilder.query(QueryBuilders.matchAllQuery());

		searchSourceBuilder.size(10_000);

		return searchRequest.source(searchSourceBuilder);
	}

	@Reference
	private DatasourceClient _datasourceClient;

	@Reference
	private SearchRequestFactory _searchRequestFactory;

	@Reference
	private Search _search;

	@Reference
	private HttpResponseWriter _httpResponseWriter;

}
