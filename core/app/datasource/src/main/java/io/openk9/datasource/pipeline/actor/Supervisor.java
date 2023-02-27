package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.vertx.core.json.JsonObject;

public class Supervisor extends AbstractBehavior<Supervisor.Command> {

	public Supervisor(ActorContext<Command> context) {
		super(context);
		this.tokenActorRef = context.spawn(Token.create(), "token-actor");
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Callback.class, this::onCallback)
			.onMessage(Call.class, this::onCall)
			.onMessage(ResponseWrapper.class, wrapper -> {

				Processor.Response response = wrapper.response;

				ActorRef<Response> replyTo = wrapper.replyTo;

				if (response instanceof Processor.Body) {
					Processor.Body ok = (Processor.Body) response;
					replyTo.tell(new Body(ok.jsonObject()));
				}
				else {
					Processor.Error error = (Processor.Error) response;
					replyTo.tell(new Error(error.message()));
				}

				return Behaviors.same();

			})
			.build();
	}

	private Behavior<Command> onCall(Call call) {

		ActorRef<Processor.Command> actorRef =
			getContext().spawnAnonymous(
				Processor.create(call.async, tokenActorRef));

		ActorRef<Processor.Response> responseActorRef =
			getContext().messageAdapter(
				Processor.Response.class,
				response -> new ResponseWrapper(response, call.replyTo));

		actorRef.tell(new Processor.Start(call.url, call.jsonObject, responseActorRef));

		return Behaviors.same();

	}

	public static Behavior<Command> create() {
		return Behaviors.setup(Supervisor::new);
	}

	private Behavior<Command> onCallback(Callback callback) {
		tokenActorRef.tell(new Token.Callback(callback.tokenId, callback.jsonObject));
		return Behaviors.same();
	}

	private final ActorRef<Token.Command> tokenActorRef;
	public sealed interface Command {}
	public record Call(
		boolean async, String url, JsonObject jsonObject, ActorRef<Response> replyTo) implements Command {}
	public record Callback(String tokenId, JsonObject jsonObject) implements Command {}
	private record ResponseWrapper(Processor.Response response, ActorRef<Response> replyTo) implements Command {}
	public sealed interface Response {}
	public record Body(JsonObject jsonObject) implements Response {}
	public record Error(String error) implements Response {}


}
