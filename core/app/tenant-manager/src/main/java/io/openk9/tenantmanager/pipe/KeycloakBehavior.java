package io.openk9.tenantmanager.pipe;

import io.openk9.tenantmanager.actor.TypedActor;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KeycloakBehavior implements TypedActor.Behavior<TenantMessage> {

	public KeycloakBehavior(
		Keycloak keycloak, TypedActor.Address<TenantMessage> tenantActor) {
		this.tenantActor = tenantActor;
		this.keycloak = keycloak;
	}

	@Override
	public TypedActor.Effect<TenantMessage> apply(TenantMessage message) {
		if (message instanceof TenantMessage.CreateRealm) {
			TenantMessage.CreateRealm createSchema = (TenantMessage.CreateRealm)message;
			String virtualHost = createSchema.virtualHost();
			String realmName = createSchema.realmName();
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

			try {
				keycloak.realms().create(realmRepresentation);
				_saveUserInfo(realmRepresentation);
			}
			catch (Exception e) {
				tenantActor.tell(new TenantMessage.Error(e));
				return TypedActor.Stay();
			}

			createSchema
				.next()
				.tell(
					new TenantMessage.RealmCreated(
						realmName, "openk9", null
					)
				);

		}
		else if (message instanceof TenantMessage.SimpleError) {
			if (realmName != null) {
				keycloak.realms().realm(realmName).remove();
				logger.warn("Realm " + realmName + " rollbacked");
			}
			return TypedActor.Die();
		}
		else if (message instanceof TenantMessage.Finished) {
			logger.info("Tenant " + realmName + " finished");
			logger.info(adminUserInfo);
			return TypedActor.Die();
		}

		return TypedActor.Stay();
 	}

	private void _saveUserInfo(RealmRepresentation realmRepresentation) {

		adminUserInfo = "Realm created: " +
						realmRepresentation.getRealm() +
						"\n" +
						"Client created: " +
						realmRepresentation
						.getClients()
						.get(0)
						.getClientId() +
						"\n" +
						"User created: " +
						realmRepresentation
						.getUsers()
						.get(0)
						.getUsername() +
						"\n" +
						"User password: " +
						realmRepresentation
						.getUsers()
						.get(0)
						.getCredentials()
						.get(0)
						.getValue() +
						"\n";

		this.realmName = realmRepresentation.getRealm();

	}

	private UserRepresentation _createAdminUserRepresentation() {
		UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setUsername("k9admin");
		userRepresentation.setFirstName("k9admin");
		userRepresentation.setLastName("k9admin");
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

	private CredentialRepresentation _createAdminCredentialRepresentation() {
		CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
		credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
		credentialRepresentation.setValue(UUID.randomUUID().toString());
		credentialRepresentation.setTemporary(false);
		return credentialRepresentation;
	}

	private RolesRepresentation _createRolesRepresentation() {

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
		clientRepresentation.setAttributes(
			Map.of(
				"login_theme", clientId
			)
		);
		clientRepresentation.setRedirectUris(
			List.of(
				"http://" + virtualHost + "/*",
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

	private final TypedActor.Address<TenantMessage> tenantActor;
	private final Keycloak keycloak;

	private String realmName;
	private String adminUserInfo;
	private final static Logger logger = Logger.getLogger(KeycloakBehavior.class);

}
