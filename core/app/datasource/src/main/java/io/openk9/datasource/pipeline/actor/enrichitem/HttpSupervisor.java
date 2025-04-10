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

import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.util.CborSerializable;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.RecipientRef;
import org.apache.pekko.actor.typed.SupervisorStrategy;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding;

import java.time.LocalDateTime;

public class HttpSupervisor extends AbstractBehavior<HttpSupervisor.Command> {

	public HttpSupervisor(
		ActorContext<Command> context, ShardingKey key) {

		super(context);
		ClusterSharding clusterSharding = ClusterSharding.get(context.getSystem());
		this.tokenActorRef = clusterSharding.entityRefFor(Token.ENTITY_TYPE_KEY, key.asString());
	}

	public static Behavior<Command> create(ShardingKey key) {
		return Behaviors
			.<Command>supervise(Behaviors.setup(ctx -> new HttpSupervisor(ctx, key)))
			.onFailure(SupervisorStrategy.resume());
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Call.class, this::onCall)
			.onMessage(ResponseWrapper.class, wrapper -> {

				HttpProcessor.Response response = wrapper.response;

				ActorRef<Response> replyTo = wrapper.replyTo;

				if (response instanceof HttpProcessor.Body) {
					HttpProcessor.Body ok = (HttpProcessor.Body) response;
					replyTo.tell(new Body(ok.body()));
				}
				else {
					HttpProcessor.Error error = (HttpProcessor.Error) response;
					replyTo.tell(new Error(error.message()));
				}

				return Behaviors.same();

			})
			.build();
	}

	private Behavior<Command> onCall(Call call) {

		ActorRef<HttpProcessor.Command> httpProcessor =
			getContext().spawnAnonymous(
				HttpProcessor.create(call.async, tokenActorRef));

		ActorRef<HttpProcessor.Response> httpProcessorAdapter =
			getContext().messageAdapter(
				HttpProcessor.Response.class,
				response -> new ResponseWrapper(response, call.replyTo));

		httpProcessor.tell(new HttpProcessor.Start(
			call.url,
			call.jsonObject,
			call.expiredDate,
			httpProcessorAdapter
		));

		return Behaviors.same();

	}

	private final RecipientRef<Token.Command> tokenActorRef;

	public sealed interface Command extends CborSerializable {}
	public record Call(
		boolean async, String url, byte[] jsonObject,
		LocalDateTime expiredDate, ActorRef<Response> replyTo) implements Command {}
	private record ResponseWrapper(HttpProcessor.Response response, ActorRef<Response> replyTo) implements Command {}
	public sealed interface Response extends CborSerializable {}
	public record Body(byte[] jsonObject) implements Response {}
	public record Error(String error) implements Response {}


}
