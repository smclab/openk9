package io.openk9.common.graphql.util.service;

import io.openk9.common.graphql.util.exception.InvalidPageSizeException;
import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.graphql.util.relay.DefaultConnection;
import io.openk9.common.graphql.util.relay.DefaultPageInfo;
import io.openk9.common.graphql.util.relay.Edge;
import io.openk9.common.graphql.util.relay.GraphqlId;
import io.openk9.common.graphql.util.relay.PageInfo;
import io.openk9.common.graphql.util.relay.RelayUtil;
import io.openk9.common.util.SortBy;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SingularAttribute;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.lang.String.format;

public abstract class GraphQLService<ENTITY extends GraphqlId> {

	public Uni<Connection<ENTITY>> findConnection(
		String after, String before, Integer first, Integer last) {

		return findConnection(after, before, first, last, null, Set.of());

	}

	public <T extends GraphqlId> Uni<Connection<T>> findJoinConnection(
		long entityId, String joinField, Class<T> joinType, String[] searchFields,
		String after, String before, Integer first, Integer last) {

		return findJoinConnection(
			entityId, joinField, joinType, searchFields, after, before, first,
			last, null, Set.of(), false);

	}

	public <T extends GraphqlId> Uni<Connection<T>> findJoinConnection(
		long entityId, String joinField, Class<T> joinType,
		String[] searchFields, String after,
		String before, Integer first, Integer last, String searchText,
		Set<SortBy> sortByList) {

		return findJoinConnection(
			entityId, joinField, joinType, searchFields, after, before, first,
			last, searchText, sortByList, false,
			entityRoot -> entityRoot.join(joinField),
			(i1, i2) -> List.of());

	}

	public <T extends GraphqlId> Uni<Connection<T>> findJoinConnection(
		long entityId, String joinField, Class<T> joinType,
		String[] searchFields, String after,
		String before, Integer first, Integer last, String searchText,
		Set<SortBy> sortByList, boolean not) {

		return findJoinConnection(
			entityId, joinField, joinType, searchFields, after, before, first,
			last, searchText, sortByList, not,
			entityRoot -> entityRoot.join(joinField),
			(i1, i2) -> List.of());

	}

	public <T extends GraphqlId> Uni<Connection<T>> findJoinConnection(
		long entityId, String joinField, Class<T> joinType,
		String[] searchFields, String after,
		String before, Integer first, Integer last, String searchText,
		Set<SortBy> sortByList, boolean not,
		BiFunction<CriteriaBuilder, Path<T>, Predicate> whereFun) {

		return findJoinConnection(
			entityId, joinField, joinType, searchFields, after, before, first,
			last, searchText, sortByList, not,
			entityRoot -> entityRoot.join(joinField),
			(i1, i2) -> List.of(),
			whereFun);

	}

	public <T extends GraphqlId> Uni<Connection<T>> findJoinConnection(
		long entityId, String joinField, Class<T> joinType,
		String[] searchFields, String after,
		String before, Integer first, Integer last, String searchText,
		Set<SortBy> sortByList, boolean not,
		Function<Root<ENTITY>, Path<T>> mapper,
		BiFunction<CriteriaBuilder, Root<ENTITY>, List<Order>> defaultOrderFun) {

		return findJoinConnection(
			entityId, joinField, joinType, searchFields, after, before, first,
			last, searchText, sortByList, not,
			mapper,
			defaultOrderFun,
			(criteriaBuilder, tPath) -> criteriaBuilder.conjunction());

	}

	public <T extends GraphqlId> Uni<Connection<T>> findJoinConnection(
		long entityId, String joinField, Class<T> joinType,
		String[] searchFields, String after,
		String before, Integer first, Integer last, String searchText,
		Set<SortBy> sortByList, boolean not,
		Function<Root<ENTITY>, Path<T>> mapper,
		BiFunction<CriteriaBuilder, Root<ENTITY>, List<Order>> defaultOrderFun,
		BiFunction<CriteriaBuilder, Path<T>, Predicate> whereFun) {

		CriteriaBuilder builder = getCriteriaBuilder();

		CriteriaQuery<T> joinEntityQuery = builder.createQuery(joinType);

		if (not) {

			Root<T> upperRoot = joinEntityQuery.from(joinType);

			Subquery<Long> subquery = joinEntityQuery.subquery(Long.class);

			Root<ENTITY> subRoot = subquery.from(getEntityClass());

			Path<T> subJoin = mapper.apply(subRoot);

			subquery.select(subJoin.get(getIdAttribute()));

			subquery.where(
				builder.equal(
					subRoot.get(getIdAttribute()), entityId));

			return findConnection(
				joinEntityQuery, upperRoot,
				builder.and(
					whereFun.apply(builder, subJoin),
					builder.in(upperRoot.get(getIdAttribute())).value(subquery).not()),
				searchFields, after, before, first, last, searchText, sortByList);

		}
		else {

			Root<ENTITY> entityRoot = joinEntityQuery.from(getEntityClass());

			Path<T> join = mapper.apply(entityRoot);

			joinEntityQuery.orderBy(defaultOrderFun.apply(builder, entityRoot));

			return findConnection(
				joinEntityQuery.select(join), join,
				builder.and(
					whereFun.apply(builder, join),
					builder.equal(entityRoot.get(getIdAttribute()), entityId)),
				searchFields, after, before, first, last, searchText, sortByList);
		}

	}

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
	 * @see {@link <a href="https://github.com/graphql/graphql-relay-js/issues/94#issuecomment-232410564">...</a>}
	 */
	public <T extends GraphqlId> Uni<Connection<T>> findConnection(
		CriteriaQuery<T> criteriaBuilderQuery, Path<T> root,
		Predicate defaultWhere, String[] searchFields, String after,
		String before, Integer first, Integer last, String searchText,
		Set<SortBy> sortByList) {

		return withTransaction((s, t) -> {

			Predicate where = defaultWhere;

			CriteriaBuilder criteriaBuilder = getCriteriaBuilder();

			if (after != null) {
				RelayUtil.Cursor cursor = RelayUtil.decodeCursor(after);
				where = criteriaBuilder.and(
					where, criteriaBuilder.greaterThan(root.get(getIdAttribute()), cursor.getId()));
			}

			if (before != null) {
				RelayUtil.Cursor cursor = RelayUtil.decodeCursor(before);
				where = criteriaBuilder.and(
					where, criteriaBuilder.lessThan(root.get(getIdAttribute()), cursor.getId()));
			}

			if (searchText != null && !searchText.isBlank()) {

				Predicate searchConditions = criteriaBuilder.disjunction();

				for (String searchField : searchFields) {

					Path<?> searchPath = root.get(searchField);

					searchConditions = _addSearchCondition(
						criteriaBuilder, searchConditions, searchPath,
						searchText);

				}
				if (StringUtils.isNumeric(searchText)) {
					searchConditions = criteriaBuilder.or(
						searchConditions, criteriaBuilder.equal(
							root.get(getIdAttribute()),
							Long.parseLong(searchText)
						)
					);
				}

				where = criteriaBuilder.and(where, searchConditions);

			}

			criteriaBuilderQuery.where(where);

			List<Order> orders =
				new ArrayList<>(criteriaBuilderQuery.getOrderList());

			if (sortByList != null) {
				for (SortBy sortBy : sortByList) {
					Path<?> sortPath = root.get(sortBy.getColumn());
					orders.add(
						sortBy.getDirection() == SortBy.Direction.ASC
							? criteriaBuilder.asc(sortPath)
							: criteriaBuilder.desc(sortPath)
					);
				}
			}

			Order order = criteriaBuilder.asc(root.get(getIdAttribute()));

			if (last != null) {
				order = criteriaBuilder.desc(root.get(getIdAttribute()));
			}

			if (order != null) {
				orders.add(order);
			}

			criteriaBuilderQuery.orderBy(orders);

			Mutiny.Query<T> query = s.createQuery(criteriaBuilderQuery);

			if (first != null) {
				if (first < 0) {
					return Uni.createFrom().failure(
						() -> new InvalidPageSizeException(format("The page size must not be negative: 'first'=%s", first)));
				}
				query.setMaxResults(first + 1);
			}
			if (last != null) {
				if (last < 0) {
					return Uni.createFrom().failure(
						() -> new InvalidPageSizeException(format("The page size must not be negative: 'last'=%s", last)));
				}
				query.setMaxResults(last + 1);
			}

			Uni<List<T>> entities = query
				.setCacheable(true)
				.getResultList();

			return entities.map(entitiesList -> {

				List<Edge<T>> edges = RelayUtil.toEdgeList(entitiesList);

				int size = entitiesList.size();

				String startCursor =
					entitiesList.isEmpty()
						? null
						: edges.get(0).getCursor();

				String endCursor = null;

				boolean hasNextPage = false;
				boolean hasPreviousPage = false;

				if (first != null) {

					hasNextPage = size == first + 1;

					if (hasNextPage) {
						endCursor =
							entitiesList.isEmpty()
								? null
								: edges.get(size - 2).getCursor();
					}

				}

				if (last != null) {

					hasPreviousPage = size == last + 1;
					hasNextPage = false;

					if (hasPreviousPage) {
						endCursor =
							entitiesList.isEmpty()
								? null
								: edges.get(size - 2).getCursor();
					}
				}

				if ((hasNextPage || hasPreviousPage) && !edges.isEmpty()) {
					edges.remove(size - 1);
				}

				PageInfo pageInfo = new DefaultPageInfo(
					startCursor, endCursor, hasPreviousPage, hasNextPage);

				return new DefaultConnection<>(edges, pageInfo);

			});

		});

	}

	public Uni<Connection<ENTITY>> findConnection(
		String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList) {

		CriteriaBuilder criteriaBuilder = getCriteriaBuilder();

		CriteriaQuery<ENTITY> query =
			criteriaBuilder.createQuery(getEntityClass());

		Path<ENTITY> root = query.from(getEntityClass());

		return findConnection(
			query, root, criteriaBuilder.conjunction(), getSearchFields(),
			after, before, first, last, searchText, sortByList);

	}

	private Predicate _addSearchCondition(
		CriteriaBuilder criteriaBuilder, Predicate searchConditions,
		Path<?> searchPath, String searchText) {

		Class<?> javaType = searchPath.getJavaType();

		if (javaType == String.class) {
			searchConditions = criteriaBuilder.or(
				searchConditions, criteriaBuilder.like(
					criteriaBuilder.lower(searchPath.as(String.class)),
					"%" + searchText.toLowerCase() + "%")
			);
		}
		else if (StringUtils.isNumeric(searchText)) {

			Number number;

			if (ClassUtils.isAssignable(javaType, Integer.class)) {
				number = Integer.parseInt(searchText);
			}
			else if (ClassUtils.isAssignable(javaType, Long.class)) {
				number = Long.parseLong(searchText);
			}
			else if (ClassUtils.isAssignable(javaType, Float.class)) {
				number = Float.parseFloat(searchText);
			}
			else if (ClassUtils.isAssignable(javaType, Double.class)) {
				number = Double.parseDouble(searchText);
			}
			else {
				number = new BigDecimal(searchText);
			}

			searchConditions = criteriaBuilder.or(
				searchConditions, criteriaBuilder.equal(
					searchPath, number)
			);

		}
		else if (javaType == Boolean.class) {
			searchConditions = criteriaBuilder.or(
				searchConditions, criteriaBuilder.equal(
					searchPath, Boolean.valueOf(searchText))
			);
		}
		else if (javaType == UUID.class) {

			try {
				searchConditions = criteriaBuilder.or(
					searchConditions, criteriaBuilder.equal(
						searchPath,
						UUID.fromString(searchText)
					)
				);
			}
			catch (IllegalArgumentException e) {
				// ignore
			}

		}
		else {
			searchConditions = criteriaBuilder.or(
				searchConditions, criteriaBuilder.like(
					criteriaBuilder.lower(searchPath.as(String.class)),
					"%" + searchText.toLowerCase() + "%")
			);
		}

		return searchConditions;
	}

	protected abstract Class<ENTITY> getEntityClass();

	protected abstract String[] getSearchFields();

	protected abstract CriteriaBuilder getCriteriaBuilder();

	protected abstract <T> SingularAttribute<T, Long> getIdAttribute();

	protected abstract <T> Uni<T> withTransaction(
		BiFunction<Mutiny.Session, Mutiny.Transaction, Uni<T>> function);

	private static final Logger LOGGER = Logger.getLogger(GraphQLService.class);

}
