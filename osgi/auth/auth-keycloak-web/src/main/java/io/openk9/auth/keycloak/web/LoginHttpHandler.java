package io.openk9.auth.keycloak.web;

import io.openk9.auth.keycloak.api.KeycloakClient;
import io.openk9.auth.keycloak.web.request.LoginRequest;
import io.openk9.http.exception.HttpException;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.util.HttpUtil;
import io.openk9.http.web.Endpoint;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.json.api.JsonFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

@Component(
	immediate = true,
	service = Endpoint.class
)
public class LoginHttpHandler implements HttpHandler {

	@Override
	public String getPath() {
		return "/v1/auth/login";
	}

	@Override
	public int method() {
		return HttpHandler.POST;
	}

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		String hostName = HttpUtil.getHostName(httpRequest);

		Mono<LoginRequest> loginRequest =
			Mono
				.from(httpRequest.aggregateBodyToByteArray())
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
