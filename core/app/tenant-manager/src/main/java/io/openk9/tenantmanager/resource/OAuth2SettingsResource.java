package io.openk9.tenantmanager.resource;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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