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

package io.openk9.auth.resolver;

import io.openk9.auth.tenant.TenantRegistry;
import io.quarkus.oidc.OidcRequestContext;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.TenantConfigResolver;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class OIDCTenantResolver implements TenantConfigResolver {

    @Override
    public Uni<OidcTenantConfig> resolve(
        RoutingContext context,
        OidcRequestContext<OidcTenantConfig> requestContext) {

        return createTenantConfig(context);
    }

    private Uni<OidcTenantConfig> createTenantConfig(RoutingContext routingContext) {

        String tenantName = routingContext.request().host();

        return tenantRegistry
            .getTenantByVirtualHost(tenantName)
            .map(tenant -> {

                if (tenant == null ) {
                    logger.warn("tenant " + tenantName + " not found");
                    return null;
                }

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

                routingContext.put("_tenantId", tenant.schemaName());

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

    @ConfigProperty(
        name = "openk9.authServerUrl.template"
    )
    String authServerUrlTemplate;

}