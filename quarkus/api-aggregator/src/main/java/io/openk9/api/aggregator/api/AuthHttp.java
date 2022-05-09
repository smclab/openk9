/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
