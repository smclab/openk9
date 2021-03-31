package io.openk9.entity.manager.api;

import org.neo4j.cypherdsl.core.Statement;

public interface EntityNameCleaner {

	String getEntityType();

	Statement cleanEntityName(long tenantId, String entityName);

}
