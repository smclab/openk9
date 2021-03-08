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

package io.openk9.sql.api.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class Criteria implements CriteriaDefinition {

	static final Criteria EMPTY = new Criteria(
		SqlIdentifier.EMPTY, Comparator.INITIAL, null);

	private final Criteria previous;
	private final Combinator combinator;
	private final List<CriteriaDefinition> group;

	private final SqlIdentifier column;
	private final Comparator comparator;
	private final Object value;
	private final boolean ignoreCase;

	private Criteria(SqlIdentifier column, Comparator comparator, Object value) {
		this(null, Combinator.INITIAL, Collections.emptyList(), column, comparator, value, false);
	}

	private Criteria( Criteria previous, Combinator combinator, List<CriteriaDefinition> group, SqlIdentifier column, Comparator comparator, Object value) {
		this(previous, combinator, group, column, comparator, value, false);
	}

	private Criteria(Criteria previous, Combinator combinator, List<CriteriaDefinition> group, SqlIdentifier column, Comparator comparator, Object value, boolean ignoreCase) {

		this.previous = previous;
		this.combinator = previous != null && previous.isEmpty() ? Combinator.INITIAL : combinator;
		this.group = group;
		this.column = column;
		this.comparator = comparator;
		this.value = value;
		this.ignoreCase = ignoreCase;
	}

	private Criteria(Criteria previous, Combinator combinator, List<CriteriaDefinition> group) {

		this.previous = previous;
		this.combinator = previous != null && previous.isEmpty() ? Combinator.INITIAL : combinator;
		this.group = group;
		this.column = null;
		this.comparator = null;
		this.value = null;
		this.ignoreCase = false;
	}

	public static Criteria empty() {
		return EMPTY;
	}

	public static Criteria from(Criteria... criteria) {
		return from(Arrays.asList(criteria));
	}

	public static Criteria from(List<Criteria> criteria) {

		if (criteria.isEmpty()) {
			return EMPTY;
		}

		if (criteria.size() == 1) {
			return criteria.get(0);
		}

		return EMPTY.and(criteria);
	}

	public static Criteria.CriteriaStep where(String column) {

		return new Criteria.DefaultCriteriaStep(SqlIdentifier.unquoted(column));
	}

	public Criteria.CriteriaStep and(String column) {

		SqlIdentifier identifier = SqlIdentifier.unquoted(column);
		return new Criteria.DefaultCriteriaStep(identifier) {
			@Override
			protected Criteria createCriteria(Comparator comparator, Object value) {
				return new Criteria(
					Criteria.this, Combinator.AND, Collections.emptyList(), identifier, comparator, value);
			}
		};
	}

	public Criteria and(CriteriaDefinition criteria) {

		return and(Collections.singletonList(criteria));
	}

	@SuppressWarnings("unchecked")
	public Criteria and(List<? extends CriteriaDefinition> criteria) {
		return new Criteria(
			Criteria.this, Combinator.AND, (List<CriteriaDefinition>) criteria);
	}

	public Criteria.CriteriaStep or(String column) {

		SqlIdentifier identifier = SqlIdentifier.unquoted(column);
		return new Criteria.DefaultCriteriaStep(identifier) {
			@Override
			protected Criteria createCriteria(Comparator comparator, Object value) {
				return new Criteria(
					Criteria.this, Combinator.OR, Collections.emptyList(), identifier, comparator, value);
			}
		};
	}

	public Criteria or(
		CriteriaDefinition criteria) {

		return or(Collections.singletonList(criteria));
	}

	@SuppressWarnings("unchecked")
	public Criteria or(List<? extends CriteriaDefinition> criteria) {

		return new Criteria(
			Criteria.this, Combinator.OR, (List<CriteriaDefinition>) criteria);
	}

	public Criteria ignoreCase(boolean ignoreCase) {
		if (this.ignoreCase != ignoreCase) {
			return new Criteria(previous, combinator, group, column, comparator, value, ignoreCase);
		}
		return this;
	}

	public Criteria getPrevious() {
		return previous;
	}

	public boolean hasPrevious() {
		return previous != null;
	}

	@Override
	public boolean isEmpty() {

		if (!doIsEmpty()) {
			return false;
		}

		Criteria parent = this.previous;

		while (parent != null) {

			if (!parent.doIsEmpty()) {
				return false;
			}

			parent = parent.previous;
		}

		return true;
	}

	private boolean doIsEmpty() {

		if (this.comparator == Comparator.INITIAL) {
			return true;
		}

		if (this.column != null) {
			return false;
		}

		for (CriteriaDefinition criteria : group) {

			if (!criteria.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	public boolean isGroup() {
		return !this.group.isEmpty();
	}

	public Combinator getCombinator() {
		return combinator;
	}

	@Override
	public List<CriteriaDefinition> getGroup() {
		return group;
	}

	public SqlIdentifier getColumn() {
		return column;
	}

	public Comparator getComparator() {
		return comparator;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	@Override
	public String toString() {

		if (isEmpty()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		unroll(this, builder);

		return builder.toString();
	}

	private void unroll(
		CriteriaDefinition criteria, StringBuilder stringBuilder) {

		CriteriaDefinition
			current = criteria;

		// reverse unroll criteria chain
		Map<CriteriaDefinition, CriteriaDefinition>
			forwardChain = new HashMap<>();

		while (current.hasPrevious()) {
			forwardChain.put(current.getPrevious(), current);
			current = current.getPrevious();
		}

		// perform the actual mapping
		render(current, stringBuilder);
		while (forwardChain.containsKey(current)) {

			CriteriaDefinition
				criterion = forwardChain.get(current);

			if (criterion.getCombinator() != Combinator.INITIAL) {
				stringBuilder.append(' ').append(criterion.getCombinator().name()).append(' ');
			}

			render(criterion, stringBuilder);

			current = criterion;
		}
	}

	private void unrollGroup(List<? extends CriteriaDefinition> criteria, StringBuilder stringBuilder) {

		stringBuilder.append("(");

		boolean first = true;
		for (CriteriaDefinition criterion : criteria) {

			if (criterion.isEmpty()) {
				continue;
			}

			if (!first) {
				Combinator combinator = criterion.getCombinator() == Combinator.INITIAL ? Combinator.AND
					: criterion.getCombinator();
				stringBuilder.append(' ').append(combinator.name()).append(' ');
			}

			unroll(criterion, stringBuilder);
			first = false;
		}

		stringBuilder.append(")");
	}

	private void render(CriteriaDefinition criteria, StringBuilder stringBuilder) {

		if (criteria.isEmpty()) {
			return;
		}

		if (criteria.isGroup()) {
			unrollGroup(criteria.getGroup(), stringBuilder);
			return;
		}

		stringBuilder.append(criteria.getColumn().toSql(IdentifierProcessing.NONE)).append(' ')
			.append(criteria.getComparator().getComparator());

		switch (criteria.getComparator()) {
			case BETWEEN:
			case NOT_BETWEEN:
				Pair<Object, Object> pair = (Pair<Object, Object>) criteria.getValue();
				stringBuilder.append(' ').append(pair.getFirst()).append(" AND ").append(pair.getSecond());
				break;

			case IS_NULL:
			case IS_NOT_NULL:
			case IS_TRUE:
			case IS_FALSE:
				break;

			case IN:
			case NOT_IN:
				stringBuilder.append(" (").append(renderValue(criteria.getValue())).append(')');
				break;

			default:
				stringBuilder.append(' ').append(renderValue(criteria.getValue()));
		}
	}

	private static String renderValue( Object value) {

		if (value instanceof Number) {
			return value.toString();
		}

		if (value instanceof Collection) {

			StringJoiner joiner = new StringJoiner(", ");
			((Collection<?>) value).forEach(o -> joiner.add(renderValue(o)));
			return joiner.toString();
		}

		if (value != null) {
			return String.format("'%s'", value);
		}

		return "null";
	}

	/**
	 * Interface declaring terminal builder methods to build a {@link Criteria}.
	 */
	public interface CriteriaStep {

		/**
		 * Creates a {@link Criteria} using equality.
		 *
		 * @param value must not be {@literal null}.
		 */
		Criteria is(Object value);

		/**
		 * Creates a {@link Criteria} using equality (is not).
		 *
		 * @param value must not be {@literal null}.
		 */
		Criteria not(Object value);

		/**
		 * Creates a {@link Criteria} using {@code IN}.
		 *
		 * @param values must not be {@literal null}.
		 */
		Criteria in(Object... values);

		/**
		 * Creates a {@link Criteria} using {@code IN}.
		 *
		 * @param values must not be {@literal null}.
		 */
		Criteria in(Collection<?> values);

		/**
		 * Creates a {@link Criteria} using {@code NOT IN}.
		 *
		 * @param values must not be {@literal null}.
		 */
		Criteria notIn(Object... values);

		/**
		 * Creates a {@link Criteria} using {@code NOT IN}.
		 *
		 * @param values must not be {@literal null}.
		 */
		Criteria notIn(Collection<?> values);

		/**
		 * Creates a {@link Criteria} using between ({@literal BETWEEN begin AND end}).
		 *
		 * @param begin must not be {@literal null}.
		 * @param end must not be {@literal null}.
		 * @since 2.2
		 */
		Criteria between(Object begin, Object end);

		/**
		 * Creates a {@link Criteria} using not between ({@literal NOT BETWEEN begin AND end}).
		 *
		 * @param begin must not be {@literal null}.
		 * @param end must not be {@literal null}.
		 * @since 2.2
		 */
		Criteria notBetween(Object begin, Object end);

		/**
		 * Creates a {@link Criteria} using less-than ({@literal <}).
		 *
		 * @param value must not be {@literal null}.
		 */
		Criteria lessThan(Object value);

		/**
		 * Creates a {@link Criteria} using less-than or equal to ({@literal <=}).
		 *
		 * @param value must not be {@literal null}.
		 */
		Criteria lessThanOrEquals(Object value);

		/**
		 * Creates a {@link Criteria} using greater-than({@literal >}).
		 *
		 * @param value must not be {@literal null}.
		 */
		Criteria greaterThan(Object value);

		/**
		 * Creates a {@link Criteria} using greater-than or equal to ({@literal >=}).
		 *
		 * @param value must not be {@literal null}.
		 */
		Criteria greaterThanOrEquals(Object value);

		/**
		 * Creates a {@link Criteria} using {@code LIKE}.
		 *
		 * @param value must not be {@literal null}.
		 */
		Criteria like(Object value);

		/**
		 * Creates a {@link Criteria} using {@code NOT LIKE}.
		 *
		 * @param value must not be {@literal null}
		 * @return a new {@link Criteria} object
		 */
		Criteria notLike(Object value);

		/**
		 * Creates a {@link Criteria} using {@code IS NULL}.
		 */
		Criteria isNull();

		/**
		 * Creates a {@link Criteria} using {@code IS NOT NULL}.
		 */
		Criteria isNotNull();

		/**
		 * Creates a {@link Criteria} using {@code IS TRUE}.
		 *
		 * @return a new {@link Criteria} object
		 */
		Criteria isTrue();

		/**
		 * Creates a {@link Criteria} using {@code IS FALSE}.
		 *
		 * @return a new {@link Criteria} object
		 */
		Criteria isFalse();
	}

	/**
	 * Default {@link Criteria.CriteriaStep} implementation.
	 */
	static class DefaultCriteriaStep implements
		Criteria.CriteriaStep {

		private final SqlIdentifier property;

		DefaultCriteriaStep(SqlIdentifier property) {
			this.property = property;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.relational.query.Criteria.CriteriaStep#is(java.lang.Object)
		 */
		@Override
		public Criteria is(Object value) {
			return createCriteria(Comparator.EQ, value);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.relational.query.Criteria.CriteriaStep#not(java.lang.Object)
		 */
		@Override
		public Criteria not(Object value) {

			return createCriteria(Comparator.NEQ, value);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.relational.query.Criteria.CriteriaStep#in(java.lang.Object[])
		 */
		@Override
		public Criteria in(Object... values) {

			if (values.length > 1 && values[1] instanceof Collection) {
				throw new RuntimeException(
					"You can only pass in one argument of type " + values[1].getClass().getName());
			}

			return createCriteria(Comparator.IN, Arrays.asList(values));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.relational.query.Criteria.CriteriaStep#in(java.util.Collection)
		 */
		@Override
		public Criteria in(Collection<?> values) {

			return createCriteria(Comparator.IN, values);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.relational.query.Criteria.CriteriaStep#notIn(java.lang.Object[])
		 */
		@Override
		public Criteria notIn(Object... values) {

			if (values.length > 1 && values[1] instanceof Collection) {
				throw new RuntimeException(
					"You can only pass in one argument of type " + values[1].getClass().getName());
			}

			return createCriteria(Comparator.NOT_IN, Arrays.asList(values));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.relational.query.Criteria.CriteriaStep#notIn(java.util.Collection)
		 */
		@Override
		public Criteria notIn(Collection<?> values) {

			return createCriteria(Comparator.NOT_IN, values);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.relational.query.Criteria.CriteriaStep#between(java.lang.Object, java.lang.Object)
		 */
		@Override
		public Criteria between(Object begin, Object end) {

			return createCriteria(Comparator.BETWEEN, Pair.of(begin, end));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.relational.query.Criteria.CriteriaStep#notBetween(java.lang.Object, java.lang.Object)
		 */
		@Override
		public Criteria notBetween(Object begin, Object end) {

			return createCriteria(Comparator.NOT_BETWEEN, Pair.of(begin, end));
		}

		@Override
		public Criteria lessThan(Object value) {

			return createCriteria(Comparator.LT, value);
		}

		@Override
		public Criteria lessThanOrEquals(Object value) {

			return createCriteria(Comparator.LTE, value);
		}

		@Override
		public Criteria greaterThan(Object value) {

			return createCriteria(Comparator.GT, value);
		}

		@Override
		public Criteria greaterThanOrEquals(Object value) {

			return createCriteria(Comparator.GTE, value);
		}

		@Override
		public Criteria like(Object value) {

			return createCriteria(Comparator.LIKE, value);
		}

		@Override
		public Criteria notLike(Object value) {
			return createCriteria(Comparator.NOT_LIKE, value);
		}

		@Override
		public Criteria isNull() {
			return createCriteria(Comparator.IS_NULL, null);
		}

		@Override
		public Criteria isNotNull() {
			return createCriteria(Comparator.IS_NOT_NULL, null);
		}

		@Override
		public Criteria isTrue() {
			return createCriteria(Comparator.IS_TRUE, null);
		}

		@Override
		public Criteria isFalse() {
			return createCriteria(Comparator.IS_FALSE, null);
		}

		protected Criteria createCriteria(Comparator comparator, Object value) {
			return new Criteria(this.property, comparator, value);
		}
	}

}
