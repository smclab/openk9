package io.openk9.auth.api;

import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

public interface AuthVerifier {

	Mono<UserInfo> getUserInfo(HttpServerRequest httpRequest);

	Mono<UserInfo> getUserInfo(HttpServerRequest httpRequest, Mono<String> nameSupplier);

	UserInfo GUEST = new UserInfo();

}
