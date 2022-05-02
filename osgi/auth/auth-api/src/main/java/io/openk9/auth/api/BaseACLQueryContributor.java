package io.openk9.auth.api;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.List;
import java.util.Map;

public abstract class BaseACLQueryContributor implements ACLQueryContributor {

	public abstract String getPluginName();

	@Override
	public void accept(UserInfo userInfo, BoolQueryBuilder boolQueryBuilder) {

		Map<String, List<String>> realmAccess = userInfo.getRealmAccess();

		List<String> roles = realmAccess.getOrDefault("roles", List.of());

		if (!roles.isEmpty()) {

			String fieldName = "acl." + getPluginName() + ".rolesName.keyword";

			boolQueryBuilder.should(QueryBuilders.termsQuery(fieldName, roles));

		}

	}

}