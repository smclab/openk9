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

package io.openk9.experimental.spring_apigw_sample.security.oauth2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.AuthenticationException;

import io.openk9.experimental.spring_apigw_sample.security.TenantSecurityService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class TenantJwtAuthenticationManagerResolver
	implements ReactiveAuthenticationManagerResolver<ServerWebExchange> {

	private final TenantSecurityService service;
	private final Map<String, ReactiveAuthenticationManager> authenticationManagers =
		new ConcurrentHashMap<>();

	@Override
	public Mono<ReactiveAuthenticationManager> resolve(ServerWebExchange exchange) {

		return service
			.getIssuerUri(exchange)
			.map(issuerUri -> authenticationManagers
				.computeIfAbsent(
					issuerUri,
					TenantJwtAuthenticationManagerResolver::fromIssuerLocation)
			)
			.defaultIfEmpty(denied());
	}

	private static ReactiveAuthenticationManager fromIssuerLocation(String issuer) {
		return new JwtReactiveAuthenticationManager(
			ReactiveJwtDecoders.fromIssuerLocation(issuer));
	}

	private static ReactiveAuthenticationManager denied() {
		return authentication -> Mono.error(AuthenticationException::new);
	}
}
