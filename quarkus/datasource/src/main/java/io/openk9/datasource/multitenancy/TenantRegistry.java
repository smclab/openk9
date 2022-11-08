package io.openk9.datasource.multitenancy;

import io.openk9.tenantmanager.grpc.TenantManager;
import io.openk9.tenantmanager.grpc.TenantRequest;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CaffeineCache;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;

@ApplicationScoped
@ActivateRequestContext
public class TenantRegistry {

	public Uni<Tenant> getTenantNullable(String name) {
		return _getTenantFromCache(name);
	}

	private Uni<Tenant> _getTenantFromCache(String name) {

		CaffeineCache caffeineCache = cache.as(CaffeineCache.class);

		return caffeineCache.get(
			name, k ->
				tenantManager.findTenant(
					TenantRequest
						.newBuilder()
						.setVirtualHost(name)
						.build()
				)
					.map(tenantResponse -> new Tenant(
						tenantResponse.getVirtualHost(),
						tenantResponse.getSchemaName(),
						tenantResponse.getClientId(),
						tenantResponse.getClientSecret(),
						tenantResponse.getRealmName()
					))
					.await()
					.indefinitely()
		);
	}

	@GrpcClient("tenantmanager")
	TenantManager tenantManager;

	@CacheName("tenant")
	Cache cache;

	public record Tenant(
		String virtualHost, String schemaName, String clientId,
		String clientSecret, String realmName) {}

}