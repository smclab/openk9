package io.openk9.auth.keycloak.web;

import io.openk9.auth.keycloak.api.AuthVerifier;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.Endpoint;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;

@Component(
	immediate = true,
	service = Endpoint.class
)
public class UserInfoHttpHandler implements HttpHandler {

	@Override
	public String getPath() {
		return "/v1/auth/user-info";
	}

	@Override
	public int method() {
		return HttpHandler.POST;
	}

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

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
