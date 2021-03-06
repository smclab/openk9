package io.openk9.datasource.client.internal;

import io.openk9.datasource.client.api.DatasourceClient;
import io.openk9.http.client.HttpClient;
import io.openk9.http.client.HttpClientFactory;
import io.openk9.http.web.HttpHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.Datasource;
import io.openk9.model.Tenant;
import io.openk9.model.TenantDatasource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component(
	immediate = true,
	service = DatasourceClient.class
)
public class DatasourceClientImpl implements DatasourceClient {

	@interface Config {
		String baseUrl() default "http://openk9-datasource:8080/";
	}

	@Activate
	public void activate(Config config) {
		_httpClient = _httpClientFactory.getHttpClient(config.baseUrl());
	}

	@Modified
	public void modified(Config config) {
		activate(config);
	}

	@Override
	public Mono<TenantDatasource> findTenantAndDatasourceByDatasourceId(
		long datasourceId) {
		return findDatasource(datasourceId)
			.zipWhen(
				datasource -> findTenant(datasource.getTenantId()),
				TenantDatasource::of
			);
	}

	@Override
	public Mono<Datasource> findDatasource(long datasourceId) {
		return Mono.from(
			_httpClient
				.request(
					HttpHandler.GET,
					"/v2/datasource/" + datasourceId
				)
		)
			.map(response -> _jsonFactory.fromJson(response, Datasource.class));
	}

	@Override
	public Flux<Datasource> findDatasourceByTenantIdAndIsActive(long tenantId) {
		return Mono.from(
			_httpClient
				.request(
					HttpHandler.POST,
					"/v2/datasource/filter",
					_jsonFactory
						.createObjectNode()
						.put("tenantId", tenantId)
						.put("active", true)
						.toString()
				)
		)
			.flatMapIterable(
				response -> _jsonFactory.fromJsonList(
					response, Datasource.class));
	}

	@Override
	public Flux<Datasource> findDatasourceByTenantId(long tenantId) {
		return Mono.from(
			_httpClient
				.request(
					HttpHandler.POST,
					"/v2/datasource/filter",
					_jsonFactory
						.createObjectNode()
						.put("tenantId", tenantId)
						.toString()
				)
		)
			.flatMapIterable(
				response -> _jsonFactory.fromJsonList(
					response, Datasource.class));
	}

	@Override
	public Flux<Tenant> findTenantByVirtualHost(String virtualHost) {
		return Mono.from(
			_httpClient
				.request(
					HttpHandler.POST,
					"/v2/tenant/filter",
					_jsonFactory
						.createObjectNode()
						.put("virtualHost", virtualHost)
						.toString()
				)
		)
			.flatMapIterable(response -> _jsonFactory.fromJsonList(response, Tenant.class));
	}

	@Override
	public Mono<Tenant> findTenant(long tenantId) {
		return Mono.from(
			_httpClient
				.request(
					HttpHandler.GET,
					"/v2/tenant/" + tenantId
				)
		)
			.map(response -> _jsonFactory.fromJson(response, Tenant.class));
	}

	private HttpClient _httpClient;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private HttpClientFactory _httpClientFactory;

}
