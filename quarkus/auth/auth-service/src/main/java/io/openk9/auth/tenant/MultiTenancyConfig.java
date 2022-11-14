package io.openk9.auth.tenant;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MultiTenancyConfig {

	@ConfigProperty(
		name = "openk9.datasource.multitenancy.enabled",
		defaultValue = "false"
	)
	boolean enabled;

	public boolean isEnabled() {
		return enabled;
	}
}
