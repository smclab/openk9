package io.openk9.datasource.pipeline.actor.enrichitem;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import io.openk9.datasource.util.CborSerializable;
import io.vertx.core.json.JsonObject;

import java.util.Map;

public class GroovyActor {
	public sealed interface Command extends CborSerializable {}
	public record Execute(
		JsonObject jsonObject, String groovyScript, ActorRef<Response> replyTo) implements Command {}
	public record Validate(JsonObject jsonObject, String groovyScript, ActorRef<Response> replyTo) implements Command {}
	public sealed interface Response extends CborSerializable {}
	public record GroovyResponse(byte[] response) implements Response {}
	public record GroovyError(String error) implements Response {}
	public record GroovyValidateResponse(boolean valid) implements Response {}

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
			.onMessage(Validate.class, validate -> onValidate(groovyShell, validate, ctx))
			.build();
	}

	private static Behavior<Command> onValidate(
		GroovyShell groovyShell, Validate validate, ActorContext<Command> ctx) {

		String groovyScript = validate.groovyScript;

		JsonObject dataPayload = validate.jsonObject;

		Script parse = groovyShell.parse(groovyScript);

		parse.setBinding(new Binding(Map.copyOf(dataPayload.getMap())));

		try {

			Object response = parse.run();

			if (response instanceof Boolean) {
				validate.replyTo.tell(new GroovyValidateResponse((Boolean)response));
			}
			else {
				validate.replyTo.tell(new GroovyError("Invalid return type: " + response.getClass()));
			}

		}
		catch (Exception e) {
			validate.replyTo.tell(new GroovyError(e.getMessage()));
			ctx.getLog().error(e.getMessage(), e);
		}

		return Behaviors.stopped();

	}

	private static Behavior<Command> onExecute(
		GroovyShell groovyShell, Execute execute, ActorContext<Command> ctx) {

		String groovyScript = execute.groovyScript;
		JsonObject dataPayload = execute.jsonObject;
		ActorRef<Response> replyTo = execute.replyTo;

		Script parse = groovyShell.parse(groovyScript);

		parse.setBinding(new Binding(Map.copyOf(dataPayload.getMap())));

		try {

			Object response = parse.run();

			if (response instanceof Map) {
				JsonObject jsonResponse = new JsonObject((Map<String, Object>) response);
				replyTo.tell(new GroovyResponse(jsonResponse.toBuffer().getBytes()));
			}
			else if (response instanceof JsonObject) {
				JsonObject jsonResponse = (JsonObject)response;
				replyTo.tell(new GroovyResponse(jsonResponse.toBuffer().getBytes()));
			}
			else {
				replyTo.tell(new GroovyError("Invalid return type: " + response.getClass()));
			}

		}
		catch (Exception e) {
			replyTo.tell(new GroovyError(e.getMessage()));
			ctx.getLog().error(e.getMessage(), e);
		}

		return Behaviors.stopped();

	}

}
