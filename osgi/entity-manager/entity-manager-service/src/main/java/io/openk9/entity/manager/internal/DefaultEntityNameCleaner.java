package io.openk9.entity.manager.internal;

import io.openk9.entity.manager.api.Constants;
import io.openk9.entity.manager.api.EntityNameCleaner;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
	service = EntityNameCleaner.class
)
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
		return QueryBuilders.matchQuery(Constants.ENTITY_NAME_FIELD, entityName);
	}

}
