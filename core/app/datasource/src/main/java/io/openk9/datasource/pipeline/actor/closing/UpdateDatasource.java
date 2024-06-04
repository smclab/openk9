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
import io.openk9.common.util.SchedulingKey;
import io.openk9.datasource.pipeline.stages.closing.Protocol;
import io.openk9.datasource.service.DatasourceService;

public class UpdateDatasource extends AbstractBehavior<Protocol.Command> {

	private final SchedulingKey schedulingKey;

	protected UpdateDatasource(
		ActorContext<Protocol.Command> context,
		SchedulingKey schedulingKey) {

		super(context);
		this.schedulingKey = schedulingKey;
	}

	public static Behavior<Protocol.Command> create(SchedulingKey schedulingKey) {
		return Behaviors.setup(ctx -> new UpdateDatasource(ctx, schedulingKey));
	}

	@Override
	public Receive<Protocol.Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Protocol.Start.class, this::onStart)
			.onMessage(Stop.class, this::onStop)
			.build();
	}

	private Behavior<Protocol.Command> onStart(Protocol.Start start) {
		var tenantId = schedulingKey.tenantId();
		var scheduler = start.scheduler();
		var lastIngestionDate = scheduler.getLastIngestionDate();
		var newDataIndexId = scheduler.getNewDataIndexId();

		if (lastIngestionDate != null) {

			getContext().pipeToSelf(
				DatasourceService.updateDatasource(
					tenantId, scheduler.getDatasourceId(), lastIngestionDate, newDataIndexId),
				(vo1d, throwable) -> new Stop(start.replyTo())
			);
		}
		else {

			getContext()
				.getSelf()
				.tell(new Stop(start.replyTo()));
		}

		return Behaviors.same();
	}

	private Behavior<Protocol.Command> onStop(Stop stop) {

		stop.replyTo().tell(Success.INSTANCE);

		return Behaviors.stopped();
	}

	public enum Success implements Protocol.Reply {
		INSTANCE
	}

	private record Stop(ActorRef<Protocol.Reply> replyTo) implements Protocol.Command {}

}
