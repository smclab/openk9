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

package io.openk9.datasource.searcher.util;

import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;

public enum QueryType {
	MUST {
		@Override
		public void useConfiguredQueryType(
			BoolQueryBuilder mutableQuery, QueryBuilder queryBuilder) {

			mutableQuery.must(queryBuilder);
		}
	},
	SHOULD {
		@Override
		public void useConfiguredQueryType(
			BoolQueryBuilder mutableQuery, QueryBuilder queryBuilder) {

			mutableQuery.should(queryBuilder);
		}
	},
	MIN_SHOULD_1 {
		@Override
		public void useConfiguredQueryType(
			BoolQueryBuilder mutableQuery, QueryBuilder queryBuilder) {

			mutableQuery.should(queryBuilder).minimumShouldMatch(1);
		}
	},
	MIN_SHOULD_2 {
		@Override
		public void useConfiguredQueryType(
			BoolQueryBuilder mutableQuery, QueryBuilder queryBuilder) {

			mutableQuery.should(queryBuilder).minimumShouldMatch(2);
		}
	},
	MIN_SHOULD_3 {
		@Override
		public void useConfiguredQueryType(
			BoolQueryBuilder mutableQuery, QueryBuilder queryBuilder) {

			mutableQuery.should(queryBuilder).minimumShouldMatch(3);
		}
	},
	MUST_NOT {
		@Override
		public void useConfiguredQueryType(
			BoolQueryBuilder mutableQuery, QueryBuilder queryBuilder) {

			mutableQuery.mustNot(queryBuilder);
		}
	},
	FILTER {
		@Override
		public void useConfiguredQueryType(
			BoolQueryBuilder mutableQuery, QueryBuilder queryBuilder) {

			mutableQuery.filter(queryBuilder);
		}
	};

	public abstract void useConfiguredQueryType(
		BoolQueryBuilder boolQueryBuilder, QueryBuilder query);

}
