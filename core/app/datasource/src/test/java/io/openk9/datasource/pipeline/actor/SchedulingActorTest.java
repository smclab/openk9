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

package io.openk9.datasource.pipeline.actor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.closing.DeletionCompareNotifier;
import io.openk9.datasource.pipeline.actor.closing.EvaluateStatus;
import io.openk9.datasource.pipeline.actor.closing.UpdateDatasource;
import io.openk9.datasource.pipeline.actor.common.AggregateItem;

import org.junit.jupiter.api.Test;

class SchedulingActorTest {

	@Test
	void should_get_status_default() {
		List<AggregateItem.Reply> replies = List.of(
			UpdateDatasource.Success.INSTANCE,
			DeletionCompareNotifier.Success.INSTANCE
		);

		var aggregate = Scheduling.closeResponseAggregator(replies);

		assertEquals(Scheduler.SchedulerStatus.FINISHED, aggregate.status());
	}

	@Test
	void should_get_status_from_reply() {
		List<AggregateItem.Reply> replies = List.of(
			new EvaluateStatus.Success(Scheduler.SchedulerStatus.ERROR),
			UpdateDatasource.Success.INSTANCE
		);

		var aggregate = Scheduling.closeResponseAggregator(replies);

		assertEquals(Scheduler.SchedulerStatus.ERROR, aggregate.status());
	}

}