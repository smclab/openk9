package io.openk9.tenantmanager.pipe.tenant.create.message;

import io.openk9.tenantmanager.actor.TypedActor;

public sealed interface KeycloakMessage {

	record Start(
		TypedActor.Address<TenantMessage> tenant,
		String virtualHost,
		String realmName) implements KeycloakMessage {}

	record Rollback() implements KeycloakMessage {}
	record Stop() implements KeycloakMessage {}

}
