package io.openk9.datasource.pipeline.actor.enrichitem;

import akka.actor.Cancellable;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.vertx.core.json.JsonObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Token {

	public sealed interface Command {}
	public record Generate(ActorRef<Response> replyTo) implements Command {}
	public record Callback(String token, JsonObject jsonObject) implements Command {}
	private enum Tick implements Command {INSTANCE}
	public sealed interface Response {}
	public record TokenGenerated(String token) implements Response {}
	public record TokenCallback(JsonObject jsonObject) implements Response {}
	public enum TokenState implements Response {EXPIRED, VALID}

	private record TokenInfo(LocalDateTime creationDate, ActorRef<Response> replyTo) {}

	public static Behavior<Command> create() {
		return create(-1);
	}

	public static Behavior<Command> create(long validityTokenMillis) {
		return Behaviors.<Command>supervise(
			Behaviors
				.setup(ctx -> initial(validityTokenMillis, new HashMap<>(), ctx, null)))
			.onFailure(SupervisorStrategy.resume());
	}

	private static Behavior<Command> initial(
		long validityTokenMillis, Map<String, TokenInfo> tokens,
		ActorContext<Command> ctx, Cancellable cancellable) {

		Cancellable newCancellable;

		if (cancellable == null) {
			if (validityTokenMillis != -1) {
				newCancellable = ctx.scheduleOnce(
					Duration.ofSeconds(1), ctx.getSelf(), Tick.INSTANCE);
			}
			else {
				newCancellable = ctx.scheduleOnce(
					Duration.ofMinutes(15), ctx.getSelf(), Tick.INSTANCE);
			}
		}
		else {
			newCancellable = cancellable;
		}

		return Behaviors
			.receive(Command.class)
			.onMessage(Generate.class, generate -> onGenerate(
				validityTokenMillis, generate.replyTo(), tokens, ctx, newCancellable))
			.onMessage(Callback.class, callback -> onCallback(
				validityTokenMillis, callback.token(), callback.jsonObject, tokens, ctx, newCancellable))
			.onMessage(Tick.class, tick -> onTick(validityTokenMillis, tokens, ctx, newCancellable))
			.build();
	}

	private static Behavior<Command> onCallback(
		long validityTokenMillis, String token, JsonObject jsonObject,
		Map<String, TokenInfo> tokens, ActorContext<Command> ctx,
		Cancellable cancellable) {

		TokenInfo tokenInfo = tokens.get(token);

		if (tokenInfo == null) {
			ctx.getLog().warn("Token not found: {}", token);
			return Behaviors.same();
		}

		if (validityTokenMillis == -1 || isValid(validityTokenMillis, tokenInfo)) {

			ctx.getLog()
				.info(
					"Token found: {}, elapsed: {} ms",
					token, Duration.between(
						tokenInfo.creationDate,
						LocalDateTime.now()
					).toMillis());

			tokenInfo.replyTo.tell(new TokenCallback(jsonObject));

		}
		else {
			Map<String, TokenInfo> newMap = new HashMap<>(tokens);
			newMap.remove(token, tokenInfo);
			ctx.getLog().warn("Token expired: {}", token);
			tokenInfo.replyTo.tell(TokenState.EXPIRED);
			return initial(validityTokenMillis, newMap, ctx, cancellable);
		}

		return Behaviors.same();
	}

	private static Behavior<Command> onTick(
		long validityTokenMillis, Map<String, TokenInfo> tokens,
		ActorContext<Command> ctx, Cancellable cancellable) {

		ctx.getLog().info("Start token cleanup");

		if (tokens.isEmpty()) {
			return Behaviors.same();
		}

		Map<String, TokenInfo> newTokens = new HashMap<>();

		for (Map.Entry<String, TokenInfo> entry : tokens.entrySet()) {
			TokenInfo value = entry.getValue();
			if (isValid(validityTokenMillis, value)) {
				newTokens.put(entry.getKey(), value);
			}
			else {
				ctx.getLog().warn("Token expired: {}", entry.getKey());
				value.replyTo.tell(TokenState.EXPIRED);
			}
		}

		ctx.getLog().info("token expire count: {}", tokens.size() - newTokens.size());

		return initial(validityTokenMillis, newTokens, ctx, cancellable);
	}

	private static Behavior<Command> onGenerate(
		long validityTokenMillis, ActorRef<Response> replyTo,
		Map<String, TokenInfo> tokens, ActorContext<Command> ctx,
		Cancellable cancellable) {

		String token = generateToken();

		Map<String, TokenInfo> newTokens = new HashMap<>(tokens);

		newTokens.put(token, new TokenInfo(LocalDateTime.now(), replyTo));

		replyTo.tell(new TokenGenerated(token));

		return initial(validityTokenMillis, newTokens, ctx, cancellable);
	}

	private static boolean isValid(
		long validityTokenMillis, TokenInfo createDate) {

		return validityTokenMillis != -1 &&
			   LocalDateTime
				   .now()
				   .isBefore(createDate.creationDate.plus(validityTokenMillis, ChronoUnit.MILLIS));

	}

	private static String generateToken() {
		return UUID.randomUUID().toString();
	}

}