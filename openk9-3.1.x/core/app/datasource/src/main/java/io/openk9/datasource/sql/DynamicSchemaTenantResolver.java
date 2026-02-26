/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.sql;

import io.quarkus.arc.Unremovable;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.InjectionException;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@RequestScoped
@Unremovable
@PersistenceUnitExtension
public class DynamicSchemaTenantResolver implements TenantResolver {

	private static final Logger LOG = Logger.getLogger(DynamicSchemaTenantResolver.class);

	@Inject
	Instance<RoutingContext> routingContextInstance;

	@Override
	public String getDefaultTenantId() {
		return "<unknown>";
	}

	@Override
	public String resolveTenantId() {
		try {
			RoutingContext context = routingContextInstance.get();

			String tenantId = context.get("_tenantId", getDefaultTenantId());
			if (LOG.isDebugEnabled()) {
				LOG.debug(String.format("tenant resolved: %s", tenantId));
			}
			return tenantId;
		}
		catch (InjectionException re) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Tenant cannot be resolved, because no RoutingContext is injected");
			}
			return getDefaultTenantId();
		}

	}
}
