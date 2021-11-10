package io.openk9.entity.manager.cleaner;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class DefaultEntityNameCleaner implements EntityNameCleaner {

	@Override
	public String getEntityType() {
		return "default";
	}

	@Override
	public QueryBuilder cleanEntityName(long tenantId, String entityName) {
		return createQueryBuilder(cleanEntityName(entityName));

	}

	protected QueryBuilder createQueryBuilder(String entityName) {
		return QueryBuilders.matchQuery("name", entityName);
	}

}