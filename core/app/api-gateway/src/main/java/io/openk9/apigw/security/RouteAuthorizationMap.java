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

import org.springframework.security.core.Authentication;

public final class RouteAuthorizationMap {

	private final EnumMap<RoutePath, AuthorizationSchemeToken> tenantMappings;
	private static final EnumMap<RoutePath, AuthorizationSchemeToken> FALLBACKS = new EnumMap<>(
		RoutePath.class);

	static {
		for (RoutePath r : RoutePath.values()) {
			switch (r) {
				case ANY, SEARCHER -> FALLBACKS.put(r, AuthorizationSchemeToken.NO_AUTH);
				case DATASOURCE -> FALLBACKS.put(r, AuthorizationSchemeToken.OAUTH2);
			}
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
		AuthorizationSchemeToken required = schemeFor(routePath);
		return required == null || required.match(authentication.getClass());
	}
}
