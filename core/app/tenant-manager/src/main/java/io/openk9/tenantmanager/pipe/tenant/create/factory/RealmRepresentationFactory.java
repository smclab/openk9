package io.openk9.tenantmanager.pipe.tenant.create.factory;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RealmRepresentationFactory {

	private RealmRepresentationFactory() {}

	public static RealmRepresentation createRealmRepresentation(
		String virtualHost, String realmName) {

		RealmRepresentation realmRepresentation = new RealmRepresentation();
		realmRepresentation.setRealm(realmName);
		realmRepresentation.setEnabled(true);
		realmRepresentation.setDisplayName(virtualHost);

		realmRepresentation.setClients(
			List.of(
				_createClientRepresentation(virtualHost, "openk9")
			)
		);
		realmRepresentation.setRoles(_createRolesRepresentation());

		realmRepresentation.setUsers(
			List.of(
				_createAdminUserRepresentation()
			)
		);

		return realmRepresentation;
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

	private static ClientRepresentation _createClientRepresentation(
		String virtualHost, String clientId) {
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
		clientRepresentation.setRedirectUris(
			List.of(
				"https://" + virtualHost + "/*"
			)
		);
		clientRepresentation.setWebOrigins(
			List.of(
				"+"
			)
		);
		clientRepresentation.setPublicClient(true);
		clientRepresentation.setStandardFlowEnabled(true);
		clientRepresentation.setDefaultClientScopes(
			List.of(
				"k9-admin",
				"k9-read",
				"k9-write"
			)
		);

		return clientRepresentation;
	}

}
