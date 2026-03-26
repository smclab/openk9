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

package io.openk9.datasource.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import io.openk9.common.util.security.SecurityProperties;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Quarkus Security {@link IdentityProvider} that validates HTTP Basic
 * Authentication credentials against a configured admin password.
 * <p>
 * Basic Auth is <strong>enabled</strong> only when the
 * {@value SecurityProperties#ADMIN_PASSWORD} property is set to a
 * non-blank value. When the property is absent or blank, every
 * Basic Auth attempt is rejected with
 * {@link AuthenticationFailedException}, effectively disabling
 * this mechanism at runtime without requiring a separate feature flag.
 * <p>
 * This provides direct admin access to the datasource service for
 * simple installations, port-forwarding, and no-Keycloak setups.
 * In production with Keycloak, the API Gateway JWT flow coexists
 * alongside this mechanism via the {@code %prod} OIDC policies.
 * <p>
 * The authenticated identity is granted the {@code k9-admin} role,
 * so it is covered by the same {@code administrators} HTTP auth
 * policy used for OIDC-authenticated requests.
 */
@ApplicationScoped
public class AdminIdentityProvider
	implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

	@ConfigProperty(name = SecurityProperties.ADMIN_PASSWORD)
	Optional<String> passwordProperty;

	private boolean enabled;
	private byte[] adminPasswordBytes;

	/**
	 * Authenticates a Basic Auth request against the configured
	 * admin credentials. Returns a failure if Basic Auth is
	 * disabled or if the credentials are invalid.
	 */
	@Override
	public Uni<SecurityIdentity> authenticate(
		UsernamePasswordAuthenticationRequest request,
		AuthenticationRequestContext context) {

		if (!enabled) {
			return Uni.createFrom().failure(
				new AuthenticationFailedException(
					"Basic authentication is disabled: "
					+ SecurityProperties.ADMIN_PASSWORD
					+ " is not configured"));
		}

		if (!"admin".equals(request.getUsername())) {
			return Uni.createFrom().failure(
				new AuthenticationFailedException("Invalid credentials"));
		}

		byte[] actual = new String(request.getPassword().getPassword())
			.getBytes(StandardCharsets.UTF_8);

		if (!MessageDigest.isEqual(adminPasswordBytes, actual)) {
			return Uni.createFrom().failure(
				new AuthenticationFailedException("Invalid credentials"));
		}

		return Uni.createFrom().item(
			QuarkusSecurityIdentity.builder()
				.setPrincipal(new QuarkusPrincipal("admin"))
				.addRole("k9-admin")
				.build());
	}

	/** {@inheritDoc} */
	@Override
	public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
		return UsernamePasswordAuthenticationRequest.class;
	}

	@PostConstruct
	void init() {
		String passwordValue = passwordProperty.orElse("");
		this.enabled = !passwordValue.isBlank();
		this.adminPasswordBytes = enabled
			? passwordValue.getBytes(StandardCharsets.UTF_8)
			: new byte[0];
	}

}
