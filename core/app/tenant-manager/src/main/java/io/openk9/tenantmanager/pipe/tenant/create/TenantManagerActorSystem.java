package io.openk9.tenantmanager.pipe.tenant.create;


import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.quarkus.keycloak.admin.client.common.KeycloakAdminClientConfig;
import io.smallrye.mutiny.Uni;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class TenantManagerActorSystem {

	private ActorSystem<Supervisor.Command> _actorSystem;

	@PostConstruct
	public void init() {
		_actorSystem = ActorSystem.create(
			Supervisor.create(), "tenant-manager-creator"
		);
	}

	public Uni<Tenant> startCreateTenant(String virtualHost, String realmName) {

		CompletionStage<Supervisor.Response> ask =
			AskPattern.ask(
				_actorSystem,
				(ActorRef<Supervisor.Response> actorRef) ->
					new Supervisor.Start(
						virtualHost,
						realmName,
						liquibaseService,
						config,
						actorRef
					),
				Duration.ofSeconds(10),
				_actorSystem.scheduler()
			);

		return Uni
			.createFrom()
			.completionStage(ask)
			.onItem()
			.transformToUni(response -> {
				if (response instanceof Supervisor.Success) {
					Supervisor.Success success = (Supervisor.Success) response;
					Tenant tenant = new Tenant();
					tenant.setVirtualHost(success.virtualHost());
					tenant.setSchemaName(success.schemaName());
					tenant.setRealmName(success.realmName());
					tenant.setClientId(success.clientId());
					tenant.setClientSecret(success.clientSecret());
					tenant.setLiquibaseSchemaName(
						success.liquibaseSchemaName());
					return tenantService.persist(tenant);
				}
				else {
					throw new RuntimeException("error");
				}
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
	TenantService tenantService;

	@Inject
	DatasourceLiquibaseService liquibaseService;

	@Inject
	KeycloakAdminClientConfig config;

}
