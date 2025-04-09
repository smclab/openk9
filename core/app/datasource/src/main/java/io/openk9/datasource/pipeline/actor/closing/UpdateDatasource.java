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

import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.pipeline.actor.common.AggregateItem;
import io.openk9.datasource.pipeline.stages.closing.CloseStage;
import io.openk9.datasource.service.DatasourceService;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.jboss.logging.Logger;

public class UpdateDatasource extends AbstractBehavior<AggregateItem.Command> {

	private static final Logger log = Logger.getLogger(UpdateDatasource.class);
	private final ShardingKey shardingKey;

	protected UpdateDatasource(
		ActorContext<AggregateItem.Command> context,
		ShardingKey shardingKey) {

		super(context);
		this.shardingKey = shardingKey;
	}

	public static Behavior<AggregateItem.Command> create(ShardingKey shardingKey) {
		return Behaviors.setup(ctx -> new UpdateDatasource(ctx, shardingKey));
	}

	@Override
	public Receive<AggregateItem.Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(CloseStage.StartHandler.class, this::onStart)
			.onMessage(Stop.class, this::onStop)
			.build();
	}

	private Behavior<AggregateItem.Command> onStart(CloseStage.StartHandler start) {
		var tenantId = shardingKey.tenantId();
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

			log.infof("Nothing to do on Datasource %s", scheduler.getId());

			getContext()
				.getSelf()
				.tell(new Stop(start.replyTo()));
		}

		return Behaviors.same();
	}

	private Behavior<AggregateItem.Command> onStop(Stop stop) {

		stop.replyTo().tell(Success.INSTANCE);

		return Behaviors.stopped();
	}

	public enum Success implements AggregateItem.Reply {
		INSTANCE
	}

	private record Stop(ActorRef<AggregateItem.Reply> replyTo)
		implements AggregateItem.Command {}

}
