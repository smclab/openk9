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

package io.openk9.datasource.pipeline.stages.working;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.datasource.pipeline.actor.common.AggregateBehavior;
import io.openk9.datasource.pipeline.actor.common.AggregateBehaviorException;
import io.openk9.datasource.pipeline.actor.common.AggregateItem;

import java.util.ArrayList;
import java.util.List;

public class EndProcess extends AggregateBehavior {

	public EndProcess(
		ActorContext<Command> context,
		List<ActorRef<AggregateItem.Command>> handlers,
		ActorRef<Response> replyTo) {

		super(context, EndProcess.handlers(context, handlers), replyTo, EndProcess::aggregator);
	}

	public static Behavior<AggregateBehavior.Command> create(
		List<ActorRef<AggregateItem.Command>> handlers,
		ActorRef<Response> replyTo) {

		return Behaviors.setup(ctx -> new EndProcess(ctx, handlers, replyTo));
	}

	@Override
	protected AggregateItem.Starter mapCommand(Starter starter) {

		if (starter instanceof Start start) {
			var payload = start.payload();
			var heldMessage = start.heldMessage();
			return new StartHandler(payload, heldMessage, handlerAdapter);
		}

		throw new AggregateBehaviorException();
	}

	private static List<ActorRef<AggregateItem.Command>> handlers(
		ActorContext<AggregateBehavior.Command> context,
		List<ActorRef<AggregateItem.Command>> handlers) {

		ActorRef<AggregateItem.Command> basicHandler = context.spawnAnonymous(Behaviors.setup(ctx ->
			Behaviors.receive(AggregateItem.Command.class)
				.onMessage(StartHandler.class, param -> {
					param.replyTo.tell(new HandlerReply(param.heldMessage));
					return Behaviors.stopped();
				})
				.build()
		));

		List<ActorRef<AggregateItem.Command>> newHandlers = new ArrayList<>(
			handlers != null ? handlers : List.of());

		newHandlers.add(basicHandler);

		return newHandlers;
	}

	private static EndProcessDone aggregator(List<AggregateItem.Reply> replies) {

		for (AggregateItem.Reply reply : replies) {
			if (reply instanceof HandlerReply handlerReply) {
				return new EndProcessDone(handlerReply.heldMessage());
			}
		}

		throw new AggregateBehaviorException();

	}

	public record Start(byte[] payload, HeldMessage heldMessage) implements Starter {}

	public record StartHandler(
		byte[] payload, HeldMessage heldMessage, ActorRef<AggregateItem.Reply> replyTo
	)
		implements AggregateItem.Starter {}

	public record HandlerReply(HeldMessage heldMessage) implements AggregateItem.Reply {}

	public record EndProcessDone(HeldMessage heldMessage) implements AggregateBehavior.Response {}


}
