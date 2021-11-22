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

	protected QueryBuilder createQueryBuilder(String entityName) {

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		String[] tokens = entityName.split("\\s+");

		for (String token : tokens) {

			if (token.contains(".")) {
				String replace = token.replace(".", "");
				boolQueryBuilder.must(
					QueryBuilders.prefixQuery("name", replace)
				);
			}
			else {

				boolQueryBuilder.must(
					QueryBuilders.matchQuery("name", token)
				);

			}

		}

		boolQueryBuilder.filter(
			QueryBuilders.matchQuery("type.keyword", getEntityType())
		);

		return boolQueryBuilder;

	}

}
