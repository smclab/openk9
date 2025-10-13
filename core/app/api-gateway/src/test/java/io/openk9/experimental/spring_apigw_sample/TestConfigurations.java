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

package io.openk9.experimental.spring_apigw_sample;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class TestConfigurations {

	@Bean
	@Profile("test")
	ReactiveAuthenticationManagerResolver<ServerWebExchange> jwtAuthManagerResolver() {

		return exchange -> {

			String tenantId = exchange.getAttribute("tenantId");

			ReactiveJwtDecoder jwtDecoder = token -> Mono.defer(() -> {

					if (token == null) {
						return Mono.error(new JwtValidationException(
							"empty token",
							List.of(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN))
						));
					}

					if (token.toLowerCase().contains("invalid")) {

						return Mono.error(new JwtValidationException(
							"invalid token",
							List.of(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN))
						));
					}

					if (tenantId == null || !token.contains(tenantId)) {
						return Mono.error(new JwtValidationException(
							"the issuer isn't known for this tenant",
							List.of(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN))
						));
					}

					// todo: create a more dynamic token authentication
					return Mono.just(Jwt.withTokenValue(token)
						.issuer("noop://" + tenantId + ".issuer/")
						.subject("cobra")
						.claim("scope", "admin user reader")
						.header("typ", "JWT")
						.header("alg", "HS256")
						.build());
				}

			);

			return Mono.just(new JwtReactiveAuthenticationManager(jwtDecoder));
		};

	}

}
