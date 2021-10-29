package io.openk9.entity.manager.internal;

import io.openk9.entity.manager.api.Constants;
import io.openk9.entity.manager.api.EntityNameCleaner;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

@Component(
	immediate = true,
	service = EntityNameCleaner.class
)
public class LocationEntityNameCleaner extends DefaultEntityNameCleaner {

	@Override
	public String getEntityType() {
		return "loc";
	}

	@Override
	protected QueryBuilder createQueryBuilder(String entityName) {

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		boolQueryBuilder.must(
			QueryBuilders.matchQuery(Constants.ENTITY_NAME_KEYWORD_FIELD, entityName)
		);

		boolQueryBuilder.must(
			QueryBuilders.matchQuery(Constants.ENTITY_TYPE_FIELD, getEntityType())
		);

		return boolQueryBuilder;

	}

}
