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

package io.openk9.search.query.internal.http.v2;

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
import org.osgi.service.component.annotations.ReferencePolicyOption;
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

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class DocumentTypesEndpoint
	implements RouterHandler, HttpHandler {

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router
			.get("/v2/document-types", this)
			.get("/v2/document-types/{keywordType}", this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		String hostName = HttpUtil.getHostName(httpRequest);

		String keywordType = httpRequest.param("keywordType");

		KeywordType keywordTypeEnum =
			KeywordType.typeOf(keywordType);

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
				.map(t2 -> _mapToResponse(t2, keywordTypeEnum));

		return _httpResponseWriter.write(httpResponse, response);

	}

	private Object _mapToResponse(
		List<Tuple2<Datasource, PluginDriverDTO>> list,
		KeywordType keywordType) {

		Map<String, Map<String, Collection<String>>> response =
			new HashMap<>(list.size());

		for (Tuple2<Datasource, PluginDriverDTO> t2 : list) {
			PluginDriverDTO pluginDriver = t2.getT2();

			for (DocumentTypeDTO documentType : pluginDriver.getDocumentTypes()) {

				String name = documentType.getName();

				List<SearchKeywordDTO> searchKeywords =
					documentType.getSearchKeywords();

				for (SearchKeywordDTO searchKeyword : searchKeywords) {

					if (keywordType.typeEquals(searchKeyword.getType())) {

						Map<String, Collection<String>> typeKeywords =
							response.computeIfAbsent(name, k -> new HashMap<>());

						Collection<String> keywords =
							typeKeywords.computeIfAbsent(
								searchKeyword.getType().name(),
								k -> new HashSet<>());

						keywords.add(searchKeyword.getKeyword());

					}
				}

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

	enum KeywordType {
		NUMBER(SearchKeywordDTO.Type.NUMBER),
		TEXT(SearchKeywordDTO.Type.TEXT),
		AUTOCOMPLETE(SearchKeywordDTO.Type.AUTOCOMPLETE),
		DATE(SearchKeywordDTO.Type.DATE),
		ANY(null);

		KeywordType(SearchKeywordDTO.Type type) {
			_type = type;
		}

		public boolean typeEquals(SearchKeywordDTO.Type type) {
			return _type == null || _type == type;
		}

		public static KeywordType typeOf(SearchKeywordDTO.Type keywordType) {

			if (keywordType == null) {
				return ANY;
			}

			for (KeywordType value : values()) {
				if (value.getType().equals(keywordType)) {
					return value;
				}
			}

			return ANY;

		}

		public static KeywordType typeOf(String keywordType) {

			if (keywordType == null) {
				return ANY;
			}

			for (KeywordType value : values()) {
				if (value.name().equalsIgnoreCase(keywordType)) {
					return value;
				}
			}

			return ANY;

		}

		public SearchKeywordDTO.Type getType() {
			return _type;
		}

		private final SearchKeywordDTO.Type _type;

	}

}
