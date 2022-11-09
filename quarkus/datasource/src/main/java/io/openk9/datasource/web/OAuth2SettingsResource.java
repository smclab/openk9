package io.openk9.datasource.web;

import io.quarkus.oidc.OidcTenantConfig;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

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
            oidcConfig.getAuthServerUrl().orElse(""),
            oidcConfig.getClientId().orElse(""),
            oidcConfig.getTenantId().orElse("")
        ));

    }

    public record Settings(String issuer, String clientId, String tenantId) {}

}