package io.openk9.datasource.searcher.util;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;

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
