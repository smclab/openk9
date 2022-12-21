package io.openk9.datasource.searcher.parser.impl;

import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.datasource.searcher.util.JWT;
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

		JWT jwt = parserContext.getJwt();

		List<AclMapping> aclMappings =
			parserContext
				.getCurrentTenant()
				.getDatasources()
				.stream()
				.flatMap(d -> d.getPluginDriver().getAclMappings().stream())
				.distinct()
				.toList();

		for (AclMapping aclMapping : aclMappings) {

			DocTypeField docTypeField = aclMapping.getDocTypeField();

			aclMapping.getUserField().apply(docTypeField, jwt, innerQuery);

		}

		parserContext.getMutableQuery().filter(innerQuery);

	}

	@Override
	public boolean isQueryParserGroup() {
		return false;
	}
}