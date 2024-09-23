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

import io.quarkus.oidc.OidcTenantConfig;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.util.Optional;

@Path("/oauth2")
public class OAuth2SettingsResource {

    @Inject
    RoutingContext routingContext;

    @GET
    @Path("/settings")
    public Uni<Settings> settings() {
        Uni<OidcTenantConfig> oidcConfigUni =
            routingContext.get("dynamic.tenant.config");

        return oidcConfigUni.map(oidcConfig -> new Settings(
            _createUrl(oidcConfig.getAuthServerUrl()),
            oidcConfig.getTenantId().orElse(""),
            oidcConfig.getClientId().orElse("")
        ));

    }

    @GET
    @Path("/settings.js")
    @Produces("text/javascript")
    public Uni<String> settingsJs() {
        Uni<OidcTenantConfig> oidcConfigUni =
            routingContext.get("dynamic.tenant.config");

        return oidcConfigUni.map(
            oidcConfig -> String.format(
                "window.KEYCLOAK_URL='%s';" +
                "window.KEYCLOAK_REALM='%s';" +
                "window.KEYCLOAK_CLIENT_ID='%s';",
                _createUrl(oidcConfig.getAuthServerUrl()),
                oidcConfig.getTenantId().orElse(""),
                oidcConfig.getClientId().orElse("")
            )
        );

    }

    private static String _createUrl(Optional<String> authServerUrl) {

        return authServerUrl
            .map(s -> {
                int start = s.indexOf("://");
                int end = s.indexOf("/", start + 3);
                return s.substring(0, end);
            })
            .orElse("");
    }

    public record Settings(String url, String realm, String clientId) {}

}