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
import io.openk9.model.SuggestionCategory;
import io.openk9.model.Tenant;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.List;
import java.util.function.Function;

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class SuggestionCategoriesHttpHandler
	implements RouterHandler, HttpHandler {

	@Override
	public HttpServerRoutes handle(
		HttpServerRoutes router) {
		return router
			.get("/suggestion-categories", this)
			.get("/suggestion-categories/{categoryId}", this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpServerRequest,
		HttpServerResponse httpServerResponse) {

		String hostName = HttpUtil.getHostName(httpServerRequest);

		String categoryId = httpServerRequest.param("categoryId");

		Function<Long, Mono<List<SuggestionCategory>>> response;

		if (categoryId != null) {
			response = (tenantId) -> _datasourceClient
				.findSuggestionCategoryByTenantIdAndCategoryId(tenantId, Long.parseLong(categoryId));
		}
		else {
			response = (tenantId) -> _datasourceClient.findSuggestionCategories(tenantId);
		}

		return _httpResponseWriter.write(
			httpServerResponse,
			_datasourceClient
				.findTenantByVirtualHost(hostName)
				.switchIfEmpty(
					Mono.error(
						() -> new RuntimeException(
							"tenant not found for virtualhost: " + hostName)))
				.map(Tenant::getTenantId)
				.flatMap(response)
				.flatMapIterable(Function.identity())
		);

	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private DatasourceClient _datasourceClient;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private HttpResponseWriter _httpResponseWriter;

}
