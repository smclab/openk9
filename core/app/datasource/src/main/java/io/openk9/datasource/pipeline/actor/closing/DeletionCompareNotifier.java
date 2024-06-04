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
import io.openk9.datasource.events.DatasourceEventBus;
import io.openk9.datasource.events.DatasourceMessage;
import io.openk9.datasource.pipeline.service.SchedulingService;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.closing.Protocol;

import java.util.List;
import javax.enterprise.inject.spi.CDI;

public class DeletionCompareNotifier extends AbstractBehavior<Protocol.Command> {

	private final DatasourceEventBus sender;
	private final SchedulingKey schedulingKey;

	public DeletionCompareNotifier(
		ActorContext<Protocol.Command> context,
		SchedulingKey schedulingKey) {

		super(context);
		this.sender = CDI.current().select(DatasourceEventBus.class).get();
		this.schedulingKey = schedulingKey;
	}

	public static Behavior<Protocol.Command> create(SchedulingKey key) {
		return Behaviors.setup(ctx -> new DeletionCompareNotifier(ctx, key));
	}

	@Override
	public Receive<Protocol.Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Protocol.Start.class, this::onStart)
			.onMessage(SendEvents.class, this::onSendEvents)
			.onMessage(Stop.class, this::onStop)
			.build();
	}

	private Behavior<Protocol.Command> onStart(Protocol.Start start) {

		var replyTo = start.replyTo();
		var scheduler = start.scheduler();
		String tenantName = schedulingKey.tenantId();
		String scheduleId = schedulingKey.scheduleId();

		if (scheduler.isReindex()) {

			getContext()
				.getLog()
				.info(
					"Compare index and sending notification for tenant {} scheduleId {} for deleted content",
					tenantName,
					scheduleId
				);


			getContext().pipeToSelf(
				SchedulingService.getDeletedContentIds(schedulingKey),
				(list, throwable) -> new SendEvents(list, throwable, scheduler, replyTo)
			);

		}
		else {

			getContext()
				.getLog()
				.info(
					"Nothing to compare for tenant {} scheduleId {}",
					tenantName, scheduleId
				);

			getContext().getSelf().tell(new Stop(replyTo));

		}

		return Behaviors.same();
	}

	private Behavior<Protocol.Command> onSendEvents(SendEvents sendEvents) {
		var scheduler = sendEvents.scheduler();
		var list = sendEvents.list();
		var tenantId = schedulingKey.tenantId();
		var replyTo = sendEvents.replyTo();

		Long datasourceId = scheduler.getDatasourceId();
		String newDataIndexName = scheduler.getNewDataIndexName();

		for (String deletedContentId : list) {
			sender.sendEvent(
				DatasourceMessage
					.Delete
					.builder()
					.indexName(newDataIndexName)
					.datasourceId(datasourceId)
					.tenantId(tenantId)
					.contentId(deletedContentId)
					.build()
			);
		}

		getContext().getSelf().tell(new Stop(replyTo));

		return Behaviors.same();
	}

	private Behavior<Protocol.Command> onStop(Stop stop) {
		stop.replyTo().tell(Success.INSTANCE);
		return Behaviors.stopped();
	}

	private record Stop(ActorRef<Protocol.Reply> replyTo) implements Protocol.Command {}

	public enum Success implements Protocol.Reply {
		INSTANCE
	}

	private record SendEvents(
		List<String> list,
		Throwable throwable,
		SchedulerDTO scheduler,
		ActorRef<Protocol.Reply> replyTo
	) implements Protocol.Command {}

}
