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
import java.util.List;

public interface CriteriaDefinition {

	static CriteriaDefinition empty() {
		return Criteria.EMPTY;
	}

	static CriteriaDefinition from(CriteriaDefinition... criteria) {

		return from(Arrays.asList(criteria));
	}

	static CriteriaDefinition from(List<? extends CriteriaDefinition> criteria) {

		if (criteria.isEmpty()) {
			return Criteria.EMPTY;
		}

		if (criteria.size() == 1) {
			return criteria.get(0);
		}

		return Criteria.EMPTY.and(criteria);
	}

	boolean isGroup();

	List<CriteriaDefinition> getGroup();

	SqlIdentifier getColumn();

	CriteriaDefinition.Comparator getComparator();

	Object getValue();

	boolean isIgnoreCase();

	CriteriaDefinition getPrevious();

	boolean hasPrevious();

	boolean isEmpty();

	CriteriaDefinition.Combinator getCombinator();

	enum Combinator {
		INITIAL, AND, OR;
	}

	enum Comparator {
		INITIAL(""), EQ("="), NEQ("!="), BETWEEN("BETWEEN"), NOT_BETWEEN("NOT BETWEEN"), LT("<"), LTE("<="), GT(">"), GTE(
			">="), IS_NULL("IS NULL"), IS_NOT_NULL("IS NOT NULL"), LIKE(
			"LIKE"), NOT_LIKE("NOT LIKE"), NOT_IN("NOT IN"), IN("IN"), IS_TRUE("IS TRUE"), IS_FALSE("IS FALSE");

		private final String comparator;

		Comparator(String comparator) {
			this.comparator = comparator;
		}

		public String getComparator() {
			return comparator;
		}
	}
}
