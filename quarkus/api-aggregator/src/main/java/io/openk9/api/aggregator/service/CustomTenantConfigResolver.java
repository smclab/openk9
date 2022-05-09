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