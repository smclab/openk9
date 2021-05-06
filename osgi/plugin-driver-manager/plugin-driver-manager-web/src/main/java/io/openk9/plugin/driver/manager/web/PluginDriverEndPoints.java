package io.openk9.plugin.driver.manager.web;

import io.openk9.http.exception.HttpException;
import io.openk9.http.util.BaseEndpointRegister;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.json.api.JsonFactory;
import io.openk9.plugin.driver.manager.api.PluginDriver;
import io.openk9.plugin.driver.manager.api.PluginDriverDTOService;
import io.openk9.plugin.driver.manager.api.PluginDriverRegistry;
import io.openk9.plugin.driver.manager.model.InvokeDataParserDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTOList;
import io.openk9.plugin.driver.manager.model.SchedulerEnabledDTO;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component(immediate = true, service = PluginDriverEndPoints.class)
public class PluginDriverEndPoints extends BaseEndpointRegister {

	@Activate
	public void activate(BundleContext bundleContext) {

		setBundleContext(bundleContext);

		this.registerEndpoint(
			HttpHandler.get("/{serviceDriverName}", this::_findPluginDriverByName),
			HttpHandler.post("/", this::_findPluginDriverByNames),
			HttpHandler.get("/", this::_findAll),
			HttpHandler.get("/scheduler-enabled/{serviceDriverName}", this::_schedulerEnabled),
			HttpHandler.post("/invoke-data-parser/", this::_invokeDataParser)
		);

	}

	private Publisher<Void> _invokeDataParser(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		return Mono.defer(() ->
			Mono
				.from(httpRequest.aggregateBodyToString())
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
									invokeDataParserDTO.getToDate()
								)
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
		HttpRequest httpRequest, HttpResponse httpResponse) {

		return Mono.defer(() -> {

			String serviceDriverName =
				httpRequest.pathParam("serviceDriverName");

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
		HttpRequest httpRequest, HttpResponse httpResponse) {

		return Mono.defer(() -> {

			Mono<PluginDriverDTOList> response =
				Mono
					.from(httpRequest.aggregateBodyToString())
					.map(json -> _jsonFactory.fromJsonList(json, String.class))
					.map(_pluginDriverDTOService::findPluginDriverDTOByNames);

			return Mono.from(_httpResponseWriter.write(httpResponse, response));

		});
	}

	private Publisher<Void> _findPluginDriverByName(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		return Mono.defer(() -> {

			String serviceDriverName =
				httpRequest.pathParam("serviceDriverName");

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
		HttpRequest httpRequest, HttpResponse httpResponse) {

		return Mono.defer(() ->
			Mono.from(
				_httpResponseWriter.write(
					httpResponse,
					_pluginDriverDTOService.findPluginDriverDTOList()
				)
			)
		);
	}

	@Deactivate
	public void deactivate() {
		this.close();
	}

	@Override
	public String getBasePath() {
		return "/v1/plugin-driver";
	}

	@Reference
	private HttpResponseWriter _httpResponseWriter;

	@Reference
	private PluginDriverDTOService _pluginDriverDTOService;

	@Reference
	private PluginDriverRegistry _pluginDriverRegistry;

	@Reference
	private JsonFactory _jsonFactory;

}
