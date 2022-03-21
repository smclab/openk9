package io.openk9.plugin.driver.manager.client.service;

import io.openk9.http.client.HttpClient;
import io.openk9.http.client.HttpClientFactory;
import io.openk9.http.web.HttpHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.Datasource;
import io.openk9.plugin.driver.manager.client.api.PluginDriverManagerClient;
import io.openk9.plugin.driver.manager.model.InvokeDataParserDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTOList;
import io.openk9.plugin.driver.manager.model.SchedulerEnabledDTO;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

@Component(
	immediate = true,
	service = PluginDriverManagerClient.class
)
public class PluginDriverManagerClientImpl
	implements PluginDriverManagerClient {

	@interface Config {
		String url() default "http://plugin-driver-manager";
	}

	@Activate
	void activate(Config config) {

		String url = config.url();

		_pluginDriverManagerClient = _httpClientFactory.getHttpClient(url);
	}

	@Modified
	void modified(Config config) {

		deactivate();

		activate(config);

	}

	@Deactivate
	void deactivate() {
		_pluginDriverManagerClient = null;
	}

	@Override
	public Mono<Void> invokeDataParser(
		String serviceDriverName, Datasource datasource, Date fromDate,
		Date toDate) {

		return Mono
			.from(
				_pluginDriverManagerClient
					.request(
						HttpHandler.POST,
						"/v1/plugin-driver/invoke-data-parser/",
						_jsonFactory.toJson(
							InvokeDataParserDTO
								.builder()
								.serviceDriverName(serviceDriverName)
								.datasource(datasource)
								.fromDate(fromDate)
								.toDate(toDate)
								.build()
						),
						Map.of()
					)
			)
			.then();
	}

	@Override
	public Mono<SchedulerEnabledDTO> schedulerEnabled(String serviceDriverName) {
		return Mono
			.from(
				_pluginDriverManagerClient
					.request(
						HttpHandler.GET,
						"/v1/plugin-driver/scheduler-enabled/" + serviceDriverName
					)
			)
			.map(bytes -> _jsonFactory.fromJson(bytes, SchedulerEnabledDTO.class));
	}

	@Override
	public Mono<PluginDriverDTO> getPluginDriver(String serviceDriverName) {
		return Mono
			.from(
				_pluginDriverManagerClient
					.request(
						HttpHandler.GET,
						"/v1/plugin-driver/" + serviceDriverName
					)
			)
			.map(bytes -> _jsonFactory.fromJson(bytes, PluginDriverDTO.class));
	}

	@Override
	public Mono<PluginDriverDTOList> getPluginDriverList(
		Collection<String> serviceDriverNames) {
		return Mono
			.from(
				_pluginDriverManagerClient
					.request(
						HttpHandler.POST,
						"/v1/plugin-driver/",
						_jsonFactory.toJson(serviceDriverNames),
						Map.of()
					)
			)
			.map(bytes -> _jsonFactory.fromJson(bytes, PluginDriverDTOList.class));
	}

	@Override
	public Mono<PluginDriverDTOList> getPluginDriverList() {

		return Mono
			.from(
				_pluginDriverManagerClient
					.request(
						HttpHandler.GET,
						"/v1/plugin-driver/"
					)
			)
			.map(bytes -> _jsonFactory.fromJson(bytes, PluginDriverDTOList.class));

	}

	private HttpClient _pluginDriverManagerClient;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private JsonFactory _jsonFactory;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private HttpClientFactory _httpClientFactory;

}
