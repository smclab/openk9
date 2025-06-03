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

package io.openk9.tenantmanager.pipe.tenant.create.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

public class RealmRepresentationFactory {

	private RealmRepresentationFactory() {}

	public static RealmRepresentation createRealmRepresentation(
		String virtualHost, String realmName) {

		return createRealmRepresentation(
			virtualHost, realmName, new RealmRepresentation());

	}

	public static RealmRepresentation createRealmRepresentation(
		String virtualHost, String realmName, RealmRepresentation defaultRealmRepresentation) {

		Objects.requireNonNull(defaultRealmRepresentation, "defaultRealmRepresentation is null");

		defaultRealmRepresentation.setRealm(realmName);
		defaultRealmRepresentation.setEnabled(true);
		defaultRealmRepresentation.setDisplayName(virtualHost);

		List<ClientRepresentation> clients =
			defaultRealmRepresentation.getClients();

		if (clients == null) {
			clients = new ArrayList<>(1);
			defaultRealmRepresentation.setClients(clients);
		}

		clients
			.stream()
			.filter(clientRepresentation -> clientRepresentation.getClientId().equals("openk9"))
			.findFirst()
			.or(() -> Optional.of(_createClientRepresentation("openk9")))
			.ifPresent(
				clientRepresentation -> {
					clientRepresentation.setWebOrigins(List.of("+"));
					clientRepresentation.setRedirectUris(List.of("https://" + virtualHost + "/*"));
				}
			);

		RolesRepresentation roles = defaultRealmRepresentation.getRoles();

		if (!(roles != null && (roles.getRealm() != null && !roles.getRealm().isEmpty())))  {
			defaultRealmRepresentation.setRoles(_createRolesRepresentation());
		}

		defaultRealmRepresentation.setUsers(
			List.of(
				_createAdminUserRepresentation()
			)
		);

		return defaultRealmRepresentation;
	}

	private static UserRepresentation _createAdminUserRepresentation() {
		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setUsername("k9admin");
		userRepresentation.setFirstName("k9admin");
		userRepresentation.setLastName("k9admin");
		userRepresentation.setEmail("k9admin@openk9.io");
		userRepresentation.setRealmRoles(
			List.of("k9-admin", "k9-write", "k9-read")
		);
		userRepresentation.setEnabled(true);
		userRepresentation.setCredentials(
			List.of(
				_createAdminCredentialRepresentation()
			)
		);
		userRepresentation.setRequiredActions(List.of("UPDATE_PASSWORD"));
		return userRepresentation;
	}

	private static CredentialRepresentation _createAdminCredentialRepresentation() {
		CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
		credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
		credentialRepresentation.setValue(UUID.randomUUID().toString());
		credentialRepresentation.setTemporary(false);
		return credentialRepresentation;
	}

	private static RolesRepresentation _createRolesRepresentation() {

		RolesRepresentation rolesRepresentation = new RolesRepresentation();
		rolesRepresentation.setRealm(
			List.of(
				_createRoleRepresentation("k9-admin", "K9 Admin Role"),
				_createRoleRepresentation("k9-write", "K9 Write Role"),
				_createRoleRepresentation("k9-read", "K9 Read Role")
			)
		);
		return rolesRepresentation;

	}


	private static RoleRepresentation _createRoleRepresentation(
		String name, String description) {
		RoleRepresentation k9admin = new RoleRepresentation();
		k9admin.setName(name);
		k9admin.setDescription(description);
		k9admin.setComposite(false);
		k9admin.setClientRole(false);
		return k9admin;
	}

	private static ClientRepresentation _createClientRepresentation(String clientId) {
		ClientRepresentation clientRepresentation = new ClientRepresentation();
		clientRepresentation.setName(clientId);
		clientRepresentation.setClientId(clientId);
		clientRepresentation.setEnabled(true);
		clientRepresentation.setProtocol("openid-connect");
		clientRepresentation.setClientAuthenticatorType("client-secret");
		clientRepresentation.setDefaultClientScopes(
			List.of("profile", "email", "roles"));

		clientRepresentation.setAttributes(
			Map.of(
				"login_theme", clientId
			)
		);
		clientRepresentation.setPublicClient(true);
		clientRepresentation.setStandardFlowEnabled(true);

		return clientRepresentation;
	}

}
