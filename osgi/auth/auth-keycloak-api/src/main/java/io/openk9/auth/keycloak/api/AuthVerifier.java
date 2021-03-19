package io.openk9.auth.keycloak.api;

import io.openk9.http.web.HttpRequest;
import reactor.core.publisher.Mono;

public interface AuthVerifier {

	Mono<UserInfo> getUserInfo(HttpRequest httpRequest);

	UserInfo GUEST = new UserInfo();

}
