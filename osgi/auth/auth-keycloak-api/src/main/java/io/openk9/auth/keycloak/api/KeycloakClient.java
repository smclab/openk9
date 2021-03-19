package io.openk9.auth.keycloak.api;

import reactor.core.publisher.Mono;

public interface KeycloakClient {

	Mono<byte[]> logout(
		String virtualHost, String accessToken, String refreshToken);

	Mono<LoginInfo> login(
		String virtualHost, String username, String password);

	Mono<LoginInfo> refresh(String virtualHost, String refreshToken);

	Mono<UserInfo> introspect(String virtualHost, String token);

}
