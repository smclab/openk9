package io.openk9.tenantmanager.pipe.tenant.create;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.quarkus.keycloak.admin.client.common.KeycloakAdminClientConfig;

public class Supervisor {
	public sealed interface Command {}

	public record Start(
		String virtualHost, String schemaName,
		DatasourceLiquibaseService service,
		KeycloakAdminClientConfig keycloakAdminClientConfig,
		ActorRef<Supervisor.Response> replyTo) implements Command {}
	public record Rollback(
		String schemaName, DatasourceLiquibaseService service,
		KeycloakAdminClientConfig keycloakAdminClientConfig
	) implements Command {}
	public record ResponseWrapper(
		Manager.Response response,
		ActorRef<Supervisor.Response> replyTo) implements Command {}

	public sealed interface Response {}
	public record Success(
		String virtualHost, String schemaName, String liquibaseSchemaName,
		String realmName, String clientId, String clientSecret) implements Response {}

	public static Behavior<Command> create() {
		return Behaviors.setup(Supervisor::initial);
	}

	private static Behavior<Command> initial(ActorContext<Command> context) {

		return Behaviors
			.receive(Command.class)
			.onMessage(Start.class, start -> {

				ActorRef<Manager.Response> responseActorRef =
					context.messageAdapter(
						Manager.Response.class,
						param -> new ResponseWrapper(param, start.replyTo));

				context.spawn(
					Manager.create(
						start.virtualHost(),
						start.schemaName(),
						start.service(),
						start.keycloakAdminClientConfig(),
						responseActorRef
					),
					"manager-" + start.schemaName()
				);
				return Behaviors.same();
			})
			.onMessage(Rollback.class, rollback -> {

				context.spawn(
					Manager.createRollback(
						rollback.schemaName(),
						rollback.service(),
						rollback.keycloakAdminClientConfig()
					),
					"rollback-" + rollback.schemaName()
				);

				return Behaviors.same();
			})
			.onMessage(ResponseWrapper.class, responseWrapper -> {
				if (responseWrapper.response instanceof Manager.Success) {
					Manager.Success response =
						(Manager.Success)responseWrapper.response;
					responseWrapper.replyTo.tell(
						new Success(
							response.virtualHost(),
							response.schemaName(),
							response.liquibaseSchemaName(),
							response.realmName(),
							response.clientId(),
							response.clientSecret()
						)
					);
				}
				return Behaviors.same();
			})
			.build();
	}

}
