package io.openk9.tenantmanager.pipe.tenant.create;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;

public class Schema {

	public sealed interface Command {}
	public enum Start implements Command {INSTANCE}

	public sealed interface Response {}
	public record Success(String schemaName) implements Response {}
	public record Error(String message) implements Response {}

	public record Params(String virtualHost, String schemaName) {}

	public static Behavior<Command> create(
		DatasourceLiquibaseService service, Params params, ActorRef<Response> replyTo) {
		return Behaviors.setup(context -> initial(context, service, params, replyTo));
	}

	public static Behavior<Command> createRollback(
		DatasourceLiquibaseService service, String schemaName) {
		return Behaviors.setup(context -> rollback(context, service, schemaName));
	}

	private static Behavior<Command> rollback(
		ActorContext<Command> context, DatasourceLiquibaseService service, String schemaName) {
		return Behaviors.receive(Command.class)
			.onMessageEquals(Start.INSTANCE, () -> {

				try {
					service.rollbackRunLiquibaseMigration(schemaName);
				}
				catch (Exception e) {
					context.getLog().error(e.getMessage(), e);
				}

				return Behaviors.stopped();

			})
			.build();
	}

	private static Behavior<Command> initial(
		ActorContext<Command> context, DatasourceLiquibaseService service,
		Params params, ActorRef<Response> replyTo) {
		return Behaviors.receive(Command.class)
			.onMessageEquals(Start.INSTANCE, () -> onStart(context, service, params, replyTo))
			.build();
	}

	private static Behavior<Command> onStart(
		ActorContext<Command> context, DatasourceLiquibaseService service,
		Params params, ActorRef<Response> replyTo) {

		try {
			service.runInitialization(params.schemaName(), params.virtualHost(), true);
			replyTo.tell(new Success(params.schemaName()));
		}
		catch (Exception e) {
			replyTo.tell(new Error(e.getMessage()));
		}

		return Behaviors.stopped();

	}


}
