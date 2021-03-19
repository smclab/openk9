package io.openk9.auth.keycloak.web;

import io.openk9.auth.keycloak.api.KeycloakClient;
import io.openk9.auth.keycloak.web.request.RefreshTokenRequest;
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
public class RefreshLoginTokenHttpHandler implements HttpHandler {

	@Override
	public String getPath() {
		return "/v1/auth/refresh";
	}

	@Override
	public int method() {
		return HttpHandler.POST;
	}

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		String hostName = HttpUtil.getHostName(httpRequest);

		Mono<String> loginRequest =
			Mono
				.from(httpRequest.aggregateBodyToByteArray())
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
