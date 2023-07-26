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

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.Scheduler_;
import io.openk9.datasource.model.dto.SchedulerDTO;
import io.openk9.datasource.model.util.Mutiny2;
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
		return em.withStatelessTransaction(s -> Mutiny2.fetch(s, scheduler.getDatasource()));
	}

	public Uni<DataIndex> getOldDataIndex(Scheduler scheduler) {
		return em.withStatelessTransaction(s -> Mutiny2.fetch(s, scheduler.getOldDataIndex()));
	}

	public Uni<DataIndex> getNewDataIndex(Scheduler scheduler) {
		return em.withStatelessTransaction(s -> Mutiny2.fetch(s, scheduler.getNewDataIndex()));
	}

	public Uni<List<String>> getDeletedContentIds(String tenant, String schedulerId) {
		return em.withStatelessTransaction(tenant, s -> s.createQuery(
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
		return em.withStatelessTransaction(s -> s.createQuery(
			"select s " +
				"from Scheduler s " +
				"join fetch s.oldDataIndex " +
				"join fetch s.newDataIndex " +
				"where s.id = :schedulerId", Scheduler.class)
				.setParameter("schedulerId", id)
			.getSingleResultOrNull())
			.flatMap(this::indexesDiff);
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
}
