package io.openk9.tenantmanager.pipe.liquibase.validate;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.tenantmanager.pipe.liquibase.validate.util.Params;
import io.quarkus.runtime.util.ExceptionUtil;

import java.util.Deque;

public class Manager {

	public sealed interface Command {}
	public enum Start implements Command { INSTANCE }
	private record LiquibaseResponseWrapper(
		Liquibase.Response response) implements Command {}
	private enum Timeout implements Command { INSTANCE }

	public sealed interface Response {}
	public enum Success implements Response {INSTANCE}
	public record Error(String message) implements Response {}

	public static Behavior<Command> create(
		Deque<Params> liquibaseParamsList, ActorRef<Response> replyTo) {
		return Behaviors.setup(context -> initial(liquibaseParamsList, replyTo, context));
	}

	private static Behavior<Command> initial(
		Deque<Params> liquibaseParamsList, ActorRef<Response> replyTo,
		ActorContext<Command> context) {

		int size = liquibaseParamsList.size();

		return Behaviors
			.receive(Command.class)
			.onMessage(Start.class, start -> onStart(liquibaseParamsList, replyTo, context))
			.onMessageEquals(Timeout.INSTANCE, () -> onTimeout(replyTo))
			.build();
	}

	private static Behavior<Command> onTimeout(ActorRef<Response> replyTo) {

		replyTo.tell(new Error("Timeout"));

		return Behaviors.stopped();

	}

	private static Behavior<Command> onStart(
		Deque<Params> liquibaseParamsList, ActorRef<Response> replyTo,
		ActorContext<Command> context) {

		if (liquibaseParamsList.isEmpty()) {
			replyTo.tell(Success.INSTANCE);
			return Behaviors.stopped();
		}

		Params firstLiquibaseParams = liquibaseParamsList.pop();

		ActorRef<Liquibase.Response> responseActorRef =
			context.messageAdapter(Liquibase.Response.class,
				LiquibaseResponseWrapper::new);

		ActorRef<Liquibase.Command> liquibaseValidateRef = context.spawn(
			Liquibase.validate(firstLiquibaseParams, responseActorRef),
			"liquibase-validate-" + firstLiquibaseParams.schemaName());

		liquibaseValidateRef.tell(Liquibase.Start.INSTANCE);

		return Behaviors
			.receive(Command.class)
			.onMessage(LiquibaseResponseWrapper.class, msg -> {
				if (msg.response() instanceof Liquibase.Success) {
					return onStart(liquibaseParamsList, replyTo, context);
				}
				else if (msg.response() instanceof Liquibase.Error) {
					replyTo.tell(
						new Error(ExceptionUtil.generateStackTrace(
							((Liquibase.Error) msg.response()).throwable())));
					return Behaviors.stopped();
				}
				else {
					return Behaviors.ignore();
				}
			})
			.onMessageEquals(Timeout.INSTANCE, () -> onTimeout(replyTo))
			.build();

	}

}
