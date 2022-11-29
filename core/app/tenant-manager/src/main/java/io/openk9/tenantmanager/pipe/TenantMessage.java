package io.openk9.tenantmanager.pipe;

import io.openk9.tenantmanager.actor.TypedActor;

public sealed interface TenantMessage {
	record CreateRealm(TypedActor.Address<TenantMessage> next, String virtualHost, String realmName) implements TenantMessage {}
	record RealmCreated(String realmName, String clientId, String clientSecret) implements TenantMessage {}
	record CreateSchema(
		TypedActor.Address<TenantMessage> next,
		String virtualHost,
		String schemaName
	) implements TenantMessage {}
	record SchemaCreated(String schemaName) implements TenantMessage {}

	record Start(
		TypedActor.Address<TenantMessage> keycloak,
		TypedActor.Address<TenantMessage> schema,
		String realmName) implements TenantMessage {}
	record Finished() implements TenantMessage {}
	record Error(Throwable exception) implements TenantMessage {}
	record SimpleError() implements TenantMessage {}

}
