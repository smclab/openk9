package io.openk9.entity.manager.cleaner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public interface EntityNameCleaner {

	String getEntityType();

	QueryBuilder cleanEntityName(long tenantId, String entityName);

	default String cleanEntityName(String entityName) {
		return entityName.strip();
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	class DefaultEntityNameCleaner implements EntityNameCleaner {

		private String entityType;

		@Override
		public QueryBuilder cleanEntityName(long tenantId, String entityName) {
			return createQueryBuilder(cleanEntityName(entityName));
		}

		protected QueryBuilder createQueryBuilder(String entityName) {

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

			boolQueryBuilder.must(
				QueryBuilders.matchQuery("name.keyword", entityName)
			);

			boolQueryBuilder.must(
				QueryBuilders.matchQuery("type.keyword", getEntityType())
			);

			return boolQueryBuilder;

		}

	}

}