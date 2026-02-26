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

package io.openk9.apigw.security.apikey;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * A specialized version of the {@link AuthenticationWebFilter} for the
 * {@code ApiKey} credential scheme, that use the same logic.
 */
@Slf4j
public class ApiKeyAuthenticationFilter extends AuthenticationWebFilter {

	private static final ApiKeyAuthenticationConverter AUTHENTICATION_CONVERTER =
		new ApiKeyAuthenticationConverter();

	public ApiKeyAuthenticationFilter(ReactiveAuthenticationManager authenticationManager) {

		super(authenticationManager);
		setServerAuthenticationConverter(AUTHENTICATION_CONVERTER);

	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

		return super.filter(exchange, chain)
				.onErrorResume(
					ApiKeyMalformedException.class, exception -> {
					log.warn(exception.getMessage());

					ServerHttpResponse response = exchange.getResponse();
					response.setStatusCode(HttpStatus.BAD_REQUEST);
					return response.writeWith(Mono.just(response.bufferFactory().allocateBuffer(0)));
			});
	}
}
