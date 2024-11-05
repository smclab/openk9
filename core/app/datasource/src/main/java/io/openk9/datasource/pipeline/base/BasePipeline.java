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

package io.openk9.datasource.pipeline.base;

import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.EnrichPipeline;
import io.openk9.datasource.pipeline.actor.IndexWriter;
import io.openk9.datasource.pipeline.actor.Scheduling;
import io.openk9.datasource.pipeline.actor.closing.DeletionCompareNotifier;
import io.openk9.datasource.pipeline.actor.closing.EvaluateStatus;
import io.openk9.datasource.pipeline.actor.closing.SendLast;
import io.openk9.datasource.pipeline.actor.closing.UpdateDatasource;
import io.openk9.datasource.pipeline.actor.common.AggregateItem;
import io.openk9.datasource.pipeline.actor.working.Forward;
import io.openk9.datasource.pipeline.stages.closing.CloseStage;
import io.openk9.datasource.pipeline.stages.working.WorkStage;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityTypeKey;

import java.util.List;

public class BasePipeline {

	public static final EntityTypeKey<Scheduling.Command> ENTITY_TYPE_KEY =
		EntityTypeKey.create(Scheduling.Command.class, "base-pipeline");

	public static Behavior<Scheduling.Command> createScheduling(ShardingKey shardingKey) {

		return Scheduling.create(
			shardingKey,
			new WorkStage.Configurations(
				EnrichPipeline.ENTITY_TYPE_KEY,
				IndexWriter::create,
				Forward::create
			),
			new CloseStage.Configurations(
				BasePipeline::closeResponseAggregator,
				UpdateDatasource::create,
				DeletionCompareNotifier::create,
				EvaluateStatus::create,
				SendLast::create
			)
		);
	}

	public static CloseStage.Aggregated closeResponseAggregator(List<AggregateItem.Reply> replies) {

		return replies.stream()
			.filter(EvaluateStatus.Success.class::isInstance)
			.findFirst()
			.map(reply -> (EvaluateStatus.Success) reply)
			.map(EvaluateStatus.Success::status)
			.map(CloseStage.Aggregated::new)
			.orElse(new CloseStage.Aggregated(Scheduler.SchedulerStatus.FINISHED));

	}

}
