package io.openk9.entity.manager.cleaner;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class OrganizationEntityNameCleaner extends DefaultEntityNameCleaner {

	@Inject
	@ConfigProperty(
		name = "openk9.entity.cleaner.stop-words",
		defaultValue = "spa,s.p.a.,srl,s.r.l.,s.r.l,s.p.a"
	)
	String[] _stopWords;

	@Override
	public String getEntityType() {
		return "organization";
	}

	@Override
	public QueryBuilder cleanEntityName(long tenantId, String entityName) {
		return super.cleanEntityName(tenantId, entityName);
	}

	@Override
	public String cleanEntityName(String entityName) {

		for (String stopWord : _stopWords) {
			entityName = entityName.replaceAll(stopWord, "");
		}

		return super.cleanEntityName(entityName);
	}

	@Override
	protected QueryBuilder createQueryBuilder(String entityName) {

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		boolQueryBuilder.must(
			QueryBuilders.matchQuery("name", entityName)
		);

		boolQueryBuilder.must(
			QueryBuilders.matchQuery("type.keyword", getEntityType())
		);

		return boolQueryBuilder;

	}

}
