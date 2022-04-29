package io.openk9.auth.api;

import org.elasticsearch.index.query.BoolQueryBuilder;

import java.util.Collection;

public interface ACLQueryContributorRegistry {

	void contribute(
		String driverServiceName, UserInfo userInfo,
		BoolQueryBuilder booleanQuery);

	void contribute(
		Collection<String> driverServiceName,
		UserInfo userInfo,
		BoolQueryBuilder booleanQuery);

}
