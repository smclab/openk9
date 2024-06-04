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
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.events.DatasourceEventBus;
import io.openk9.datasource.events.DatasourceMessage;
import io.openk9.datasource.pipeline.stages.closing.Protocol;
import io.openk9.datasource.service.SchedulerService;

import javax.enterprise.inject.spi.CDI;

public class DeletionCompareNotifier extends AbstractBehavior<Protocol.Command> {

	private final SchedulerService service;
	private final DatasourceEventBus sender;
	private final SchedulingKey schedulingKey;

	public DeletionCompareNotifier(
		ActorContext<Protocol.Command> context,
		SchedulingKey schedulingKey) {
		super(context);
		this.service = CDI.current().select(SchedulerService.class).get();
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
			.onMessageEquals(Stop.INSTANCE, this::onStop)
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

			VertxUtil.runOnContext(
				() -> service.getDeletedContentIds(tenantName, scheduleId),
				list -> {

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

					replyTo.tell(Success.INSTANCE);
					getContext().getSelf().tell(Stop.INSTANCE);
				}
			);

		}
		else {

			getContext()
				.getLog()
				.info(
					"Nothing to compare for tenant {} scheduleId {}",
					tenantName, scheduleId
				);

			replyTo.tell(Success.INSTANCE);
			getContext().getSelf().tell(Stop.INSTANCE);

		}
		return Behaviors.same();
	}

	private Behavior<Protocol.Command> onStop() {
		return Behaviors.stopped();
	}

	private enum Stop implements Protocol.Command {
		INSTANCE
	}

	public enum Success implements Protocol.Reply {
		INSTANCE
	}

}
