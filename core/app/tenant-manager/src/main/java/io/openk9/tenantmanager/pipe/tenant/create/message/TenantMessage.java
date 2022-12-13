package io.openk9.tenantmanager.pipe.tenant.create.message;

public sealed interface TenantMessage {
	record RealmCreated(String realmName, String clientId, String clientSecret) implements
		TenantMessage {}
	record SchemaCreated(String schemaName) implements
		TenantMessage {}

	record Start(String realmName) implements TenantMessage {}
	record ProcessCreatedId(long processId, String realmName) implements TenantMessage {}
	enum Stop implements TenantMessage {INSTANCE}
	record Error(Throwable exception) implements TenantMessage {}
}
