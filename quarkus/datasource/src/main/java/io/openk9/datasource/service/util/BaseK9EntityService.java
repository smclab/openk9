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

import graphql.relay.Connection;
import graphql.relay.ConnectionCursor;
import graphql.relay.DefaultConnection;
import graphql.relay.DefaultPageInfo;
import graphql.relay.Edge;
import graphql.relay.PageInfo;
import io.openk9.datasource.graphql.util.RelayUtil;
import io.openk9.datasource.mapper.K9EntityMapper;
import io.openk9.datasource.model.dto.util.K9EntityDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.FilterField;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.quarkus.hibernate.reactive.panache.common.runtime.AbstractJpaOperations;
import io.quarkus.hibernate.reactive.panache.runtime.PanacheQueryImpl;
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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseK9EntityService<ENTITY extends K9Entity, DTO extends K9EntityDTO>
	implements K9EntityService<ENTITY, DTO> {

	public static final DefaultPageInfo DEFAULT_PAGE_INFO =
		new DefaultPageInfo(null, null, false, false);

	public abstract Class<ENTITY> getEntityClass();

	/**
	 * Thus when I get the request I process it in the following way:
	 *
	 * Start from the greedy query: SELECT * FROM table
	 * If the after argument is provided, add id > parsed_cursor to the WHERE clause
	 * If the before argument is provided, add id < parsed_cursor to the WHERE clause
	 * If the first argument is provided, add ORDER BY id DESC LIMIT first+1 to the query
	 * If the last argument is provided, add ORDER BY id ASC LIMIT last+1 to the query
	 * If the last argument is provided, I reverse the order of the results
	 * If the first argument is provided then I set hasPreviousPage: false (see spec for a description of this behavior).
	 * If no less than first+1 results are returned, I set hasNextPage: true, otherwise I set it to false.
	 * If the last argument is provided then I set hasNextPage: false (see spec for a description of this behavior).
	 * If no less last+1 results are returned, I set hasPreviousPage: true, otherwise I set it to false.
	 * Using this "algorithm", only the needed data is fetched. While after and before can be both set, I make sure first and last args are treated as mutually exclusive to avoid making a mess. The spec itself discourage making requests with both the arguments set.
	 *
	 * Notably, I return an object with the shape described above (and in the linked spec) and I don't use the connectionFromArray helper, which expects a raw collection to slice accordingly to the args.
	 *
	 * EDIT: The above algorithm produces array with different orders depending on which of first or last is used to paginate. This is against the specification, so there should be one more step:
	 *
	 * Re-order results in a consistent manner (e.g. by creation date)
	 * Thanks to @Sytten for bringing this problem to my attention.
	 *
	 * EDIT2: The above ordering reflects pagination, not chronological order. Hence first is interpreted as 'first to be displayed', not 'first chronologically'. Therefore, you might want to swap ASC and DESC in step (4) and (5). Thanks to @Sytten for bringing this problem to my attention.
	 *
	 * @see https://github.com/graphql/graphql-relay-js/issues/94#issuecomment-232410564
	 */
	public Uni<Connection<ENTITY>> findConnection(
		String after, String before, Integer first, Integer last) {

		return withTransaction((s, t) -> {

			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

			CriteriaQuery<ENTITY> criteriaBuilderQuery =
				criteriaBuilder.createQuery(getEntityClass());

			Root<ENTITY> root = criteriaBuilderQuery.from(getEntityClass());

			Predicate where = criteriaBuilder.conjunction();

			if (after != null) {
				RelayUtil.Cursor cursor = RelayUtil.decodeCursor(after);
				where = criteriaBuilder.greaterThan(root.get("id"), cursor.getId());
			}

			if (before != null) {
				RelayUtil.Cursor cursor = RelayUtil.decodeCursor(before);
				where = criteriaBuilder.lessThan(root.get("id"), cursor.getId());
			}

			criteriaBuilderQuery.where(where);

			if (first != null) {
				criteriaBuilderQuery.orderBy(criteriaBuilder.desc(root.get("id")));
			}

			if (last != null) {
				criteriaBuilderQuery.orderBy(criteriaBuilder.asc(root.get("id")));
			}

			Mutiny.Query<ENTITY> query = s.createQuery(criteriaBuilderQuery);

			if (first != null) {
				query.setMaxResults(first + 1);
			}

			if (last != null) {
				query.setMaxResults(last + 1);
			}

			Uni<List<ENTITY>> entities = query.getResultList();

			return entities.map(entitiesList -> {

				List<Edge<ENTITY>> edges = RelayUtil.toEdgeList(entitiesList);

				int size = edges.size();

				ConnectionCursor startCursor =
					edges.isEmpty()
						? null
						: edges.get(0).getCursor();

				ConnectionCursor endCursor =
					edges.isEmpty()
						? null
						: edges.get(size - 1).getCursor();


				PageInfo pageInfo = DEFAULT_PAGE_INFO;

				if (first != null) {
					pageInfo = new DefaultPageInfo(
						startCursor, endCursor, false, size > first + 1);
				}

				if (last != null) {
					pageInfo = new DefaultPageInfo(
						startCursor, endCursor, size > last + 1, false);
				}

				return new DefaultConnection<>(edges, pageInfo);

			});

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

			Root<T> root = joinEntityQuery.from(joinType);

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
				limit, sortBy, afterId, beforeId, filter, builder, root,
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

	protected String[] getSearchFields() {
		return new String[] {"name"};
	}

	protected String getEntityIdField() {
		return "id";
	}

	private <T extends K9Entity> Uni<Page<T>> _pageCriteriaQuery(
		int limit, String sortBy, long afterId, long beforeId, Filter filter,
		CriteriaBuilder builder, Root<T> root, CriteriaQuery<T> criteriaQuery,
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
			return Sort.by(prefix + "id");
		}
		else {
			return Sort.by(prefix + sortBy).and(prefix + "id");
		}

	}

	@Override
	public Uni<ENTITY> findById(long id) {
		return withTransaction((s) -> s.find(getEntityClass(), id));
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

	protected Uni<ENTITY> persist(ENTITY entity) {
		return withTransaction(
			(s, t) -> jpaOperations
				.persist(Uni.createFrom().item(s), entity)
				.map(v -> entity)
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
		return em.withTransaction(fun);
	}

	private final Processor<K9EntityEvent<ENTITY>, K9EntityEvent<ENTITY>>
		processor =
		BroadcastProcessor.create();

	protected K9EntityMapper<ENTITY, DTO> mapper;

	@Inject
	protected AbstractJpaOperations<PanacheQueryImpl<ENTITY>> jpaOperations;

	@Inject
	protected Mutiny.SessionFactory em;

	@Inject
	Logger logger;

}