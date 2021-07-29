package io.openk9.auth.keycloak.web;

import io.openk9.auth.keycloak.api.AuthVerifier;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.RouterHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

@Component(
	immediate = true,
	service = RouterHandler.class
)
public class UserInfoHttpHandler
	implements RouterHandler, HttpHandler {

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {
		return router.post("/v1/auth/user-info", this);
	}

	@Override
	public Publisher<Void> apply(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		return _httpResponseWriter.write(
			httpResponse,
			_authVerifier.getUserInfo(httpRequest)
		);
	}

	@Reference(
		target = "(type=json)"
	)
	private HttpResponseWriter _httpResponseWriter;

	@Reference
	private AuthVerifier _authVerifier;

}
