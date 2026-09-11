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

package io.openk9.datasource.mock;

import java.util.concurrent.atomic.AtomicBoolean;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.quarkus.test.Mock;
import jakarta.enterprise.context.RequestScoped;

@Mock
@RequestScoped
@PersistenceUnitExtension
public class TestTenantResolver implements TenantResolver {

	/**
	 * When set, resolves the tenant as the production resolver does off an
	 * HTTP request: to the default, unknown, tenant. Lets a test exercise a
	 * path that runs on a Pekko dispatcher or an event loop, where no
	 * {@code RoutingContext} exists and every session opened without an
	 * explicit tenant fails.
	 */
	public static final AtomicBoolean OFF_REQUEST = new AtomicBoolean(false);

	@Override
	public String getDefaultTenantId() {
		return "public";
	}

	@Override
	public String resolveTenantId() {
		return OFF_REQUEST.get() ? "<unknown>" : "public";
	}

	@Override
	public boolean isRoot(String tenantId) {
		return TenantResolver.super.isRoot(tenantId);
	}

}
