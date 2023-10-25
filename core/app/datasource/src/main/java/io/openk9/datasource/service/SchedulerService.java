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

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.Scheduler_;
import io.openk9.datasource.model.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.SchedulationKeyUtils;
import io.openk9.datasource.pipeline.actor.Schedulation;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.util.UniActionListener;
import io.smallrye.mutiny.Uni;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SchedulerService extends BaseK9EntityService<Scheduler, SchedulerDTO> {
	SchedulerService() {}

	@Override
	public Class<Scheduler> getEntityClass() {
		return Scheduler.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {Scheduler_.STATUS};
	}

	public Uni<Datasource> getDatasource(Scheduler scheduler) {
		return sessionFactory.withStatelessTransaction(s -> s.fetch(scheduler.getDatasource()));
	}

	public Uni<DataIndex> getOldDataIndex(Scheduler scheduler) {
		return sessionFactory.withStatelessTransaction(s -> s.fetch(scheduler.getOldDataIndex()));
	}

	public Uni<DataIndex> getNewDataIndex(Scheduler scheduler) {
		return sessionFactory.withStatelessTransaction(s -> s.fetch(scheduler.getNewDataIndex()));
	}

	public Uni<List<String>> getDeletedContentIds(String tenant, String schedulerId) {
		return sessionFactory.withStatelessTransaction(tenant, (s, t) -> s.createQuery(
					"select s " +
					"from Scheduler s " +
					"join fetch s.oldDataIndex " +
					"join fetch s.newDataIndex " +
					"where s.scheduleId = :schedulerId", Scheduler.class)
				.setParameter("schedulerId", schedulerId)
				.getSingleResultOrNull())
			.flatMap(this::indexesDiff);
	}

	public Uni<List<String>> getDeletedContentIds(String schedulerId) {
		return getDeletedContentIds(null, schedulerId);
	}

	public Uni<List<String>> getDeletedContentIds(long id) {
		return sessionFactory.withStatelessTransaction(s -> s.createQuery(
			"select s " +
				"from Scheduler s " +
				"join fetch s.oldDataIndex " +
				"join fetch s.newDataIndex " +
				"where s.id = :schedulerId", Scheduler.class)
				.setParameter("schedulerId", id)
			.getSingleResultOrNull())
			.flatMap(this::indexesDiff);
	}

	public Uni<Void> cancelSchedulation(String tenantId, long schedulerId) {
		return findById(schedulerId)
			.chain(scheduler -> {
				if (scheduler.getStatus() == Scheduler.SchedulerStatus.STARTED) {

					ActorSystem<?> actorSystem = actorSystemProvider.getActorSystem();

					ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);

					EntityRef<Schedulation.Command> schedulationRef = clusterSharding.entityRefFor(
						Schedulation.ENTITY_TYPE_KEY,
						SchedulationKeyUtils.getValue(tenantId, scheduler.getScheduleId())
					);

					schedulationRef.tell(Schedulation.Cancel.INSTANCE);
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
			scheduler.getOldDataIndex().getName(),
			scheduler.getNewDataIndex().getName());

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
				client.searchAsync(
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


	@Inject
	RestHighLevelClient client;
	@Inject
	ActorSystemProvider actorSystemProvider;

}
