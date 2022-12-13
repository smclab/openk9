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
import io.openk9.datasource.mapper.K9EntityMapper;
import io.openk9.datasource.model.dto.util.K9EntityDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.model.util.K9Entity_;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.FilterField;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.datasource.validation.ValidatorK9EntityWrapper;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.hibernate.common.runtime.PanacheJpaUtil;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseK9EntityService<ENTITY extends K9Entity, DTO extends K9EntityDTO>
	extends GraphQLService<ENTITY>
	implements K9EntityService<ENTITY, DTO> {

	public static final DefaultPageInfo DEFAULT_PAGE_INFO =
		new DefaultPageInfo(null, null, false, false);

	public abstract Class<ENTITY> getEntityClass();

	public Uni<List<ENTITY>> findAll() {
		return withTransaction(s -> {

			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

			CriteriaQuery<ENTITY> query =
				criteriaBuilder.createQuery(getEntityClass());

			query.from(getEntityClass());

			return s.createQuery(query)
				.setCacheable(true)
				.getResultList();

		});

	}

	@Override
	public Uni<Page<ENTITY>> findAllPaginated(
		Pageable pageable, String searchText) {

		if (searchText == null || searchText.isEmpty()) {
			return findAllPaginated(pageable);
		}

		return findAllPaginated(pageable, searchTextToFilter(searchText));

	}

	@Override
	public Uni<Page<ENTITY>> findAllPaginated(
		Pageable pageable, Filter filter) {

		return findAllPaginated(
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(), filter);

	}

	@Override
	public Uni<Page<ENTITY>> findAllPaginated(Pageable pageable) {

		return findAllPaginated(pageable, Filter.DEFAULT);

	}

	@Override
	public <T extends K9Entity> Uni<Page<T>> findAllPaginatedJoin(
		Long[] entityIds, String joinField, Class<T> joinType, int limit,
		String sortBy,
		long afterId, long beforeId, String searchText) {

		Filter filter = searchTextToFilter(searchText);

		return findAllPaginatedJoin(
			entityIds, joinField, joinType, limit, sortBy, afterId, beforeId, filter);

	}

	@Override
	public <T extends K9Entity> Uni<Page<T>> findAllPaginatedJoin(
		Long[] entityIds, String joinField, Class<T> joinType, int limit,
		String sortBy,
		long afterId, long beforeId, Filter filter) {

		return Uni.createFrom().deferred(() -> {

			CriteriaBuilder builder = em.getCriteriaBuilder();

			CriteriaQuery<T> joinEntityQuery =
				builder.createQuery(joinType);

			Root<ENTITY> entityRoot = joinEntityQuery.from(getEntityClass());

			Join<ENTITY, T> join = entityRoot.joinSet(joinField);

			CriteriaQuery<T> criteriaQuery = joinEntityQuery.select(join);

			criteriaQuery.where(entityRoot.get(getEntityIdField()).in(Arrays.asList(entityIds)));

			CriteriaQuery countJoinEntityQuery =
				builder.createQuery();

			Root<ENTITY> countEntityRoot = countJoinEntityQuery.from(getEntityClass());

			Root<T> countRoot = countJoinEntityQuery.from(joinType);

			countEntityRoot.joinSet(joinField);

			CriteriaQuery countQuery = countJoinEntityQuery.select(builder.count(countRoot));

			countQuery.where(entityRoot.get(getEntityIdField()).in(Arrays.asList(entityIds)));

			return _pageCriteriaQuery(
				limit, sortBy, afterId, beforeId, filter, builder, join,
				criteriaQuery, countQuery);

		});

	}

	@Override
	public Uni<Page<ENTITY>> findAllPaginated(
		int limit, String sortBy, long afterId, long beforeId, Filter filter) {

		return Uni.createFrom().deferred(() -> {

			CriteriaBuilder builder = em.getCriteriaBuilder();

			CriteriaQuery<ENTITY> criteriaQuery =
				builder.createQuery(getEntityClass());
			Root<ENTITY> root = criteriaQuery.from(getEntityClass());

			CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);

			countQuery.select(builder.count(countQuery.from(getEntityClass())));

			return _pageCriteriaQuery(
				limit, sortBy, afterId, beforeId,
				filter == null ? Filter.DEFAULT : filter, builder, root,
				criteriaQuery, countQuery);
		});

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

	public String[] getSearchFields() {
		return new String[] {};
	}

	protected String getEntityIdField() {
		return K9Entity_.ID;
	}

	private <T extends K9Entity> Uni<Page<T>> _pageCriteriaQuery(
		int limit, String sortBy, long afterId, long beforeId, Filter filter,
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

		return withTransaction((s) -> {

				Uni<List<T>> resultList = (limit >= 0
					? s.createQuery(criteriaQuery).setMaxResults(limit)
					: s.createQuery(criteriaQuery)).getResultList();

				Uni<Long> count = s.createQuery(countQuery).getSingleResult();

				return Uni
					.combine()
					.all()
					.unis(count, resultList)
					.asTuple();

			})
			.map(t -> Page.of(limit, t.getItem1(), t.getItem2()));
	}

	public static Sort createSort(String sortBy) {
		return createSort("", sortBy);
	}

	public static Sort createSort(String prefix, String sortBy) {

		prefix = (prefix == null || prefix.isEmpty()) ? "" : prefix + ".";

		if (sortBy != null && !sortBy.isBlank()) {
			return Sort.by(prefix + K9Entity_.ID);
		}
		else {
			return Sort.by(prefix + sortBy).and(prefix + K9Entity_.ID);
		}

	}

	@Override
	public Uni<ENTITY> findById(long id) {
		return withTransaction((s) -> s.find(getEntityClass(), id));
	}

	@Override
	public Uni<List<ENTITY>> findByIds(Set<Long> ids) {
		return withTransaction(
			(s) -> s.find(getEntityClass(), ids.toArray(new Object[0])));
	}

	@Override
	public Uni<ENTITY> patch(long id, DTO dto) {
		return withTransaction(() -> findById(id)
			.onItem().ifNotNull()
			.transformToUni(
				(prev) -> persist(mapper.patch(prev, dto))
					.invoke(newEntity -> processor.onNext(
						K9EntityEvent.of(
							K9EntityEvent.EventType.UPDATE, newEntity, prev))))
			.onItem().ifNull().failWith(
				() -> new IllegalStateException(
					"dto: " + dto.getClass().getSimpleName() + " with id: " +
					id + " not found")));
	}

	@Override
	public Uni<ENTITY> update(long id, DTO dto) {
		return withTransaction(() -> findById(id)
			.onItem().ifNotNull()
			.transformToUni(
				(prev) -> persist(mapper.update(prev, dto))
					.invoke(newEntity -> processor.onNext(
						K9EntityEvent.of(
							K9EntityEvent.EventType.UPDATE, newEntity, prev))))
			.onItem().ifNull().failWith(
				() -> new IllegalStateException(
					"entity: " + dto.getClass().getSimpleName() + " with id: " +
					id + " not found")));
	}

	@Override
	public Uni<ENTITY> create(DTO dto) {
		return create(mapper.create(dto));
	}

	public <T extends K9Entity> Uni<T> merge(T entity) {
		return withTransaction(
			(s, t) -> s.merge(entity)
				.call(s::flush)
		);
	}

	public <T extends K9Entity> Uni<T> persist(T entity) {
		return withTransaction(
			(s, t) -> s.persist(entity)
				.map(v -> entity)
				.call(s::flush)
		);
	}

	@Override
	public Uni<ENTITY> create(ENTITY entity) {
		return persist(entity)
			.invoke(e -> processor.onNext(
				K9EntityEvent.of(K9EntityEvent.EventType.CREATE, e)));
	}

	protected Uni<Void> remove(ENTITY entity) {
		return withTransaction((s, t) -> s.remove(entity));
	}

	@Override
	public Uni<ENTITY> deleteById(long entityId) {
		return withTransaction(() -> findById(entityId)
			.onItem().ifNotNull()
			.call(this::remove)
			.invoke(e -> processor.onNext(
				K9EntityEvent.of(K9EntityEvent.EventType.DELETE, e)))
			.onItem().ifNull().failWith(
				() -> new IllegalStateException(
					"entity with id: " + entityId + " for service: " +
					getClass().getSimpleName() + " not found")));
	}

	public ValidatorK9EntityWrapper<ENTITY, DTO> getValidator() {
		ValidatorK9EntityWrapper<ENTITY, DTO> wrapper = validatorWrapper.get();

		if (wrapper == null) {
			validatorWrapper.set(
				wrapper = new ValidatorK9EntityWrapper<>(this, validator));
		}

		return wrapper;

	}

	public Uni<Long> count() {
		return withTransaction(
			session -> session.createQuery(
				"SELECT COUNT(*) FROM " + PanacheJpaUtil.getEntityName(getEntityClass()), Long.class)
				.getSingleResult());
	}

	public BroadcastProcessor<K9EntityEvent<ENTITY>> getProcessor() {
		return (BroadcastProcessor<K9EntityEvent<ENTITY>>) processor;
	}

	protected <T> Uni<T> withTransaction(Supplier<Uni<T>> fun) {
		return withTransaction((session, transaction) -> fun.get());
	}

	protected <T> Uni<T> withTransaction(
		Function<Mutiny.Session, Uni<T>> fun) {
		return withTransaction((session, transaction) -> fun.apply(session));
	}

	protected <T> Uni<T> withTransaction(
		BiFunction<Mutiny.Session, Mutiny.Transaction, Uni<T>> fun) {

		return transactionInvoker.withTransaction(fun);

	}

	protected <T> Uni<T> withStatelessTransaction(Supplier<Uni<T>> fun) {
		return withStatelessTransaction((session, transaction) -> fun.get());

	}

	protected <T> Uni<T> withStatelessTransaction(
		Function<Mutiny.StatelessSession, Uni<T>> fun) {

		return withStatelessTransaction((session, transaction) -> fun.apply(session));

	}

	protected <T> Uni<T> withStatelessTransaction(
		BiFunction<Mutiny.StatelessSession, Mutiny.Transaction, Uni<T>> fun) {

		return transactionInvoker.withStatelessTransaction(fun);

	}

	@Override
	public final SingularAttribute<K9Entity, Long> getIdAttribute() {
		return K9Entity_.id;
	}

	@Override
	public final CriteriaBuilder getCriteriaBuilder() {
		return transactionInvoker.getCriteriaBuilder();
	}

	private final Processor<K9EntityEvent<ENTITY>, K9EntityEvent<ENTITY>>
		processor =
		BroadcastProcessor.create();

	protected K9EntityMapper<ENTITY, DTO> mapper;

	@Inject
	protected TransactionInvoker em;

	@Inject
	Logger logger;

	@Inject
	protected Validator validator;

	@Inject
	TransactionInvoker transactionInvoker;

	private AtomicReference<ValidatorK9EntityWrapper<ENTITY, DTO>> validatorWrapper =
		new AtomicReference<>();

}