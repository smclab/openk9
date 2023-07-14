package io.openk9.auth.tenant.impl;

import io.openk9.auth.tenant.TenantResolver;
import io.vertx.core.Vertx;
import org.jboss.logging.Logger;

class VertxTenantResolver implements TenantResolver {

	public long getTenantId() {
		return getTenantName().hashCode();
	}

	@Override
	public String getTenantName() {

		String tenantName = Vertx.currentContext().getLocal(TENANT_ID);

		if (logger.isDebugEnabled()) {
			logger.debugf("get tenant: {}", tenantName);
		}

		return tenantName;

	}

	@Override
	public void setTenant(String name) {

		if (logger.isDebugEnabled()) {
			logger.debugf("set tenant: {}", name);
		}

		Vertx.currentContext().putLocal(TENANT_ID, name);
	}

	public static final String TENANT_ID = "tenantId";

	private static final Logger logger = Logger.getLogger(VertxTenantResolver.class);

}
