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

package io.openk9.apigw.filter;

import io.openk9.apigw.security.TenantIdResolverFilter;
import io.openk9.common.util.web.InternalHeaders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * A {@link GlobalFilter} that injects the header {@link InternalHeaders#TENANT_ID}
 * to the request towards the downstream service.
 */
@Slf4j
@Component
public class TenantIdHeaderGlobalPreFilter implements GlobalFilter {

	@Override
	public Mono<Void> filter(
		ServerWebExchange exchange, GatewayFilterChain chain) {

		String tenantId = TenantIdResolverFilter.getTenantId(exchange);

		ServerHttpRequest request = exchange.getRequest().mutate()
			.header(InternalHeaders.TENANT_ID, tenantId)
			.build();

		if (log.isDebugEnabled()) {
			log.debug(
				"Setting internalHeader: {} value to {} for request with id {}",
				InternalHeaders.TENANT_ID, tenantId, request.getId());
		}

		return chain.filter(exchange.mutate().request(request).build());
	}
}
