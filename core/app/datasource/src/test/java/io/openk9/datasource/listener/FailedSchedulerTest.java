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

package io.openk9.datasource.listener;

import jakarta.validation.ValidationException;

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A reindex that cannot produce a coherent dataIndex is recorded as a
 * scheduling in {@code FAILURE}, carrying the reason it was refused.
 */
class FailedSchedulerTest {

	private static final String REFUSAL =
		"Cannot create the dataIndex 7-data-a-schedule-id: knnIndex is set to"
		+ " true but there is no active embedding model on this tenant.";
	private static final String SCHEDULE_ID = "a-schedule-id";

	@Test
	@DisplayName("Should record the refusal without carrying the new dataIndex")
	void should_record_the_refusal_of_a_reindex() {
		// a reindex whose new dataIndex was refused
		var datasource = new Datasource();
		var oldDataIndex = new DataIndex();

		var scheduler = new Scheduler();
		scheduler.setScheduleId(SCHEDULE_ID);
		scheduler.setDatasource(datasource);
		scheduler.setOldDataIndex(oldDataIndex);
		scheduler.setNewDataIndex(new DataIndex());
		scheduler.setStatus(Scheduler.SchedulerStatus.RUNNING);
		scheduler.setReindex(true);

		var failed = JobScheduler.failedScheduler(
			scheduler, new ValidationException(REFUSAL));

		// the scheduling is recorded as failed, with the reason it was refused
		assertEquals(Scheduler.SchedulerStatus.FAILURE, failed.getStatus());
		assertTrue(
			failed.getErrorDescription().contains("no active embedding model"),
			failed.getErrorDescription()
		);

		// it keeps identifying the reindex it was
		assertEquals(SCHEDULE_ID, failed.getScheduleId());
		assertEquals(datasource, failed.getDatasource());
		assertEquals(oldDataIndex, failed.getOldDataIndex());
		assertTrue(failed.isReindex());

		// and it does not carry the dataIndex that could not be created
		assertNull(failed.getNewDataIndex());
	}

}
