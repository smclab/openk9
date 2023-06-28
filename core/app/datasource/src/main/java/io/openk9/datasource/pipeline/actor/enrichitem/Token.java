package io.openk9.datasource.pipeline.actor.enrichitem;

import akka.actor.Cancellable;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import io.openk9.datasource.pipeline.actor.Schedulation;
import io.openk9.datasource.util.CborSerializable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Token extends AbstractBehavior<Token.Command> {

	public static final EntityTypeKey<Token.Command> ENTITY_TYPE_KEY =
		EntityTypeKey.create(Token.Command.class, "tokenKey");

	public sealed interface Command extends CborSerializable {}
	public record Generate(LocalDateTime expiredDate, ActorRef<Response> replyTo) implements Command {}
	public record Callback(String token, byte[] jsonObject) implements Command {}
	private enum Tick implements Command {INSTANCE}
	public sealed interface Response extends CborSerializable {}
	public record TokenGenerated(String token) implements Response {}
	public record TokenCallback(byte[] jsonObject) implements Response {}
	public enum TokenState implements Response {EXPIRED, VALID}

	public record SchedulationToken(String tenantId, String scheduleId, String token) {}

	private record TokenInfo(
		LocalDateTime createDate, LocalDateTime expiredDate, ActorRef<Response> replyTo) implements CborSerializable {}

	private final Cancellable cancellable;
	private final Schedulation.SchedulationKey key;
	private final Map<String, TokenInfo> tokens = new HashMap<>();

	public Token(ActorContext<Command> context, Schedulation.SchedulationKey key) {
		super(context);

		this.key = key;
		this.cancellable = getContext()
			.getSystem()
			.scheduler()
			.scheduleAtFixedRate(
				Duration.ZERO, Duration.ofMinutes(15),
				() -> getContext().getSelf().tell(Tick.INSTANCE),
				getContext().getExecutionContext());
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Generate.class, this::onGenerate)
			.onMessage(Callback.class, this::onCallback)
			.onMessage(Tick.class, tick -> onTick())
			.build();
	}

	public static Behavior<Command> create(Schedulation.SchedulationKey key) {

		return Behaviors
			.<Command>supervise(Behaviors.setup(ctx -> new Token(ctx, key)))
			.onFailure(SupervisorStrategy.resume());
	}

	private Behavior<Command> onCallback(Callback callback) {

		String token = callback.token;

		TokenInfo tokenInfo = tokens.get(token);

		if (tokenInfo == null) {
			getContext().getLog().warn("Token not found: {}", token);
			return Behaviors.same();
		}

		if (isValid(tokenInfo)) {

			getContext().getLog()
				.info(
					"Token found: {}, elapsed: {} ms",
					token, Duration.between(
						tokenInfo.createDate,
						LocalDateTime.now()
					).toMillis());

			tokenInfo.replyTo.tell(new TokenCallback(callback.jsonObject));

		}
		else {
			tokens.remove(token, tokenInfo);
			getContext().getLog().warn("Token expired: {}", token);
			tokenInfo.replyTo.tell(TokenState.EXPIRED);
		}

		return Behaviors.same();
	}

	private Behavior<Command> onTick() {

		getContext().getLog().info("Start token cleanup");

		if (tokens.isEmpty()) {
			return Behaviors.same();
		}

		int expiredCount = 0;

		for (Map.Entry<String, TokenInfo> entry : tokens.entrySet()) {
			TokenInfo value = entry.getValue();
			if (isValid(value)) {
				tokens.put(entry.getKey(), value);
			}
			else {
				getContext().getLog().warn("Token expired: {}", entry.getKey());
				value.replyTo.tell(TokenState.EXPIRED);
				expiredCount++;
			}
		}

		getContext().getLog().info("token expire count: {}", expiredCount);

		return Behaviors.same();
	}

	private Behavior<Command> onGenerate(Generate generate) {

		SchedulationToken schedulationToken = generateToken();

		ActorRef<Response> replyTo = generate.replyTo;

		tokens.put(
			schedulationToken.token,
			new TokenInfo(LocalDateTime.now(), generate.expiredDate, replyTo));

		replyTo.tell(new TokenGenerated(TokenUtils.encode(schedulationToken)));

		return Behaviors.same();
	}

	private static boolean isValid(TokenInfo tokenInfo) {
		return tokenInfo.expiredDate().isAfter(LocalDateTime.now());
	}

	private static boolean isExpired(TokenInfo tokenInfo) {
		return tokenInfo.expiredDate().isBefore(LocalDateTime.now());
	}

	private SchedulationToken generateToken() {
		return new SchedulationToken(
			key.tenantId(), key.scheduleId(), UUID.randomUUID().toString());
	}

}
