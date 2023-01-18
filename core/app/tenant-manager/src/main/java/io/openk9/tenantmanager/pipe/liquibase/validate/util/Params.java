package io.openk9.tenantmanager.pipe.liquibase.validate.util;

public record Params(
	String schemaName, String liquibaseSchemaName,
	String liquibaseChangeLog, String changeLogLockTableName,
	String changeLogTableName, String jdbcUrl, String username,
	String password) { }
