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

import java.util.List;

import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.events.DatasourceEventBus;
import io.openk9.datasource.events.DatasourceMessage;
import io.openk9.datasource.pipeline.actor.common.AggregateItem;
import io.openk9.datasource.pipeline.service.SchedulingService;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.closing.CloseStage;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.jboss.logging.Logger;

public class DeletionCompareNotifier extends AbstractBehavior<AggregateItem.Command> {

	private final ShardingKey shardingKey;

	public DeletionCompareNotifier(
		ActorContext<AggregateItem.Command> context,
		ShardingKey shardingKey) {

		super(context);
		this.shardingKey = shardingKey;
	}

	public static Behavior<AggregateItem.Command> create(ShardingKey key) {
		return Behaviors.setup(ctx -> new DeletionCompareNotifier(ctx, key));
	}

	@Override
	public Receive<AggregateItem.Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(CloseStage.StartHandler.class, this::onStart)
			.onMessage(SendEvents.class, this::onSendEvents)
			.onMessage(Stop.class, this::onStop)
			.build();
	}

	private Behavior<AggregateItem.Command> onStart(
		CloseStage.StartHandler start) {

		var replyTo = start.replyTo();
		var scheduler = start.scheduler();
		String tenantName = shardingKey.tenantId();
		String scheduleId = shardingKey.scheduleId();

		if (scheduler.isReindex() && scheduler.getOldDataIndexId() != null) {

			getContext()
				.getLog()
				.info(
					"Compare index and sending notification for tenant {} scheduleId {} for deleted content",
					tenantName,
					scheduleId
				);


			getContext().pipeToSelf(
				SchedulingService.getDeletedContentIds(shardingKey),
				(list, throwable) -> new SendEvents(
					list, throwable, scheduler, replyTo)
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

	private static Logger log = Logger.getLogger(DeletionCompareNotifier.class);

	private Behavior<AggregateItem.Command> onStop(Stop stop) {
		stop.replyTo().tell(Success.INSTANCE);
		return Behaviors.stopped();
	}

	public enum Success implements AggregateItem.Reply {
		INSTANCE
	}

	private record Stop(ActorRef<AggregateItem.Reply> replyTo)
		implements AggregateItem.Command {}

	private record SendEvents(
		List<String> list,
		Throwable throwable,
		SchedulerDTO scheduler,
		ActorRef<AggregateItem.Reply> replyTo
	) implements AggregateItem.Command {}

	private Behavior<AggregateItem.Command> onSendEvents(SendEvents sendEvents) {
		var scheduler = sendEvents.scheduler();
		var list = sendEvents.list();
		var tenantId = shardingKey.tenantId();
		var replyTo = sendEvents.replyTo();

		Long datasourceId = scheduler.getDatasourceId();
		String newDataIndexName = scheduler.getNewIndexName();

		if (list != null) {
			for (String deletedContentId : list) {

				DatasourceEventBus.sendMessage(
					DatasourceMessage.Delete
						.builder()
						.tenantId(tenantId)
						.datasourceId(datasourceId)
						.indexName(newDataIndexName)
						.contentId(deletedContentId)
						.build()
				);

			}
		}
		else {
			log.warnf(
				"%s cannot send delete events because deleted contents are unknown.",
				shardingKey
			);
		}

		getContext().getSelf().tell(new Stop(replyTo));

		return Behaviors.same();
	}
}
