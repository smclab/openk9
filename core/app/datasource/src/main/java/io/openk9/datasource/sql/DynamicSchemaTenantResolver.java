package io.openk9.datasource.sql;

import io.quarkus.arc.Unremovable;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.vertx.ext.web.RoutingContext;
import org.jboss.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

@RequestScoped
@Unremovable
@PersistenceUnitExtension
public class DynamicSchemaTenantResolver implements TenantResolver {

	private static final Logger LOG = Logger.getLogger(DynamicSchemaTenantResolver.class);

	@Inject
	RoutingContext routingContext;

	@Override
	public String getDefaultTenantId() {
		return "public";
	}

	@Override
	public String resolveTenantId() {
		String tenantId = routingContext.get("_tenantId", getDefaultTenantId());
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("tenant resolved: %s", tenantId));
		}
		return tenantId;
	}
}
