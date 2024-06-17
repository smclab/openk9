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

import akka.actor.typed.Behavior;
import io.openk9.common.util.SchedulingKey;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.Scheduling;
import io.openk9.datasource.pipeline.actor.closing.DeletionCompareNotifier;
import io.openk9.datasource.pipeline.actor.closing.EvaluateStatus;
import io.openk9.datasource.pipeline.actor.closing.UpdateDatasource;
import io.openk9.datasource.pipeline.stages.closing.CloseProtocol;
import io.openk9.datasource.pipeline.stages.closing.CloseStage;

import java.util.List;

public class BasePipeline {

	public static Behavior<Scheduling.Command> createScheduling(SchedulingKey schedulingKey) {

		return Scheduling.create(
			schedulingKey,
			BasePipeline::closeResponseAggregator,
			UpdateDatasource::create,
			DeletionCompareNotifier::create,
			EvaluateStatus::create
		);
	}

	public static CloseStage.Aggregate closeResponseAggregator(List<CloseProtocol.Reply> replies) {

		return replies.stream()
			.filter(EvaluateStatus.Success.class::isInstance)
			.findFirst()
			.map(reply -> (EvaluateStatus.Success) reply)
			.map(EvaluateStatus.Success::status)
			.map(CloseStage.Aggregate::new)
			.orElse(new CloseStage.Aggregate(Scheduler.SchedulerStatus.FINISHED));

	}

}
