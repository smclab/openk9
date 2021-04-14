package io.openk9.entity.manager.internal;

import io.openk9.entity.manager.api.Constants;
import io.openk9.entity.manager.api.EntityNameCleaner;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

@Component(
	immediate = true,
	service = EntityNameCleaner.class
)
public class PersonEntityNameCleaner extends DefaultEntityNameCleaner {

	@Override
	public String getEntityType() {
		return "person";
	}

	@Override
	protected QueryBuilder createQueryBuilder(
		String entityName) {

		return QueryBuilders.boolQuery()
			.must(QueryBuilders.matchQuery(Constants.ENTITY_NAME_FIELD, entityName).operator(Operator.AND))
			.must(QueryBuilders.matchQuery(Constants.ENTITY_TYPE_FIELD, getEntityType()));
	}
}
