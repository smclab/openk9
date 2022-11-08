package io.openk9.datasource.multitenancy;

import com.google.protobuf.Empty;
import io.openk9.tenantmanager.grpc.TenantListResponse;
import io.openk9.tenantmanager.grpc.TenantManager;
import io.openk9.tenantmanager.grpc.TenantResponse;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@ActivateRequestContext
public class TenantRegistry {

	public Optional<Tenant> getTenant(String name) {
		return Optional.ofNullable(getTenantNullable(name));
	}

	public Tenant getTenantNullable(String name) {
		return _tenantMap.get(name);
	}

	public Collection<Tenant> getTenantList() {
		return _tenantMap.values();
	}

	@Scheduled(every="30s")
	@Blocking
	void initializeTenantMap() {

		Uni<TenantListResponse> tenantList =
			tenantManager.findTenantList(Empty.newBuilder().build());

		TenantListResponse tenantListResponse = tenantList.await().indefinitely();

		List<TenantResponse> tenantResponseList =
			tenantListResponse.getTenantResponseList();

		Map<String, Tenant> map = new HashMap<>(tenantResponseList.size());

		for (TenantResponse tenantResponse : tenantResponseList) {

			Tenant tenant = new Tenant(
				tenantResponse.getVirtualHost(),
				tenantResponse.getSchemaName(),
				tenantResponse.getClientId(),
				tenantResponse.getClientSecret(),
				tenantResponse.getRealmName()
			);

			map.put(tenantResponse.getVirtualHost(), tenant);
		}

		_tenantMap.clear();

		_tenantMap.putAll(map);

	}

	private final Map<String, Tenant> _tenantMap =
		new ConcurrentHashMap<>();

	@GrpcClient("tenantmanager")
	TenantManager tenantManager;

	public record Tenant(
		String virtualHost, String schemaName, String clientId,
		String clientSecret, String realmName) {}

}