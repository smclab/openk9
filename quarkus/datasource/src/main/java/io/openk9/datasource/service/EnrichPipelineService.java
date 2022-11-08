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

import io.openk9.datasource.graphql.util.relay.Connection;
import io.openk9.datasource.mapper.EnrichPipelineMapper;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.EnrichPipelineItem;
import io.openk9.datasource.model.EnrichPipelineItemKey;
import io.openk9.datasource.model.EnrichPipelineItemKey_;
import io.openk9.datasource.model.EnrichPipelineItem_;
import io.openk9.datasource.model.EnrichPipeline_;
import io.openk9.datasource.model.dto.EnrichPipelineDTO;
import io.openk9.datasource.model.util.K9Entity_;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

@RequestScoped
public class EnrichPipelineService extends BaseK9EntityService<EnrichPipeline, EnrichPipelineDTO> {
	 EnrichPipelineService(EnrichPipelineMapper mapper) {
		 this.mapper = mapper;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {EnrichPipeline_.NAME, EnrichPipeline_.DESCRIPTION};
	}

	public Uni<Connection<EnrichItem>> getEnrichItemsConnection(
		long enrichPipelineId, String after,
		String before, Integer first, Integer last, String searchText,
		Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			enrichPipelineId, EnrichPipeline_.ENRICH_PIPELINE_ITEMS, EnrichItem.class,
			enrichItemService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual, enrichPipelineRoot ->
				enrichPipelineRoot
					.join(EnrichPipeline_.enrichPipelineItems)
					.join(EnrichPipelineItem_.enrichItem),
			(cb, enrichPipelineRoot) ->
				enrichPipelineRoot
					.getJoins()
					.stream()
					.filter(e -> Objects.equals(e.getAttribute(), EnrichPipeline_.enrichPipelineItems))
					.map(e -> (Join<EnrichPipeline, EnrichPipelineItem>) e)
					.map(e -> e.get(EnrichPipelineItem_.weight))
					.map(cb::asc)
					.collect(Collectors.toList()));

	}

	public Uni<Page<EnrichItem>> getEnrichItems(
		long enrichPipelineId, Pageable pageable) {
		 return getEnrichItems(enrichPipelineId, pageable, Filter.DEFAULT);
	}

	public Uni<Page<EnrichItem>> getEnrichItems(
		long enrichPipelineId, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			new Long[] { enrichPipelineId },
			EnrichPipeline_.ENRICH_PIPELINE_ITEMS, EnrichItem.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			searchText);
	}

	public Uni<Set<EnrichItem>> getEnrichItemsInEnrichPipeline(
		long enrichPipelineId) {

		return withTransaction(
			s ->
				findById(enrichPipelineId)
					.flatMap(ep -> Mutiny2.fetch(s, ep.getEnrichPipelineItems()))
					.map(l -> l
						.stream()
						.map(EnrichPipelineItem::getEnrichItem)
						.collect(Collectors.toSet()))
		);

	}

	public Uni<List<EnrichItem>> getEnrichItemsNotInEnrichPipeline(
		long enrichPipelineId) {

		return withTransaction(
			s -> {

				CriteriaBuilder cb = em.getCriteriaBuilder();

				CriteriaQuery<EnrichItem> query =
					cb.createQuery(EnrichItem.class);

				Root<EnrichItem> root = query.from(EnrichItem.class);

				Subquery<Long> subquery = query.subquery(Long.class);

				Root<EnrichPipeline> from = subquery.from(EnrichPipeline.class);

				SetJoin<EnrichPipeline, EnrichPipelineItem> rootJoin =
					from.joinSet(EnrichPipeline_.ENRICH_PIPELINE_ITEMS);

				Path<EnrichPipelineItemKey> enrichPipelineItemKeyPath =
					rootJoin.get(EnrichPipelineItem_.key);

				Path<Long> enrichItemId = enrichPipelineItemKeyPath.get(
					EnrichPipelineItemKey_.enrichItemId);

				subquery.select(enrichItemId);

				subquery.where(
					cb.equal(from.get(K9Entity_.id), enrichPipelineId));

				query.where(
					cb.in(root.get(K9Entity_.id)).value(subquery).not());

				return s.createQuery(query).getResultList();

			}
		);

	}

	public Uni<EnrichPipeline> sortEnrichItems(
		long enrichPipelineId, List<Long> enrichItemIdList) {


		return withTransaction(s -> findById(enrichPipelineId)
			.onItem()
			.ifNotNull()
			.transformToUni(enrichPipeline -> {

				List<Uni<EnrichItem>> enrichItemList = new ArrayList<>();

				for (Long enrichItemId : enrichItemIdList) {
					enrichItemList.add(
						enrichItemService.findById(enrichItemId));
				}

				return Uni
					.combine()
					.all()
					.unis(enrichItemList)
					.combinedWith(EnrichItem.class, Function.identity())
					.flatMap(l -> Mutiny2
						.fetch(s, enrichPipeline.getEnrichPipelineItems())
						.flatMap(epi -> {

							float weight = 0.0f;

							for (Long enrichItemId : enrichItemIdList) {

								for (EnrichPipelineItem enrichPipelineItem : epi) {
									if (
											Objects.equals(
												enrichPipelineItem.getEnrichItem().getId(),
												enrichItemId
											)
									) {
										enrichPipelineItem.setWeight(weight);
										weight += 1.0f;
										break;
									}
								}

							}

							return merge(enrichPipeline);


						})
					);

				}));
	}

	public Uni<Page<EnrichItem>> getEnrichItems(
		long enrichPipelineId, Pageable pageable,
		Filter filter) {

		return findAllPaginatedJoin(
			new Long[] { enrichPipelineId },
			EnrichPipeline_.ENRICH_PIPELINE_ITEMS, EnrichItem.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			filter);
	}

	public Uni<Tuple2<EnrichPipeline, EnrichItem>> addEnrichItem(
		long enrichPipelineId, long enrichItemId, boolean tail) {

		return withTransaction((s) -> findById(enrichPipelineId)
			.onItem()
			.ifNotNull()
			.transformToUni(enrichPipeline ->
				enrichItemService.findById(enrichItemId)
					.onItem()
					.ifNotNull()
					.transformToUni(enrichItem ->
						Mutiny2
							.fetch(s, enrichPipeline.getEnrichPipelineItems())
							.flatMap(enrichPipelineItems -> {

								DoubleStream doubleStream =
									enrichPipelineItems
										.stream()
										.mapToDouble(EnrichPipelineItem::getWeight);

								double weight;

								if (tail) {
									weight = doubleStream.max().orElse(0.0) + 1.0;
								}
								else {
									weight = doubleStream.min().orElse(0.0) - 1.0;
								}

								EnrichPipelineItem newEnrichPipelineItem =
									EnrichPipelineItem.of(
										EnrichPipelineItemKey.of(
											enrichPipelineId, enrichItemId),
										enrichPipeline, enrichItem, (float)weight
									);

								if (enrichPipelineItems.add(newEnrichPipelineItem)) {
									enrichPipeline.setEnrichPipelineItems(enrichPipelineItems);
									return persist(enrichPipeline).map(ep -> Tuple2.of(ep, enrichItem));
								} else {
									return Uni.createFrom().nullItem();
								}

							})
					)
			)
		);
	}

	public Uni<Tuple2<EnrichPipeline, EnrichItem>> removeEnrichItem(long enrichPipelineId, long enrichItemId) {
		return withTransaction((s) -> findById(enrichPipelineId)
			.onItem()
			.ifNotNull()
			.transformToUni(enrichPipeline ->
				enrichItemService.findById(enrichItemId)
					.onItem()
					.ifNotNull()
					.transformToUni(enrichItem ->
						Mutiny2
							.fetch(s, enrichPipeline.getEnrichPipelineItems())
							.flatMap(enrichPipelineItems -> {

								boolean removed = enrichPipelineItems.removeIf(
									epi -> epi.getKey().getEnrichItemId() == enrichItemId
										   && epi.getKey().getEnrichPipelineId() == enrichPipelineId);

								if (removed) {
									return s.find(
										EnrichPipelineItem.class,
										EnrichPipelineItemKey.of(enrichPipelineId, enrichItemId)
									)
										.call(s::remove)
										.map(ep -> Tuple2.of(enrichPipeline, enrichItem));
								} else {
									return Uni.createFrom().nullItem();
								}

							}))));
	}

	public Uni<EnrichItem> findFirstEnrichItem(
		Mutiny.Session s, long enrichPipelineId) {

		String queryString =
			"select epi.enrichItem " +
			"from EnrichPipelineItem epi " +
			"where epi.enrichPipeline.id = :enrichPipelineId " +
			"order by epi.weight asc";

		Mutiny.Query<EnrichItem> query =
			s.createQuery(queryString, EnrichItem.class);

		query.setParameter("enrichPipelineId", enrichPipelineId);

		query.setMaxResults(1);

		return query.getSingleResultOrNull();
	}

	public Uni<EnrichItem> findFirstEnrichItem(long enrichPipelineId) {
		return withTransaction(s -> findFirstEnrichItem(s, enrichPipelineId));
	}

	public Uni<EnrichItem> findNextEnrichItem(
		long enrichPipelineId, long enrichItemId) {

		 return withTransaction(s -> {

			 String queryString =
				 "select epi_next.enrichItem " +
				 "from EnrichPipelineItem epi " +
				 "left join EnrichPipelineItem epi_next on epi_next.enrichPipeline.id = epi.enrichPipeline.id " +
				 "where epi.enrichPipeline.id = :enrichPipelineId " +
				 "and epi.enrichItem.id = :enrichItemId " +
				 "and epi_next.weight > epi.weight " +
				 "order by epi_next.weight asc";

			 Mutiny.Query<EnrichItem> query =
				 s.createQuery(queryString, EnrichItem.class);

			 query.setParameter("enrichPipelineId", enrichPipelineId);
			 query.setParameter("enrichItemId", enrichItemId);

			 query.setMaxResults(1);

			 return query.getSingleResultOrNull();

		 });

	}

	@Inject
	EnrichItemService enrichItemService;

	@Override
	public Class<EnrichPipeline> getEntityClass() {
		return EnrichPipeline.class;
	}

}
