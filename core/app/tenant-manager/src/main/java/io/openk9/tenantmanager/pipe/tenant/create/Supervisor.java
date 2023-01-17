package io.openk9.tenantmanager.pipe.tenant.create;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.tenantmanager.config.KeycloakContext;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;

public class Supervisor {
	public sealed interface Command {}

	public record Start(
		String virtualHost, String schemaName,
		DatasourceLiquibaseService liquibaseService,
		TenantService tenantService,
		KeycloakContext keycloakContext,
		ActorRef<Supervisor.Response> replyTo) implements Command {}
	public record Rollback(
		String schemaName, DatasourceLiquibaseService service,
		KeycloakContext keycloakContext
	) implements Command {}
	public record ResponseWrapper(
		Manager.Response response,
		ActorRef<Supervisor.Response> replyTo) implements Command {}

	public sealed interface Response {}
	public record Success(Tenant tenant) implements Response {}

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
						start.liquibaseService(),
						start.tenantService(),
						start.keycloakContext(),
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
						rollback.keycloakContext()
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
						new Success(response.tenant()));
				}
				return Behaviors.same();
			})
			.build();
	}

}
