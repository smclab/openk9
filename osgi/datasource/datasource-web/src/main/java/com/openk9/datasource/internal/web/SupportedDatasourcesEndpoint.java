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

package com.openk9.datasource.internal.web;

import com.openk9.datasource.model.Datasource;
import com.openk9.datasource.model.Tenant;
import com.openk9.datasource.repository.DatasourceRepository;
import com.openk9.datasource.repository.TenantRepository;
import com.openk9.http.util.HttpResponseWriter;
import com.openk9.http.util.HttpUtil;
import com.openk9.http.web.Endpoint;
import com.openk9.http.web.HttpHandler;
import com.openk9.http.web.HttpRequest;
import com.openk9.http.web.HttpResponse;
import com.openk9.ingestion.driver.manager.api.DocumentType;
import com.openk9.ingestion.driver.manager.api.DocumentTypeProvider;
import com.openk9.ingestion.driver.manager.api.PluginDriver;
import com.openk9.ingestion.driver.manager.api.PluginDriverRegistry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Optional;

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
			_tenantRepository
				.findByVirtualHost(hostName)
				.switchIfEmpty(
					Mono.error(
						() -> new RuntimeException(
							"tenant not found for virtualhost: " + hostName)))
				.map(Tenant::getTenantId)
				.flatMapMany(_datasourceRepository::findByTenantId)
				.map(datasource -> Tuples
					.of(
						datasource,
						_pluginDriverRegistry.getPluginDriver(
							datasource.getDriverServiceName())))
				.filter(t2 -> t2.getT2().isPresent())
				.map(this::_mapToResponse);

		return _httpResponseWriter.write(httpResponse, response);

	}

	private SupportedDatasourcesResponse _mapToResponse(
		Tuple2<Datasource, Optional<PluginDriver>> t2) {

		Datasource t1 = t2.getT1();

		PluginDriver pluginDriver = t2.getT2().get();

		String pluginDriverName = pluginDriver.getName();

		return SupportedDatasourcesResponse.of(
			pluginDriverName, t1.getActive(),
			_documentTypeProvider.getDocumentTypeList(pluginDriverName),
			_documentTypeProvider.getDefaultDocumentType(
				pluginDriverName));
	}

	@Data
	@Builder
	@AllArgsConstructor(staticName = "of")
	@NoArgsConstructor(staticName = "of")
	public static class SupportedDatasourcesResponse {
		private String name;
		private Boolean active;
		private List<DocumentType> documentTypes;
		private DocumentType defaultDocumentType;
	}

	@Reference
	private HttpResponseWriter _httpResponseWriter;

	@Reference
	private PluginDriverRegistry _pluginDriverRegistry;

	@Reference
	private TenantRepository _tenantRepository;

	@Reference
	private DatasourceRepository _datasourceRepository;

	@Reference
	private DocumentTypeProvider _documentTypeProvider;

}
