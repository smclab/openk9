package io.openk9.tenantmanager.pipe.tenant.create.message;

import io.openk9.tenantmanager.actor.TypedActor;

public sealed interface SchemaMessage {

	record Start(
		TypedActor.Address<TenantMessage> tenant,
		String virtualHost,
		String schemaName
	) implements SchemaMessage {}

	record Rollback() implements SchemaMessage {}
	record Stop() implements SchemaMessage {}

}
