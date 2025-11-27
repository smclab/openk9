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

package io.openk9.apigw.mock;

import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class MockOAuth2Configuration {

	@Bean
	ReactiveAuthenticationManagerResolver<ServerWebExchange> jwtAuthManagerResolver() {

		return exchange -> {

			String tenantId = exchange.getAttribute("tenantId");

			ReactiveJwtDecoder jwtDecoder = token -> Mono.defer(() -> {

				SignedJWT jwt;
				JWSHeader jwtHeader;
				JWTClaimsSet jwtClaimSet;
				String issuer;
				try {
					jwt = SignedJWT.parse(token);
					jwtHeader = jwt.getHeader();
					jwtClaimSet = jwt.getJWTClaimsSet();
					issuer = jwtClaimSet.getIssuer();
				}
				catch (ParseException e) {
					return Mono.error(new JwtValidationException(
						"Token cannot be parsed",
						List.of(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN))
					));
				}

				if (tenantId == null || !issuer.contains(tenantId)) {

					return Mono.error(new JwtValidationException(
						"The token was signed from another issuer",
						List.of(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN))
					));
				}

				Map<String, Object> headerMap = jwtHeader.toJSONObject();
				Map<String, Object> claimMap = jwtClaimSet.getClaims();
				return Mono.just(Jwt.withTokenValue(token)
					.headers(headers -> headers.putAll(headerMap))
					.claims(claims -> claims.putAll(claimMap))
					.issuedAt(Instant.now())
					.expiresAt(Instant.MAX)
					.build());
			});

			return Mono.just(new JwtReactiveAuthenticationManager(jwtDecoder));
		};

	}

}
