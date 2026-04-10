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

package io.openk9.apigw.security;

import java.util.EnumMap;
import java.util.Map;

import io.openk9.apigw.security.apikey.ApiKeyAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public final class RouteAuthorizationMap {

	private final EnumMap<ApiRoute, AuthorizationSchemeToken> tenantMappings;
	private static final EnumMap<ApiRoute, AuthorizationSchemeToken> FALLBACKS = new EnumMap<>(
		ApiRoute.class);

	static {
		for (ApiRoute r : ApiRoute.values()) {
			var enforcer = switch (r) {
				case ANY,
				     DATASOURCE_OAUTH2_SETTINGS,
				     DATASOURCE_OAUTH2_SETTINGS_JS,
				     DATASOURCE_CURRENT_BUCKET,
				     DATASOURCE_TEMPLATES,
				     SEARCHER,
				     RAG -> 
					FALLBACKS.put(r, AuthorizationSchemeToken.NO_AUTH);
				case DATASOURCE -> 
					FALLBACKS.put(r, AuthorizationSchemeToken.OAUTH2);
				case INGESTION,
				     DATASOURCE_PIPELINE_CALLBACK ->
					FALLBACKS.put(r, AuthorizationSchemeToken.API_KEY);
				// no default case to prevent accidental omissions at compile-time.
			};
		}
	}

	private RouteAuthorizationMap(Map<ApiRoute, AuthorizationSchemeToken> tenantMappings) {
		this.tenantMappings = new EnumMap<>(tenantMappings);
	}

	/**
	 * Creates a {@link RouteAuthorizationMap} using only the
	 * default fallback mappings.
	 *
	 * @return a new map with fallback authorization schemes
	 */
	public static RouteAuthorizationMap of() {
		return new RouteAuthorizationMap(FALLBACKS);
	}

	/**
	 * Creates a {@link RouteAuthorizationMap} with tenant-specific
	 * route authorization overrides.
	 *
	 * @param tenantMappings route-to-scheme overrides
	 * @return a new map merging overrides with fallbacks
	 */
	public static RouteAuthorizationMap of(
		Map<ApiRoute, AuthorizationSchemeToken> tenantMappings) {

		return new RouteAuthorizationMap(tenantMappings);
	}

	private AuthorizationSchemeToken schemeFor(ApiRoute apiRoute) {
		return tenantMappings.getOrDefault(apiRoute, FALLBACKS.get(apiRoute));
	}

	/**
	 * Checks whether the given authentication is allowed to access
	 * the specified route.
	 *
	 * @param apiRoute the route being accessed
	 * @param authentication the current authentication token
	 * @return {@code true} if access is allowed
	 */
	public boolean allows(
		ApiRoute apiRoute, Authentication authentication) {
		AuthorizationSchemeToken authSchemeToken = schemeFor(apiRoute);

		// allows access when authScheme not defined or explicitly not required
		if (authSchemeToken == null
			|| authSchemeToken == AuthorizationSchemeToken.NO_AUTH) {

			return true;
		}

		// allows access when required authentication match
		if (!authSchemeToken.match(authentication.getClass())) {
			return false;
		}

		// for API key authentication, verify the key's ApiGroup
		// covers the requested route
		if (authentication instanceof ApiKeyAuthenticationToken) {
			String requiredAuthority = "ROUTE_" + apiRoute.name();

			return authentication.getAuthorities()
				.stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch(requiredAuthority::equals);
		}

		return true;
	}

}
