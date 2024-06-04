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
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.common.util.SchedulingKey;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CloseStage extends AbstractBehavior<CloseStage.Command> {

	private static final Logger log = Logger.getLogger(CloseStage.class);
	private final int expectedReplies;
	private final List<ActorRef<Protocol.Command>> handlers;
	private final ActorRef<Response> replyTo;
	private final Function<List<Protocol.Reply>, Aggregate> aggregator;
	private final List<Protocol.Reply> replies = new ArrayList<>();
	private final SchedulingKey schedulingKey;
	private SchedulerDTO scheduler;

	public CloseStage(
		ActorContext<Command> context,
		SchedulingKey schedulingKey,
		List<ActorRef<Protocol.Command>> handlers,
		ActorRef<Response> replyTo,
		Function<List<Protocol.Reply>, Aggregate> aggregator) {

		super(context);

		this.schedulingKey = schedulingKey;
		this.handlers = handlers;
		this.expectedReplies = handlers.size();
		this.replyTo = replyTo;
		this.aggregator = aggregator;
	}

	public static Behavior<Command> create(
		SchedulingKey schedulingKey,
		ActorRef<Response> replyTo,
		Function<List<Protocol.Reply>, Aggregate> aggregator,
		List<ActorRef<Protocol.Command>> handlers) {

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
		Function<List<Protocol.Reply>, Aggregate> aggregator,
		Function<SchedulingKey, Behavior<Protocol.Command>>... handlersFactories) {

		return Behaviors.setup(ctx -> {
			List<ActorRef<Protocol.Command>> handlers = new ArrayList<>();

			for (Function<SchedulingKey, Behavior<Protocol.Command>> handlerFactory : handlersFactories) {
				var handler = ctx.spawnAnonymous(handlerFactory.apply(schedulingKey));
				handlers.add(handler);
			}

			return new CloseStage(ctx, schedulingKey, handlers, replyTo, aggregator);
		});
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Start.class, this::onStart)
			.build();
	}

	public Behavior<Command> collectAndAggregate() {
		if (replies.size() == expectedReplies) {
			log.infof(
				"Aggregating response and reply to scheduler %s",
				scheduler.getId()
			);

			var aggregate = aggregator.apply(replies);

			replyTo.tell(aggregate);

			return Behaviors.stopped();
		}
		else {
			return newReceiveBuilder()
				.onMessage(HandlerReply.class, this::onHandlerReply)
				.build();
		}
	}

	public Behavior<Command> onStart(Start start) {
		this.scheduler = start.schedulerDTO();
		var replyTo = getContext().messageAdapter(Protocol.Reply.class, HandlerReply::new);

		log.infof("Starting close handlers for scheduler with id: %s", scheduler.getId());

		for (ActorRef<Protocol.Command> handler : handlers) {
			handler.tell(new Protocol.Start(scheduler, replyTo));
		}

		return collectAndAggregate();
	}

	private Behavior<Command> onHandlerReply(HandlerReply handlerReply) {
		this.replies.add(handlerReply.reply());

		log.infof(
			"Received %s of %s expected replies for scheduler %s.",
			replies.size(),
			expectedReplies,
			scheduler.getId()
		);

		return collectAndAggregate();
	}

	public sealed interface Command {}

	public sealed interface Response {}

	public record Start(SchedulerDTO schedulerDTO) implements Command {}

	private record HandlerReply(Protocol.Reply reply) implements Command {}

	public record Aggregate(Scheduler.SchedulerStatus status) implements Response {}

}
