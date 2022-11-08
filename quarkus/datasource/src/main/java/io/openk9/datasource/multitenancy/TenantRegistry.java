package io.openk9.datasource.multitenancy;

import io.openk9.tenantmanager.grpc.TenantManager;
import io.openk9.tenantmanager.grpc.TenantRequest;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TenantRegistry {

	public Uni<Tenant> getTenantByVirtualHost(String virtualHost) {
		return tenantManager.findTenant(
				TenantRequest
					.newBuilder()
					.setVirtualHost(virtualHost)
					.build()
			)
			.map(tenantResponse -> new Tenant(
				tenantResponse.getVirtualHost(),
				tenantResponse.getSchemaName(),
				tenantResponse.getClientId(),
				tenantResponse.getClientSecret(),
				tenantResponse.getRealmName()
			));
	}

	@GrpcClient("tenantmanager")
	TenantManager tenantManager;

	public record Tenant(
		String virtualHost, String schemaName, String clientId,
		String clientSecret, String realmName) {}

}