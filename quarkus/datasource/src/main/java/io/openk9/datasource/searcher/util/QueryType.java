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
