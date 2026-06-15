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

package io.openk9.datasource.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.model.dto.base.DatasourceDTO;
import jakarta.inject.Inject;


@QuarkusTest
class SchedulerServiceTest {

	@Inject
	DatasourceService datasourceService;

	@Inject
	SchedulerService schedulerService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	private static final String DATASOURCE_NAME = "DatasourceTest";

	@BeforeEach
	void setup() {
		createDatasource();
		createSchedulerRunning();
		createSchedulerFinished();
		createSchedulerFailure();
		createSchedulerStale();
	}

	@Test
	void getStatusList() {

		var newest = schedulerService.findAll()
			.map(schedulers -> schedulers.stream()
				.max(Comparator.comparing(K9Entity::getCreateDate)))
			.await()
			.indefinitely()
			.get();


		var statusList = schedulerService.getHealthStatusList("public")
			.await().indefinitely();

		assertEquals(
			statusList.getFirst().status(),
			SchedulerService.getHealthStatus(newest.getStatus())
		);
	}

	@Test
	void removeOldScheduler() {
		updateLastModifiedDate();

		schedulerService.removeScheduling()
			.await()
			.indefinitely();

		var result = schedulerService.findAll()
			.await()
			.indefinitely();

		var allowedStatuses = Set.of(
			Scheduler.SchedulerStatus.RUNNING,
			Scheduler.SchedulerStatus.STALE,
			Scheduler.SchedulerStatus.ERROR
		);

		assertTrue(result.stream()
			.allMatch(scheduler -> allowedStatuses.contains(scheduler.getStatus()))
		);
	}

	@Test
	void should_does_not_throw_when_removing_empty_scheduler() {
		sessionFactory.withTransaction(session ->
			session.createNativeQuery("DELETE FROM scheduler").executeUpdate()
		).await().indefinitely();

		assertDoesNotThrow(() ->
			schedulerService.removeScheduling()
				.await()
				.indefinitely()
		);
	}

	@AfterEach
	void tearDown() {
		var id = getDatasource().getId();

		sessionFactory.withTransaction(
				(session, transaction) -> datasourceService.deleteById(id)
			)
			.await()
			.indefinitely();
	}

	private void createDatasource() {
		DatasourceDTO dto = DatasourceDTO.builder()
			.name(DATASOURCE_NAME)
			.scheduling(DatasourceConnectionObjects.SCHEDULING)
			.schedulable(false)
			.reindexing(DatasourceConnectionObjects.REINDEXING)
			.reindexable(false)
			.build();

		sessionFactory.withTransaction(
				(session, transaction) -> datasourceService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createSchedulerRunning() {
		var datasource = getDatasource();

		Scheduler scheduler = new Scheduler();
		scheduler.setScheduleId(UUID.randomUUID().toString());
		scheduler.setDatasource(datasource);
		scheduler.setStatus(Scheduler.SchedulerStatus.RUNNING);
		scheduler.setReindex(false);

		sessionFactory.withTransaction(
				(session, transaction) -> schedulerService.create(scheduler)
			)
			.await()
			.indefinitely();
	}

	private void createSchedulerFailure() {
		var datasource = getDatasource();

		Scheduler scheduler = new Scheduler();
		scheduler.setScheduleId(UUID.randomUUID().toString());
		scheduler.setDatasource(datasource);
		scheduler.setStatus(Scheduler.SchedulerStatus.FAILURE);
		scheduler.setReindex(false);

		sessionFactory.withTransaction(
				(session, transaction) -> schedulerService.create(scheduler)
			)
			.await()
			.indefinitely();
	}

	private void createSchedulerStale() {
		var datasource = getDatasource();

		Scheduler scheduler = new Scheduler();
		scheduler.setScheduleId(UUID.randomUUID().toString());
		scheduler.setDatasource(datasource);
		scheduler.setStatus(Scheduler.SchedulerStatus.STALE);
		scheduler.setReindex(false);

		sessionFactory.withTransaction(
				(session, transaction) -> schedulerService.create(scheduler)
			)
			.await()
			.indefinitely();
	}

	private void createSchedulerFinished() {
		var datasource = getDatasource();

		Scheduler scheduler = new Scheduler();
		scheduler.setScheduleId(UUID.randomUUID().toString());
		scheduler.setDatasource(datasource);
		scheduler.setOldDataIndex(datasource.getDataIndex());
		scheduler.setStatus(Scheduler.SchedulerStatus.FINISHED);

		sessionFactory.withTransaction(
				(session, transaction) -> schedulerService.create(scheduler)
			)
			.await()
			.indefinitely();
	}

	private void updateLastModifiedDate() {
		sessionFactory.withTransaction(session ->
			session.createNativeQuery(
					"UPDATE scheduler SET modified_date = :date")
				.setParameter("date", OffsetDateTime.now().minusDays(8))
				.executeUpdate()
		).await().indefinitely();
	}

	private Datasource getDatasource() {
		return sessionFactory.withTransaction(
				(session, transaction) -> datasourceService.findByName(session, DATASOURCE_NAME)
			)
			.await()
			.indefinitely();
	}
}