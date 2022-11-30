package io.openk9.tenantmanager.pipe.tenant.delete.message;

import io.openk9.tenantmanager.actor.TypedActor;

import java.time.Duration;

public sealed interface TimeoutMessage {

	record Start(TypedActor.Address<DeleteMessage> deleteActor, Duration duration) implements TimeoutMessage {}
	record Wait() implements TimeoutMessage {}
	record Stop() implements TimeoutMessage {}

}
