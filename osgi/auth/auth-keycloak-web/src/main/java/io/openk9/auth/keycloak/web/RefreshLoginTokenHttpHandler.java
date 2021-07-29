package io.openk9.auth.keycloak.web;

import io.openk9.auth.keycloak.api.KeycloakClient;
import io.openk9.auth.keycloak.web.request.RefreshTokenRequest;
import io.openk9.http.exception.HttpException;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.util.HttpUtil;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.reactor.netty.util.ReactorNettyUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class RefreshLoginTokenHttpHandler
	implements RouterHandler, HttpHandler {

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router.post("/v1/auth/refresh", this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		String hostName = HttpUtil.getHostName(httpRequest);

		Mono<String> loginRequest =
			Mono
				.from(
					ReactorNettyUtils.aggregateBodyAsByteArray(httpRequest))
				.handle(this::_validate);

		return _httpResponseWriter.write(
			httpResponse,
			loginRequest
				.flatMap(
					refreshToken -> _keycloakClient.refresh(
						hostName, refreshToken))
		);
	}

	private void _validate(
		byte[] json, SynchronousSink<String> sink) {

		RefreshTokenRequest refreshTokenRequest = _jsonFactory.fromJson(
			json, RefreshTokenRequest.class);

		String refreshToken = refreshTokenRequest.getRefreshToken();

		if (refreshToken == null || refreshToken.isEmpty()) {
			sink.error(new HttpException(400, "required refreshToken"));
			return;
		}

		sink.next(refreshToken);

	}

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private KeycloakClient _keycloakClient;

	@Reference(
		target = "(type=json)"
	)
	private HttpResponseWriter _httpResponseWriter;

}
