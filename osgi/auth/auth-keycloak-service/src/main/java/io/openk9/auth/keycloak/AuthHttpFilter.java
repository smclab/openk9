package io.openk9.auth.keycloak;

import io.openk9.auth.keycloak.api.AuthVerifier;
import io.openk9.http.web.HttpFilter;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;

import java.util.function.BiFunction;

@Component(
	immediate = true,
	property = {
		HttpFilter.URL_PATTERNS + "=" + "/.*"
	},
	enabled = false
)
public class AuthHttpFilter implements HttpFilter {

	@Override
	public Publisher<Void> doFilter(
		HttpRequest httpRequest, HttpResponse httpResponse,
		BiFunction<HttpRequest, HttpResponse, Publisher<Void>> chain) {

		return chain.apply(httpRequest, httpResponse);
	}

	@Reference
	private AuthVerifier _authVerifier;

}
