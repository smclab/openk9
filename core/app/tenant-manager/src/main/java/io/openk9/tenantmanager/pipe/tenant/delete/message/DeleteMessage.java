package io.openk9.tenantmanager.pipe.tenant.delete.message;

import io.openk9.tenantmanager.actor.TypedActor;

public sealed interface DeleteMessage {

	record Start(
		TypedActor.Address<DeleteGroupMessage> deleteGroupActor, String virtualHost)
		implements DeleteMessage {}

	record Delete(String token) implements DeleteMessage {}

	record Stop() implements DeleteMessage {}

	record Finished() implements DeleteMessage { }
}
