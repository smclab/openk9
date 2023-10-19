package io.openk9.datasource.sql;

import io.quarkus.arc.Unremovable;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.vertx.ext.web.RoutingContext;
import org.jboss.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.ResolutionException;
import javax.inject.Inject;

@RequestScoped
@Unremovable
@PersistenceUnitExtension
public class DynamicSchemaTenantResolver implements TenantResolver {

	private static final Logger LOG = Logger.getLogger(DynamicSchemaTenantResolver.class);

	@Inject
	Instance<RoutingContext> routingContextI;

	@Override
	public String getDefaultTenantId() {
		return "<unknown>";
	}

	@Override
	public String resolveTenantId() {
		try {
			RoutingContext context = routingContextI.get();
			String tenantId = context.get("_tenantId", getDefaultTenantId());
			if (LOG.isDebugEnabled()) {
				LOG.debug(String.format("tenant resolved: %s", tenantId));
			}
			return tenantId;
		}
		catch (ResolutionException re) {
			return getDefaultTenantId();
		}

	}
}
