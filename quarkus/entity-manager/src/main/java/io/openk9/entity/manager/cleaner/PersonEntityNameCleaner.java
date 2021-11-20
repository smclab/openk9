package io.openk9.entity.manager.cleaner;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class PersonEntityNameCleaner extends DefaultEntityNameCleaner {

	@Override
	public String getEntityType() {
		return "person";
	}

	@Override
	public QueryBuilder cleanEntityName(long tenantId, String entityName) {
		return super.cleanEntityName(tenantId, entityName);
	}

	@Override
	public String cleanEntityName(String entityName) {

		String[] nameParts = entityName.split("\\s+");

		for (String namePart : nameParts) {
			if (namePart.contains(".")) {
				entityName = entityName.replaceAll(namePart, "");
			}
		}

		return super.cleanEntityName(entityName);
	}

	protected QueryBuilder createQueryBuilder(String entityName) {

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		boolQueryBuilder.must(
			QueryBuilders.matchQuery("name", entityName)
				.operator(Operator.AND)
		);

		boolQueryBuilder.must(
			QueryBuilders.matchQuery("type.keyword", getEntityType())
		);

		return boolQueryBuilder;

	}

}
