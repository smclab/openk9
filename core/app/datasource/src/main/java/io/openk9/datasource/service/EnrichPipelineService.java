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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;
import jakarta.persistence.criteria.Subquery;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.FieldValidator;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.mapper.EnrichPipelineMapper;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichItem_;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.EnrichPipelineItem;
import io.openk9.datasource.model.EnrichPipelineItemKey;
import io.openk9.datasource.model.EnrichPipelineItemKey_;
import io.openk9.datasource.model.EnrichPipelineItem_;
import io.openk9.datasource.model.EnrichPipeline_;
import io.openk9.datasource.model.dto.base.EnrichPipelineDTO;
import io.openk9.datasource.model.dto.request.PipelineWithItemsDTO;
import io.openk9.datasource.model.util.K9Entity_;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.Tuple2;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

;

@ApplicationScoped
public class EnrichPipelineService extends BaseK9EntityService<EnrichPipeline, EnrichPipelineDTO> {
	@Inject
	EnrichItemService enrichItemService;

	EnrichPipelineService(EnrichPipelineMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public String[] getSearchFields() {
		return new String[]{EnrichPipeline_.NAME, EnrichPipeline_.DESCRIPTION};
	}

	public Uni<Response<EnrichPipeline>> createWithItems(PipelineWithItemsDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) -> {
				var constraintViolations = validator.validate(dto);

				if ( !constraintViolations.isEmpty() ) {
					var fieldValidators = constraintViolations.stream()
						.map(constraintViolation -> FieldValidator.of(
							constraintViolation.getPropertyPath().toString(),
							constraintViolation.getMessage()))
						.collect(Collectors.toList());

					return Uni.createFrom().item(Response.of(null, fieldValidators));
				}

				return createWithItems(session, dto)
					.flatMap(enrichPipeline ->
						Uni.createFrom().item(Response.of(enrichPipeline,null)));
			});
	}

	public Uni<Response<EnrichPipeline>> patchOrUpdateWithItems(
			long id, PipelineWithItemsDTO dto, boolean patch) {

		return sessionFactory.withTransaction(
			(session, transaction) -> {
				var constraintViolations = validator.validate(dto);

				if ( !constraintViolations.isEmpty() ) {
					var fieldValidators = constraintViolations.stream()
						.map(constraintViolation -> FieldValidator.of(
							constraintViolation.getPropertyPath().toString(),
							constraintViolation.getMessage()))
						.collect(Collectors.toList());

					return Uni.createFrom().item(Response.of(null,fieldValidators));
				}

				return patchOrUpdateWithItems(session, id, dto, patch)
					.flatMap(enrichPipeline -> 
						Uni.createFrom().item(Response.of(enrichPipeline, null)));
			});
	}

	public Uni<EnrichPipeline> createWithItems(
			Mutiny.Session s, PipelineWithItemsDTO pipelineWithItemsDTO) {

		var transientPipeline = mapper.create(pipelineWithItemsDTO);

		return super.create(s, transientPipeline)
			.flatMap(pipeline -> {
				var enrichPipelineItems = new LinkedHashSet<EnrichPipelineItem>();

				for (PipelineWithItemsDTO.ItemDTO item : pipelineWithItemsDTO.getItems()) {
					var enrichItem =
						s.getReference(EnrichItem.class, item.getEnrichItemId());

					var enrichPipelineItem = new EnrichPipelineItem();
					enrichPipelineItem.setEnrichPipeline(pipeline);
					enrichPipelineItem.setEnrichItem(enrichItem);
					enrichPipelineItem.setWeight(item.getWeight());
					enrichPipelineItems.add(enrichPipelineItem);

					var key = EnrichPipelineItemKey.of(
						pipeline.getId(),
						item.getEnrichItemId()
					);

					enrichPipelineItem.setKey(key);
				}

				pipeline.setEnrichPipelineItems(enrichPipelineItems);

				return s
					.persist(pipeline)
					.flatMap(__ -> s.merge(pipeline));
			});
	}

	public Uni<EnrichPipeline> patchOrUpdateWithItems(
			Mutiny.Session s, long id, PipelineWithItemsDTO pipelineWithItemsDTO, boolean patch) {

		var itemDTOSet = pipelineWithItemsDTO.getItems();

		return findById(s, id)
			.onItem().ifNotNull()
			.call(pipeline -> {

				CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
				CriteriaDelete<EnrichPipelineItem> deletePipelineItem =
					cb.createCriteriaDelete(EnrichPipelineItem.class);
				Root<EnrichPipelineItem> deleteFrom =
					deletePipelineItem.from(EnrichPipelineItem.class);

				var pipelineIdPath =
					deleteFrom.get(EnrichPipelineItem_.enrichPipeline).get(EnrichPipeline_.id);

				if (patch && (itemDTOSet == null || itemDTOSet.isEmpty())) {

					return Uni.createFrom().voidItem();

				}
				else {

					deletePipelineItem.where(pipelineIdPath.in(id));

					return s.createQuery(deletePipelineItem)
						.executeUpdate();

				}
			})
			.call(pipeline -> {

				Set<EnrichPipelineItem> items = new HashSet<>();

				//set new pipeline-item Set
				if (itemDTOSet != null) {

					for (PipelineWithItemsDTO.ItemDTO itemDTO : itemDTOSet) {

						var itemId = itemDTO.getEnrichItemId();
						var key = EnrichPipelineItemKey.of(id, itemId);
						var itemReference = s.getReference(EnrichItem.class, itemId);

						var enrichPipelineItem = new EnrichPipelineItem();
						enrichPipelineItem.setEnrichPipeline(pipeline);
						enrichPipelineItem.setEnrichItem(itemReference);
						enrichPipelineItem.setKey(key);
						enrichPipelineItem.setWeight(itemDTO.getWeight());

						items.add(enrichPipelineItem);
					}

				}

				if (items.isEmpty()) {

					return Uni.createFrom().voidItem();

				}
				else {

					return s.persistAll(items.toArray());

				}

			})
			.flatMap(pipeline -> {

				EnrichPipeline newStatePipeline;

				if (patch) {
					newStatePipeline = mapper.patch(pipeline, pipelineWithItemsDTO);
				}
				else {
					newStatePipeline = mapper.update(pipeline, pipelineWithItemsDTO);
				}

				return s.merge(newStatePipeline).call(s::flush);
			});
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
					.filter(e -> Objects.equals(
						e.getAttribute(),
						EnrichPipeline_.enrichPipelineItems
					))
					.map(e -> (Join<EnrichPipeline, EnrichPipelineItem>) e)
					.map(e -> e.get(EnrichPipelineItem_.weight))
					.map(cb::asc)
					.collect(Collectors.toList())
		);

	}

	public Uni<Page<EnrichItem>> getEnrichItems(
		long enrichPipelineId, Pageable pageable) {
		return getEnrichItems(enrichPipelineId, pageable, Filter.DEFAULT);
	}

	public Uni<Page<EnrichItem>> getEnrichItems(
		long enrichPipelineId, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			new Long[]{enrichPipelineId},
			EnrichPipeline_.ENRICH_PIPELINE_ITEMS, EnrichItem.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			searchText
		);
	}

	public Uni<Set<EnrichItem>> getEnrichItemsInEnrichPipeline(
		long enrichPipelineId) {

		return sessionFactory.withTransaction(
			s ->
				findById(enrichPipelineId)
					.flatMap(ep -> s.fetch(ep.getEnrichPipelineItems()))
					.map(l -> l
						.stream()
						.map(EnrichPipelineItem::getEnrichItem)
						.collect(Collectors.toSet()))
		);

	}

	public Uni<List<EnrichItem>> getEnrichItemsNotInEnrichPipeline(
		long enrichPipelineId) {

		return sessionFactory.withTransaction(
			s -> {

				CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

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

		return sessionFactory.withTransaction(s -> {

			float weight = 0.0f;

			Set<PipelineWithItemsDTO.ItemDTO> items = new TreeSet<>();

			for (Long enrichItemId : enrichItemIdList) {

				PipelineWithItemsDTO.ItemDTO itemDto =
					PipelineWithItemsDTO.ItemDTO
						.builder()
						.enrichItemId(enrichItemId)
						.weight(weight)
						.build();

				items.add(itemDto);

				weight += 1.0f;

			}

			PipelineWithItemsDTO pipelineDto = PipelineWithItemsDTO.builder()
				.items(items)
				.build();

			return patchOrUpdateWithItems(s, enrichPipelineId, pipelineDto, true);
		});


	}

	public Uni<Page<EnrichItem>> getEnrichItems(
		long enrichPipelineId, Pageable pageable, Filter filter) {

		return findAllPaginatedJoin(
			new Long[]{enrichPipelineId},
			EnrichPipeline_.ENRICH_PIPELINE_ITEMS, EnrichItem.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			filter
		);
	}

	public Uni<Tuple2<EnrichPipeline, EnrichItem>> addEnrichItem(
		long enrichPipelineId, long enrichItemId, boolean tail) {

		return sessionFactory.withTransaction((s) -> findById(s, enrichPipelineId)
			.onItem()
			.ifNotNull()
			.transformToUni(enrichPipeline ->
				enrichItemService.findById(s, enrichItemId)
					.onItem()
					.ifNotNull()
					.transformToUni(enrichItem -> s
						.fetch(enrichPipeline.getEnrichPipelineItems())
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
									enrichPipeline, enrichItem, (float) weight
								);

							if (enrichPipelineItems.add(newEnrichPipelineItem)) {
								enrichPipeline.setEnrichPipelineItems(enrichPipelineItems);
								return persist(s, enrichPipeline).map(ep -> Tuple2.of(
									ep,
									enrichItem
								));
							}
							else {
								return Uni.createFrom().nullItem();
							}

						})
					)
			)
		);
	}

	public Uni<Tuple2<EnrichPipeline, EnrichItem>> removeEnrichItem(
		long enrichPipelineId, long enrichItemId) {

		return sessionFactory.withTransaction((s, t) -> s
			.find(EnrichPipeline.class, enrichPipelineId)
			.flatMap(enrichPipeline -> s
				.find(EnrichItem.class, enrichItemId)
				.flatMap(enrichItem -> {

					CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
					CriteriaDelete<EnrichPipelineItem> deletePipelineItem =
						cb.createCriteriaDelete(EnrichPipelineItem.class);
					Root<EnrichPipelineItem> deleteFrom =
						deletePipelineItem.from(EnrichPipelineItem.class);

					var pipelineIdPath =
						deleteFrom.get(EnrichPipelineItem_.enrichPipeline).get(EnrichPipeline_.id);

					var itemIdPath =
						deleteFrom.get(EnrichPipelineItem_.enrichItem).get(EnrichItem_.id);

					deletePipelineItem.where(cb.and(
						cb.equal(pipelineIdPath, enrichPipelineId),
						cb.equal(itemIdPath, enrichItemId)
					));

					return s.createQuery(deletePipelineItem)
						.executeUpdate()
						.map(rows -> {
							if (rows > 0) {
								return Tuple2.of(enrichPipeline, enrichItem);
							}

							return null;
						});

				})
			)
		);
	}

	public Uni<EnrichItem> findFirstEnrichItem(
		Mutiny.Session s, long enrichPipelineId) {

		String queryString =
			"select epi.enrichItem " +
			"from EnrichPipelineItem epi " +
			"where epi.enrichPipeline.id = :enrichPipelineId " +
			"order by epi.weight asc";

		var query = s.createQuery(queryString, EnrichItem.class);

		query.setParameter("enrichPipelineId", enrichPipelineId);

		query.setMaxResults(1);

		return query.getSingleResultOrNull();
	}

	public Uni<EnrichItem> findFirstEnrichItem(long enrichPipelineId) {
		return sessionFactory.withTransaction(s -> findFirstEnrichItem(s, enrichPipelineId));
	}

	public Uni<EnrichItem> findNextEnrichItem(
		long enrichPipelineId, long enrichItemId) {

		return sessionFactory.withTransaction(s -> {

			String queryString =
				"select epi_next.enrichItem " +
				"from EnrichPipelineItem epi " +
				"left join EnrichPipelineItem epi_next on epi_next.enrichPipeline.id = epi.enrichPipeline.id " +
				"where epi.enrichPipeline.id = :enrichPipelineId " +
				"and epi.enrichItem.id = :enrichItemId " +
				"and epi_next.weight > epi.weight " +
				"order by epi_next.weight asc";

			var query = s.createQuery(queryString, EnrichItem.class);

			query.setParameter("enrichPipelineId", enrichPipelineId);
			query.setParameter("enrichItemId", enrichItemId);

			query.setMaxResults(1);

			return query.getSingleResultOrNull();

		});

	}

	public Uni<List<EnrichPipeline>> findUnboundEnrichPipelines(long itemId) {

		return sessionFactory.withTransaction(s ->{
			String queryString = "SELECT p FROM EnrichPipeline p " +
				"WHERE p.id not in (" +
				"SELECT pi.enrichPipeline.id FROM EnrichPipelineItem pi " +
				"WHERE pi.enrichItem.id = (:itemId))";

			return s.createQuery(queryString, EnrichPipeline.class)
				.setParameter("itemId", itemId)
				.getResultList();
		});
	}

	@Override
	public Class<EnrichPipeline> getEntityClass() {
		return EnrichPipeline.class;
	}

}
