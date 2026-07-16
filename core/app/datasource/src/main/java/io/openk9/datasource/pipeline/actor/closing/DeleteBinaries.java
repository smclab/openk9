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

import java.util.concurrent.CompletableFuture;

import io.openk9.common.storage.PresignedUrlService;
import io.openk9.common.util.ingestion.ShardingKey;
import io.openk9.datasource.pipeline.actor.common.AggregateItem;
import io.openk9.datasource.pipeline.stages.closing.CloseStage;

import jakarta.enterprise.inject.spi.CDI;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.jboss.logging.Logger;

/**
 * Closing handler that drops, at the end of a scheduling, the transient
 * binaries staged on the object storage while the enrichers processed them.
 *
 * <p>The working copy under {@code {datasourceId}/...} in the tenant bucket is
 * only needed during processing; a re-index re-fetches the binaries from the
 * connector. Cleanup is best-effort: a failure is logged and never fails the
 * scheduling closure.
 */
public class DeleteBinaries extends AbstractBehavior<AggregateItem.Command> {

	private static final Logger log = Logger.getLogger(DeleteBinaries.class);
	private final ShardingKey shardingKey;

	private DeleteBinaries(
		ActorContext<AggregateItem.Command> context,
		ShardingKey shardingKey) {

		super(context);
		this.shardingKey = shardingKey;
	}

	/**
	 * Creates the closing handler bound to a scheduling shard.
	 *
	 * @param shardingKey the shard identifying the tenant and the scheduling
	 * @return the handler behavior
	 */
	public static Behavior<AggregateItem.Command> create(ShardingKey shardingKey) {
		return Behaviors.setup(ctx -> new DeleteBinaries(ctx, shardingKey));
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
		var datasourceId = start.scheduler().getDatasourceId();

		PresignedUrlService storage =
			CDI.current().select(PresignedUrlService.class).get();

		getContext().pipeToSelf(
			CompletableFuture.runAsync(
				() -> storage.deleteByDatasource(tenantId, datasourceId)),
			(vo1d, throwable) -> {
				if (throwable != null) {
					log.warnf(
						throwable,
						"Failed to delete staged binaries for datasource %s",
						datasourceId);
				}

				return new Stop(start.replyTo());
			}
		);

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
