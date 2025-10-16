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

package io.openk9.experimental.spring_apigw_sample.security;

import java.util.List;

import io.openk9.experimental.spring_apigw_sample.security.apikey.ApiKeyAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Service interface for managing and querying multi-tenant security configurations.
 * <p>
 * This service provides reactive methods to:
 * <ul>
 *     <li>Verify route-based authorization for a given authentication token.</li>
 *     <li>Retrieve issuer URIs for tenants (useful for OAuth2/OIDC flows).</li>
 *     <li>Query API key permissions for tenants.</li>
 *     <li>Resolve tenant IDs based on hostnames.</li>
 * </ul>
 * <p>
 * Used by the {@link org.springframework.web.server.WebFilter}s
 * involved in the {@link org.springframework.security.web.server.SecurityWebFilterChain},
 * it helps request authentication.
 */
public interface TenantSecurityService {

	/**
	 * Determines whether a given authentication is authorized to access the current request
	 * path in the specified server exchange.
	 *
	 * @param authentication a {@link Mono} emitting the {@link Authentication} to check; if empty,
	 *                       it will be treated as anonymous or no-auth.
	 * @param exchange       the {@link ServerWebExchange} representing the current request,
	 *                       used to resolve the tenant and route.
	 * @return a {@link Mono} emitting {@code true} if the authentication is authorized for
	 *         the route, {@code false} otherwise.
	 */
	Mono<Boolean> isAuthorized(Mono<Authentication> authentication, ServerWebExchange exchange);

	/**
	 * Retrieves the issuer URI associated with the tenant of the current request.
	 * <p>
	 * Typically used in OAuth2/OpenID Connect flows to obtain the authorization server URI.
	 *
	 * @param exchange the {@link ServerWebExchange} representing the current request.
	 * @return a {@link Mono} emitting the issuer URI as a {@link String}, or empty if
	 *         no issuer is configured for the tenant.
	 */
	Mono<String> getIssuerUri(ServerWebExchange exchange);

	/**
	 * Retrieves the permission roles associated with a given API key for a tenant.
	 * <p>
	 * Typically used to determine what operations a given API key is allowed to perform.
	 *
	 * @param apiKeyToken the {@link ApiKeyAuthenticationToken} containing the tenant ID
	 *                    and API key to check.
	 * @return a {@link Mono} emitting a {@link List} of permission roles (e.g., "ADMIN"),
	 *         or empty if the API key is invalid or not associated with the tenant.
	 */
	Mono<List<String>> getApiKeyPermission(ApiKeyAuthenticationToken apiKeyToken);

	/**
	 * Resolves a tenant ID based on a given host name.
	 * <p>
	 * Useful in multi-tenant applications where the host name identifies the tenant.
	 *
	 * @param hostName the host name of the incoming request (e.g., "alabasta.localhost").
	 * @return a {@link Mono} emitting the tenant ID as a {@link String}, or empty if
	 *         no tenant matches the given host name.
	 */
	Mono<String> getTenantId(String hostName);

	/**
	 * Resolves a tenant based on a given host name.
	 * <p>
	 * Useful in multi-tenant applications where the host name identifies the tenant.
	 *
	 * @param tenantId the id of the registered tenant (e.g., "alabasta").
	 * @return a {@link Mono} emitting the {@link Tenant}, or empty if
	 *         no tenant matches the given host name.
	 */
	Mono<Tenant> getTenantAggregate(String tenantId);
}
