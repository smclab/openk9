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
import io.openk9.model.Datasource;
import io.openk9.model.Tenant;
import io.openk9.plugin.driver.manager.client.api.PluginDriverManagerClient;
import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.SearchKeywordDTO;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class DocumentTypesEndpoint
	implements RouterHandler, HttpHandler {

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router.get("/v1/document-types", this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		String hostName = HttpUtil.getHostName(httpRequest);

		Mono<Object> response =
			_datasourceClient
				.findTenantByVirtualHost(hostName)
				.next()
				.switchIfEmpty(
					Mono.error(
						() -> new RuntimeException(
							"tenant not found for virtualhost: " + hostName)))
				.map(Tenant::getTenantId)
				.flatMapMany(_datasourceClient::findDatasourceByTenantId)
				.flatMap(datasource -> _pluginDriverManagerClient
					.getPluginDriver(datasource.getDriverServiceName())
					.onErrorResume(throwable -> {
						if (_log.isWarnEnabled()) {
							_log.warn(throwable.getMessage());
						}
						return Mono.empty();
					})
					.map(pluginDriverDTO -> Tuples.of(datasource, pluginDriverDTO))
				)
				.collectList()
				.map(this::_mapToResponse);

		return _httpResponseWriter.write(httpResponse, response);

	}

	private Object _mapToResponse(
		List<Tuple2<Datasource, PluginDriverDTO>> list) {

		Map<String, Collection<String>> response = new HashMap<>();

		for (Tuple2<Datasource, PluginDriverDTO> t2 : list) {
			PluginDriverDTO pluginDriver = t2.getT2();

			for (DocumentTypeDTO documentType : pluginDriver.getDocumentTypes()) {

				String name = documentType.getName();

				List<SearchKeywordDTO> searchKeywords =
					documentType.getSearchKeywords();

				List<String> keywords =
					searchKeywords
						.stream()
						.map(SearchKeywordDTO::getKeyword)
						.collect(Collectors.toList());

				Collection<String> prevKeywords = response.get(name);

				Set<String> combine = new HashSet<>(keywords);

				if (prevKeywords != null) {
					combine.addAll(prevKeywords);
				}

				response.put(name, combine);

			}

		}

		return response;

 	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private HttpResponseWriter _httpResponseWriter;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private DatasourceClient _datasourceClient;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private PluginDriverManagerClient _pluginDriverManagerClient;

	private static final Logger _log = LoggerFactory.getLogger(
		DocumentTypesEndpoint.class);

}
