package io.openk9.entity.manager.cleaner;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class LocationEntityNameCleaner extends DefaultEntityNameCleaner {

	@Override
	public String getEntityType() {
		return "loc";
	}

	protected QueryBuilder createQueryBuilder(String entityName) {

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		String[] tokens = entityName.split("\\s+");

		for (String token : tokens) {

			boolQueryBuilder.must(
					QueryBuilders.matchQuery("name", token)
			);

		}

		boolQueryBuilder.filter(
				QueryBuilders.matchQuery("type.keyword", getEntityType())
		);

		return boolQueryBuilder;

	}

}
