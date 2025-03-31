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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;
import jakarta.inject.Inject;

import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.dto.base.DatasourceDTO;
import io.openk9.datasource.model.dto.base.EnrichPipelineDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StatusByDatasourcesTest {

	private static final String ENTITY_NAME_PREFIX = "StatusByDatasourcesTest - ";
	private static final String ENRICH_PIPELINE_ONE_NAME = ENTITY_NAME_PREFIX + "Enrich pipeline 1";
	private static final String DATASOURCE_ONE_NAME = ENTITY_NAME_PREFIX + "Datasource 1";

	@Inject
	DatasourceService datasourceService;

	@Inject
	EnrichPipelineService enrichPipelineService;

	@Inject
	SchedulerService schedulerService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	@Order(1)
	void setup() {
		createDatasourceOne();
		createSchedulerRunning();
		createSchedulerFinished();
		createEnrichPipelineOne();

		var enrichPipeline = getEnrichPipelineOne();
		var datasource = getDatasourceOne();
		datasource.setEnrichPipeline(enrichPipeline);

		persistDatasource(datasource);
	}

	@Test
	@Order(2)
	void should_retrieve_status_by_datasource() {

		var datasource = getDatasourceOne();
		List<Long> datasourceIds = List.of(datasource.getId());



		var statusList =
			schedulerService.getStatusByDatasources(datasourceIds)
				.await()
				.indefinitely();

		assertEquals(1, statusList.size());
		statusList.forEach(
			status ->
				assertEquals(SchedulerService.JobStatus.ALREADY_RUNNING, status.status())
		);
	}

	@Test
	@Order(3)
	void tearDown() {
		removeDatasourceOne();
		removeEnrichPipelineOne();
	}

	private void createDatasourceOne() {
		DatasourceDTO dto = DatasourceDTO.builder()
			.name(DATASOURCE_ONE_NAME)
			.scheduling(DatasourceConnectionObjects.SCHEDULING)
			.schedulable(false)
			.reindexing(DatasourceConnectionObjects.REINDEXING)
			.reindexable(false)
			.build();

		sessionFactory.withTransaction(
				(s,transaction) ->
					datasourceService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createEnrichPipelineOne() {
		EnrichPipelineDTO dto = EnrichPipelineDTO.builder()
			.name(ENRICH_PIPELINE_ONE_NAME)
			.build();

		sessionFactory.withTransaction(
				(s,transaction) ->
					enrichPipelineService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createSchedulerFinished() {
		var datasource = getDatasourceOne();

		io.openk9.datasource.model.Scheduler scheduler = new io.openk9.datasource.model.Scheduler();
		scheduler.setScheduleId(UUID.randomUUID().toString());
		scheduler.setDatasource(datasource);
		scheduler.setOldDataIndex(datasource.getDataIndex());
		scheduler.setStatus(Scheduler.SchedulerStatus.FINISHED);

		sessionFactory.withTransaction(
				(s,transaction) ->
					schedulerService.create(scheduler)
			)
			.await()
			.indefinitely();
	}

	private void createSchedulerRunning() {
		var datasource = getDatasourceOne();

		io.openk9.datasource.model.Scheduler scheduler = new io.openk9.datasource.model.Scheduler();
		scheduler.setScheduleId(UUID.randomUUID().toString());
		scheduler.setDatasource(datasource);
		scheduler.setOldDataIndex(datasource.getDataIndex());
		scheduler.setStatus(io.openk9.datasource.model.Scheduler.SchedulerStatus.RUNNING);

		sessionFactory.withTransaction(
				(s,transaction) ->
					schedulerService.create(scheduler)
			)
			.await()
			.indefinitely();
	}

	private Datasource getDatasourceOne() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.findByName(s, DATASOURCE_ONE_NAME)
			)
			.await()
			.indefinitely();
	}

	private EnrichPipeline getEnrichPipelineOne() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					enrichPipelineService.findByName(s, ENRICH_PIPELINE_ONE_NAME)
			)
			.await()
			.indefinitely();
	}

	private void persistDatasource(Datasource datasource) {
		sessionFactory.withTransaction((s, transaction) ->
				s.merge(datasource)
			)
			.await()
			.indefinitely();
	}

	private void removeDatasourceOne() {
		var id = getDatasourceOne().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.deleteById(id)
			)
			.await()
			.indefinitely();
	}

	private void removeEnrichPipelineOne() {
		var id = getEnrichPipelineOne().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					enrichPipelineService.deleteById(id)
			)
			.await()
			.indefinitely();
	}

}
