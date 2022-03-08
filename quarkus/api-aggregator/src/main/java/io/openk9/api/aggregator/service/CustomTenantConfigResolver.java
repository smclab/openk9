package io.openk9.api.aggregator.service;

import io.openk9.api.aggregator.model.Tenant;
import io.quarkus.oidc.OidcRequestContext;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.TenantConfigResolver;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
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

        logger.info("tenant: " + tenantName);

        Tenant tenant = _tenantRegistry.getTenantNullable(tenantName);

        if (tenant == null) {
            return Uni.createFrom().nullItem();
        }

        final OidcTenantConfig config = new OidcTenantConfig();

        config.setTenantId(tenant.getName());
        config.setAuthServerUrl(tenant.getAuthServerUrl());
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

    @Inject
    Logger logger;

    @Inject
    TenantRegistry _tenantRegistry;

}