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

package io.openk9.experimental.spring_apigw_sample;

//@Path("/oauth2")
public interface Oauth2SettingsResource {

//	@Operation(operationId = "settings")
//	@Tag(name = "Keycloak Settings API", description = "Return keycloak settings")
//	@APIResponses(value = {
//			@APIResponse(responseCode = "200", description = "success"),
//			@APIResponse(responseCode = "404", description = "not found"),
//			@APIResponse(responseCode = "400", description = "invalid"),
//			@APIResponse(
//					responseCode = "200",
//					description = "Keycloak Settings returned",
//					content = {
//							@Content(
//									mediaType = MediaType.APPLICATION_JSON,
//									schema = @Schema(implementation = Response.class)
//							)
//					}
//			),
//			@APIResponse(ref = "#/components/responses/bad-request"),
//			@APIResponse(ref = "#/components/responses/not-found"),
//			@APIResponse(ref = "#/components/responses/internal-server-error"),
//	})
//    @GET
//    @Path("/settings")
//	Uni<Oauth2Settings> keycloakSettings();

//	@Operation(operationId = "settings.js")
//	@Tag(name = "Keycloak Settings JS API", description = "Return keycloak settings as javascript file")
//	@APIResponses(value = {
//			@APIResponse(responseCode = "200", description = "success"),
//			@APIResponse(responseCode = "404", description = "not found"),
//			@APIResponse(responseCode = "400", description = "invalid"),
//			@APIResponse(
//					responseCode = "200",
//					description = "Keycloak Settings returned",
//					content = {
//							@Content(
//									mediaType = MediaType.APPLICATION_JSON,
//									schema = @Schema(implementation = Response.class)
//							)
//					}
//			),
//			@APIResponse(ref = "#/components/responses/bad-request"),
//			@APIResponse(ref = "#/components/responses/not-found"),
//			@APIResponse(ref = "#/components/responses/internal-server-error"),
//	})
//    @GET
//    @Path("/settings.js")
//    @Produces("text/javascript")
//    Uni<String> settingsJs();

	private static String encodeSettingsJs(Oauth2Settings oauth2Settings) {

		return String.format(
			"""
				window.KEYCLOAK_URL='%s';
				window.KEYCLOAK_REALM='%s';
				window.KEYCLOAK_CLIENT_ID='%s';
				""",
			oauth2Settings.url(),
			oauth2Settings.realm(),
			oauth2Settings.clientId()
		);
	}

	 record Oauth2Settings(String url, String realm, String clientId) {}

}