package io.openk9.api.aggregator.resource;

import io.openk9.api.aggregator.client.AuthClient;
import io.openk9.api.aggregator.client.dto.LoginResponseDTO;
import io.openk9.api.aggregator.client.dto.UserInfoResponseDTO;
import io.openk9.api.aggregator.model.Tenant;
import io.openk9.api.aggregator.service.TenantRegistry;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.handler.HttpException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("/api/searcher")
@RequestScoped
public class AuthResource extends FaultTolerance {

	@PermitAll
	@Path("/v1/auth/login")
	@POST
	public Uni<LoginResponseDTO> login(
		@Context HttpServerRequest context, LoginRequest request) {

		String host = context.host();

		Tenant tenant = _findTenant(host);

		String realmName =  tenant.getRealmName();
		String clientSecret = tenant.getClientSecret();
		String clientId = tenant.getClientId();
		String username = request.getUsername();
		String password = request.getPassword();

		if (username == null) {
			return Uni.createFrom().failure(() -> new HttpException(400, "required username"));
		}

		if (password == null) {
			return Uni.createFrom().failure(() -> new HttpException(400, "required password"));
		}

		if (clientSecret != null) {
			return _authClient.login(
				realmName,
				username,
				password,
				clientId,
				clientSecret,
				"password"
			);
		}
		else {
			return _authClient.login(
				realmName,
				username,
				password,
				clientId,
				"password"
			);
		}

	}

	@PermitAll
	@Path("/v1/auth/refresh")
	@POST
	public Uni<LoginResponseDTO> refreshToken(
		@Context HttpServerRequest context, RefreshToken request) {

		String host = context.host();

		Tenant tenant = _findTenant(host);

		String realmName = tenant.getRealmName();
		String clientSecret = tenant.getClientSecret();
		String clientId = tenant.getClientId();

		String refreshToken = request.getRefreshToken();

		if (refreshToken == null) {
			return Uni.createFrom().failure(() -> new HttpException(400, "required refreshToken"));
		}

		if (clientSecret != null) {
			return _authClient.refresh(
				realmName,
				clientId,
				clientSecret,
				refreshToken,
				"refresh_token"
			);
		}
		else {
			return _authClient.refresh(
				realmName,
				clientId,
				refreshToken,
				"refresh_token"
			);
		}

	}

	@PermitAll
	@Path("/v1/auth/user-info")
	@POST
	@SecurityRequirement(name = "SecurityScheme")
	public Uni<UserInfoResponseDTO> userInfo(
		@Context HttpServerRequest context) {

		System.out.println("userInfo");

		String host = context.host();

		Tenant tenant = _findTenant(host);

		String realmName = tenant.getRealmName();
		String clientSecret = tenant.getClientSecret();
		String clientId = tenant.getClientId();

		String rawToken = jwt.getRawToken();

		if (clientSecret != null) {
			return _authClient.userInfo(
				realmName,
				clientId,
				clientSecret,
				rawToken
			);
		}
		else {
			return _authClient.userInfo(
				realmName,
				clientId,
				rawToken
			);
		}

	}

	@PermitAll
	@Path("/v1/auth/logout")
	@POST
	public Uni<byte[]> logout(
		@Context HttpServerRequest context, RefreshToken request) {

		String host = context.host();

		Tenant tenant = _findTenant(host);

		String realmName = tenant.getRealmName();
		String clientSecret = tenant.getClientSecret();
		String clientId = tenant.getClientId();
		String refreshToken = request.getRefreshToken();

		if (refreshToken == null) {
			return Uni.createFrom().failure(() -> new HttpException(400, "required refreshToken"));
		}

		if (clientSecret != null) {
			return _authClient.logout(
				realmName,
				clientId,
				clientSecret,
				refreshToken
			);
		}
		else {
			return _authClient.logout(
				realmName,
				clientId,
				refreshToken
			);
		}

	}

	private Tenant _findTenant(String host) {
		return _tenantRegistry
			.getTenant(host)
			.orElseThrow(
				() -> new HttpException(404, "tenant: " + host + " not found"));
	}

	@Inject
	@RestClient
	AuthClient _authClient;

	@Inject
	TenantRegistry _tenantRegistry;

	@Inject
	Logger logger;

	@Inject
	JsonWebToken jwt;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	@Builder
	@RegisterForReflection
	public static class LoginRequest {
		private String username;
		private String password;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	@Builder
	@RegisterForReflection
	public static class RefreshToken {
		private String refreshToken;
	}

}
