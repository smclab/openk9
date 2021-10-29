package io.openk9.entity.manager.internal;

import io.openk9.entity.manager.api.Constants;
import io.openk9.entity.manager.api.EntityNameCleaner;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
	service = EntityNameCleaner.class
)
public class PersonEntityNameCleaner extends DefaultEntityNameCleaner {

	@Override
	public String getEntityType() {
		return "person";
	}

	protected QueryBuilder createQueryBuilder(String entityName) {

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		boolQueryBuilder.must(
			QueryBuilders.matchQuery(Constants.ENTITY_NAME_FIELD, entityName)
				.operator(Operator.AND)
		);

		boolQueryBuilder.must(
			QueryBuilders.matchQuery(Constants.ENTITY_TYPE_FIELD, getEntityType())
		);

		return boolQueryBuilder;

	}

}
