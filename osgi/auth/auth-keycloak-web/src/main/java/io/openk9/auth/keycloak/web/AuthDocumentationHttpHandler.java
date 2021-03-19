package io.openk9.auth.keycloak.web;

import io.openk9.auth.keycloak.api.LoginInfo;
import io.openk9.auth.keycloak.api.UserInfo;
import io.openk9.auth.keycloak.web.request.LoginRequest;
import io.openk9.auth.keycloak.web.request.LogoutRequest;
import io.openk9.auth.keycloak.web.request.RefreshTokenRequest;
import io.openk9.auth.keycloak.web.request.UserInfoRequest;
import io.openk9.documentation.model.RestDocumentation;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.Endpoint;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.json.api.JsonFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component(
	immediate = true,
	service = Endpoint.class
)
public class AuthDocumentationHttpHandler implements HttpHandler {

	@Override
	public String getPath() {
		return "/v1/auth/doc";
	}

	@Override
	public int method() {
		return HttpHandler.GET;
	}

	@Override
	public Publisher<Void> apply(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		return Mono.defer(() -> {
			List<RestDocumentation> documentationList = new ArrayList<>();

			documentationList.add(
				RestDocumentation.of(
					List.of("POST"),
					"/v1/auth/login",
					false,
					List.of(),
					_jsonFactory.toJsonClassDefinition(
						LoginRequest.class),
					_jsonFactory.toJsonClassDefinition(LoginInfo.class)
				)
			);

			documentationList.add(
				RestDocumentation.of(
					List.of("POST"),
					"/v1/auth/logout",
					false,
					List.of(),
					_jsonFactory.toJsonClassDefinition(
						LogoutRequest.class),
					null
				)
			);

			documentationList.add(
				RestDocumentation.of(
					List.of("POST"),
					"/v1/auth/refresh",
					false,
					List.of(),
					_jsonFactory.toJsonClassDefinition(
						RefreshTokenRequest.class),
					_jsonFactory.toJsonClassDefinition(LoginInfo.class)
				)
			);

			documentationList.add(
				RestDocumentation.of(
					List.of("POST"),
					"/v1/auth/user-info",
					false,
					List.of(),
					_jsonFactory.toJsonClassDefinition(
						UserInfoRequest.class),
					_jsonFactory.toJsonClassDefinition(UserInfo.class)
				)
			);

			return Mono.from(
				_httpResponseWriter
					.write(httpResponse, documentationList)
			);

		});

	}

	@Reference
	private JsonFactory _jsonFactory;

	@Reference(
		target = "(type=json)"
	)
	private HttpResponseWriter _httpResponseWriter;

}
