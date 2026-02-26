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
 * A {@link WebFilter} that find a {@code tenantId} from the
 * {@link org.springframework.http.server.reactive.ServerHttpRequest} URI
 * and inject that * as an {@code attribute} in the {@link ServerWebExchange}.
 */
@Slf4j
@RequiredArgsConstructor
public class TenantIdResolverFilter implements WebFilter {

	private static final String TENANT_ID = "tenantId";

	private final TenantSecurityService service;

	public static String getTenantId(ServerWebExchange exchange) {
		return exchange.getAttribute(TENANT_ID);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

		var uri = exchange.getRequest().getURI();
		var host = uri.getHost();

		log.info("Request host {}", host);

		return service.getTenantId(host)
			.doOnNext(tenantId -> {
				var attributes = exchange.getAttributes();

				if (log.isDebugEnabled()) {
					log.debug("Save tenantId: {} in attributes", tenantId);
				}

				attributes.put(TENANT_ID, tenantId);
			})
			.then(chain.filter(exchange));

	}

}
