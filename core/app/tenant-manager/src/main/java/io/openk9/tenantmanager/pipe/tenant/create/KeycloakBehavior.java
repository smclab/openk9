package io.openk9.tenantmanager.pipe.tenant.create;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.tenantmanager.model.BackgroundProcess;
import io.openk9.tenantmanager.pipe.tenant.create.message.KeycloakMessage;
import io.openk9.tenantmanager.pipe.tenant.create.message.TenantMessage;
import io.openk9.tenantmanager.service.BackgroundProcessService;
import io.openk9.tenantmanager.util.VertxUtil;
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

public class KeycloakBehavior extends AbstractBehavior<KeycloakMessage> {

	public KeycloakBehavior(
		ActorContext<KeycloakMessage> context,
		Keycloak keycloak, UUID processId,
		BackgroundProcessService backgroundProcessService,
		ActorRef<TenantMessage> tenantActor) {
		super(context);
		this.tenantActor = tenantActor;
		this.keycloak = keycloak;
		this.requestId = processId;
		this.backgroundProcessService = backgroundProcessService;
	}

	public static Behavior<KeycloakMessage> create(
		Keycloak keycloak, UUID processId,
		BackgroundProcessService backgroundProcessService,
		ActorRef<TenantMessage> tenantActor) {
		return Behaviors.setup(
			context -> new KeycloakBehavior(
				context, keycloak, processId, backgroundProcessService,
				tenantActor));
	}

	@Override
	public Receive<KeycloakMessage> createReceive() {
		return newReceiveBuilder()
			.onMessage(KeycloakMessage.Start.class, this::onStart)
			.onMessage(KeycloakMessage.ProcessCreatedId.class, this::onProcessCreatedId)
			.onMessage(KeycloakMessage.Rollback.class, this::onRollback)
			.onMessage(KeycloakMessage.Stop.class, this::onStop)
			.build();
	}

	private Behavior<KeycloakMessage> onProcessCreatedId(KeycloakMessage.ProcessCreatedId pcid) {

		this.processId = pcid.processId();

		String virtualHost = pcid.virtualHost();
		String realmName = pcid.realmName();
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
			return Behaviors.same();
		}

		pcid
			.tenant()
			.tell(
				new TenantMessage.RealmCreated(
					realmName, "openk9", null
				)
			);

		return this;
	}

	private Behavior<KeycloakMessage> onStart(KeycloakMessage.Start createSchema) {

		VertxUtil.runOnContext(() ->
			backgroundProcessService.createBackgroundProcess(
				BackgroundProcess
					.builder()
					.processId(requestId)
					.name("create-keycloak-realm")
					.message("Starting create keycloak realm: " + createSchema.realmName())
					.status(BackgroundProcess.Status.IN_PROGRESS)
					.build()
			)
				.invoke(bp ->
					getContext()
						.getSelf()
						.tell(
							new KeycloakMessage.ProcessCreatedId(
								bp.getId(),
								createSchema.virtualHost(),
								createSchema.realmName(),
								createSchema.tenant()))
				)
		);

		return this;

	}

	private Behavior<KeycloakMessage> onRollback(KeycloakMessage.Rollback rollback) {
		if (realmName != null) {
			keycloak.realms().realm(realmName).remove();
			getContext().getLog().warn("Realm " + realmName + " rollbacked");
			VertxUtil.runOnContext(() ->
				backgroundProcessService.updateBackgroundProcess(
					processId, BackgroundProcess.Status.ROOLBACK,
					"Realm " + realmName + " rollbacked",
					"create-keycloak-realm")
			);
		}
		return Behaviors.stopped();
	}

	private Behavior<KeycloakMessage> onStop(KeycloakMessage.Stop stop) {
		if (realmName != null) {
			getContext().getLog().info("Realm created");
			getContext().getLog().info("Realm: " + realmRepresentation.getRealm());
			getContext().getLog().info("User: " + realmRepresentation.getUsers().get(0).getUsername());
			getContext().getLog().info("Password: " + realmRepresentation.getUsers().get(0).getCredentials().get(0).getValue());
			VertxUtil.runOnContext(() ->
				backgroundProcessService.updateBackgroundProcess(
					processId, BackgroundProcess.Status.FINISHED,
					"Realm " + realmName + " created",
					"create-keycloak-realm")
			);
		}
		return Behaviors.stopped();
	}

	private void _saveUserInfo(RealmRepresentation realmRepresentation) {
		this.realmRepresentation = realmRepresentation;
		this.realmName = realmRepresentation.getRealm();
	}

	private UserRepresentation _createAdminUserRepresentation() {
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
		clientRepresentation.setDefaultClientScopes(
			List.of("profile", "email"));

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

	private final ActorRef<TenantMessage> tenantActor;
	private final Keycloak keycloak;
	private final UUID requestId;
	private long processId;
	private final BackgroundProcessService backgroundProcessService;
	private String realmName;
	private RealmRepresentation realmRepresentation;

}
