package io.openk9.entity.manager.internal;

import io.openk9.entity.manager.api.Constants;
import io.openk9.entity.manager.api.EntityNameCleaner;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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
	public SearchRequest cleanEntityName(long tenantId, String entityName) {

		SearchRequest searchRequest = new SearchRequest(tenantId + Constants.ENTITY_INDEX_SUFFIX);

		SearchSourceBuilder builder = new SearchSourceBuilder();

		builder.query(createQueryBuilder(cleanEntityName(entityName)));

		return searchRequest.source(builder);

	}

	protected QueryBuilder createQueryBuilder(String entityName) {
		return QueryBuilders.matchQuery(Constants.ENTITY_NAME_FIELD, entityName);
	}

}
