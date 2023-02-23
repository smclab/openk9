package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.typesafe.config.Config;
import io.vertx.core.json.JsonObject;

import java.time.Duration;
import java.util.Map;

public class Processor extends AbstractBehavior<Processor.Command> {

	public Processor(ActorContext<Command> context, boolean async) {
		super(context);
		this.async = async;
		if (async) {
			this.tokenActorRef = context.spawnAnonymous(
				Token.create(_getTimeoutFromActorContext()));
		}
		else {
			this.tokenActorRef = null;
		}
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Start.class, this::onStart)
			.build();
	}


	private Behavior<Command> onResponseWrapper(ResponseWrapper responseWrapper) {

		Http.Response response = responseWrapper.response;

		if (response instanceof Http.OK) {

			if (async) {
				return Behaviors.receive(Command.class)
					.onMessage(Callback.class, callback -> onCallback(callback, responseWrapper.replyTo))
					.build();
			}
			else {
				Http.OK ok = (Http.OK) response;
				byte[] body = ok.body();
				responseWrapper.replyTo.tell(
					new Body(new JsonObject(new String(body))));
			}

		}
		else {
			responseWrapper.replyTo.tell(new Error(response.toString()));
		}

		return Behaviors.stopped();

	}

	private Behavior<Command> onCallback(
		Callback callback, ActorRef<Response> replyTo) {

		ActorRef<Token.TokenValidationResponse> tokenResponseActorRef =
			getContext().messageAdapter(
				Token.TokenValidationResponse.class,
				TokenValidationResponseWrapper::new);

		tokenActorRef.tell(
			new Token.Validate(callback.tokenId, tokenResponseActorRef));

		return Behaviors.receive(Command.class)
			.onMessage(
				TokenValidationResponseWrapper.class,
				wrapper -> {

					if (wrapper.response.valid()) {
						getContext().getLog().info("TOKEN {} VALID", callback.tokenId);
						replyTo.tell(new Body(callback.jsonObject));
						return Behaviors.stopped();
					}

					getContext().getLog().warn("TOKEN {} INVALID", callback.tokenId);

					return Behaviors.same();

				})
			.build();

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

		ActorRef<Token.TokenResponse> tokenResponseActorRef =
			getContext().messageAdapter(
				Token.TokenResponse.class,
				TokenResponseWrapper::new);

		if (async) {

			tokenActorRef.tell(new Token.Generate(tokenResponseActorRef));

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

					JsonObject newJson = new JsonObject();

					for (Map.Entry<String, Object> entry : jsonObject) {
						newJson.put(entry.getKey(), entry.getValue());
					}

					newJson.put("replyTo", wrapper.response.token());

					return started(url, newJson, replyTo);

				}
			)
			.build();
	}

	private long _getTimeoutFromActorContext() {
		return _getTimeout(getContext().getSystem().settings().config());
	}

	private long _getTimeout(Config config) {

		String timeoutText = "PT10m";

		String path = "openk9.pipeline.http.timeout";

		if (config.hasPathOrNull(path) && !config.getIsNull(path)) {
			timeoutText = config.getString(path);
			if (!timeoutText.startsWith("PT")) {
				timeoutText = "PT" + timeoutText;
			}
		}

		try {
			return Duration.parse(timeoutText).toMillis();
		}
		catch (Exception e) {
			getContext().getLog().warn(
				"Invalid timeout value: {}", timeoutText);
			return Duration.ofMinutes(10).toMillis();
		}

	}

	public static Behavior<Command> create(boolean async) {
		return Behaviors.setup(param -> new Processor(param, async));
	}

	private final boolean async;
	private final ActorRef<Token.Command> tokenActorRef;
	public sealed interface Command {}
	public record Start(
		String url, JsonObject jsonObject, ActorRef<Response> replyTo) implements Command {}
	public record Callback(String tokenId, JsonObject jsonObject) implements Command {}
	private record TokenResponseWrapper(Token.TokenResponse response) implements Command {}
	private record TokenValidationResponseWrapper(Token.TokenValidationResponse response) implements Command {}
	private record ResponseWrapper(Http.Response response, ActorRef<Response> replyTo) implements Command {}
	public sealed interface Response {}
	public record Error(String message) implements Response {}
	public record Body(JsonObject jsonObject) implements Response {}

}
