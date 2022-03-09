package io.openk9.api.aggregator.service;

import io.openk9.api.aggregator.model.Tenant;
import io.quarkus.oidc.OidcRequestContext;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.TenantConfigResolver;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CustomTenantConfigResolver implements TenantConfigResolver {

    @Override
    public Uni<OidcTenantConfig> resolve(
        RoutingContext context,
        OidcRequestContext<OidcTenantConfig> requestContext) {

        return createTenantConfig(context.request().host());
    }

    private Uni<OidcTenantConfig> createTenantConfig(String tenantName) {

        Tenant tenant = _tenantRegistry.getTenantNullable(tenantName);

        if (tenant == null) {
            logger.warn("tenant " + tenantName + " not found");
            return Uni.createFrom().nullItem();
        }

        logger.info("tenant: " + tenantName);

        final OidcTenantConfig config = new OidcTenantConfig();

        config.setTenantId(tenant.getRealmName());
        config.setAuthServerUrl(
            _createAuthServerUrl(tenant.getRealmName())
        );
        config.setClientId(tenant.getClientId());
        config.setApplicationType(OidcTenantConfig.ApplicationType.SERVICE);

        if (tenant.getClientSecret() != null && !tenant.getClientSecret().isBlank()) {
            OidcTenantConfig.Credentials credentials =
                new OidcTenantConfig.Credentials();
            credentials.setSecret(tenant.getClientSecret());
            config.setCredentials(credentials);
        }

        return Uni.createFrom().item(config);

    }

    private String _createAuthServerUrl(String realmName) {
        return authServerUrlTemplate
            .replace("{realm}", realmName);
    }

    @Inject
    Logger logger;

    @Inject
    TenantRegistry _tenantRegistry;

    @ConfigProperty(
        name = "openk9.authServerUrl.template"
    )
    String authServerUrlTemplate;

}