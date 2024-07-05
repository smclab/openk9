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

package io.openk9.datasource.pipeline.actor.closing;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.pipeline.actor.common.AggregateItem;
import io.openk9.datasource.pipeline.service.InternalVectorPipelineIngestionService;
import io.openk9.datasource.pipeline.stages.closing.CloseStage;
import io.vertx.core.json.JsonObject;

public class SendLast extends AbstractBehavior<AggregateItem.Command> {

	private final ShardingKey shardingKey;
	private final byte[] LAST_MESSAGE = JsonObject
		.of("last", true)
		.toBuffer()
		.getBytes();

	public SendLast(
		ActorContext<AggregateItem.Command> context,
		ShardingKey shardingKey) {
		super(context);
		this.shardingKey = shardingKey;
	}

	public static Behavior<AggregateItem.Command> create(ShardingKey shardingKey) {

		return Behaviors.setup(ctx -> new SendLast(ctx, shardingKey));

	}

	@Override
	public Receive<AggregateItem.Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(CloseStage.StartHandler.class, this::onStart)
			.onMessage(Stop.class, this::onStop)
			.build();
	}

	private Behavior<AggregateItem.Command> onStart(CloseStage.StartHandler startHandler) {

		getContext().pipeToSelf(
			InternalVectorPipelineIngestionService.send(shardingKey, LAST_MESSAGE),
			(result, exception) -> new Stop(startHandler.replyTo())
		);

		return this;
	}

	private Behavior<AggregateItem.Command> onStop(Stop stop) {

		stop.replyTo.tell(Success.INSTANCE);

		return Behaviors.stopped();
	}

	public enum Success implements AggregateItem.Reply {
		INSTANCE
	}

	private record Stop(ActorRef<AggregateItem.Reply> replyTo) implements AggregateItem.Command {}

}
