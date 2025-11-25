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

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SelfSignedJWTGlobalPreFilter implements GlobalFilter {

	private static final OctetSequenceKey JWK;
	private static final JWSSigner SIGNER;
	static {
		try {
			ResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();
			Resource JWK_RESOURCE = RESOURCE_LOADER.getResource("classpath:gateway.jwk");
			String JWK_JSON_STRING = JWK_RESOURCE.getContentAsString(StandardCharsets.UTF_8);
			JWK = OctetSequenceKey.parse(JWK_JSON_STRING);
			SIGNER = new MACSigner(JWK);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		String token = createToken();
		ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
			.header(HttpHeaders.AUTHORIZATION, "bearer " + token)
			.build();

		return chain.filter(exchange.mutate().request(mutatedRequest).build());
	}

	public String createToken() {

		try {
			JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
				.keyID(JWK.getKeyID())
				.build();

			JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.subject("admin")
				.issuer("openk9-gateway")
				.claim("upn", "admin")
				.claim("groups", List.of("k9-admin"))
				.expirationTime(new Date(Long.MAX_VALUE))
				.issueTime(new Date())
				.build();

			SignedJWT signedJWT = new SignedJWT(
				header,
				claimsSet
			);

			signedJWT.sign(SIGNER);

			return signedJWT.serialize();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
