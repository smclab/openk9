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
import io.openk9.http.web.Endpoint;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.model.Datasource;
import io.openk9.model.Tenant;
import io.openk9.plugin.driver.manager.client.api.PluginDriverManagerClient;
import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;

@Component(
	immediate = true,
	service = Endpoint.class
)
public class SupportedDatasourcesEndpoint implements HttpHandler {

	@Override
	public String getPath() {
		return "/v1/supported-datasources";
	}

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		String hostName = HttpUtil.getHostName(httpRequest);

		Flux<SupportedDatasourcesResponse> response =
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
				.map(this::_mapToResponse);

		return _httpResponseWriter.write(httpResponse, response);

	}

	private SupportedDatasourcesResponse _mapToResponse(
		Tuple2<Datasource, PluginDriverDTO> t2) {

		Datasource t1 = t2.getT1();

		PluginDriverDTO pluginDriver = t2.getT2();

		String pluginDriverName = pluginDriver.getName();

		return SupportedDatasourcesResponse.of(
			pluginDriverName, t1.getActive(),
			pluginDriver.getDocumentTypes(),
			pluginDriver.getDefaultDocumentType());
	}

	@Data
	@Builder
	@AllArgsConstructor(staticName = "of")
	@NoArgsConstructor(staticName = "of")
	public static class SupportedDatasourcesResponse {
		private String name;
		private Boolean active;
		private List<DocumentTypeDTO> documentTypes;
		private DocumentTypeDTO defaultDocumentType;
	}

	@Reference
	private HttpResponseWriter _httpResponseWriter;

	@Reference
	private DatasourceClient _datasourceClient;

	@Reference
	private PluginDriverManagerClient _pluginDriverManagerClient;

	private static final Logger _log = LoggerFactory.getLogger(
		SupportedDatasourcesEndpoint.class);

}
