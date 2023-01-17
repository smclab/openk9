package io.openk9.tenantmanager.pipe.tenant.create.factory;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
