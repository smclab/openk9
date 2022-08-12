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

package io.openk9.plugin.driver.manager.web;

import io.openk9.auth.api.AuthVerifier;
import io.openk9.http.exception.HttpException;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.plugin.driver.manager.api.PluginDriver;
import io.openk9.plugin.driver.manager.api.PluginDriverDTOService;
import io.openk9.plugin.driver.manager.api.PluginDriverRegistry;
import io.openk9.plugin.driver.manager.model.InvokeDataParserDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverContextDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTOList;
import io.openk9.plugin.driver.manager.model.SchedulerEnabledDTO;
import io.openk9.reactor.netty.util.ReactorNettyUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.Optional;

@Component(immediate = true, service = RouterHandler.class)
public class PluginDriverEndPoints implements RouterHandler {

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router
			.get("/v1/plugin-driver/{serviceDriverName}", this::_findPluginDriverByName)
			.post("/v1/plugin-driver/", this::_findPluginDriverByNames)
			.post("/v1/plugin-driver/context", this::_createPluginDriverContext)
			.get("/v1/plugin-driver/", this::_findAll)
			.get("/v1/plugin-driver/scheduler-enabled/{serviceDriverName}", this::_schedulerEnabled)
			.post("/v1/plugin-driver/invoke-data-parser", this::_invokeDataParser);
	}

	private Publisher<Void> _createPluginDriverContext(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		return Mono.defer(() -> {

			Mono<PluginDriverContextDTO> response =
				Mono
					.from(ReactorNettyUtils.aggregateBodyAsString(httpRequest))
					.map(json -> _jsonFactory.fromJsonList(json, String.class))
					.map(names -> _pluginDriverDTOService.findPluginDriverContextDTO(
						names, _authVerifier.getUserInfo_(httpRequest)));

			return Mono.from(_httpResponseWriter.write(httpResponse, response));

		});
	}

	private Publisher<Void> _invokeDataParser(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		return Mono.defer(() ->
			Mono
				.from(ReactorNettyUtils.aggregateBodyAsString(httpRequest))
				.map(json -> _jsonFactory.fromJson(json, InvokeDataParserDTO.class))
				.flatMap((invokeDataParserDTO)-> {

					String serviceDriverName =
						invokeDataParserDTO.getServiceDriverName();

					Optional<PluginDriver> optional =
						_pluginDriverRegistry.getPluginDriver(
							serviceDriverName);

					if (optional.isEmpty()) {
						throw new HttpException(
							404,
							"No Content. PluginDriver not found for serviceDriverName: "
							+ serviceDriverName
						);
					}
					else {
						PluginDriver pluginDriver = optional.get();
						if (pluginDriver.schedulerEnabled()) {
							return Mono.from(
								pluginDriver.invokeDataParser(
									invokeDataParserDTO.getDatasource(),
									invokeDataParserDTO.getFromDate(),
									invokeDataParserDTO.getToDate(),
									invokeDataParserDTO.getScheduleId()
								)
							)
								.then(
									Mono.from(
										_httpResponseWriter.write(
											httpResponse, "{}"))
								);
						}
						else {
							throw new HttpException(
								412,
								" Precondition Failed. PluginDriver scheduler is disabled for serviceDriverName: "
								+ serviceDriverName
							);
						}
					}

				})
			.then()
		);

	}

	private Publisher<Void> _schedulerEnabled(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		return Mono.defer(() -> {

			String serviceDriverName =
				httpRequest.param("serviceDriverName");

			Optional<PluginDriver> pluginDriver =
				_pluginDriverRegistry.getPluginDriver(serviceDriverName);

			if (pluginDriver.isEmpty()) {
				throw new HttpException(
					404,
					"No Content. PluginDriver not found for serviceDriverName: "
					+ serviceDriverName
				);
			}
			else {
				return Mono.from(
					_httpResponseWriter.write(
						httpResponse,
						SchedulerEnabledDTO.of(
							pluginDriver.get().schedulerEnabled()
						)
					)
				);
			}

		});
	}

	private Publisher<Void> _findPluginDriverByNames(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		return Mono.defer(() -> {

			Mono<PluginDriverDTOList> response =
				Mono
					.from(ReactorNettyUtils.aggregateBodyAsString(httpRequest))
					.map(json -> _jsonFactory.fromJsonList(json, String.class))
					.map(_pluginDriverDTOService::findPluginDriverDTOByNames);

			return Mono.from(_httpResponseWriter.write(httpResponse, response));

		});
	}

	private Publisher<Void> _findPluginDriverByName(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		return Mono.defer(() -> {

			String serviceDriverName =
				httpRequest.param("serviceDriverName");

			PluginDriverDTO response =
				_pluginDriverDTOService
					.findPluginDriverDTOByName(serviceDriverName)
					.orElseThrow(() -> new HttpException(
						404,
						"No Content. PluginDriver not found for serviceDriverName: " +
						serviceDriverName));

			return Mono.from(
				_httpResponseWriter.write(httpResponse, response));

		});
	}

	private Publisher<Void> _findAll(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		return Mono.defer(() ->
			Mono.from(
				_httpResponseWriter.write(
					httpResponse,
					_pluginDriverDTOService.findPluginDriverDTOList()
				)
			)
		);
	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private HttpResponseWriter _httpResponseWriter;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private PluginDriverDTOService _pluginDriverDTOService;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private PluginDriverRegistry _pluginDriverRegistry;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private JsonFactory _jsonFactory;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private AuthVerifier _authVerifier;

}
