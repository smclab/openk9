package io.openk9.auth.tenant.impl;

import io.openk9.auth.tenant.MultiTenancyConfig;
import io.openk9.auth.tenant.TenantResolver;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

@Dependent
public class TenantResolverInitializer {

	@Produces
	@ApplicationScoped
	public TenantResolver createTenantResolver(MultiTenancyConfig multiTenancyConfig) {

		if (multiTenancyConfig.isEnabled()) {
			return new VertxTenantResolver();
		}
		else {
			return new TenantResolver() {
				private static final String TENANT_NAME = "default";

				@Override
				public String getTenantName() {
					return TENANT_NAME;
				}

				@Override
				public void setTenant(String name) {}
			};
		}

	}

}
