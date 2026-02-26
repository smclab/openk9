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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * A {@link WebFilter} that verify the correctness of the
 * <a href="https://datatracker.ietf.org/doc/html/rfc7235#section-4.2" target="_blank">Authorization header</a> values.
 */
public class AuthorizationHeaderFilter implements WebFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

		var request = exchange.getRequest();
		var headers = request.getHeaders();

		var authorizations = headers.get(HttpHeaders.AUTHORIZATION);

		// request cannot contain more than one authorization scheme
		if (authorizations != null && authorizations.size() != 1) {
			var response = exchange.getResponse();
			response.setStatusCode(HttpStatus.BAD_REQUEST);
			return Mono.empty();
		}

		return chain.filter(exchange);
	}

}
