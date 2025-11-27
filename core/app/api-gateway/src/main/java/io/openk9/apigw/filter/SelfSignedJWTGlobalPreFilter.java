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
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import jakarta.annotation.PostConstruct;

import io.openk9.apigw.security.TenantIdResolverFilter;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
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

	private static OctetSequenceKey JWK;
	private static JWSSigner SIGNER;
	private static final String UNIQUE_PRINCIPAL_NAME = "upn";
	private static final String GROUPS_PERMISSION_GRANT = "groups";
	private static final String ISSUER_VALUE = "openk9-gateway";
	private static final String AUDIENCE_VALUE = "openk9";
	private static final String TENANT_ID = "tenantId";

	@PostConstruct
	void init() {
		try {
			ResourceLoader resourceLoader = new DefaultResourceLoader();
			Resource jwkResource = resourceLoader.getResource("classpath:gateway.jwk");
			String jwkJsonString = jwkResource.getContentAsString(StandardCharsets.UTF_8);
			JWK = OctetSequenceKey.parse(jwkJsonString);
			SIGNER = new MACSigner(JWK);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		HttpHeaders headers = exchange.getRequest().getHeaders();
		String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);

		String tenantId = TenantIdResolverFilter.getTenantId(exchange);
		String authScheme = "";
		String token = createToken(
			"anonymous",
			List.of(),
			new Date(),
			new Date(Long.MAX_VALUE),
			tenantId
		);

		if (authorization != null) {
			authScheme = authorization.substring(0, 7);
		}

		if (authScheme.equalsIgnoreCase("bearer ")) {
			String jwtString = authorization.substring(7);
			try {
				SignedJWT jwt = SignedJWT.parse(jwtString);

				JWTClaimsSet claims = jwt.getJWTClaimsSet();

				String subject = claims.getSubject();
				List<String> groups = claims.getStringListClaim("groups");
				Date issueTime = claims.getIssueTime();
				Date expirationTime = claims.getExpirationTime();

				token = createToken(
					subject, groups, issueTime, expirationTime, tenantId);
			}
			catch (ParseException e) {
				throw new RuntimeException(e);
			}

		}
		else if (authScheme.equalsIgnoreCase("apikey ")) {
			String apiKey = authorization.substring(7);
			token = createToken(
				"system",
				List.of("k9-admin"),
				new Date(),
				new Date(Long.MAX_VALUE),
				tenantId
			);
		}

		ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
			.header(HttpHeaders.AUTHORIZATION, "bearer " + token)
			.build();

		return chain.filter(exchange.mutate().request(mutatedRequest).build());
	}

	public static String createToken(
		String sub, List<String> groups, Date iat, Date exp, String tenantId) {

		try {
			JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
				.keyID(JWK.getKeyID())
				.build();

			JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.subject(sub)
				.claim(UNIQUE_PRINCIPAL_NAME, sub)
				.issuer(ISSUER_VALUE)
				.audience(AUDIENCE_VALUE)
				.claim(GROUPS_PERMISSION_GRANT, groups)
				.issueTime(iat)
				.expirationTime(exp)
				.claim(TENANT_ID, tenantId)
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
