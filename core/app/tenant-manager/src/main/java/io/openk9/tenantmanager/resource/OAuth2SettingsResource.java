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

package io.openk9.tenantmanager.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/oauth2")
public class OAuth2SettingsResource {

    @GET
    @Path("/settings.js")
    @Produces("text/javascript")
    public String settingsJs() {

        return String.format(
            "window.KEYCLOAK_URL='%s';" +
            "window.KEYCLOAK_REALM='%s';" +
            "window.KEYCLOAK_CLIENT_ID='%s';",
            _createUrl(authServerUrl),
            _getRealmFromPathUrl(authServerUrl),
            clientId
        );

    }

    private static String _getRealmFromPathUrl(String pathUrl) {
        String[] split = pathUrl.split("/");
        return split[split.length - 1];
    }

    private static String _createUrl(String s) {
        int start = s.indexOf("://");
        int end = s.indexOf("/", start + 3);
        return s.substring(0, end);
    }

    public record Settings(String url, String realm, String clientId) {}

    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String authServerUrl;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;

}