package io.openk9.datasource.service;

import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.dto.DatasourceDTO;
import io.openk9.datasource.model.dto.EnrichPipelineDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
			.reindexRate(0)
			.schedulable(false)
			.scheduling("0 0 * ? * * *")
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
