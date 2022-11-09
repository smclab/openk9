package io.openk9.datasource.multitenancy.resolver;

import io.openk9.datasource.multitenancy.TenantRegistry;
import io.openk9.datasource.tenant.TenantResolver;
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
public class OIDCTenantResolver implements TenantConfigResolver {

    @Override
    public Uni<OidcTenantConfig> resolve(
        RoutingContext context,
        OidcRequestContext<OidcTenantConfig> requestContext) {

        return createTenantConfig(context.request().host());
    }

    private Uni<OidcTenantConfig> createTenantConfig(String tenantName) {

        return tenantRegistry
            .getTenantByVirtualHost(tenantName)
            .map(tenant -> {

                if (tenant == null ) {
                    logger.warn("tenant " + tenantName + " not found");
                    return null;
                }

                logger.info("tenant: " + tenantName);

                final OidcTenantConfig config = new OidcTenantConfig();

                config.setTenantId(tenant.realmName());
                config.setDiscoveryEnabled(true);
                config.setApplicationType(
                    OidcTenantConfig.ApplicationType.HYBRID);
                config.setAuthServerUrl(
                    _createAuthServerUrl(tenant.realmName())
                );
                config.setClientId(tenant.clientId());
                config.setApplicationType(OidcTenantConfig.ApplicationType.SERVICE);

                if (tenant.clientSecret() != null && !tenant.clientSecret().isBlank()) {
                    OidcTenantConfig.Credentials credentials =
                        new OidcTenantConfig.Credentials();
                    credentials.setSecret(tenant.clientSecret());
                    config.setCredentials(credentials);
                }

                tenantResolver.setTenant(tenant.schemaName());

                return config;

            });

    }

    private String _createAuthServerUrl(String realmName) {
        return authServerUrlTemplate
            .replace("{realm}", realmName);
    }

    @Inject
    Logger logger;

    @Inject
    TenantRegistry tenantRegistry;

    @Inject
    TenantResolver tenantResolver;

    @ConfigProperty(
        name = "openk9.authServerUrl.template"
    )
    String authServerUrlTemplate;

}