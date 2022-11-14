package io.openk9.tenantmanager.client.service.impl;

import io.openk9.api.tenantmanager.TenantManager;
import io.openk9.tenantmanager.grpc.TenantRequest;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TenantManagerRemote implements TenantManager {
	@Override
	public Uni<Tenant> getTenantByVirtualHost(String virtualHost) {
		return tenantManager.findTenant(
			TenantRequest.newBuilder()
				.setVirtualHost(virtualHost)
				.build())
			.map(response -> new Tenant(
				response.getVirtualHost(),
				response.getSchemaName(),
				response.getClientId(),
				response.getClientSecret(),
				response.getRealmName())
			);
	}

	@GrpcClient("tenantmanager")
	io.openk9.tenantmanager.grpc.TenantManager tenantManager;

}
