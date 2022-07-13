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
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import org.reactivestreams.Processor;

import java.util.List;

public abstract class BaseK9EntityService<ENTITY extends K9Entity, DTO extends K9EntityDTO> {

	public Uni<List<ENTITY>> findAll() {
		return ENTITY.listAll();
	}

	public Uni<List<ENTITY>> findAll(
		int limit, int offset, String sortBy, SortType sortType) {

		return ENTITY
			.findAll(Sort.by(sortBy, sortType.getDirection()))
			.page(offset, limit)
			.list();
	}

	public Uni<Page<ENTITY>> findAllPaginated(
		int limit, int offset, String sortBy, SortType sortType) {

		PanacheQuery<PanacheEntityBase> panacheQuery =
			ENTITY
				.findAll(Sort.by(sortBy, sortType.getDirection()))
				.page(offset, limit);

		return Uni
			.combine()
			.all()
			.unis(panacheQuery.pageCount(), panacheQuery.count(), panacheQuery.hasNextPage(), panacheQuery.<ENTITY>list())
			.combinedWith((pageCount, count, hasNextPage, content) ->
				Page.of(limit, offset, pageCount, count, hasNextPage, content));


	}

	public Uni<ENTITY> findById(long id) {
		return ENTITY.<ENTITY>findById(id).invoke(e -> {
			if (e == null) {
				throw new IllegalArgumentException("Entity not found with id: " + id + "class: " + this.getClass());
			}
		});
	}

	public Uni<ENTITY> patch(long id, DTO dto) {
		return findById(id)
			.onItem().ifNotNull()
			.transformToUni((prev) -> mapper.patch(prev, dto).<ENTITY>persistAndFlush()
				.invoke(newEntity -> processor.onNext(
					K9EntityEvent.of(K9EntityEvent.EventType.UPDATE, newEntity, prev))))
			.onItem().ifNull().failWith(
				() -> new IllegalStateException(
					"dto: " + dto.getClass().getSimpleName() + " with id: " + id + " not found"));
	}

	public Uni<ENTITY> update(long id, DTO dto) {
		return findById(id)
			.onItem().ifNotNull()
			.transformToUni((prev) -> mapper.update(prev, dto).<ENTITY>persistAndFlush()
				.invoke(newEntity -> processor.onNext(
					K9EntityEvent.of(K9EntityEvent.EventType.UPDATE, newEntity, prev))))
			.onItem().ifNull().failWith(
				() -> new IllegalStateException(
					"entity: " + dto.getClass().getSimpleName() + " with id: " + id + " not found"));
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
					"entity with id: " + entityId + " for service: " + getClass().getSimpleName() + " not found"));
	}

	public Uni<Long> count() {
		return ENTITY.count();
	}

	public BroadcastProcessor<K9EntityEvent<ENTITY>> getProcessor() {
		return (BroadcastProcessor<K9EntityEvent<ENTITY>>)processor;
	}

	private final Processor<K9EntityEvent<ENTITY>, K9EntityEvent<ENTITY>> processor =
		BroadcastProcessor.create();

	protected K9EntityMapper<ENTITY, DTO> mapper;

}
