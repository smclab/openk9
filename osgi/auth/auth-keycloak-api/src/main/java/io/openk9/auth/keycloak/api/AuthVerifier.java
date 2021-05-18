package io.openk9.auth.keycloak.api;

import io.openk9.http.web.HttpRequest;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public interface AuthVerifier {

	Mono<UserInfo> getUserInfo(HttpRequest httpRequest);

	Mono<UserInfo> getUserInfo(HttpRequest httpRequest, Mono<String> nameSupplier);

	UserInfo GUEST = new UserInfo();

}
