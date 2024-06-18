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

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.common.util.SchedulingKey;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.common.AggregateBehavior;
import io.openk9.datasource.pipeline.actor.common.AggregateProtocol;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CloseStage extends AggregateBehavior {

	private final SchedulingKey schedulingKey;

	public CloseStage(
		ActorContext<Command> context,
		SchedulingKey schedulingKey,
		List<ActorRef<AggregateProtocol.Command>> handlers,
		ActorRef<Response> replyTo,
		Function<List<AggregateProtocol.Reply>, Response> aggregator) {

		super(context, handlers, replyTo, aggregator);

		this.schedulingKey = schedulingKey;
	}

	public static Behavior<Command> create(
		SchedulingKey schedulingKey,
		ActorRef<Response> replyTo,
		Function<List<AggregateProtocol.Reply>, Response> aggregator,
		List<ActorRef<AggregateProtocol.Command>> handlers) {

		return Behaviors.setup(ctx -> new CloseStage(
			ctx,
			schedulingKey,
			handlers,
			replyTo,
			aggregator
		));
	}

	@SafeVarargs
	public static Behavior<Command> create(
		SchedulingKey schedulingKey,
		ActorRef<Response> replyTo,
		Function<List<AggregateProtocol.Reply>, Response> aggregator,
		Function<SchedulingKey, Behavior<AggregateProtocol.Command>>... handlersFactories) {

		return Behaviors.setup(ctx -> {
			List<ActorRef<AggregateProtocol.Command>> handlers = new ArrayList<>();

			for (Function<SchedulingKey, Behavior<AggregateProtocol.Command>> handlerFactory : handlersFactories) {
				var handler = ctx.spawnAnonymous(handlerFactory.apply(schedulingKey));
				handlers.add(handler);
			}

			return new CloseStage(ctx, schedulingKey, handlers, replyTo, aggregator);
		});
	}

	@Override
	protected void invokeHandler(HandlerContext handlerContext) {
		var handler = handlerContext.handler();
		var start = handlerContext.start();

		if (start instanceof CloseStage.Start closeStageStart) {
			var scheduler = closeStageStart.scheduler();
			handler.tell(new StartHandler(scheduler, handlerAdapter));
		}
	}

	public record Start(SchedulerDTO scheduler) implements AggregateBehavior.Start {}

	public record Aggregated(Scheduler.SchedulerStatus status) implements Response {}

	public record StartHandler(
		SchedulerDTO scheduler,
		ActorRef<AggregateProtocol.Reply> replyTo
	) implements AggregateProtocol.Command {}

}
