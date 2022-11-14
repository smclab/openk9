package io.openk9.datasource.searcher.parser.impl;

import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class AclQueryParser implements QueryParser {

	@Override
	public String getType() {
		return "ACL";
	}

	@Override
	public void accept(ParserContext parserContext) {

		BoolQueryBuilder innerQuery =
			QueryBuilders
				.boolQuery()
				.minimumShouldMatch(1)
				.should(QueryBuilders.matchQuery("acl.public", true));

		List<String> acl = parserContext.getAcl();

		if (acl != null && !acl.isEmpty()) {

			String fieldName = "acl.rolesName.keyword";

			innerQuery.should(QueryBuilders.termsQuery(fieldName, acl));
		}

		parserContext.getMutableQuery().filter(innerQuery);

	}

	@Override
	public boolean isQueryParserGroup() {
		return false;
	}
}