package io.openk9.tenantmanager.pipe.tenant.create.message;

import akka.actor.typed.ActorRef;

public sealed interface SchemaMessage {

	record Start(
		ActorRef<TenantMessage> tenant,
		String virtualHost,
		String schemaName
	) implements SchemaMessage {}

	record ProcessCreatedId(
		Long processId, String virtualHost, String schemaName,
		ActorRef<TenantMessage> tenant
	) implements SchemaMessage {}
	enum Rollback implements SchemaMessage {INSTANCE}
	enum Stop implements SchemaMessage {INSTANCE}

}
