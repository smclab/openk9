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

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.common.util.SchedulingKey;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.common.AggregateProtocol;
import io.openk9.datasource.pipeline.stages.closing.CloseStage;
import org.jboss.logging.Logger;

public class EvaluateStatus extends AbstractBehavior<AggregateProtocol.Command> {

	private final static Logger log = Logger.getLogger(EvaluateStatus.class);
	private final SchedulingKey key;

	public EvaluateStatus(ActorContext<AggregateProtocol.Command> context, SchedulingKey key) {
		super(context);
		this.key = key;
	}

	public static Behavior<AggregateProtocol.Command> create(SchedulingKey schedulingKey) {
		return Behaviors.setup(ctx -> new EvaluateStatus(ctx, schedulingKey));
	}

	@Override
	public Receive<AggregateProtocol.Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(CloseStage.StartHandler.class, this::onStart)
			.onMessageEquals(Stop.INSTANCE, this::onStop)
			.build();
	}

	public Behavior<AggregateProtocol.Command> onStart(CloseStage.StartHandler start) {
		var scheduler = start.scheduler();
		var replyTo = start.replyTo();

		log.infof("Evaluates status on end for %s", key);

		var status = Scheduler.SchedulerStatus.FINISHED;

		if (scheduler.getLastIngestionDate() == null && !scheduler.isNewIndex()) {
			log.infof(
				"Nothing was changed during this Scheduling on %s index.",
				scheduler.getOldDataIndexName()
			);
		}

		if (scheduler.getLastIngestionDate() == null && scheduler.isNewIndex()) {
			log.warnf(
				"LastIngestionDate was null, " +
				"means that no content was received in this Scheduling. " +
				"%s will be cancelled",
				key
			);

			status = Scheduler.SchedulerStatus.CANCELLED;
		}

		replyTo.tell(new Success(status));
		getContext().getSelf().tell(Stop.INSTANCE);

		return this;
	}

	public Behavior<AggregateProtocol.Command> onStop() {

		return Behaviors.stopped();
	}

	private enum Stop implements AggregateProtocol.Command {
		INSTANCE
	}

	public record Success(Scheduler.SchedulerStatus status) implements AggregateProtocol.Reply {}

}
