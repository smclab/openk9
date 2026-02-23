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

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Quarkus Security {@link IdentityProvider} that validates HTTP Basic
 * Authentication credentials against a configured admin password.
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

        @ConfigProperty(name = "openk9.security.admin.password")
        String adminPassword;

        private byte[] adminPasswordBytes;

        @PostConstruct
        void init() {
                this.adminPasswordBytes = adminPassword.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
                return UsernamePasswordAuthenticationRequest.class;
        }

        @Override
        public Uni<SecurityIdentity> authenticate(
                        UsernamePasswordAuthenticationRequest request,
                        AuthenticationRequestContext context) {

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

}
