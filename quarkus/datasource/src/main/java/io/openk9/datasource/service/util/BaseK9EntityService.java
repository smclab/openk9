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

package io.openk9.datasource.service.util;

import io.openk9.datasource.graphql.util.SortType;
import io.openk9.datasource.mapper.K9EntityMapper;
import io.openk9.datasource.model.dto.util.K9EntityDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.common.runtime.AbstractJpaOperations;
import io.quarkus.hibernate.reactive.panache.runtime.PanacheQueryImpl;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import org.reactivestreams.Processor;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseK9EntityService<ENTITY extends K9Entity, DTO extends K9EntityDTO> {

	public abstract Class<ENTITY> getEntityClass();

	public Uni<List<ENTITY>> findAll() {
		return jpaOperations.findAll(getEntityClass()).list();
	}

	public Uni<List<ENTITY>> findAll(
		int limit, int offset, String sortBy, SortType sortType) {

		return jpaOperations
			.findAll(getEntityClass(), Sort.by(sortBy, sortType.getDirection()))
			.page(offset, limit)
			.list();
	}

	public Uni<List<ENTITY>> findAll(Pageable pageable) {

		PanacheQuery<ENTITY> panacheQuery =
			createPanacheQuery(
				pageable.getSortBy().name(),
				pageable.getAfterId(), pageable.getBeforeId());

		return panacheQuery.list();

	}

	public Uni<Page<ENTITY>> findAllPaginated(Pageable pageable) {

		return findAllPaginated(
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId());

	}

	public Uni<Page<ENTITY>> findAllPaginated(
		int limit, String sortBy, long afterId, long beforeId) {

		PanacheQuery<ENTITY> panacheQuery =
			createPanacheQuery(sortBy, afterId, beforeId);

		return createPage(
			limit, panacheQuery.page(0, limit),
			panacheQuery.count());

	}

	private PanacheQuery<ENTITY> createPanacheQuery(
		String sortBy, long afterId, long beforeId) {
		Map<String, Object> params = new HashMap<>();

		String query = "from " + getEntityClass().getSimpleName() + " e ";

		if (afterId > 0 && beforeId > 0) {
			query += "where id > :afterId and id < :beforeId";
			params.put("afterId", afterId);
			params.put("beforeId", beforeId);
		}
		else if (afterId > 0) {
			query += "where id > :afterId";
			params.put("afterId", afterId);
		}
		else if (beforeId > 0) {
			query += "where id < :beforeId";
			params.put("beforeId", beforeId);
		}

		Sort sort = createSort(sortBy);

		return createPanacheQuery(params, query, sort);
	}

	private PanacheQuery<ENTITY> createPanacheQuery(
		Map<String, Object> params, String query, Sort sort) {

		return params.isEmpty()
			? jpaOperations.find(getEntityClass(), query, sort)
			: jpaOperations.find(getEntityClass(), query, sort, params);

	}

	public static Sort createSort(String sortBy) {
		return createSort("", sortBy);
	}

	public static Sort createSort(String prefix, String sortBy) {

		prefix = (prefix == null || prefix.isEmpty()) ? "" : prefix + ".";

		if (sortBy != null && !sortBy.isBlank()) {
			return Sort.by(prefix + "id");
		}
		else {
			return Sort.by(prefix + sortBy).and(prefix + "id");
		}

	}

	public Uni<ENTITY> findById(long id) {
		return (Uni<ENTITY>)jpaOperations.findById(getEntityClass(), id);
	}

	public Uni<ENTITY> patch(long id, DTO dto) {
		return findById(id)
			.onItem().ifNotNull()
			.transformToUni(
				(prev) -> mapper.patch(prev, dto).<ENTITY>persistAndFlush()
					.invoke(newEntity -> processor.onNext(
						K9EntityEvent.of(
							K9EntityEvent.EventType.UPDATE, newEntity, prev))))
			.onItem().ifNull().failWith(
				() -> new IllegalStateException(
					"dto: " + dto.getClass().getSimpleName() + " with id: " +
					id + " not found"));
	}

	public Uni<ENTITY> update(long id, DTO dto) {
		return findById(id)
			.onItem().ifNotNull()
			.transformToUni(
				(prev) -> mapper.update(prev, dto).<ENTITY>persistAndFlush()
					.invoke(newEntity -> processor.onNext(
						K9EntityEvent.of(
							K9EntityEvent.EventType.UPDATE, newEntity, prev))))
			.onItem().ifNull().failWith(
				() -> new IllegalStateException(
					"entity: " + dto.getClass().getSimpleName() + " with id: " +
					id + " not found"));
	}

	public Uni<ENTITY> persist(DTO dto) {
		return persist(mapper.create(dto));
	}

	public Uni<ENTITY> persist(ENTITY entity) {
		return entity.<ENTITY>persistAndFlush()
			.invoke(e -> processor.onNext(
				K9EntityEvent.of(K9EntityEvent.EventType.CREATE, e)));
	}

	public Uni<ENTITY> deleteById(long entityId) {
		return findById(entityId)
			.onItem().ifNotNull()
			.call(() -> ENTITY.deleteById(entityId))
			.invoke(e -> processor.onNext(
				K9EntityEvent.of(K9EntityEvent.EventType.DELETE, e)))
			.onItem().ifNull().failWith(
				() -> new IllegalStateException(
					"entity with id: " + entityId + " for service: " +
					getClass().getSimpleName() + " not found"));
	}

	public Uni<Long> count() {
		return jpaOperations.count(getEntityClass());
	}

	public BroadcastProcessor<K9EntityEvent<ENTITY>> getProcessor() {
		return (BroadcastProcessor<K9EntityEvent<ENTITY>>) processor;
	}

	public static <T> Uni<Page<T>> createPage(
		int limit, PanacheQuery<T> panacheQuery,
		Uni<Long> countQuery) {

		return Uni
			.combine()
			.all()
			.unis(countQuery.memoize().indefinitely(), panacheQuery.list())
			.combinedWith((count, content) -> Page.of(limit, count, content));
	}

	public static String createPageableQuery(
		Pageable pageable, Map<String, Object> params, String query,
		String prefix) {
		if (pageable.getAfterId() > 0 && pageable.getBeforeId() > 0) {
			query += "and " + prefix + ".id between :afterId and :beforeId ";
			params.put("afterId", pageable.getAfterId());
			params.put("beforeId", pageable.getBeforeId());
		}
		else if (pageable.getAfterId() > 0) {
			query += "and " + prefix + ".id > :afterId ";
			params.put("afterId", pageable.getAfterId());
		}
		else if (pageable.getBeforeId() > 0) {
			query += "and " + prefix + ".id < :beforeId ";
			params.put("beforeId", pageable.getBeforeId());
		}
		return query;
	}

	private final Processor<K9EntityEvent<ENTITY>, K9EntityEvent<ENTITY>>
		processor =
		BroadcastProcessor.create();

	protected K9EntityMapper<ENTITY, DTO> mapper;

	@Inject
	protected AbstractJpaOperations<PanacheQueryImpl<ENTITY>> jpaOperations;

}