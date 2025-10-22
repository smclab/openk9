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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;

import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.index.model.IndexName;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.Scheduler_;
import io.openk9.datasource.model.dto.base.SchedulerDTO;
import io.openk9.datasource.pipeline.actor.MessageGateway;
import io.openk9.datasource.pipeline.actor.Scheduling;
import io.openk9.datasource.util.UniActionListener;

import io.smallrye.mutiny.Uni;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding;
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityRef;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.script.Script;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.PipelineAggregatorBuilders;
import org.opensearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.quartz.Trigger;
import org.quartz.impl.jdbcjobstore.TriggerStatus;

@ApplicationScoped
public class SchedulerService extends BaseK9EntityService<Scheduler, SchedulerDTO> {
	@Inject
	ActorSystemProvider actorSystemProvider;
	@Inject
	RestHighLevelClient restHighLevelClient;

	SchedulerService() {}

	protected static HealthStatus getHealthStatus(Scheduler.SchedulerStatus schedulerStatus) {
		return switch (schedulerStatus) {

			case RUNNING, STALE -> HealthStatus.RUNNING;
			case ERROR, FAILURE -> HealthStatus.ERROR;
			case FINISHED, CANCELLED -> HealthStatus.IDLE;
			case null -> HealthStatus.IDLE;

		};
	}

	protected static JobStatus getJobStatus(Scheduler.SchedulerStatus schedulerStatus) {
		return schedulerStatus == null ? JobStatus.NOT_RUNNING : JobStatus.TRIGGER_RUNNING;
	}

	public Uni<Void> cancelScheduling(String tenantId, long schedulerId) {
		return findById(tenantId, schedulerId)
			.chain(scheduler -> switch (scheduler.getStatus()) {
				case RUNNING, STALE, ERROR -> {
					ActorSystem<?> actorSystem = actorSystemProvider.getActorSystem();

					ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);

					var shardingKey = ShardingKey.fromStrings(tenantId, scheduler.getScheduleId());

					EntityRef<Scheduling.Command> schedulingRef = clusterSharding.entityRefFor(
						Scheduling.ENTITY_TYPE_KEY, shardingKey.asString());

					schedulingRef.tell(new Scheduling.GracefulEnd(Scheduler.SchedulerStatus.CANCELLED));
					yield Uni.createFrom().voidItem();
				}
				default -> Uni.createFrom().voidItem();
			});
	}

	/**
	 * Checks whether a new scheduling operation can start based on the current job status and
	 * reindex type, and produce the {@link TriggerResponse}.
	 * <p>
	 * A new scheduling is permitted if no job is currently running. If a running job exists
	 * and is of type trigger, a new reindex-type scheduling may proceed; otherwise, the
	 * scheduling is not permitted.
	 *
	 * @param jobStatus the current status of the job, indicating whether no job is running,
	 *                  a trigger job is running, or a reindex job is running
	 * @param reindex   {@code true} if the new scheduling is of type reindex, {@code false} otherwise
	 * @return {@link TriggerResponse} containing information about the status of old jobs and
	 *          whether the current scheduling operation is allowed or blocked
	 */
	public TriggerResponse checkStartScheduling(JobStatus jobStatus, boolean reindex) {
		var status = switch (jobStatus) {
			case NOT_RUNNING -> SchedulingStatus.ALLOWED;
			case TRIGGER_RUNNING -> reindex ? SchedulingStatus.ALLOWED : SchedulingStatus.BLOCKED;
			case REINDEX_RUNNING -> SchedulingStatus.BLOCKED;
		};

		return new TriggerResponse(jobStatus, status);
	}

	public Uni<Void> closeScheduling(String tenantId, long schedulerId) {
		return findById(schedulerId)
			.chain(scheduler -> switch (scheduler.getStatus()) {
				case RUNNING, STALE, ERROR -> {
					ActorSystem<?> actorSystem = actorSystemProvider.getActorSystem();

					ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);

					var shardingKey = ShardingKey.fromStrings(tenantId, scheduler.getScheduleId());

					EntityRef<Scheduling.Command> schedulingRef = clusterSharding.entityRefFor(
						Scheduling.ENTITY_TYPE_KEY, shardingKey.asString());

					schedulingRef.tell(Scheduling.Close.INSTANCE);
					yield Uni.createFrom().voidItem();
				}
				default -> Uni.createFrom().voidItem();
			});
	}

	public Uni<Datasource> getDatasource(Scheduler scheduler) {
		return sessionFactory.withTransaction(s -> findById(s, scheduler.getId())
			.flatMap(found -> s.fetch(found.getDatasource()))
		);
	}

	public Uni<List<String>> getDeletedContentIds(long id) {
		return sessionFactory.withTransaction(s -> getCurrentTenant(s)
			.flatMap(tenant -> s
				.createNamedQuery(Scheduler.FETCH_BY_ID, Scheduler.class)
				.setPlan(s.getEntityGraph(Scheduler.class, Scheduler.DATA_INDEXES_ENTITY_GRAPH))
				.setParameter("schedulerId", id)
				.getSingleResultOrNull()
				.flatMap(scheduler -> indexesDiff(tenant.schemaName(), scheduler))
			)
		);
	}

	public Uni<List<String>> getDeletedContentIds(String schedulerId) {
		return getDeletedContentIds(null, schedulerId);
	}

	public Uni<List<String>> getDeletedContentIds(String tenantId, String scheduleId) {
		return sessionFactory.withTransaction(
				tenantId, (s, t) -> s
				.createNamedQuery(Scheduler.FETCH_BY_SCHEDULE_ID, Scheduler.class)
				.setParameter("scheduleId", scheduleId)
				.setPlan(s.getEntityGraph(Scheduler.class, Scheduler.DATA_INDEXES_ENTITY_GRAPH))
				.getSingleResultOrNull())
			.flatMap(scheduler -> indexesDiff(tenantId, scheduler));
	}

	@Override
	public Class<Scheduler> getEntityClass() {
		return Scheduler.class;
	}

	/**
	 * Asynchronously retrieves a status for every datasource in the system.
	 * <p>
	 * The status is determined by the most recent (latest {@code createDate})
	 * scheduler execution for each datasource. It uses an efficient JPQL query
	 * that implements the "greatest N per group" pattern to fetch only the
	 * single most recent record per datasource.
	 * <p>
	 * Datasources that have never been executed are also included in the result set,
	 * allowing the subsequent mapping process to assign an appropriate default status.
	 *
	 * @return A {@link Uni} that emits a list of {@link DatasourceHealthStatus} objects,
	 *         representing each datasource and its derived status.
	 * @see #mapToHealthStatusList(List)
	 */
	public Uni<List<DatasourceHealthStatus>> getHealthStatusList() {
		return sessionFactory.withTransaction((session, transaction) -> session
			.createQuery(
				"""
					SELECT a.id, a.name, b.status
					FROM Datasource a
					LEFT JOIN Scheduler b ON a = b.datasource
					LEFT JOIN (
					 SELECT sl.datasource AS datasource, MAX(sl.createDate) AS maxDate
					 FROM Scheduler sl
					 GROUP BY sl.datasource
					) AS latest
					ON b.datasource  = latest.datasource AND b.createDate = latest.maxDate
					WHERE latest.maxDate IS NOT NULL OR b.id IS NULL
					""", Tuple.class
			)
			.getResultList()
			.map(this::mapToHealthStatusList)
		);
	}

	/**
	 * Retrieves the job status for a specific datasource.
	 *
	 * <p>This method queries the database for active {@code Scheduler} instances in a running state
	 * associated with the provided datasource ID.
	 *
	 * <p>The returned status depends on the scheduler's state. If a scheduler exists in a running
	 * state (one of {@link Scheduler#RUNNING_STATES_SET}), the status reflects the scheduler type.
	 * If no running scheduler is found, the status is {@link JobStatus#NOT_RUNNING}.
	 *
	 * @param datasourceId the datasource ID to check
	 * @return a {@code Uni<JobStatus>}
	 */
	public Uni<JobStatus> getJobStatus(Long datasourceId) {

		return sessionFactory.withTransaction((session, transaction) -> session
			.createQuery(
				"""
					SELECT d.id, d.name, s.status, s.reindex
					FROM Datasource d
					LEFT JOIN d.schedulers s ON s.status IN :runningStates
					WHERE d.id = :datasourceId
					""",
				Tuple.class
			)
			.setParameter("datasourceId", datasourceId)
			.setParameter("runningStates", Scheduler.RUNNING_STATES_SET)
			.getSingleResult()
			.map(this::mapToJobStatus)
		);
	}

	/**
	 * Retrieves the job status for a list of datasources.
	 *
	 * <p>This method queries the database for active {@code Scheduler} instances associated with
	 * the provided datasource IDs. If a datasource is found in a running state,
	 * a {@link io.openk9.datasource.service.SchedulerService.DatasourceJobStatus} instance is created
	 * with the status {@link  JobStatus#TRIGGER_RUNNING}.
	 * Otherwise, it defaults to {@link JobStatus#NOT_RUNNING}.
	 *
	 * @param datasourceIds the list of datasource IDs to check
	 * @return a {@code Uni<List<DatasourceJobStatus>>} where each datasource ID is mapped
	 *         to its corresponding job status
	 * @deprecated Replaced by {@link #getJobStatus(Long)} which uses unique datasource id.
	 *         Since only one scheduler per datasource can now be in running state,
	 *         this method is no longer necessary.
	 */
	@Deprecated
	public Uni<List<DatasourceJobStatus>> getJobStatusList(List<Long> datasourceIds) {

		return sessionFactory.withTransaction((session, transaction) -> session
			.createQuery(
				"""
					SELECT d.id, d.name, s.status
					FROM Datasource d
					LEFT JOIN d.schedulers s ON s.status IN :runningStates
					WHERE d.id IN :datasourceIds
					""",
				Tuple.class
			)
			.setParameter("datasourceIds", datasourceIds)
			.setParameter("runningStates", Scheduler.RUNNING_STATES_SET)
			.getResultList()
			.map(this::mapToJobStatusList)
		);
	}

	public Uni<DataIndex> getNewDataIndex(Scheduler scheduler) {
		return sessionFactory.withTransaction(s -> findById(s, scheduler.getId())
			.flatMap(found -> s.fetch(found.getNewDataIndex()))
		);
	}

	public Uni<DataIndex> getOldDataIndex(Scheduler scheduler) {
		return sessionFactory.withTransaction(s -> findById(s, scheduler.getId())
			.flatMap(found -> s.fetch(found.getOldDataIndex()))
		);
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {Scheduler_.STATUS};
	}

	public Uni<Void> rereouteScheduling(String tenantId, long schedulerId) {
		return findById(schedulerId)
			.chain(scheduler -> {
				if (scheduler.getStatus() == Scheduler.SchedulerStatus.ERROR) {

					ActorSystem<?> actorSystem = actorSystemProvider.getActorSystem();

					MessageGateway.askReroute(
						actorSystem,
						ShardingKey.fromStrings(tenantId, scheduler.getScheduleId())
					);
				}
				return Uni.createFrom().voidItem();
			});
	}

	private Uni<List<String>> indexesDiff(String tenantId, Scheduler scheduler) {
		if (scheduler == null) {
			return Uni.createFrom().item(List.of());
		}

		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(
			IndexName.from(tenantId, scheduler.getOldDataIndex()).toString(),
			IndexName.from(tenantId, scheduler.getNewDataIndex()).toString()
		);

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		TermsAggregationBuilder aggregation = AggregationBuilders
			.terms("contentId_agg")
			.field("contentId.keyword")
			.size(10000)
			.subAggregation(PipelineAggregatorBuilders
				.bucketSelector(
					"count_bucket_selector",
					Map.of("count", "_count"),
					Script.parse(
						Map.of(
							"inline", "count < 2",
							"lang", "expression"
						)
					)
				)
			);
		sourceBuilder.size(0);
		sourceBuilder.aggregation(aggregation);

		searchRequest.source(sourceBuilder);

		return Uni.createFrom().<SearchResponse>emitter(emitter ->
				restHighLevelClient.searchAsync(
					searchRequest,
					RequestOptions.DEFAULT,
					UniActionListener.of(emitter)
				)
			)
			.map(this::mapToList);
	}

	/**
	 * Transforms a list of raw JPA query results into a list of DatasourceHealthStatus DTOs.
	 * <p>
	 * It maps the internal {@link Scheduler.SchedulerStatus} to a simplified, client-facing
	 * {@link HealthStatus}. Crucially, if a datasource has no scheduler history (resulting in a
	 * null status from the query), it is explicitly assigned {@link HealthStatus#IDLE}.
	 *
	 * @param tuples The list of raw tuples from the database query.
	 * @return A final, mapped list of {@link DatasourceHealthStatus} objects.
	 */
	private List<DatasourceHealthStatus> mapToHealthStatusList(List<Tuple> tuples) {

		List<DatasourceHealthStatus> healthStatusList = new ArrayList<>();

		for (Tuple tuple : tuples) {
			Scheduler.SchedulerStatus schedulerStatus =
				tuple.get(2, Scheduler.SchedulerStatus.class);

			var status = getHealthStatus(schedulerStatus);

			var datasourceStatus = new DatasourceHealthStatus(
				tuple.get(0, Long.class),
				tuple.get(1, String.class),
				status
			);

			healthStatusList.add(datasourceStatus);
		}

		return healthStatusList;
	}

	/**
	 * Transforms a raw JPA query result into a {@link JobStatus}.
	 * <p>
	 * This method extracts the raw datasource, scheduler data, and type from a
	 * {@link jakarta.persistence.Tuple} and maps it to a {@link JobStatus} value representing the
	 * running job status.
	 *
	 * @param tuple The raw tuple from the database query, conforming to the expected structure.
	 * @return A mapped {@link JobStatus}.
	 */
	private JobStatus mapToJobStatus(Tuple tuple) {

		var schedulerStatus = tuple.get(2, Scheduler.SchedulerStatus.class);
		var reindex = tuple.get(3, Boolean.class);

		if (schedulerStatus == null) {
			return JobStatus.NOT_RUNNING;
		}

		return reindex ? JobStatus.REINDEX_RUNNING : JobStatus.TRIGGER_RUNNING;
	}

	/**
	 * Transforms a list of raw JPA query results into a list of {@link DatasourceJobStatus} DTOs.
	 * <p>
	 * This method iterates through each {@link jakarta.persistence.Tuple} and extracts the raw
	 * datasource and scheduler data. It delegates the mapping of the internal
	 * {@link Scheduler.SchedulerStatus} to a simplified, client-facing {@link JobStatus}
	 * by calling the {@code getJobStatus} helper method.
	 *
	 * @param tuples The list of raw tuples from the database query, conforming to the expected structure.
	 * @return A final, mapped list of {@link DatasourceJobStatus} objects.
	 */
	@Deprecated
	private List<DatasourceJobStatus> mapToJobStatusList(List<Tuple> tuples) {

		List<DatasourceJobStatus> jobStatusList = new ArrayList<>();

		for (Tuple tuple : tuples) {
			var schedulerId = tuple.get(2, Scheduler.SchedulerStatus.class);
			var jobStatus = getJobStatus(schedulerId);

			var datasourceJobStatus = new DatasourceJobStatus(
				tuple.get(0, Long.class),
				tuple.get(1, String.class),
				jobStatus
			);

			jobStatusList.add(datasourceJobStatus);
		}

		return jobStatusList;
	}

	private List<String> mapToList(SearchResponse searchResponse) {
		return searchResponse
			.getAggregations()
			.<Terms>get("contentId_agg")
			.getBuckets()
			.stream()
			.map(MultiBucketsAggregation.Bucket::getKeyAsString)
			.toList();
	}

	public enum HealthStatus {
		RUNNING,
		ERROR,
		IDLE
	}

	public enum JobStatus {
		TRIGGER_RUNNING,
		REINDEX_RUNNING,
		NOT_RUNNING
	}

	public enum SchedulingStatus {
		ALLOWED,
		BLOCKED
	}

	public record DatasourceHealthStatus(long id, String name, HealthStatus status) {}

	public record DatasourceJobStatus(long id, String name, JobStatus status) {}

	public record TriggerResponse(
		JobStatus oldJobStatus,
		SchedulingStatus status
	) {}

}
