package io.openk9.auth.keycloak;

import io.openk9.auth.keycloak.api.Constants;
import io.openk9.auth.keycloak.api.KeycloakClient;
import io.openk9.auth.keycloak.api.LoginInfo;
import io.openk9.auth.keycloak.api.UserInfo;
import io.openk9.http.client.HttpClient;
import io.openk9.http.client.HttpClientFactory;
import io.openk9.http.web.HttpHandler;
import io.openk9.json.api.JsonFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component(
	immediate = true,
	service = KeycloakClient.class
)
public class KeycloakClientImpl implements KeycloakClient {

	@interface Config {
		String baseUrl() default "http://keycloak:8080/";
		String clientId() default "openk9";
		String clientSecret() default "openk9";
	}

	@Activate
	public void activate(Config config) {
		_httpClient = _httpClientFactory.getHttpClient(config.baseUrl());
		_config = config;
	}

	@Override
	public Mono<byte[]> logout(
		String virtualHost, String accessToken, String refreshToken) {

		return Mono.from(
			_httpClient
				.request(
					HttpHandler.POST,
					"/auth/realms/" + virtualHost + "/protocol/openid-connect/logout",
					Map.of(
						Constants.CLIENT_ID, _config.clientId(),
						Constants.CLIENT_SECRET, _config.clientSecret(),
						Constants.REFRESH_TOKEN, refreshToken
					),
					Map.of(
						"Authorization", "Bearer " + accessToken
					)
				)
		);
	}

	@Override
	public Mono<LoginInfo> login(
		String virtualHost, String username, String password) {

		return Mono.from(
			_httpClient
				.request(
					HttpHandler.POST,
					"/auth/realms/" + virtualHost + "/protocol/openid-connect/token",
					Map.of(
						Constants.USERNAME, username,
						Constants.PASSWORD, password,
						Constants.CLIENT_ID, _config.clientId(),
						Constants.CLIENT_SECRET, _config.clientSecret(),
						Constants.GRANT_TYPE, Constants.PASSWORD
					)
				)
		)
			.map(json -> _jsonFactory.fromJson(json, LoginInfo.class));

	}

	@Override
	public Mono<LoginInfo> refresh(
		String virtualHost, String refreshToken) {

		return Mono.from(
			_httpClient
				.request(
					HttpHandler.POST,
					"/auth/realms/" + virtualHost + "/protocol/openid-connect/token",
					Map.of(
						Constants.CLIENT_ID, _config.clientId(),
						Constants.CLIENT_SECRET, _config.clientSecret(),
						Constants.GRANT_TYPE, Constants.REFRESH_TOKEN,
						Constants.REFRESH_TOKEN, refreshToken
					)
				)
		)
			.map(json -> _jsonFactory.fromJson(json, LoginInfo.class));
	}

	@Override
	public Mono<UserInfo> introspect(String virtualHost, String token) {

		return Mono.from(
			_httpClient
				.request(
					HttpHandler.POST,
					"/auth/realms/" + virtualHost + "/protocol/openid-connect/token/introspect",
					Map.of(
						Constants.TOKEN, token,
						Constants.CLIENT_ID, _config.clientId(),
						Constants.CLIENT_SECRET, _config.clientSecret()
					)
				)
		)
			.map(String::new)
			.filter(json -> !json.equals("{\"active\":false}"))
			.map(json -> _jsonFactory.fromJson(json, UserInfo.class));
	}

	private volatile Config _config;

	private HttpClient _httpClient;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private HttpClientFactory _httpClientFactory;

}
