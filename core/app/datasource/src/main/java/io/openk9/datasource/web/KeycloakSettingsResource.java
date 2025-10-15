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

package io.openk9.datasource.web;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import io.openk9.datasource.web.dto.KeycloakSettings;

import io.quarkus.oidc.OidcTenantConfig;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/oauth2")
public class KeycloakSettingsResource {

	private static final String PROTOCOL_END = "://";
	private static final String FIRST_PATH = "/";
	private static final String EMPTY = "";

	@Inject
	RoutingContext routingContext;

	@Operation(operationId = "settings")
	@Tag(description = "Return keycloak settings")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "200",
					description = "Keycloak Settings returned",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = Response.class)
							)
					}
			),
			@APIResponse(ref = "#/components/responses/bad-request"),
			@APIResponse(ref = "#/components/responses/not-found"),
			@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
    @GET
    @Path("/settings")
	public Uni<KeycloakSettings> keycloakSettings() {
        Uni<OidcTenantConfig> oidcConfigUni =
            routingContext.get("dynamic.tenant.config");

		return oidcConfigUni.map(KeycloakSettingsResource::mapSettings);
    }

	@Operation(operationId = "settings.js")
	@Tag(description = "Return keycloak settings as javascript file")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "200",
					description = "Keycloak Settings returned",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = Response.class)
							)
					}
			),
			@APIResponse(ref = "#/components/responses/bad-request"),
			@APIResponse(ref = "#/components/responses/not-found"),
			@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
    @GET
    @Path("/settings.js")
    @Produces("text/javascript")
    public Uni<String> settingsJs() {
        Uni<OidcTenantConfig> oidcConfigUni =
            routingContext.get("dynamic.tenant.config");

		return oidcConfigUni.map(KeycloakSettingsResource::mapSettings)
			.map(KeycloakSettingsResource::encodeSettingsJs);
    }

	private static String encodeSettingsJs(KeycloakSettings keycloakSettings) {

		return String.format(
			"""
				window.KEYCLOAK_URL='%s';
				window.KEYCLOAK_REALM='%s';
				window.KEYCLOAK_CLIENT_ID='%s';
				""",
			keycloakSettings.url(),
			keycloakSettings.realm(),
			keycloakSettings.clientId()
		);
	}

	private static String getKeycloakUrl(String authServerUrl) {

		int hostNameStartIndex = authServerUrl.indexOf(PROTOCOL_END) + 3;
		int hostNameEndIndex = authServerUrl.indexOf(FIRST_PATH, hostNameStartIndex);

		return authServerUrl.substring(0, hostNameEndIndex);
	}

	private static KeycloakSettings mapSettings(OidcTenantConfig oidcTenantConfig) {
		var authServerUrl = oidcTenantConfig.authServerUrl()
			.map(KeycloakSettingsResource::getKeycloakUrl)
			.orElse(EMPTY);
		var tenantId = oidcTenantConfig.tenantId().orElse(EMPTY);
		var clientId = oidcTenantConfig.clientId().orElse(EMPTY);

		return new KeycloakSettings(authServerUrl, tenantId, clientId);
	}

}