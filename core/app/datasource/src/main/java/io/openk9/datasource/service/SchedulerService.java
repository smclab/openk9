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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.Scheduler_;
import io.openk9.datasource.model.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.actor.MessageGateway;
import io.openk9.datasource.pipeline.actor.Scheduling;
import io.openk9.datasource.service.util.BaseK9EntityService;
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

@ApplicationScoped
public class SchedulerService extends BaseK9EntityService<Scheduler, SchedulerDTO> {
	@Inject
	ActorSystemProvider actorSystemProvider;
	@Inject
	RestHighLevelClient restHighLevelClient;

	SchedulerService() {}

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
		return sessionFactory.withStatelessTransaction(s -> s
				.createNamedQuery(Scheduler.FETCH_BY_ID, Scheduler.class)
				.setPlan(s.getEntityGraph(Scheduler.class, Scheduler.DATA_INDEXES_ENTITY_GRAPH))
				.setParameter("schedulerId", id)
				.getSingleResultOrNull())
			.flatMap(this::indexesDiff);
	}

	public Uni<List<String>> getDeletedContentIds(String schedulerId) {
		return getDeletedContentIds(null, schedulerId);
	}

	public Uni<List<String>> getDeletedContentIds(String tenant, String scheduleId) {
		return sessionFactory.withStatelessTransaction(tenant, (s, t) -> s
				.createNamedQuery(Scheduler.FETCH_BY_SCHEDULE_ID, Scheduler.class)
				.setParameter("scheduleId", scheduleId)
				.setPlan(s.getEntityGraph(Scheduler.class, Scheduler.DATA_INDEXES_ENTITY_GRAPH))
				.getSingleResultOrNull())
			.flatMap(this::indexesDiff);
	}

	@Override
	public Class<Scheduler> getEntityClass() {
		return Scheduler.class;
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

	/**
	 * Retrieves the job status for a list of datasources.
	 *
	 * <p>This method queries the database for active {@code Scheduler} instances associated with
	 * the provided datasource IDs. If a datasource is found in a running state,
	 * a {@link io.openk9.datasource.service.SchedulerService.DatasourceJobStatus} instance is created
	 * with the status {@code ALREADY_RUNNING}. Otherwise, it defaults to {@code ON_SCHEDULING}.
	 *
	 * @param datasourceIds the list of datasource IDs to check
	 * @return a {@code Uni<List<DatasourceJobStatus>>} where each datasource ID is mapped
	 *         to its corresponding job status
	 */
	public Uni<List<DatasourceJobStatus>> getStatusByDatasources(List<Long> datasourceIds) {
		return sessionFactory.withTransaction((session, transaction) -> session
			.createQuery(
				"from Scheduler s " +
					"join fetch s.datasource d " +
					"where d.id in :datasourceIds and s.status in " + Scheduler.RUNNING_STATES,
				Scheduler.class
			)
			.setParameter("datasourceIds", datasourceIds)
			.getResultList()
			.map(schedulers -> schedulers.stream()
				.map(Scheduler::getDatasource)
				.map(Datasource::getId)
				.collect(Collectors.toSet()))
			.map(ids -> datasourceIds
				.stream()
				.map(id -> new DatasourceJobStatus(id, JobStatus.ON_SCHEDULING))
				.map(djs -> ids
					.stream()
					.filter(id -> djs.id() == id)
					.findFirst()
					.map(id -> new DatasourceJobStatus(djs.id(), JobStatus.ALREADY_RUNNING))
					.orElse(djs)
				)
				.collect(Collectors.toList())
			)
		);
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

	private Uni<List<String>> indexesDiff(Scheduler scheduler) {
		if (scheduler == null) {
			return Uni.createFrom().item(List.of());
		}

		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices(
			scheduler.getOldDataIndex().getIndexName(),
			scheduler.getNewDataIndex().getIndexName()
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

	private List<String> mapToList(SearchResponse searchResponse) {
		return searchResponse
			.getAggregations()
			.<Terms>get("contentId_agg")
			.getBuckets()
			.stream()
			.map(MultiBucketsAggregation.Bucket::getKeyAsString)
			.toList();
	}

	public enum JobStatus {
		ALREADY_RUNNING,
		ON_SCHEDULING
	}

	public record DatasourceJobStatus(long id, JobStatus status) {}

}
