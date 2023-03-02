package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.openk9.datasource.processor.payload.DataPayload;
import io.vertx.core.json.JsonObject;

import java.util.Map;

public class GroovyActor {
	public sealed interface Command {}
	public record Execute(
		DataPayload dataPayload, String groovyScript, ActorRef<Response> replyTo) implements Command {}
	public sealed interface Response {}
	public record GroovyResponse(JsonObject response) implements Response {}
	public record GroovyError(String error) implements Response {}

	public static Behavior<Command> create() {
		return Behaviors.setup(ctx -> {

			GroovyShell groovyShell = new GroovyShell();

			return initial(groovyShell, ctx);
		});
	}

	private static Behavior<Command> initial(
		GroovyShell groovyShell, ActorContext<Command> ctx) {

		return Behaviors.receive(Command.class)
			.onMessage(Execute.class, execute -> onExecute(groovyShell, execute, ctx))
			.build();
	}

	private static Behavior<Command> onExecute(
		GroovyShell groovyShell, Execute execute, ActorContext<Command> ctx) {

		String groovyScript = execute.groovyScript;
		DataPayload dataPayload = execute.dataPayload;

		Script parse = groovyShell.parse(groovyScript);

		parse.setBinding(new Binding(Map.of("payload", dataPayload)));

		try {

			Object response = parse.run();

			if (response instanceof Map) {
				execute.replyTo.tell(new GroovyResponse(new JsonObject((Map<String, Object>) response)));
			}
			else if (response instanceof JsonObject) {
				execute.replyTo.tell(new GroovyResponse((JsonObject)response));
			}

			execute.replyTo.tell(new GroovyError("Invalid return type: " + response.getClass()));

		}
		catch (Exception e) {
			execute.replyTo.tell(new GroovyError(e.getMessage()));
			ctx.getLog().error(e.getMessage(), e);
		}

		return Behaviors.stopped();

	}

}
