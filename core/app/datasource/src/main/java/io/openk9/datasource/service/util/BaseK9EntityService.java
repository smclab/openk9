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


import io.openk9.common.graphql.util.relay.DefaultPageInfo;
import io.openk9.common.graphql.util.service.GraphQLService;
import io.openk9.common.model.EntityServiceValidatorWrapper;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.mapper.K9EntityMapper;
import io.openk9.datasource.model.dto.util.K9EntityDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.model.util.K9Entity_;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.FilterField;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.hibernate.common.runtime.PanacheJpaUtil;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import io.smallrye.mutiny.tuples.Tuple2;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.reactivestreams.Processor;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseK9EntityService<ENTITY extends K9Entity, DTO extends K9EntityDTO>
	extends GraphQLService<ENTITY>
	implements K9EntityService<ENTITY, DTO> {

	public static final DefaultPageInfo DEFAULT_PAGE_INFO =
		new DefaultPageInfo(null, null, false, false);

	public abstract Class<ENTITY> getEntityClass();

	@Override
	public Uni<List<ENTITY>> findAll() {

		return sessionFactory.withTransaction(this::findAll);

	}

	@Override
	public Uni<List<ENTITY>> findAll(String tenantId) {

		return sessionFactory.withTransaction(tenantId, (s, t) -> findAll(s));

	}

	@Override
	public Uni<Page<ENTITY>> findAllPaginated(Pageable pageable, String searchText) {

		return findAllPaginated(null, pageable, searchText);

	}

	@Override
	public Uni<Page<ENTITY>> findAllPaginated(
		String tenantId, Pageable pageable, String searchText) {

		if (searchText == null || searchText.isEmpty()) {
			return findAllPaginated(tenantId, pageable);
		}

		return findAllPaginated(tenantId, pageable, searchTextToFilter(searchText));

	}

	@Override
	public Uni<Page<ENTITY>> findAllPaginated(Pageable pageable, Filter filter) {

		return findAllPaginated(null, pageable, filter);

	}

	@Override
	public Uni<Page<ENTITY>> findAllPaginated(
		String tenantId, Pageable pageable, Filter filter) {

		return findAllPaginated(
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(), filter);

	}

	@Override
	public Uni<Page<ENTITY>> findAllPaginated(Pageable pageable) {

		return findAllPaginated(null, pageable, Filter.DEFAULT);

	}

	@Override
	public Uni<Page<ENTITY>> findAllPaginated(String tenantId, Pageable pageable) {

		return findAllPaginated(tenantId, pageable, Filter.DEFAULT);

	}

	@Override
	public <T extends K9Entity> Uni<Page<T>> findAllPaginatedJoin(
		Long[] entityIds, String joinField, Class<T> joinType, int limit,
		String sortBy, long afterId, long beforeId, String searchText) {

		return findAllPaginatedJoin(
			null, entityIds, joinField, joinType, limit, sortBy, afterId, beforeId, searchText);

	}

	@Override
	public <T extends K9Entity> Uni<Page<T>> findAllPaginatedJoin(
		String tenantId, Long[] entityIds, String joinField, Class<T> joinType, int limit,
		String sortBy, long afterId, long beforeId, String searchText) {

		Filter filter = searchTextToFilter(searchText);

		return findAllPaginatedJoin(
			tenantId, entityIds, joinField, joinType, limit, sortBy, afterId, beforeId, filter);

	}
	@Override
	public <T extends K9Entity> Uni<Page<T>> findAllPaginatedJoin(
		Long[] entityIds, String joinField, Class<T> joinType, int limit,
		String sortBy, long afterId, long beforeId, Filter filter) {

		return findAllPaginatedJoin(null, entityIds, joinField, joinType, limit, sortBy,
			afterId, beforeId, filter);

	}

	@Override
	public <T extends K9Entity> Uni<Page<T>> findAllPaginatedJoin(
		String tenantId, Long[] entityIds, String joinField, Class<T> joinType, int limit,
		String sortBy, long afterId, long beforeId, Filter filter) {

		return Uni.createFrom().deferred(() -> {

			CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<T> joinEntityQuery =
				builder.createQuery(joinType);

			Root<ENTITY> entityRoot = joinEntityQuery.from(getEntityClass());

			Join<ENTITY, T> join = entityRoot.joinSet(joinField);

			CriteriaQuery<T> criteriaQuery = joinEntityQuery.select(join);

			criteriaQuery.where(entityRoot.get(getEntityIdField()).in(Arrays.asList(entityIds)));

			CriteriaQuery countJoinEntityQuery = builder.createQuery();

			Root<ENTITY> countEntityRoot = countJoinEntityQuery.from(getEntityClass());

			Root<T> countRoot = countJoinEntityQuery.from(joinType);

			countEntityRoot.joinSet(joinField);

			CriteriaQuery countQuery = countJoinEntityQuery.select(builder.count(countRoot));

			countQuery.where(entityRoot.get(getEntityIdField()).in(Arrays.asList(entityIds)));

			return _pageCriteriaQuery(
				tenantId, limit, sortBy, afterId, beforeId, filter, builder, join,
				criteriaQuery, countQuery);

		});

	}

	@Override
	public Uni<Page<ENTITY>> findAllPaginated(
		int limit, String sortBy, long afterId, long beforeId, Filter filter) {

		return findAllPaginated(null, limit, sortBy, afterId, beforeId, filter);

	}

	@Override
	public Uni<Page<ENTITY>> findAllPaginated(
		String tenantId, int limit, String sortBy, long afterId, long beforeId, Filter filter) {

		return Uni.createFrom().deferred(() -> {

			CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<ENTITY> criteriaQuery =
				builder.createQuery(getEntityClass());
			Root<ENTITY> root = criteriaQuery.from(getEntityClass());

			CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);

			countQuery.select(builder.count(countQuery.from(getEntityClass())));

			return _pageCriteriaQuery(
				tenantId, limit, sortBy, afterId, beforeId,
				filter == null ? Filter.DEFAULT : filter, builder, root,
				criteriaQuery, countQuery);
		});

	}

	@Override
	public String[] getSearchFields() {

		return new String[] {};

	}

	@Override
	public Uni<ENTITY> findById(long id) {

		return sessionFactory.withTransaction((s) -> findById(s, id));

	}

	@Override
	public Uni<ENTITY> findById(String tenantId, long id) {

		return sessionFactory.withTransaction(tenantId, (s, t) -> findById(s, id));

	}

	@Override
	public Uni<List<ENTITY>> findByIds(Set<Long> ids) {

		return sessionFactory.withTransaction(
			(s) -> s.find(getEntityClass(), ids.toArray(new Object[0])));

	}

	@Override
	public Uni<List<ENTITY>> findByIds(String tenantId, Set<Long> ids) {

		return sessionFactory.withTransaction(tenantId,
			(s, t) -> s.find(getEntityClass(), ids.toArray(new Object[0])));

	}

	@Override
	public Uni<ENTITY> patch(long id, DTO dto) {

		return sessionFactory.withTransaction((s) -> patch(s, id, dto));

	}

	@Override
	public Uni<ENTITY> patch(String tenantId, long id, DTO dto) {

		return sessionFactory.withTransaction(tenantId, (s, t) -> patch(s, id, dto));

	}

	@Override
	public Uni<ENTITY> update(long id, DTO dto) {

		return sessionFactory.withTransaction((s) -> update(s, id, dto));

	}

	@Override
	public Uni<ENTITY> update(String tenantId, long id, DTO dto) {

		return sessionFactory.withTransaction(tenantId, (s, t) -> update(s, id, dto));

	}

	@Override
	public Uni<ENTITY> create(DTO dto) {

		return create(mapper.create(dto));

	}

	@Override
	public Uni<ENTITY> create(String tenantId, DTO dto) {

		return create(tenantId, mapper.create(dto));

	}

	public <T extends K9Entity> Uni<T> merge(T entity) {

		return sessionFactory.withTransaction((s, t) -> merge(s, entity));

	}

	public <T extends K9Entity> Uni<T> merge(String tenantId, T entity) {

		return sessionFactory.withTransaction(tenantId, (s, t) -> merge(s, entity));

	}

	public <T extends K9Entity> Uni<T> persist(T entity) {

		return sessionFactory.withTransaction((s, t) -> persist(s, entity));

	}

	public <T extends K9Entity> Uni<T> persist(String tenantId, T entity) {

		return sessionFactory.withTransaction(tenantId, (s, t) -> persist(s, entity));

	}

	@Override
	public Uni<ENTITY> create(ENTITY entity) {

		return persist(entity)
			.invoke(e -> processor.onNext(
				K9EntityEvent.of(K9EntityEvent.EventType.CREATE, e)));

	}

	@Override
	public Uni<ENTITY> create(String tenantId, ENTITY entity) {

		return persist(tenantId, entity)
			.invoke(e -> processor.onNext(
				K9EntityEvent.of(K9EntityEvent.EventType.CREATE, e)));

	}


	@Override
	public Uni<ENTITY> create(Mutiny.Session s, ENTITY entity) {

		return persist(s, entity)
			.invoke(e -> processor.onNext(
				K9EntityEvent.of(K9EntityEvent.EventType.CREATE, e)));

	}

	@Override
	public Uni<ENTITY> deleteById(long entityId) {

		return sessionFactory.withTransaction((s) -> deleteById(s, entityId));

	}

	@Override
	public Uni<ENTITY> deleteById(String tenantId, long entityId) {

		return sessionFactory.withTransaction(tenantId, (s, t) -> deleteById(s, entityId));

	}

	public EntityServiceValidatorWrapper<ENTITY, DTO> getValidator() {

		EntityServiceValidatorWrapper<ENTITY, DTO> wrapper = validatorWrapper.get();

		if (wrapper == null) {
			validatorWrapper.set(
				wrapper = new EntityServiceValidatorWrapper<>(this, validator));
		}

		return wrapper;

	}

	@Override
	public Uni<Long> count() {

		return sessionFactory.withTransaction(s -> s.createQuery(
			"SELECT COUNT(*) FROM " + PanacheJpaUtil.getEntityName(getEntityClass()), Long.class)
			.getSingleResult());

	}

	@Override
	public Uni<Long> count(String tenantId) {

		return sessionFactory.withTransaction(tenantId, (s, t) -> s.createQuery(
			"SELECT COUNT(*) FROM " + PanacheJpaUtil.getEntityName(getEntityClass()), Long.class)
			.getSingleResult());

	}

	public BroadcastProcessor<K9EntityEvent<ENTITY>> getProcessor() {

		return (BroadcastProcessor<K9EntityEvent<ENTITY>>) processor;

	}

	@Override
	public final SingularAttribute<K9Entity, Long> getIdAttribute() {

		return K9Entity_.id;

	}

	@Override
	public final CriteriaBuilder getCriteriaBuilder() {

		return sessionFactory.getCriteriaBuilder();

	}

	@Override
	protected Mutiny.SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	protected String getEntityIdField() {

		return K9Entity_.ID;

	}

	protected Filter searchTextToFilter(String searchText) {

		if (searchText == null || searchText.isEmpty()) {
			return Filter.DEFAULT;
		}

		List<FilterField> filterFields =
			Arrays
				.stream(getSearchFields())
				.map(s -> FilterField
					.builder()
					.fieldName(s)
					.value(searchText)
					.operator(FilterField.Operator.contains)
					.build()
				)
				.collect(Collectors.toList());

		return Filter.of(false, filterFields);

	}

	protected Uni<Void> remove(ENTITY entity) {

		return sessionFactory.withTransaction((s, t) -> remove(s, entity));

	}

	private static Sort createSort(String sortBy) {

		return createSort("", sortBy);

	}

	private static Sort createSort(String prefix, String sortBy) {

		prefix = (prefix == null || prefix.isEmpty()) ? "" : prefix + ".";

		if (sortBy != null && !sortBy.isBlank()) {
			return Sort.by(prefix + K9Entity_.ID);
		}
		else {
			return Sort.by(prefix + sortBy).and(prefix + K9Entity_.ID);
		}

	}

	private <T extends K9Entity> Uni<Page<T>> _pageCriteriaQuery(

		String tenantId, int limit, String sortBy, long afterId, long beforeId, Filter filter,
		CriteriaBuilder builder, Path<T> root, CriteriaQuery<T> criteriaQuery,
		CriteriaQuery countQuery) {

		filter = filter == null ? Filter.DEFAULT : filter;

		boolean andOperator = filter.isAndOperator();

		List<FilterField> filterFields = filter.getFilterFields();

		if (filterFields == null) {
			filterFields = new ArrayList<>();
		}
		else {
			filterFields = new ArrayList<>(filterFields);
		}

		if (afterId > 0 && beforeId > 0) {

			filterFields.add(
				FilterField
					.builder()
					.fieldName(getEntityIdField())
					.value(Long.toString(afterId))
					.operator(FilterField.Operator.greaterThan)
					.build()
			);

			filterFields.add(
				FilterField
					.builder()
					.fieldName(getEntityIdField())
					.value(Long.toString(beforeId))
					.operator(FilterField.Operator.lessThan)
					.build()
			);

		}
		else if (afterId > 0) {
			filterFields.add(
				FilterField
					.builder()
					.fieldName(getEntityIdField())
					.value(Long.toString(afterId))
					.operator(FilterField.Operator.greaterThan)
					.build()
			);
		}
		else if (beforeId > 0) {
			filterFields.add(
				FilterField
					.builder()
					.fieldName(getEntityIdField())
					.value(Long.toString(beforeId))
					.operator(FilterField.Operator.lessThan)
					.build()
			);
		}

		Optional<Predicate> reducePredicate =
			filterFields
				.stream()
				.flatMap(ff -> {

					Predicate predicate =
						ff.generateCriteria(builder, root::get);

					if (predicate != null) {
						return Stream.of(predicate);
					}

					logger.warn(
						"FilterField generated null predicate for fieldName: " + ff.getFieldName());

					return Stream.empty();


				})
				.reduce((p1, p2) -> andOperator
					? builder.and(p1, p2)
					: builder.or(p1, p2)
				);


		if (sortBy != null && !sortBy.isBlank()) {
			criteriaQuery
				.orderBy(
					builder.asc(root.get(sortBy)),
					builder.asc(root.get(getEntityIdField()))
				);
		}
		else {
			criteriaQuery.orderBy(builder.asc(root.get(getEntityIdField())));
		}

		reducePredicate.ifPresent(p -> {
			criteriaQuery.where(p);
			countQuery.where(p);
		});


		Uni<Tuple2<Long, List<T>>> tuple2Results;

		if (tenantId != null) {
			tuple2Results = sessionFactory.withTransaction(tenantId, (s, t) -> _executePagedQuery(limit, criteriaQuery, countQuery, s));
		}
		else {
			tuple2Results = sessionFactory.withTransaction((s) -> _executePagedQuery(limit, criteriaQuery, countQuery, s));
		}

		return tuple2Results.map(t -> Page.of(limit, t.getItem1(), t.getItem2()));

	}

	private static <T extends K9Entity>
		Uni<io.smallrye.mutiny.tuples.Tuple2<Long, List<T>>> _executePagedQuery(
		int limit, CriteriaQuery<T> criteriaQuery, CriteriaQuery countQuery, Mutiny.Session s) {

		Uni<List<T>> resultList = (limit >= 0
			? s.createQuery(criteriaQuery).setMaxResults(limit)
			: s.createQuery(criteriaQuery)).getResultList();

		Uni<Long> count = s.createQuery(countQuery).getSingleResult();

		return Uni
			.combine()
			.all()
			.unis(count, resultList)
			.asTuple();

	}

	public Uni<ENTITY> findById(Mutiny.Session s, long id) {

		return s.find(getEntityClass(), id);

	}

	protected Uni<List<ENTITY>> findAll(Mutiny.Session s) {

		CriteriaBuilder criteriaBuilder = sessionFactory.getCriteriaBuilder();

		CriteriaQuery<ENTITY> query =
			criteriaBuilder.createQuery(getEntityClass());

		query.from(getEntityClass());

		return s.createQuery(query)
			.getResultList();

	}

	protected Uni<ENTITY> patch(Mutiny.Session s, long id, DTO dto) {
		return findById(s, id)
			.onItem().ifNotNull()
			.transformToUni(
				(prev) -> persist(s, mapper.patch(prev, dto))
					.invoke(newEntity -> processor.onNext(
						K9EntityEvent.of(
							K9EntityEvent.EventType.UPDATE, newEntity, prev))))
			.onItem().ifNull()
			.failWith(
				() -> new IllegalStateException(
					"dto: " + dto.getClass().getSimpleName() + " with id: " +
						id + " not found"));
	}

	protected Uni<ENTITY> update(Mutiny.Session s, long id, DTO dto) {
		return findById(s, id)
			.onItem().ifNotNull()
			.transformToUni(
				(prev) -> persist(mapper.update(prev, dto))
					.invoke(newEntity -> processor.onNext(
						K9EntityEvent.of(
							K9EntityEvent.EventType.UPDATE, newEntity, prev))))
			.onItem().ifNull().failWith(
				() -> new IllegalStateException(
					"entity: " + dto.getClass().getSimpleName() + " with id: " +
						id + " not found"));
	}

	protected  <T extends K9Entity> Uni<T> merge(Mutiny.Session s, T entity) {

		return s.merge(entity)
			.call(s::flush);
	}

	public  <T extends K9Entity> Uni<T> persist(Mutiny.Session s, T entity) {

		return s.persist(entity)
			.map(v -> entity)
			.call(s::flush);
	}

	protected Uni<Void> remove(Mutiny.Session s, ENTITY entity) {

		return s.remove(entity);
	}

	public Uni<ENTITY> deleteById(Mutiny.Session s, long entityId) {

		return findById(s, entityId)
			.onItem().ifNotNull()
			.call(entity -> remove(s, entity))
			.invoke(e -> processor.onNext(
				K9EntityEvent.of(K9EntityEvent.EventType.DELETE, e)))
			.onItem().ifNull()
			.failWith(() -> new IllegalStateException("entity with id: " + entityId
				+ " for service: " + getClass().getSimpleName() + " not found"));

	}

	protected K9EntityMapper<ENTITY, DTO> mapper;

	@Inject
	protected Mutiny.SessionFactory sessionFactory;

	@Inject
	Logger logger;

	@Inject
	protected Validator validator;

	@Inject
	ActorSystemProvider actorSystemProvider;

	private final Processor<K9EntityEvent<ENTITY>, K9EntityEvent<ENTITY>> processor =
		BroadcastProcessor.create();

	private AtomicReference<EntityServiceValidatorWrapper<ENTITY, DTO>> validatorWrapper =
		new AtomicReference<>();
}