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

package io.openk9.tenantmanager.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;

import io.openk9.tenantmanager.config.KeycloakDefaultRealmRepresentationFactory;

import io.quarkus.keycloak.admin.client.common.runtime.KeycloakAdminClientConfig;
import io.quarkus.keycloak.admin.client.common.runtime.KeycloakAdminClientConfigUtil;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

@ApplicationScoped
public class TenantRealmService {

	private static final Logger log = Logger.getLogger(TenantRealmService.class);

	@Inject
	KeycloakDefaultRealmRepresentationFactory defaultRepresentationFactory;
	@Inject
	KeycloakAdminClientConfig keycloakAdminClientConfig;
	@ConfigProperty(name = "openk9.tenant-manager.keycloak-base-issuer-uri")
	String baseIssuerUri;

	private Keycloak keycloakClient;

	void setup(@Observes Startup startup) {

		this.keycloakClient = createKeycloakClient(keycloakAdminClientConfig);
	}

	private static Keycloak createKeycloakClient(KeycloakAdminClientConfig config) {

		KeycloakAdminClientConfigUtil.validate(config);

		if (config.serverUrl().isEmpty()) {
			throw new IllegalStateException("keycloak serverUrl is empty");
		}

		KeycloakBuilder keycloakBuilder = KeycloakBuilder
			.builder()
			.clientId(config.clientId())
			.clientSecret(config.clientSecret().orElse(null))
			.grantType(config.grantType().asString())
			.username(config.username().orElse(null))
			.password(config.password().orElse(null))
			.realm(config.realm())
			.serverUrl(config.serverUrl().get())
			.scope(config.scope().orElse(null));

		return keycloakBuilder.build();

	}

	public Uni<CreatedRealm> createRealm(String realmName, String virtualHost) {

		RealmRepresentation realmRepresentation =
			RealmRepresentationFactory.createRealmRepresentation(
				virtualHost, realmName,
				defaultRepresentationFactory.getDefaultRealmRepresentation()
			);

		return VertxContextSupport.executeBlocking(() -> {
			keycloakClient.realms().create(realmRepresentation);

			var client = realmRepresentation.getClients().get(0);
			var clientId = client.getName();
			var clientSecret = client.getSecret();

			UserRepresentation userRepresentation =
				realmRepresentation.getUsers().get(0);

			var username = userRepresentation.getUsername();
			var password = userRepresentation.getCredentials()
				.get(0).getValue();

			String issuerUri = baseIssuerUri + realmName;

			return new CreatedRealm(
				clientId,
				clientSecret,
				virtualHost,
				issuerUri,
				username,
				password
			);
		});

	}

	public Uni<Void> deleteRealm(String realmName) {

		return VertxContextSupport.executeBlocking(() -> {
			try {
				keycloakClient.realm(realmName).remove();
			}
			catch (Exception e) {
				log.errorf(
					e, "An error occurred while deleting realm %s", realmName);
			}

			return null;
		});
	}

	public record CreatedRealm(
		String clientId, String clientSecret,
		String virtualHost, String issuerUri,
		String username, String password
	) {}

}
