package io.openk9.auth.api;

import org.elasticsearch.index.query.BoolQueryBuilder;

import java.util.Collection;

public interface ACLQueryContributorRegistry {

	boolean contribute(
		String driverServiceName, UserInfo userInfo,
		BoolQueryBuilder booleanQuery);

	boolean contribute(
		Collection<String> driverServiceName,
		UserInfo userInfo,
		BoolQueryBuilder booleanQuery);

}
