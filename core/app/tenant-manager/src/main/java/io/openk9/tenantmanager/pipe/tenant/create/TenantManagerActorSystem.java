package io.openk9.tenantmanager.pipe.tenant.create;


import akka.actor.typed.ActorSystem;
import io.openk9.tenantmanager.pipe.tenant.create.message.TenantMessage;
import io.openk9.tenantmanager.service.BackgroundProcessService;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.smallrye.mutiny.Uni;
import org.keycloak.admin.client.Keycloak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class TenantManagerActorSystem {

	public Uni<UUID> startCreateTenant(String virtualHost, String realmName) {

		return Uni
			.createFrom()
			.item(UUID::randomUUID)
			.invoke(requestId -> {

				ActorSystem<TenantMessage> actorSystem =
					ActorSystem.create(
						TenantBehavior.create(
							requestId, virtualHost,
							keycloak, tenantService, backgroundProcessService,
							liquibaseService),
						"tenant-manager");

				actorSystem.tell(new TenantMessage.Start(realmName));

			});

	}

	public Uni<Void> populateSchema(String schemaName, String virtualHost) {
		return Uni.createFrom().emitter(sink -> {

			try {
				liquibaseService.runInitialization(schemaName, virtualHost, false);
				sink.complete(null);
			}
			catch (Exception e) {
				sink.fail(e);
			}

		});
	}

	@Inject
	Keycloak keycloak;

	@Inject
	TenantService tenantService;

	@Inject
	BackgroundProcessService backgroundProcessService;

	@Inject
	DatasourceLiquibaseService liquibaseService;

}
