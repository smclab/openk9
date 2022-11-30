package io.openk9.tenantmanager.pipe.tenant.delete.message;

public sealed interface DeleteGroupMessage {

	record addDeleteRequest(String virtualHost) implements DeleteGroupMessage {}

	record TellDelete(String virtualHost, String token) implements DeleteGroupMessage {}

	record RemoveDeleteRequest(String virtualHost) implements DeleteGroupMessage {}

}
