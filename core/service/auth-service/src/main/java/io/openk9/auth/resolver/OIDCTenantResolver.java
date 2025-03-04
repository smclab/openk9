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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.auth.tenant.TenantRegistry;

import io.quarkus.oidc.OidcRequestContext;
import io.quarkus.oidc.OidcTenantConfig;
import io.quarkus.oidc.OidcTenantConfigBuilder;
import io.quarkus.oidc.TenantConfigResolver;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class OIDCTenantResolver implements TenantConfigResolver {

	@Inject
	Logger logger;

	@Inject
	TenantRegistry tenantRegistry;

	@ConfigProperty(name = "openk9.authServerUrl.template")
	String authServerUrlTemplate;

	@Override
	public Uni<OidcTenantConfig> resolve(
		RoutingContext context,
		OidcRequestContext<OidcTenantConfig> requestContext) {
		return createTenantConfig(context);
	}

	private String _createAuthServerUrl(String realmName) {
		return authServerUrlTemplate.replace("{realm}", realmName);
	}

	private Uni<OidcTenantConfig> createTenantConfig(RoutingContext routingContext) {
		String virtualHost = routingContext.request().authority().host();
		return tenantRegistry
			.getTenantByVirtualHost(virtualHost)
			.map(tenant -> {
				if (tenant == null) {
					logger.warn("tenant " + virtualHost + " not found");
					return null;
				}

				OidcTenantConfigBuilder config = OidcTenantConfig.builder()
					.tenantId(tenant.realmName())
					.discoveryEnabled(true)
					.applicationType(io.quarkus.oidc.runtime.OidcTenantConfig.ApplicationType.SERVICE)
					.authServerUrl(_createAuthServerUrl(tenant.realmName()))
					.clientId(tenant.clientId());

				// Adding credentials if client secret is present
				if (tenant.clientSecret() != null && !tenant.clientSecret().isBlank()) {
					config.credentials(tenant.clientSecret());
				}


				routingContext.put("_tenantId", tenant.schemaName());
				return config.build();
			});
	}

}