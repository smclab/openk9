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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class MockOAuth2Configuration {

	@Bean
	ReactiveAuthenticationManagerResolver<ServerWebExchange> jwtAuthManagerResolver() {

		return exchange -> {
			ReactiveJwtDecoder jwtDecoder = token -> processNimbusJwt(exchange, token)
				.map(MockOAuth2Configuration::convertToSpringJwt);

			return Mono.just(new JwtReactiveAuthenticationManager(jwtDecoder));
		};
	}

	// Parses the jwt token, applies a mock verification, returns jwt parts
	private static Mono<JwtParts> processNimbusJwt(
		ServerWebExchange exchange, String token) {

		return Mono.defer(() -> {

			// Jwt parsing
			SignedJWT jwt;
			JWTClaimsSet jwtClaimSet;
			try {
				jwt = SignedJWT.parse(token);

				// verify that there is a valid JWTClaimSet
				jwtClaimSet = jwt.getJWTClaimsSet();
				if (jwtClaimSet == null) {
					return Mono.error(new JwtValidationException(
						"JWT Claims are not available",
						List.of(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN))
					));
				}
			}
			catch (ParseException e) {
				return Mono.error(new JwtValidationException(
					"Token cannot be parsed",
					List.of(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN))
				));
			}

			// Mock jwt verification!
			// This just verifies that the issuer contains,
			// the tenantId in its name.
			String tenantId = exchange.getAttribute("tenantId");
			String issuer = jwtClaimSet.getIssuer();
			if (tenantId == null || !issuer.contains(tenantId)) {
				return Mono.error(new JwtValidationException(
					"The tokenValue was signed from another issuer",
					List.of(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN))
				));
			}

			// return nimbus jwt parts as maps
			var jwsHeader = jwt.getHeader();
			Map<String, Object> headerMap = jwsHeader != null
				? new LinkedHashMap<>(jwsHeader.toJSONObject())
				: Map.of();
			var claimMap = jwtClaimSet.getClaims();

			return Mono.just(new JwtParts(token, headerMap, claimMap));
		});
	}

	// This function converts jwt parts, obtained from the processed SignedJWT,
	// into a Spring Security OAuth2 JWT.
	private static Jwt convertToSpringJwt(JwtParts jwtParts) {

		var headerMap = jwtParts.headerMap();
		var claimMap = NIMBUS_CLAIM_SET_CONVERTER.convert(jwtParts.claimMap());
		return Jwt.withTokenValue(jwtParts.tokenValue())
			.headers(headers -> headers.putAll(headerMap))
			.claims(claims -> claims.putAll(claimMap))
			.build();
	}

	private record JwtParts(
		String tokenValue, Map<String, Object> headerMap,
		Map<String, Object> claimMap) {}

	private static final MappedJwtClaimSetConverter NIMBUS_CLAIM_SET_CONVERTER =
		MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap());
}
