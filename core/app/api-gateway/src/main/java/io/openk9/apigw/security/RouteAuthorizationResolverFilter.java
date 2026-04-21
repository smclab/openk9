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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * A {@link WebFilter} that resolves the {@link ApiRoute} and its
 * {@link AuthorizationSchemeToken} for the current request and
 * stashes them as exchange attributes, so later stages (authorization
 * manager, {@code SelfSignedMPJwtGlobalPreFilter}) can consume the
 * decisions without going back through the {@link TenantSecurityService}.
 * <p>
 * Mirrors the pattern of {@link TenantIdResolverFilter}: the filter
 * owns the attribute keys and exposes static accessors.
 */
@Slf4j
@RequiredArgsConstructor
public class RouteAuthorizationResolverFilter implements WebFilter {

	private static final String API_ROUTE = "apiRoute";
	private static final String AUTHORIZATION_SCHEME = "authorizationScheme";

	private final TenantSecurityService service;

	public static ApiRoute getApiRoute(ServerWebExchange exchange) {
		return exchange.getAttribute(API_ROUTE);
	}

	public static AuthorizationSchemeToken getAuthorizationScheme(
		ServerWebExchange exchange) {

		return exchange.getAttribute(AUTHORIZATION_SCHEME);
	}

	@Override
	public Mono<Void> filter(
		ServerWebExchange exchange, WebFilterChain chain) {

		String tenantId = TenantIdResolverFilter.getTenantId(exchange);
		if (tenantId == null) {
			// skip the scheme resolution 
			return chain.filter(exchange);
		}

		ApiRoute route;
		try {
			route = ApiRoute.matchOf(
				exchange.getRequest().getPath().value());
		}
		catch (IllegalArgumentException e) {
			// skip the scheme resolution 
			return chain.filter(exchange);
		}

		return service.getTenantAggregate(tenantId)
			.doOnNext(tenant -> {
				var scheme = tenant.routeAuthorizationMap()
					.schemeFor(route);
				var attributes = exchange.getAttributes();
				attributes.put(API_ROUTE, route);
				attributes.put(AUTHORIZATION_SCHEME, scheme);

				if (log.isDebugEnabled()) {
					log.debug(
						"Resolved route {} with scheme {} for tenant {}",
						route, scheme, tenantId);
				}
			})
			.then(chain.filter(exchange));
	}

}
