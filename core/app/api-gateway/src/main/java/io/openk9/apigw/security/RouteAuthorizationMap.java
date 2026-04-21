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

/**
 * Per-tenant mapping from {@link ApiRoute} to the
 * {@link AuthorizationSchemeToken} that must be enforced on requests
 * reaching that route.
 * <p>
 * A tenant declares overrides (e.g. {@code SEARCHER → OAUTH2}) when
 * it wants a stricter policy than the workspace-wide defaults; any
 * route the tenant leaves unspecified falls back to the built-in
 * {@code FALLBACKS} table defined in this class. The fallbacks
 * encode the product-wide convention: the administrative
 * {@code DATASOURCE} route requires OAuth2, while search, ingestion,
 * RAG and tenant-lookup routes are public by default
 * ({@link AuthorizationSchemeToken#NO_AUTH}).
 * <p>
 * Instances are immutable and built via the factory methods
 * {@link #of()} (defaults only) and {@link #of(Map)} (with tenant
 * overrides merged over the defaults). The map is consumed by the
 * gateway authorization flow — in particular by
 * {@link RouteAuthorizationResolverFilter}, which resolves the
 * scheme for the current request and publishes it on the exchange
 * so downstream stages ({@code TenantSecurityServiceR2dbc},
 * {@code SelfSignedMPJwtGlobalPreFilter}) can act on it without a
 * second lookup.
 */
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

	/**
	 * Returns the {@link AuthorizationSchemeToken} configured for the
	 * given {@link ApiRoute}, falling back to the default mapping when
	 * the tenant has no override.
	 *
	 * @param apiRoute the route being accessed
	 * @return the authorization scheme for that route
	 */
	public AuthorizationSchemeToken schemeFor(ApiRoute apiRoute) {
		return tenantMappings.getOrDefault(apiRoute, FALLBACKS.get(apiRoute));
	}

}
