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

package io.openk9.datasource.pipeline.actor.working;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.pipeline.actor.common.AggregateItem;
import io.openk9.datasource.pipeline.service.InternalVectorPipelineIngestionService;
import io.openk9.datasource.pipeline.stages.working.EndProcess;
import io.openk9.datasource.processor.payload.DataPayload;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

public class Forward extends AbstractBehavior<AggregateItem.Command> {

	private final ShardingKey shardingKey;
	private ActorRef<AggregateItem.Reply> replyTo;

	public Forward(
		ActorContext<AggregateItem.Command> context,
		ShardingKey shardingKey) {

		super(context);
		this.shardingKey = shardingKey;
	}

	public static Behavior<AggregateItem.Command> create(ShardingKey shardingKey) {
		return Behaviors.setup(ctx -> new Forward(ctx, shardingKey));
	}

	@Override
	public Receive<AggregateItem.Command> createReceive() {

		return newReceiveBuilder()
			.onMessage(EndProcess.StartHandler.class, this::onStart)
			.onMessageEquals(Sent.INSTANCE, this::onSent)
			.build();

	}

	public Behavior<AggregateItem.Command> onStart(EndProcess.StartHandler start) {

		this.replyTo = start.replyTo();
		var heldMessage = start.heldMessage();

		var payload = start.payload();
		var shardingKey = heldMessage.shardingKey();

		var dataPayload = Json.decodeValue(Buffer.buffer(payload), DataPayload.class);

		getContext().pipeToSelf(
			InternalVectorPipelineIngestionService.send(shardingKey, dataPayload),
			(res, err) -> Sent.INSTANCE
		);


		return this;

	}

	public Behavior<AggregateItem.Command> onSent() {

		this.replyTo.tell(Done.INSTANCE);

		return Behaviors.stopped();

	}

	private enum Sent implements AggregateItem.Command {
		INSTANCE
	}

	private enum Done implements AggregateItem.Reply {
		INSTANCE
	}

}
