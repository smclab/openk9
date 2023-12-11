package io.openk9.datasource.pipeline.actor.enrichitem;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.RecipientRef;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.datasource.pipeline.actor.common.Http;
import io.openk9.datasource.util.CborSerializable;
import io.vertx.core.json.JsonObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Map;

public class HttpProcessor extends AbstractBehavior<HttpProcessor.Command> {

	public HttpProcessor(
		ActorContext<Command> context,
		RecipientRef<Token.Command> tokenActorRef, boolean async) {
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

				return Behaviors.receive(Command.class)
					.onMessage(TokenResponseWrapper.class, wrapper -> onTokenResponseWrapper(wrapper, replyTo))
					.build();

			}
			else {
				Http.OK ok = (Http.OK) response;
				byte[] body = ok.body();

				Body bodyResponse;

				if (body == null || body.length == 0) {
					bodyResponse = new Body(EMPTY_JSON);
				}
				else {
					bodyResponse = new Body(body);
				}

				replyTo.tell(bodyResponse);

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
			byte[] jsonObject = tokenCallback.jsonObject();
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
		String url, byte[] body, ActorRef<Response> replyTo) {

		ActorRef<Http.Response> responseActorRef =
			getContext().messageAdapter(
				Http.Response.class,
				param -> new ResponseWrapper(param, replyTo)
			);

		ActorRef<Http.Command> commandActorRef =
			getContext().spawnAnonymous(Http.create());

		commandActorRef.tell(new Http.POST(responseActorRef, url, body));

		return newReceiveBuilder()
			.onMessage(ResponseWrapper.class, this::onResponseWrapper)
			.build();
	}

	private Behavior<Command> onStart(Start start) {

		String url = start.url;
		ActorRef<Response> replyTo = start.replyTo;

		if (!isValidUrl(url)) {
			replyTo.tell(new Error("Invalid URL: " + url));
			return Behaviors.stopped();
		}

		byte[] body = start.body;

		if (async) {

			tokenActorRef.tell(new Token.Generate(start.expiredDate, tokenResponseAdapter));

			return waitGenerateToken(url, body, replyTo);

		}

		return started(url, body, replyTo);

	}

	private boolean isValidUrl(String url) {
		try {
			new URL(url);
			return true;
		}
		catch (MalformedURLException e) {
			return false;
		}
	}

	private Behavior<Command> waitGenerateToken(
		String url, byte[] bytes, ActorRef<Response> replyTo) {

		return Behaviors.receive(Command.class)
			.onMessage(
				TokenResponseWrapper.class,
				wrapper -> {

					Token.Response response = wrapper.response;

					if (response instanceof Token.TokenGenerated) {
						Token.TokenGenerated tokenGenerated =
							(Token.TokenGenerated) response;
						JsonObject newJson = new JsonObject();

						JsonObject jsonObject = new JsonObject(new String(bytes));

						for (Map.Entry<String, Object> entry : jsonObject) {
							newJson.put(entry.getKey(), entry.getValue());
						}

						newJson.put("replyTo", tokenGenerated.token());

						return started(url, newJson.toBuffer().getBytes(), replyTo);
					}
					else {
						return Behaviors.same();
					}
				}
			)
			.build();
	}

	public static Behavior<Command> create(boolean async, RecipientRef<Token.Command> tokenActorRef) {
		return Behaviors.setup(param -> new HttpProcessor(param, tokenActorRef, async));
	}

	private final boolean async;
	private final RecipientRef<Token.Command> tokenActorRef;
	private final ActorRef<Token.Response> tokenResponseAdapter;

	public sealed interface Command extends CborSerializable {}
	public record Start(
		String url, byte[] body, LocalDateTime expiredDate,
		ActorRef<Response> replyTo) implements Command {}
	private record TokenResponseWrapper(Token.Response response) implements Command {}
	private record ResponseWrapper(Http.Response response, ActorRef<Response> replyTo) implements Command {}
	public sealed interface Response extends CborSerializable {}
	public record Error(String message) implements Response {}
	public record Body(byte[] body) implements Response {}

	private static final byte[] EMPTY_JSON = new byte[] {123, 125}; // "{}";

}
