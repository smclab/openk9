package io.openk9.datasource.tenant.impl;

import io.openk9.datasource.tenant.TenantResolver;
import io.vertx.core.Vertx;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultTenantResolver implements TenantResolver {

	public long getTenantId() {
		return -1;
	}

	@Override
	public String getTenantName() {
		return Vertx.currentContext().get(TENANT_ID);

	}

	@Override
	public void setTenant(String name) {
		Vertx.currentContext().put(TENANT_ID, name);
	}

	public static final String TENANT_ID = "tenantId";

}
