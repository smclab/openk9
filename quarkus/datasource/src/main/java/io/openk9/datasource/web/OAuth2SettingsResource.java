package io.openk9.datasource.web;

import io.quarkus.oidc.OidcConfigurationMetadata;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/oauth2")
public class OAuth2SettingsResource {

    @Inject
    OidcConfigurationMetadata configMetadata;

    @GET
    @Path("/settings")
    public OidcConfigurationMetadata settings() {
        return configMetadata;
    }

}