package io.openk9.datasource.searcher.parser.impl;

import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.UserField;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.datasource.searcher.util.JWT;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import javax.enterprise.context.ApplicationScoped;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AclQueryParser implements QueryParser {

	@ConfigProperty(name = "openk9.datasource.acl.query.extra.params.key", defaultValue = "OPENK9_ACL")
	String extraParamsKey;
	@ConfigProperty(name = "openk9.datasource.acl.query.extra.params.enabled", defaultValue = "false")
	boolean extraParamsEnabled;

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

		if (!jwt.isEmpty()) {

			Iterator<AclMapping> iterator =
				parserContext
					.getCurrentTenant()
					.getDatasources()
					.stream()
					.flatMap(d -> d.getPluginDriver().getAclMappings().stream())
					.distinct()
					.iterator();

			while (iterator.hasNext()) {
				AclMapping aclMapping = iterator.next();

				DocTypeField docTypeField = aclMapping.getDocTypeField();

				UserField userField = aclMapping.getUserField();

				userField.apply(docTypeField, jwt, innerQuery);

				if (extraParamsEnabled && userField == UserField.ROLES) {

					Map<String, List<String>> extraParams = parserContext.getExtraParams();

					if (extraParams != null && !extraParams.isEmpty()) {
						List<String> roles = extraParams.get(extraParamsKey);
						if (roles != null && !roles.isEmpty()) {
							UserField.apply(docTypeField, roles, innerQuery);
						}
					}

				}

			}

		}

		parserContext.getMutableQuery().filter(innerQuery);

	}

	@Override
	public boolean isQueryParserGroup() {
		return false;
	}

}