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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import jakarta.annotation.PostConstruct;

import io.openk9.apigw.security.RouteAuthorizationResolverFilter;
import io.openk9.apigw.security.AuthorizationSchemeToken;
import io.openk9.apigw.security.TenantIdResolverFilter;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Spring Cloud Gateway {@link GlobalFilter} that rewrites the outbound
 * {@code Authorization} header based on the Spring Security
 * {@link Authentication} present in the reactive context and on the
 * {@link AuthorizationSchemeToken} configured for the current route.
 * <p>
 * Trust boundary: this filter never parses the raw inbound
 * {@code Authorization} header. All identity claims come from the
 * {@link Authentication} that the {@code SecurityWebFilterChain} has
 * already validated (issuer, signature, audience, expiration).
 * <p>
 * On authenticated routes, only a {@link JwtAuthenticationToken}
 * produces an internal MP-JWT. Every other authentication (anonymous,
 * API key, etc.) causes the {@code Authorization} header to be
 * stripped before the request is forwarded downstream.
 */
@Slf4j
@Component
public class SelfSignedMPJwtGlobalPreFilter implements GlobalFilter {

	/**
	 * When {@code false} (default, production), any Authorization header
	 * that was not re-signed as an internal JWT is stripped before the
	 * request reaches downstream services.
	 * When {@code true} (local dev), non-re-signed Authorization headers
	 * (e.g. Basic) are preserved so downstream services can authenticate
	 * them directly.
	 */
	@Value("${io.openk9.apigw.passthrough-non-bearer-auth:false}")
	private boolean passthroughNonBearerAuth;

	private static OctetSequenceKey JWK;
	private static JWSSigner SIGNER;

	private static final String REALM_ACCESS = "realm_access";
	private static final String ROLES = "roles";
	private static final String PREFERRED_USERNAME = "preferred_username";

	// MP-JWT claims to be added in the self-signed JWT.
	private static final String UNIQUE_PRINCIPAL_NAME = "upn";
	private static final String GROUPS_PERMISSION_GRANT = "groups";
	private static final String ISSUED_AT = "iat";
	private static final String EXPIRATION = "exp";
	private static final String SUBJECT = "sub";

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
	public Mono<Void> filter(
		ServerWebExchange exchange,
		GatewayFilterChain chain) {

		AuthorizationSchemeToken scheme =
			RouteAuthorizationResolverFilter.getAuthorizationScheme(exchange);

		return ReactiveSecurityContextHolder.getContext()
			.map(SecurityContext::getAuthentication)
			.map(auth -> headerActionFor(scheme, auth, exchange))
			.flatMap(action -> chain.filter(exchange.mutate()
				.request(request -> request.headers(action).build())
				.build()));
	}

	private Consumer<HttpHeaders> headerActionFor(
		AuthorizationSchemeToken scheme,
		Authentication authentication,
		ServerWebExchange exchange) {

		// Mint an internal MP-JWT only when the route
		// scheme is OAUTH2 and the authentication is a validated
		// JwtAuthenticationToken.
		if (scheme == AuthorizationSchemeToken.OAUTH2
			&& authentication instanceof JwtAuthenticationToken jwtAuth) {

			String internal = createInternalTokenFromJwt(jwtAuth, exchange);
			if (internal != null) {
				return setBearer(internal);
			}
		}

		// Passthrough preserves non-Bearer schemes (e.g. Basic) for
		// direct downstream auth in local/Docker environments.
		// Bearer tokens are never passed through
		if (passthroughNonBearerAuth 
			&& !(authentication instanceof JwtAuthenticationToken)) {

			return noop();
		}

		return stripAuthorization();
	}

	private static Consumer<HttpHeaders> setBearer(String internalToken) {
		return headers -> headers.set(
			HttpHeaders.AUTHORIZATION, "Bearer " + internalToken);
	}

	private static Consumer<HttpHeaders> stripAuthorization() {
		return headers -> headers.remove(HttpHeaders.AUTHORIZATION);
	}

	private static Consumer<HttpHeaders> noop() {
		return headers -> {};
	}

	private static String createInternalTokenFromJwt(
		JwtAuthenticationToken jwtAuth, ServerWebExchange exchange) {

		Jwt jwt = jwtAuth.getToken();
		Map<String, Object> claims = jwt.getClaims();

		String sub = resolveSubject(claims);
		if (sub == null) {
			log.warn("The subject cannot be found in the validated JWT.");
			return null;
		}

		Set<String> aggregateGroups = new HashSet<>();
		addGroupsFromGroupsClaim(claims, aggregateGroups);
		addGroupsFromRealmAccessRolesClaim(claims, aggregateGroups);
		List<String> groups = new ArrayList<>(aggregateGroups);

		Date iat = toDate(claims.get(ISSUED_AT));
		Date exp = toDate(claims.get(EXPIRATION));
		String tenantId = TenantIdResolverFilter.getTenantId(exchange);

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

			SignedJWT signedJWT = new SignedJWT(header, claimsSet);
			signedJWT.sign(SIGNER);

			return signedJWT.serialize();
		}
		catch (JOSEException e) {
			log.warn("An error occurred while signing the internal JWT.", e);
			return null;
		}
	}

	// Referring the MP-JWT Specification:
	//
	// MP-JWT "upn" claim is the user principal name
	// in the java.security.Principal interface,
	// and is the caller principal name
	// in jakarta.security.enterprise.identitystore.IdentityStore.
	// If this claim is missing,
	// fallback to the "preferred_username",
	// OIDC Section 5.1 should be attempted,
	// and if that claim is missing,
	// fallback to the "sub" claim should be used.
	private static String resolveSubject(Map<String, Object> claims) {
		Object upn = claims.get(UNIQUE_PRINCIPAL_NAME);
		if (upn instanceof String s && !s.isEmpty()) {
			return s;
		}
		Object preferred = claims.get(PREFERRED_USERNAME);
		if (preferred instanceof String s && !s.isEmpty()) {
			return s;
		}
		Object sub = claims.get(SUBJECT);
		if (sub instanceof String s && !s.isEmpty()) {
			return s;
		}
		return null;
	}

	private static Date toDate(Object claim) {
		if (claim instanceof Date d) {
			return d;
		}
		if (claim instanceof java.time.Instant i) {
			return Date.from(i);
		}
		if (claim instanceof Number n) {
			return new Date(n.longValue() * 1000L);
		}
		return null;
	}

	private static void addGroupsFromRealmAccessRolesClaim(
		Map<String, Object> claims, Set<String> aggregateRoles) {

		Object realmAccessClaim = claims.get(REALM_ACCESS);
		if (!(realmAccessClaim instanceof Map<?, ?> realmAccess)) {
			return;
		}

		Object rolesObj = realmAccess.get(ROLES);
		if (rolesObj instanceof List<?> list) {
			for (Object item : list) {
				if (item instanceof String role) {
					aggregateRoles.add(role);
				}
			}
		}
	}

	private static void addGroupsFromGroupsClaim(
		Map<String, Object> claims, Set<String> aggregateRoles) {

		Object groupsClaim = claims.get(GROUPS_PERMISSION_GRANT);
		if (groupsClaim instanceof List<?> list) {
			for (Object item : list) {
				if (item instanceof String group) {
					aggregateRoles.add(group);
				}
			}
		}
	}

}
