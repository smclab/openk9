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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import jakarta.annotation.PostConstruct;

import io.openk9.apigw.security.TenantIdResolverFilter;

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
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SelfSignedMPJwtGlobalPreFilter implements GlobalFilter {

	private static final long ONE_DAY_MILLIS = 86_400_000L;
	private static OctetSequenceKey JWK;
	private static JWSSigner SIGNER;

	// MP-JWT claims to be added in the self-signed JWT.
	private static final String UNIQUE_PRINCIPAL_NAME = "upn";
	private static final String GROUPS_PERMISSION_GRANT = "groups";

	// MP-JWT Constant JWT claim values,
	// because they are related to the OpenK9 Api Gateway.
	private static final String ISSUER_VALUE = "openk9-gateway";
	private static final String AUDIENCE_VALUE = "openk9";

	// TenantId claim, useful to handle multi-tenancy in downstream services.
	private static final String TENANT_ID = "tenantId";

	@PostConstruct
	void init() {
		try {
			// Retrieves the JWK used to sign the JWT.
			// This key is shared across all services as a static resource in
			// the classpath, we don't need store it as a secret,
			// because we are routing request in a trusted zone.
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

		String subject = "anonymous";
		List<String> groups = List.of();
		Date issueTime = new Date();
		Date expirationTime = new Date(System.currentTimeMillis() + ONE_DAY_MILLIS);

		if (authorization != null && authorization.length() >= 7) {
			authScheme = authorization.substring(0, 7);
		}

		if (authScheme.equalsIgnoreCase("bearer ")) {
			String jwtString = authorization.substring(7);

			try {
				SignedJWT jwt = SignedJWT.parse(jwtString);
				JWTClaimsSet claims = jwt.getJWTClaimsSet();

				subject = claims.getSubject();
				issueTime = claims.getIssueTime();
				expirationTime = claims.getExpirationTime();

				// Aggregate groups from known claims used for listing roles.
				// In MP-JWT specification, "groups" is the claim suggested for
				// grouping permission grants.
				Set<String> aggregateGroups = new HashSet<>();
				addGroupsFromGroupsClaim(claims, aggregateGroups);
				addGroupsFromRealmAccessRolesClaim(claims, aggregateGroups);
				groups = new ArrayList<>(aggregateGroups);
			}
			catch (ParseException e) {
				log.warn("A error occurred while parsing jwt. Fallback with anonymous token", e);
			}

		}
		else if (authScheme.equalsIgnoreCase("apikey ")) {
			// TODO: API Key mapping needs to be implemented.
			// right now we can create a new jwt,
			// defining a subject related to API Keys.
			subject = "apiKeyUser";
		}

		String serializedToken = createInternalToken(
			subject, groups, issueTime, expirationTime, tenantId);

		return chain.filter(exchange.mutate()
			.request(request -> request
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + serializedToken))
			.build());
	}

	private static void addGroupsFromRealmAccessRolesClaim(
		JWTClaimsSet claims, Set<String> aggregateRoles)
		throws ParseException {

		Map<String, Object> realmAccessClaim = claims.getJSONObjectClaim("realmAccess");
		if (realmAccessClaim == null) {
			return;
		}

		Object rolesObj = realmAccessClaim.get("roles");
		if (rolesObj instanceof List<?> list) {
			for (Object item : list) {
				if (item instanceof String role) {
					aggregateRoles.add(role);
				}
			}
		}
	}

	private static void addGroupsFromGroupsClaim(
		JWTClaimsSet claims, Set<String> aggregateRoles)
		throws ParseException {

		List<String> groupsClaim = claims.getStringListClaim("groups");
		if (groupsClaim != null) {
			aggregateRoles.addAll(groupsClaim);
		}
	}

	public static String createInternalToken(
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
				.jwtID(UUID.randomUUID().toString())
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
