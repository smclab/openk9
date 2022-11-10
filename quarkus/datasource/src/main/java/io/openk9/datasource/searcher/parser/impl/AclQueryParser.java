package io.openk9.datasource.searcher.parser.impl;

import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.quarkus.security.identity.SecurityIdentity;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.Set;

@RequestScoped
public class AclQueryParser implements QueryParser {

	@Override
	public String getType() {
		return "ACL";
	}

	@Override
	public void accept(ParserContext parserContext) {

		BoolQueryBuilder mutableQuery = parserContext.getMutableQuery();

		Set<String> roles = identity.getRoles();

		if (roles != null && !roles.isEmpty()) {

			String fieldName = "acl.rolesName.keyword";

			mutableQuery.should(QueryBuilders.termsQuery(fieldName, roles));

		}

	}

	@Inject
	SecurityIdentity identity;

}