package io.openk9.datasource.sql;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@PersistenceUnitExtension
public class SchemaTenantResolver implements TenantResolver {

	private static final Logger LOG = Logger.getLogger(SchemaTenantResolver.class);

	@Inject
	io.openk9.auth.tenant.TenantResolver tenantResolver;

	@Override
	public String getDefaultTenantId() {
		return "public";
	}

	@Override
	public String resolveTenantId() {
		String tenantId = tenantResolver.getTenantName();
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("tenant resolved: %s", tenantId));
			if (tenantId == null) {
				LOG.debug("fallback to default tenant");
			}
		}
		return tenantId != null ? tenantId : getDefaultTenantId();
	}
}
