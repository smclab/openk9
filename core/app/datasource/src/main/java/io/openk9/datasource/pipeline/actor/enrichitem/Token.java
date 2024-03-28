/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import io.openk9.common.util.SchedulingKey;
import io.openk9.datasource.util.CborSerializable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Token extends AbstractBehavior<Token.Command> {

	public static final EntityTypeKey<Token.Command> ENTITY_TYPE_KEY =
		EntityTypeKey.create(Token.Command.class, "tokenKey");
	private final Cancellable cancellable;
	private final SchedulingKey key;
	private final Map<String, TokenInfo> tokens = new HashMap<>();

	public Token(ActorContext<Command> context, SchedulingKey key) {
		super(context);

		this.key = key;
		this.cancellable = getContext()
			.getSystem()
			.scheduler()
			.scheduleAtFixedRate(
				Duration.ZERO, Duration.ofMinutes(15),
				() -> getContext().getSelf().tell(Tick.INSTANCE),
				getContext().getExecutionContext()
			);
	}

	public static Behavior<Command> create(SchedulingKey key) {

		return Behaviors
			.<Command>supervise(Behaviors.setup(ctx -> new Token(ctx, key)))
			.onFailure(SupervisorStrategy.resume());
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Generate.class, this::onGenerate)
			.onMessage(Callback.class, this::onCallback)
			.onMessage(Tick.class, tick -> onTick())
			.build();
	}

	private static boolean isValid(TokenInfo tokenInfo) {
		return tokenInfo.expiredDate().isAfter(LocalDateTime.now());
	}

	private static boolean isExpired(TokenInfo tokenInfo) {
		return tokenInfo.expiredDate().isBefore(LocalDateTime.now());
	}

	private Behavior<Command> onGenerate(Generate generate) {

		SchedulingToken schedulingToken = generateToken();

		ActorRef<Response> replyTo = generate.replyTo;

		tokens.put(
			schedulingToken.token,
			new TokenInfo(LocalDateTime.now(), generate.expiredDate, replyTo)
		);

		replyTo.tell(new TokenGenerated(TokenUtils.encode(schedulingToken)));

		return Behaviors.same();
	}

	private Behavior<Command> onCallback(Callback callback) {

		String token = callback.token;
		TokenInfo tokenInfo = tokens.get(token);

		if (tokenInfo == null) {
			getContext().getLog().warn("Token not found: {}", token);
			return Behaviors.same();
		}

		tokens.remove(token, tokenInfo);

		if (getContext().getLog().isDebugEnabled()) {
			getContext().getLog().debug(
				"Token found: {}, elapsed: {} ms",
				token,
				Duration.between(tokenInfo.createDate, LocalDateTime.now()).toMillis()
			);
		}

		if (isValid(tokenInfo)) {
			if (getContext().getLog().isDebugEnabled()) {
				getContext().getLog().debug("Valid token, response ready to be processed");
			}

			tokenInfo.replyTo.tell(new TokenCallback(callback.jsonObject));
		}
		else {
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

	private SchedulingToken generateToken() {
		return new SchedulingToken(
			key.tenantId(), key.scheduleId(), UUID.randomUUID().toString());
	}

	private enum Tick implements Command {
		INSTANCE
	}

	public enum TokenState implements Response {
		EXPIRED,
		VALID
	}

	public sealed interface Command extends CborSerializable {}

	public sealed interface Response extends CborSerializable {}

	public record Generate(LocalDateTime expiredDate, ActorRef<Response> replyTo)
		implements Command {}

	public record Callback(String token, byte[] jsonObject) implements Command {}

	public record TokenGenerated(String token) implements Response {}

	public record TokenCallback(byte[] jsonObject) implements Response {}

	private record TokenInfo(
		LocalDateTime createDate, LocalDateTime expiredDate, ActorRef<Response> replyTo
	) implements CborSerializable {}

	public record SchedulingToken(String tenantId, String scheduleId, String token) {}

}
