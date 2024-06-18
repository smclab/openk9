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

package io.openk9.datasource.pipeline.actor.common;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AggregateBehavior extends AbstractBehavior<AggregateBehavior.Command> {

	private static final Logger log = Logger.getLogger(AggregateBehavior.class);
	protected final int expectedReplies;
	protected final List<ActorRef<AggregateProtocol.Command>> handlers;
	protected final ActorRef<Response> replyTo;
	protected final ActorRef<AggregateProtocol.Reply> handlerAdapter;
	protected final Function<List<AggregateProtocol.Reply>, Response> aggregator;
	protected final List<AggregateProtocol.Reply> replies = new ArrayList<>();

	protected AggregateBehavior(
		ActorContext<Command> context,
		List<ActorRef<AggregateProtocol.Command>> handlers,
		ActorRef<Response> replyTo,
		Function<List<AggregateProtocol.Reply>, Response> aggregator) {

		super(context);

		this.handlers = handlers;
		this.expectedReplies = handlers.size();
		this.replyTo = replyTo;
		this.handlerAdapter = getContext().messageAdapter(
			AggregateProtocol.Reply.class, HandlerReply::new);
		this.aggregator = aggregator;
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Start.class, this::onStart)
			.build();
	}

	public Behavior<Command> collectAndAggregate() {
		if (replies.size() == expectedReplies) {
			if (log.isDebugEnabled()) {
				log.debugf(
					"Aggregating response and reply. [ReplyTo: %s]",
					replyTo
				);
			}

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
		Consumer<HandlerContext> invokeHandler = this::invokeHandler;

		for (ActorRef<AggregateProtocol.Command> handler : handlers) {
			var handlerContext = new HandlerContext(start, handler);
			invokeHandler.accept(handlerContext);
		}

		return collectAndAggregate();
	}

	protected abstract void invokeHandler(HandlerContext handlerContext);

	private Behavior<Command> onHandlerReply(HandlerReply handlerReply) {
		this.replies.add((AggregateProtocol.Reply) handlerReply.reply());

		if (log.isDebugEnabled()) {
			log.debugf(
				"Received %s of %s expected replies. [ReplyTo: %s]",
				replies.size(),
				expectedReplies,
				replyTo
			);
		}

		return collectAndAggregate();
	}

	public interface Command {}

	public interface Start extends Command {}

	public interface Response {}

	private record HandlerReply(Object reply) implements Command {}

	protected record HandlerContext(
		Start start,
		ActorRef<AggregateProtocol.Command> handler
	) {}

}
