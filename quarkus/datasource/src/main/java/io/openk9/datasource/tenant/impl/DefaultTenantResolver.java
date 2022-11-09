package io.openk9.datasource.tenant.impl;

import io.openk9.datasource.tenant.TenantResolver;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.control.RequestContextController;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

@ApplicationScoped
public class DefaultTenantResolver implements TenantResolver {

	public long getTenantId() {
		return -1;
	}

	@Override
	public String getTenantName() {

		if (beanManager.isScope(RequestScoped.class)) {
			return CDI
				.current()
				.select(RoutingContext.class)
				.get()
				.get(TENANT_ID);
		}

		return Vertx.currentContext().get(TENANT_ID);

	}

	@Override
	public void setTenant(String name) {

		if (beanManager.isScope(RequestScoped.class)) {
			try {
				requestContextController.activate();
				CDI
					.current()
					.select(RoutingContext.class)
					.get()
					.put(TENANT_ID, name);
			}
			finally {
				requestContextController.deactivate();
			}
		}
		else {
			Vertx.currentContext().put(TENANT_ID, name);
		}

	}

	public static final String TENANT_ID = "tenantId";

	@Inject
	BeanManager beanManager;

	@Inject
	RequestContextController requestContextController;

}
