package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.vertx.core.json.JsonObject;

import java.time.Duration;
import java.util.Map;

public class Processor extends AbstractBehavior<Processor.Command> {

	public Processor(
		ActorContext<Command> context,
		ActorRef<Token.Command> tokenActorRef, boolean async) {
		super(context);
		this.async = async;
		this.tokenActorRef = tokenActorRef;
		this.tokenResponseAdapter =
			context.messageAdapter(
				Token.Response.class,
				TokenResponseWrapper::new);
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Start.class, this::onStart)
			.build();
	}


	private Behavior<Command> onResponseWrapper(ResponseWrapper responseWrapper) {

		Http.Response response = responseWrapper.response;

		ActorRef<Response> replyTo = responseWrapper.replyTo;

		if (response instanceof Http.OK) {

			if (async) {

				getContext().scheduleOnce(
					Duration.ofSeconds(30), getContext().getSelf(),
					CallbackExpired.INSTANCE);

				return Behaviors.receive(Command.class)
					.onMessage(TokenResponseWrapper.class, wrapper -> onTokenResponseWrapper(wrapper, replyTo))
					.onMessageEquals(CallbackExpired.INSTANCE, Behaviors::stopped)
					.build();

			}
			else {
				Http.OK ok = (Http.OK) response;
				byte[] body = ok.body();
				replyTo.tell(
					new Body(new JsonObject(new String(body))));
			}

		}
		else {
			replyTo.tell(new Error(response.toString()));
		}

		return Behaviors.stopped();

	}

	private Behavior<Command> onTokenResponseWrapper(
		TokenResponseWrapper wrapper, ActorRef<Response> replyTo) {

		Token.Response response = wrapper.response;

		if (response instanceof Token.TokenCallback) {
			Token.TokenCallback tokenCallback =(Token.TokenCallback)response;
			JsonObject jsonObject = tokenCallback.jsonObject();
			replyTo.tell(new Body(jsonObject));
		}
		else if (response instanceof Token.TokenState) {
			Token.TokenState tokenState = (Token.TokenState)response;
			if (tokenState == Token.TokenState.EXPIRED) {
				replyTo.tell(new Error("Token expired"));
				return Behaviors.stopped();
			}
		}

		return Behaviors.same();
	}

	private Behavior<Command> started(
		String url, JsonObject jsonObject, ActorRef<Response> replyTo) {

		ActorRef<Http.Response> responseActorRef =
			getContext().messageAdapter(
				Http.Response.class,
				param -> new ResponseWrapper(param, replyTo)
			);

		ActorRef<Http.Command> commandActorRef =
			getContext().spawnAnonymous(Http.create());

		commandActorRef.tell(new Http.POST(responseActorRef, url, jsonObject));

		return newReceiveBuilder()
			.onMessage(ResponseWrapper.class, this::onResponseWrapper)
			.build();
	}

	private Behavior<Command> onStart(Start start) {

		if (async) {

			tokenActorRef.tell(new Token.Generate(tokenResponseAdapter));

			return waitGenerateToken(start.url, start.jsonObject, start.replyTo());

		}

		return started(start.url, start.jsonObject, start.replyTo());

	}

	private Behavior<Command> waitGenerateToken(
		String url, JsonObject jsonObject, ActorRef<Response> replyTo) {

		return Behaviors.receive(Command.class)
			.onMessage(
				TokenResponseWrapper.class,
				wrapper -> {

					Token.Response response = wrapper.response;

					if (response instanceof Token.TokenGenerated) {
						Token.TokenGenerated tokenGenerated =
							(Token.TokenGenerated) response;
						JsonObject newJson = new JsonObject();

						for (Map.Entry<String, Object> entry : jsonObject) {
							newJson.put(entry.getKey(), entry.getValue());
						}

						newJson.put("replyTo", tokenGenerated.token());

						return started(url, newJson, replyTo);
					}
					else {
						return Behaviors.same();
					}
				}
			)
			.build();
	}

	public static Behavior<Command> create(boolean async, ActorRef<Token.Command> tokenActorRef) {
		return Behaviors.setup(param -> new Processor(param, tokenActorRef, async));
	}

	private final boolean async;
	private final ActorRef<Token.Command> tokenActorRef;
	private final ActorRef<Token.Response> tokenResponseAdapter;

	public sealed interface Command {}
	public record Start(
		String url, JsonObject jsonObject, ActorRef<Response> replyTo) implements Command {}
	public record Callback(String tokenId, JsonObject jsonObject) implements Command {}
	private enum CallbackExpired implements Command {INSTANCE}
	private record TokenResponseWrapper(Token.Response response) implements Command {}
	private record ResponseWrapper(Http.Response response, ActorRef<Response> replyTo) implements Command {}
	public sealed interface Response {}
	public record Error(String message) implements Response {}
	public record Body(JsonObject jsonObject) implements Response {}

}
