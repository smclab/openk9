package io.openk9.datasource.web;

import io.quarkus.oidc.OidcTenantConfig;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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

    private String _createUrl(Optional<String> authServerUrl) {
        return authServerUrl
            .map(s -> s.substring(0, s.lastIndexOf("/")))
            .orElse("");
    }

    public record Settings(String url, String realm, String clientId) {}

}