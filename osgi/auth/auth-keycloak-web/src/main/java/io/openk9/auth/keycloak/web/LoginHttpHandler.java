package io.openk9.auth.keycloak.web;

import io.openk9.auth.keycloak.api.KeycloakClient;
import io.openk9.auth.keycloak.web.request.LoginRequest;
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
public class LoginHttpHandler
	implements RouterHandler, HttpHandler {

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router.post("/v1/auth/login",this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		String hostName = HttpUtil.getHostName(httpRequest);

		Mono<LoginRequest> loginRequest =
			Mono
				.from(ReactorNettyUtils.aggregateBodyAsByteArray(httpRequest))
				.handle(this::_validate);

		return _httpResponseWriter.write(
			httpResponse,
			loginRequest
				.flatMap(
					lr -> _keycloakClient.login(
						hostName, lr.getUsername(), lr.getPassword()))
		);
	}

	private void _validate(
		byte[] json, SynchronousSink<LoginRequest> sink) {

		LoginRequest loginRequest =
			_jsonFactory.fromJson(json, LoginRequest.class);

		String username = loginRequest.getUsername();

		if (username == null || username.isEmpty()) {
			sink.error(new HttpException(400, "required username"));
			return;
		}

		String password = loginRequest.getPassword();

		if (password == null || password.isEmpty()) {
			sink.error(new HttpException(400, "required password"));
			return;
		}

		sink.next(loginRequest);

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
