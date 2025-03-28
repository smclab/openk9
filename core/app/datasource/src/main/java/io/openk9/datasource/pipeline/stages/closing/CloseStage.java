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

package io.openk9.datasource.pipeline.stages.closing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.common.AggregateBehavior;
import io.openk9.datasource.pipeline.actor.common.AggregateBehaviorException;
import io.openk9.datasource.pipeline.actor.common.AggregateItem;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;

public class CloseStage extends AggregateBehavior {

	public CloseStage(
		ActorContext<Command> context,
		ActorRef<Response> replyTo,
		List<ActorRef<AggregateItem.Command>> handlers,
		Function<List<AggregateItem.Reply>, Response> aggregator) {

		super(context, handlers, replyTo, aggregator);
	}

	public static Behavior<Command> create(
		ShardingKey shardingKey,
		ActorRef<Response> replyTo,
		Configurations configurations) {

		return Behaviors.setup(ctx -> {
			List<ActorRef<AggregateItem.Command>> handlers = new ArrayList<>();

			for (Function<ShardingKey, Behavior<AggregateItem.Command>> handlerFactory : configurations.handlersFactories()) {
				var handler = ctx.spawnAnonymous(handlerFactory.apply(shardingKey));
				handlers.add(handler);
			}

			return new CloseStage(ctx, replyTo, handlers, configurations.aggregator());
		});
	}

	public record Configurations(
		Function<List<AggregateItem.Reply>, AggregateBehavior.Response> aggregator,
		List<Function<ShardingKey, Behavior<AggregateItem.Command>>> handlersFactories
	) {}

	@Override
	protected AggregateItem.Starter mapCommand(Starter starter) {
		if (starter instanceof CloseStage.Start start) {
			var scheduler = start.scheduler();
			return new StartHandler(scheduler, handlerAdapter);
		}

		throw new AggregateBehaviorException();
	}

	public record Start(SchedulerDTO scheduler) implements Starter {}

	public record Aggregated(Scheduler.SchedulerStatus status) implements Response {}

	public record StartHandler(
		SchedulerDTO scheduler,
		ActorRef<AggregateItem.Reply> replyTo
	) implements AggregateItem.Starter {}

}
