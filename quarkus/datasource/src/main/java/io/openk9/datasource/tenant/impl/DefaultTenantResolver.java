package io.openk9.datasource.tenant.impl;

import io.openk9.datasource.tenant.TenantResolver;
import io.vertx.core.Vertx;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultTenantResolver implements TenantResolver {

	public long getTenantId() {
		return getTenantName().hashCode();
	}

	@Override
	public String getTenantName() {
		return Vertx.currentContext().getLocal(TENANT_ID);

	}

	@Override
	public void setTenant(String name) {
		Vertx.currentContext().putLocal(TENANT_ID, name);
	}

	public static final String TENANT_ID = "tenantId";

}
