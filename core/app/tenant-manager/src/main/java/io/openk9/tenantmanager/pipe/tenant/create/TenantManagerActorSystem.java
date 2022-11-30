package io.openk9.tenantmanager.pipe.tenant.create;


import io.openk9.tenantmanager.actor.TypedActor;
import io.openk9.tenantmanager.pipe.tenant.create.message.KeycloakMessage;
import io.openk9.tenantmanager.pipe.tenant.create.message.SchemaMessage;
import io.openk9.tenantmanager.pipe.tenant.create.message.TenantMessage;
import io.openk9.tenantmanager.service.BackgroundProcessService;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.quarkus.keycloak.admin.client.common.KeycloakAdminClientConfig;
import io.smallrye.context.api.ManagedExecutorConfig;
import io.smallrye.context.api.NamedInstance;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.keycloak.admin.client.Keycloak;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class TenantManagerActorSystem {

	@PostConstruct
	public void init() {
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

				TypedActor.Address<KeycloakMessage> keycloakActor =
					system.actorOf(self -> new KeycloakBehavior(keycloak, tenantActor));

				TypedActor.Address<SchemaMessage> schemaActor =
					system.actorOf(
						self -> new SchemaBehavior(tenantActor, liquibaseService));

				tenantActor.tell(
					new TenantMessage.Start(keycloakActor, schemaActor, realmName));

			});

	}

	private TypedActor.System system;

	@Inject
	Keycloak keycloak;

	@Inject
	KeycloakAdminClientConfig config;

	@Inject
	TenantService tenantService;

	@Inject
	BackgroundProcessService backgroundProcessService;

	@Inject
	DatasourceLiquibaseService liquibaseService;

	@Inject
	@ManagedExecutorConfig
	@NamedInstance("tenant-actor-executor")
	ManagedExecutor sharedConfiguredExecutor;

}
