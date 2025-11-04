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

package io.openk9.tenantmanager.security;

import java.security.MessageDigest;
import java.security.Principal;
import java.util.function.Supplier;
import javax.security.auth.Subject;
import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.credential.PasswordCredential;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AdminPasswordIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

	private static final String ADMIN_USERNAME = "admin";
	private static final String ADMIN_ROLE = "admin";

	@ConfigProperty(name = "io.openk9.tenantmanager.security.admin.password")
	String adminPassword;

	@Override
	public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
		return UsernamePasswordAuthenticationRequest.class;
	}

	@Override
	public Uni<SecurityIdentity> authenticate(
		UsernamePasswordAuthenticationRequest request,
		AuthenticationRequestContext context) {

		return context.runBlocking(new Supplier<SecurityIdentity>() {
			@Override
			public SecurityIdentity get() {

				if (adminPassword == null || adminPassword.isBlank()) {
					throw new AuthenticationFailedException("admin password is not defined");
				}

				String username = request.getUsername();
				if (!ADMIN_USERNAME.equals(username)) {
					throw new AuthenticationFailedException("unknown username");
				}

				PasswordCredential passwordCredential = request.getPassword();

				char[] password = passwordCredential.getPassword();
				byte[] passwordBytes = new byte[password.length];

				for (int i = 0; i < password.length; i++) {
					passwordBytes[i] = (byte) password[i];
				}

				if (!MessageDigest.isEqual(passwordBytes, adminPassword.getBytes())) {
					throw new AuthenticationFailedException();
				}

				QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder();

				builder.setPrincipal(AdminPrincipal.INSTANCE);
				builder.addRole(ADMIN_ROLE);
				builder.addCredential(passwordCredential);

				return builder.build();
			}
		});
	}

	public enum AdminPrincipal implements Principal {
		INSTANCE;

		@Override
		public String getName() {
			return ADMIN_USERNAME;
		}

		@Override
		public boolean implies(Subject subject) {
			return Principal.super.implies(subject);
		}
	}


}
