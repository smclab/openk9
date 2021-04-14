package io.openk9.entity.manager.api;

import org.elasticsearch.action.search.SearchRequest;

public interface EntityNameCleaner {

	String getEntityType();

	SearchRequest cleanEntityName(long tenantId, String entityName);

	default String cleanEntityName(String entityName) {
		return entityName.trim();
	}

}
