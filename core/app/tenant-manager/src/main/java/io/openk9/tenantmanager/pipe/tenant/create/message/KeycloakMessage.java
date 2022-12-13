package io.openk9.tenantmanager.pipe.tenant.create.message;

import akka.actor.typed.ActorRef;

public sealed interface KeycloakMessage {

	record Start(
		ActorRef<TenantMessage> tenant,
		String virtualHost,
		String realmName) implements KeycloakMessage {}

	record ProcessCreatedId(
		Long processId, String virtualHost, String realmName,
		ActorRef<TenantMessage> tenant) implements KeycloakMessage {}

	enum Rollback implements KeycloakMessage {INSTANCE}
	enum Stop implements KeycloakMessage {INSTANCE}

}
