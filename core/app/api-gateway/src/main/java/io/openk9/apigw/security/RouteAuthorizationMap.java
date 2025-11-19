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

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class RouteAuthorizationMap {

	private final EnumMap<RoutePath, AuthorizationSchemeToken> tenantMappings;
	private static final EnumMap<RoutePath, AuthorizationSchemeToken> FALLBACKS = new EnumMap<>(
		RoutePath.class);

	static {
		for (RoutePath r : RoutePath.values()) {
			Object ignored = switch (r) {
				case ANY,
					 DATASOURCE_OAUTH2_SETTINGS,
					 DATASOURCE_PUBLIC_CONFIGS,
					 SEARCHER -> FALLBACKS.put(r, AuthorizationSchemeToken.NO_AUTH);
				case DATASOURCE -> FALLBACKS.put(r, AuthorizationSchemeToken.OAUTH2);
			};
		}
	}

	private RouteAuthorizationMap(Map<RoutePath, AuthorizationSchemeToken> tenantMappings) {
		this.tenantMappings = new EnumMap<>(tenantMappings);
	}

	public static RouteAuthorizationMap of() {
		return new RouteAuthorizationMap(FALLBACKS);
	}

	public static RouteAuthorizationMap of(Map<RoutePath, AuthorizationSchemeToken> tenantMappings) {
		return new RouteAuthorizationMap(tenantMappings);
	}

	private AuthorizationSchemeToken schemeFor(RoutePath routePath) {
		return tenantMappings.getOrDefault(routePath, FALLBACKS.get(routePath));
	}

	public boolean allows(RoutePath routePath, Authentication authentication) {
		AuthorizationSchemeToken authSchemeToken = schemeFor(routePath);

		// allows access when authScheme not defined or explicitly not required
		if (authSchemeToken == null
			|| authSchemeToken == AuthorizationSchemeToken.NO_AUTH) {

			return true;
		}

		// disallows when required authentication doesn't match
		if (!authSchemeToken.match(authentication.getClass())) {
			return false;
		}

		// verify that the provided JwtAuthenticationToken contains
		// the k9-admin role.
		// TODO: this kind of verification can be improved a lot.
		// 		Maybe could be delegated directly to datasource.
		//  	Right now this is a workaround to get the claims from
		//		a keycloak jwt token, that put user roles in the
		// 		claim realm_access.roles.
		if (routePath == RoutePath.DATASOURCE
			&& authSchemeToken == AuthorizationSchemeToken.OAUTH2) {

			JwtAuthenticationToken jwtAuthToken = (JwtAuthenticationToken)authentication;
			Map<String, Object> claims = jwtAuthToken.getTokenAttributes();

			// if realmAccess.roles list can't be obtained,
			// authorization is negated.

			Map<String, Object> realmAccess = null;
			Object realmAccessObj = claims.get("realm_access");
			if (realmAccessObj instanceof Map map) {
				realmAccess = (Map<String, Object>) map;
			}
			else {
				return false;
			}

			Collection<String> roles = null;
			Object rolesObj = realmAccess.get("roles");
			if (rolesObj instanceof Collection collection) {
				roles = (Collection<String>)rolesObj;
			}
			else {
				return false;
			}

			return roles.contains("k9-admin");

		}

		return true;
	}
}
