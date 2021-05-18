package io.openk9.entity.manager.api;

import java.util.Map;

public interface EntityNameCleaner {

	String getEntityType();

	Map<String, Object> cleanEntityName(long tenantId, String entityName);

	default String cleanEntityName(String entityName) {
		return entityName.trim();
	}

}
