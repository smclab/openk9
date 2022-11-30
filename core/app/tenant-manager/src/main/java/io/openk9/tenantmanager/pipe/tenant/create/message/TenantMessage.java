package io.openk9.tenantmanager.pipe.tenant.create.message;

import io.openk9.tenantmanager.actor.TypedActor;

public sealed interface TenantMessage {
	record RealmCreated(String realmName, String clientId, String clientSecret) implements
		TenantMessage {}
	record SchemaCreated(String schemaName) implements
		TenantMessage {}

	record Start(
		TypedActor.Address<KeycloakMessage> keycloak,
		TypedActor.Address<SchemaMessage> schema,
		String realmName) implements TenantMessage {}
	record Stop() implements TenantMessage {}
	record Error(Throwable exception) implements TenantMessage {}
}
