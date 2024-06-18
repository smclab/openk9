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
import java.util.function.Function;

public abstract class AggregateBehavior extends AbstractBehavior<AggregateBehavior.Command> {

	private static final Logger log = Logger.getLogger(AggregateBehavior.class);
	protected final int expectedReplies;
	protected final List<ActorRef<AggregateItem.Command>> handlers;
	protected final ActorRef<Response> replyTo;
	protected final ActorRef<AggregateItem.Reply> handlerAdapter;
	protected final Function<List<AggregateItem.Reply>, Response> aggregator;
	protected final List<AggregateItem.Reply> replies = new ArrayList<>();

	protected AggregateBehavior(
		ActorContext<Command> context,
		List<ActorRef<AggregateItem.Command>> handlers,
		ActorRef<Response> replyTo,
		Function<List<AggregateItem.Reply>, Response> aggregator) {

		super(context);

		this.handlers = handlers;
		this.expectedReplies = handlers.size();
		this.replyTo = replyTo;
		this.handlerAdapter = getContext().messageAdapter(
			AggregateItem.Reply.class, HandlerReply::new);
		this.aggregator = aggregator;
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Starter.class, this::onStart)
			.build();
	}

	public Behavior<Command> aggregation() {
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

	/**
	 * Map the starter for the handlers.
	 *
	 * @param starter the command that started the AggregateBehavior, received from the caller.
	 */
	protected abstract AggregateItem.Starter mapCommand(Starter starter);

	private Behavior<Command> onStart(Starter starter) {

		for (ActorRef<AggregateItem.Command> handler : handlers) {
			var command = mapCommand(starter);

			handler.tell(command);
		}

		return aggregation();
	}

	private Behavior<Command> onHandlerReply(HandlerReply handlerReply) {
		this.replies.add((AggregateItem.Reply) handlerReply.reply());

		if (log.isDebugEnabled()) {
			log.debugf(
				"Received %s of %s expected replies. [ReplyTo: %s]",
				replies.size(),
				expectedReplies,
				replyTo
			);
		}

		return aggregation();
	}

	public sealed interface Command {}

	public non-sealed interface Starter extends Command {}

	public interface Response {}

	private record HandlerReply(Object reply) implements Command {}

}
