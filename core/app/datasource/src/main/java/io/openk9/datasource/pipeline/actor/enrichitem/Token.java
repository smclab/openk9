package io.openk9.datasource.pipeline.actor.enrichitem;

import akka.actor.Cancellable;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.datasource.util.CborSerializable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Token {

	public sealed interface Command extends CborSerializable {}
	public record Generate(LocalDateTime expiredDate, ActorRef<Response> replyTo) implements Command {}
	public record Callback(String token, byte[] jsonObject) implements Command {}
	private enum Tick implements Command {INSTANCE}
	public sealed interface Response extends CborSerializable {}
	public record TokenGenerated(String token) implements Response {}
	public record TokenCallback(byte[] jsonObject) implements Response {}
	public enum TokenState implements Response {EXPIRED, VALID}

	private record TokenInfo(
		LocalDateTime createDate, LocalDateTime expiredDate, ActorRef<Response> replyTo) implements CborSerializable {}

	public static Behavior<Command> create() {
		return create(-1);
	}

	public static Behavior<Command> create(long validityTokenMillis) {
		return Behaviors.<Command>supervise(
			Behaviors
				.setup(ctx -> initial(new HashMap<>(), ctx, null)))
			.onFailure(SupervisorStrategy.resume());
	}

	private static Behavior<Command> initial(
		Map<String, TokenInfo> tokens,
		ActorContext<Command> ctx, Cancellable cancellable) {

		Cancellable newCancellable;

		if (cancellable == null) {
			newCancellable = ctx.scheduleOnce(
				Duration.ofMinutes(15), ctx.getSelf(), Tick.INSTANCE);
		}
		else {
			newCancellable = cancellable;
		}

		return Behaviors
			.receive(Command.class)
			.onMessage(Generate.class, generate -> onGenerate(
				generate.replyTo(), generate.expiredDate(), tokens, ctx, newCancellable))
			.onMessage(Callback.class, callback -> onCallback(
				callback.token(), callback.jsonObject, tokens, ctx, newCancellable))
			.onMessage(Tick.class, tick -> onTick(tokens, ctx, newCancellable))
			.build();
	}

	private static Behavior<Command> onCallback(
		String token, byte[] jsonObject,
		Map<String, TokenInfo> tokens, ActorContext<Command> ctx,
		Cancellable cancellable) {

		TokenInfo tokenInfo = tokens.get(token);

		if (tokenInfo == null) {
			ctx.getLog().warn("Token not found: {}", token);
			return Behaviors.same();
		}

		if (isValid(tokenInfo)) {

			ctx.getLog()
				.info(
					"Token found: {}, elapsed: {} ms",
					token, Duration.between(
						tokenInfo.createDate,
						LocalDateTime.now()
					).toMillis());

			tokenInfo.replyTo.tell(new TokenCallback(jsonObject));

		}
		else {
			Map<String, TokenInfo> newMap = new HashMap<>(tokens);
			newMap.remove(token, tokenInfo);
			ctx.getLog().warn("Token expired: {}", token);
			tokenInfo.replyTo.tell(TokenState.EXPIRED);
			return initial(newMap, ctx, cancellable);
		}

		return Behaviors.same();
	}

	private static Behavior<Command> onTick(
		Map<String, TokenInfo> tokens,
		ActorContext<Command> ctx, Cancellable cancellable) {

		ctx.getLog().info("Start token cleanup");

		if (tokens.isEmpty()) {
			return Behaviors.same();
		}

		Map<String, TokenInfo> newTokens = new HashMap<>();

		for (Map.Entry<String, TokenInfo> entry : tokens.entrySet()) {
			TokenInfo value = entry.getValue();
			if (isValid(value)) {
				newTokens.put(entry.getKey(), value);
			}
			else {
				ctx.getLog().warn("Token expired: {}", entry.getKey());
				value.replyTo.tell(TokenState.EXPIRED);
			}
		}

		ctx.getLog().info("token expire count: {}", tokens.size() - newTokens.size());

		return initial(newTokens, ctx, cancellable);

	}

	private static Behavior<Command> onGenerate(
		ActorRef<Response> replyTo, LocalDateTime expiredDate,
		Map<String, TokenInfo> tokens, ActorContext<Command> ctx,
		Cancellable cancellable) {

		String token = generateToken();

		Map<String, TokenInfo> newTokens = new HashMap<>(tokens);

		newTokens.put(token, new TokenInfo(LocalDateTime.now(), expiredDate, replyTo));

		replyTo.tell(new TokenGenerated(token));

		return initial(newTokens, ctx, cancellable);
	}

	private static boolean isValid(TokenInfo tokenInfo) {
		return tokenInfo.expiredDate().isAfter(LocalDateTime.now());
	}

	private static boolean isExpired(TokenInfo tokenInfo) {
		return tokenInfo.expiredDate().isBefore(LocalDateTime.now());
	}

	private static String generateToken() {
		return UUID.randomUUID().toString();
	}

}
