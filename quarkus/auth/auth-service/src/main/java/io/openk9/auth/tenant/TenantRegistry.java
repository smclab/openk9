package io.openk9.auth.tenant;

import io.openk9.api.tenantmanager.TenantManager;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class TenantRegistry {

	public Uni<TenantManager.Tenant> getTenantByVirtualHost(String virtualHost) {
		return tenantManager.get().getTenantByVirtualHost(virtualHost);
	}

	@Inject
	Instance<TenantManager> tenantManager;

}