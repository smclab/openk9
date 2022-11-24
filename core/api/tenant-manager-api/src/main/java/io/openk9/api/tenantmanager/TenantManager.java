package io.openk9.api.tenantmanager;

import io.smallrye.mutiny.Uni;

public interface TenantManager {

	Uni<Tenant> getTenantByVirtualHost(String virtualHost);

	record Tenant(
		String virtualHost, String schemaName, String clientId,
		String clientSecret, String realmName) {}

}
