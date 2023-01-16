package io.openk9.tenantmanager.pipe.tenant.create;


import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.quarkus.keycloak.admin.client.common.KeycloakAdminClientConfig;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
			Behaviors
				.supervise(Supervisor.create())
				.onFailure(SupervisorStrategy.resume()),
			"tenant-manager-creator"
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
				requestTimeout,
				_actorSystem.scheduler()
			);

		return Uni
			.createFrom()
			.completionStage(ask)
			.onItemOrFailure()
			.transformToUni((res, t) -> {

				if (t != null) {
					_actorSystem.tell(
						new Supervisor.Rollback(
							realmName,
							liquibaseService,
							config
						)
					);
					return Uni.createFrom().failure(t);
				}

				if (res instanceof Supervisor.Success) {
					Supervisor.Success success = (Supervisor.Success)res;
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

	@ConfigProperty(
		name = "openk9.tenant-manager.create-tenant-timeout",
		defaultValue = "45s"
	)
	Duration requestTimeout;

}
