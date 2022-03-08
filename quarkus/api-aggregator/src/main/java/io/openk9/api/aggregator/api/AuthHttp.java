package io.openk9.api.aggregator.api;


import io.openk9.api.aggregator.client.dto.LoginResponseDTO;
import io.openk9.api.aggregator.client.dto.UserInfoResponseDTO;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

public interface AuthHttp {

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/auth/realms/{tenant}/protocol/openid-connect/token")
	public Uni<LoginResponseDTO> login(
		@PathParam("tenant") String tenant,
		@FormParam("username") String username,
		@FormParam("password") String password,
		@FormParam("client_id") String clientId,
		@FormParam("client_secret") String clientSecret,
		@FormParam("grant_type") String grantType);

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/auth/realms/{tenant}/protocol/openid-connect/token")
	public Uni<LoginResponseDTO> login(
		@PathParam("tenant") String tenant,
		@FormParam("username") String username,
		@FormParam("password") String password,
		@FormParam("client_id") String clientId,
		@FormParam("grant_type") String grantType);

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/auth/realms/{tenant}/protocol/openid-connect/token")
	public Uni<LoginResponseDTO> refresh(
		@PathParam("tenant") String tenant,
		@FormParam("client_id") String clientId,
		@FormParam("client_secret") String clientSecret,
		@FormParam("refresh_token") String refreshToken,
		@FormParam("grant_type") String grantType);

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/auth/realms/{tenant}/protocol/openid-connect/token")
	public Uni<LoginResponseDTO> refresh(
		@PathParam("tenant") String tenant,
		@FormParam("client_id") String clientId,
		@FormParam("refresh_token") String refreshToken,
		@FormParam("grant_type") String grantType);

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/auth/realms/{tenant}/protocol/openid-connect/token/introspect")
	public Uni<UserInfoResponseDTO> userInfo(
		@PathParam("tenant") String tenant,
		@FormParam("client_id") String clientId,
		@FormParam("client_secret") String clientSecret,
		@FormParam("token") String token);

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/auth/realms/{tenant}/protocol/openid-connect/token/introspect")
	public Uni<UserInfoResponseDTO> userInfo(
		@PathParam("tenant") String tenant,
		@FormParam("client_id") String clientId,
		@FormParam("token") String token);

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/auth/realms/{tenant}/protocol/openid-connect/logout")
	public Uni<byte[]> logout(
		@PathParam("tenant") String tenant,
		@FormParam("client_id") String clientId,
		@FormParam("client_secret") String clientSecret,
		@FormParam("refresh_token") String refreshToken);

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/auth/realms/{tenant}/protocol/openid-connect/logout")
	public Uni<byte[]> logout(
		@PathParam("tenant") String tenant,
		@FormParam("client_id") String clientId,
		@FormParam("refresh_token") String refreshToken);

}
