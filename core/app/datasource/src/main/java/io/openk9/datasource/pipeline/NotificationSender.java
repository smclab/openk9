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

package io.openk9.datasource.pipeline;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.common.util.SchedulingKey;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.events.DatasourceEventBus;
import io.openk9.datasource.events.DatasourceMessage;
import io.openk9.datasource.pipeline.actor.dto.SchedulerDTO;
import io.openk9.datasource.service.SchedulerService;

import javax.enterprise.inject.spi.CDI;

public class NotificationSender extends AbstractBehavior<NotificationSender.Command> {

	private final SchedulerService service;
	private final DatasourceEventBus sender;
	private final SchedulingKey schedulingKey;
	private final SchedulerDTO scheduler;
	private final ActorRef<Response> replyTo;

	public NotificationSender(
		ActorContext<Command> context,
		SchedulerDTO scheduler,
		SchedulingKey schedulingKey, ActorRef<Response> replyTo) {
		super(context);
		this.service = CDI.current().select(SchedulerService.class).get();
		this.sender = CDI.current().select(DatasourceEventBus.class).get();
		this.schedulingKey = schedulingKey;
		this.scheduler = scheduler;
		this.replyTo = replyTo;
		context.getSelf().tell(Start.INSTANCE);
	}

	public static Behavior<Command> create(
		SchedulerDTO scheduler, SchedulingKey key, ActorRef<Response> replyTo) {

		return Behaviors.setup(ctx -> new NotificationSender(ctx, scheduler, key, replyTo));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessageEquals(Start.INSTANCE, this::onStart)
			.onMessageEquals(Stop.INSTANCE, this::onStop)
			.build();
	}

	private Behavior<Command> onStart() {

		String tenantName = schedulingKey.tenantId();
		String scheduleId = schedulingKey.scheduleId();

		getContext()
			.getLog()
			.info(
				"Sending notification for tenant {} scheduleId {} for deleted content",
				tenantName, scheduleId
			);

		VertxUtil.runOnContext(() -> service.getDeletedContentIds(tenantName, scheduleId), list -> {

			Long datasourceId = scheduler.getDatasourceId();
			String newDataIndexName = scheduler.getNewDataIndexName();

			for (String deletedContentId : list) {
				sender.sendEvent(
					DatasourceMessage
						.Delete
						.builder()
						.indexName(newDataIndexName)
						.datasourceId(datasourceId)
						.tenantId(tenantName)
						.contentId(deletedContentId)
						.build()
				);
			}
			getContext().getSelf().tell(Stop.INSTANCE);
		});

		return Behaviors.same();
	}

	private Behavior<Command> onStop() {
		replyTo.tell(Success.INSTANCE);
		return Behaviors.stopped();
	}

	public sealed interface Command {}
	private enum Start implements Command {INSTANCE}
	private enum Stop implements Command {INSTANCE}
	public sealed interface Response {}
	public enum Success implements Response {INSTANCE}
}
