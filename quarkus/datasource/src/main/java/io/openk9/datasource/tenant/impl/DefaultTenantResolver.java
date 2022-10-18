package io.openk9.datasource.tenant.impl;

import io.openk9.datasource.tenant.TenantResolver;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultTenantResolver implements TenantResolver {

	public long getTenantId() {
		return tenantId;
	}

	@ConfigProperty(name = "openk9.datasource.tenant.id", defaultValue = "1")
	long tenantId;

}
