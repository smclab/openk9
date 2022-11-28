package io.openk9.tenantmanager.pipe;


import io.openk9.tenantmanager.actor.TypedActor;
import io.openk9.tenantmanager.service.BackgroundProcessService;
import io.openk9.tenantmanager.service.LiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.quarkus.keycloak.admin.client.common.KeycloakAdminClientConfig;
import io.quarkus.keycloak.admin.client.common.KeycloakAdminClientConfigUtil;
import io.smallrye.context.api.ManagedExecutorConfig;
import io.smallrye.context.api.NamedInstance;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class TenantManagerActorSystem {

	@Inject
	KeycloakAdminClientConfig config;

	@PostConstruct
	public void init() {

		KeycloakAdminClientConfigUtil.validate(config);

		if (config.serverUrl.isEmpty()) {
			throw new IllegalStateException("keycloak serverUrl is empty");
		}

		KeycloakBuilder keycloakBuilder = KeycloakBuilder
			.builder()
			.clientId(config.clientId)
			.clientSecret(config.clientSecret.orElse(null))
			.grantType(config.grantType.asString())
			.username(config.username.orElse(null))
			.password(config.password.orElse(null))
			.realm(config.realm)
			.serverUrl(config.serverUrl.get())
			.scope(config.scope.orElse(null));

		keycloak = keycloakBuilder.build();

		system = new TypedActor.System(sharedConfiguredExecutor);

	}

	public Uni<UUID> startCreateTenant(String virtualHost, String realmName) {

		return backgroundProcessService
			.addBackgroundProcess()
			.invoke((requestId) -> {

				TypedActor.Address<TenantMessage> tenantActor =
					system.actorOf(
						self -> new TenantBehavior(
							requestId, virtualHost, tenantService,
							backgroundProcessService, self));

				TypedActor.Address<TenantMessage> keycloakActor =
					system.actorOf(self -> new KeycloakBehavior(keycloak, tenantActor));

				TypedActor.Address<TenantMessage> schemaActor =
					system.actorOf(
						self -> new SchemaBehavior(requestId, tenantActor, liquibaseService,
							backgroundProcessService));

				tenantActor.tell(
					new TenantMessage.Start(keycloakActor, schemaActor, realmName));

			});

	}

	private TypedActor.System system;

	@Inject
	TenantService tenantService;

	@Inject
	BackgroundProcessService backgroundProcessService;

	@Inject
	LiquibaseService liquibaseService;

	Keycloak keycloak;

	@Inject
	@ManagedExecutorConfig(cleared = ThreadContext.TRANSACTION)
	@NamedInstance("tenant-actor-executor")
	ManagedExecutor sharedConfiguredExecutor;

}
