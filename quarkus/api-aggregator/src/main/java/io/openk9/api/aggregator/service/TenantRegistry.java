package io.openk9.api.aggregator.service;

import io.openk9.api.aggregator.model.Tenant;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.Blocking;

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

		List<Tenant> listTenant =
			Tenant
				.<Tenant>list("active = true")
				.await()
				.indefinitely();

		Map<String, Tenant> map = new HashMap<>(listTenant.size());

		for (Tenant tenant : listTenant) {
			map.put(tenant.getName(), tenant);
		}

		_tenantMap.clear();

		_tenantMap.putAll(map);

	}

	private final Map<String, Tenant> _tenantMap =
		new ConcurrentHashMap<>();

}
