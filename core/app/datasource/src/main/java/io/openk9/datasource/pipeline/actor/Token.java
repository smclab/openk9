package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Token {

	public sealed interface Command {}
	public record Generate(ActorRef<TokenResponse> replyTo) implements Command {}
	public record Validate(String token, ActorRef<TokenValidationResponse> replyTo) implements Command {}
	private enum Tick implements Command {INSTANCE}
	public sealed interface Response {}
	public record TokenResponse(String token) implements Response {}
	public record TokenValidationResponse(boolean valid) implements Response {}

	public static Behavior<Command> create(long validityTokenMillis) {
		return Behaviors.setup(ctx -> initial(validityTokenMillis, new HashMap<>(), ctx));
	}

	private static Behavior<Command> initial(
		long validityTokenMillis, Map<String, LocalDateTime> tokens,
		ActorContext<Command> ctx) {

		ctx.scheduleOnce(Duration.ofSeconds(1), ctx.getSelf(), Tick.INSTANCE);

		return Behaviors
			.receive(Command.class)
			.onMessage(Generate.class, generate -> onGenerate(validityTokenMillis, generate.replyTo(), tokens, ctx))
			.onMessage(Validate.class, validate -> onValidate(
				validityTokenMillis, validate.token(), validate.replyTo(), tokens, ctx))
			.onMessage(Tick.class, tick -> onTick(validityTokenMillis, tokens, ctx))
			.build();
	}

	private static Behavior<Command> onValidate(
		long validityTokenMillis, String token,
		ActorRef<TokenValidationResponse> replyTo,
		Map<String, LocalDateTime> tokens, ActorContext<Command> ctx) {

		replyTo.tell(
			new TokenValidationResponse(
				isValid(validityTokenMillis, token, tokens)));

		return Behaviors.same();
	}

	private static Behavior<Command> onTick(
		long validityTokenMillis, Map<String, LocalDateTime> tokens,
		ActorContext<Command> ctx) {

		if (tokens.isEmpty()) {
			return Behaviors.same();
		}

		Map<String, LocalDateTime> newTokens = new HashMap<>();

		for (Map.Entry<String, LocalDateTime> entry : tokens.entrySet()) {
			if (isValid(validityTokenMillis, entry.getValue())) {
				newTokens.put(entry.getKey(), entry.getValue());
			}
		}

		return initial(validityTokenMillis, newTokens, ctx);
	}

	private static Behavior<Command> onGenerate(
		long validityTokenMillis, ActorRef<TokenResponse> replyTo,
		Map<String, LocalDateTime> tokens, ActorContext<Command> ctx) {

		String token = generateToken();

		Map<String, LocalDateTime> newTokens = new HashMap<>(tokens);

		newTokens.put(token, LocalDateTime.now());

		replyTo.tell(new TokenResponse(token));

		return initial(validityTokenMillis, newTokens, ctx);
	}

	private static boolean isValid(
		long validityTokenMillis, String token,
		Map<String, LocalDateTime> tokens) {

		LocalDateTime createDate = tokens.get(token);

		if (createDate == null) {
			return false;
		}

		return isValid(validityTokenMillis, createDate);

	}

	private static boolean isValid(
		long validityTokenMillis, LocalDateTime createDate) {

		return LocalDateTime
			.now()
			.isBefore(createDate.plus(validityTokenMillis, ChronoUnit.MILLIS));

	}

	private static String generateToken() {
		return UUID.randomUUID().toString();
	}


}
