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

import io.openk9.api.tenantmanager.TenantManager;
import io.openk9.auth.tenant.TenantRegistry;
import io.quarkus.test.Mock;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@Mock
@ApplicationScoped
public class MockTenantRegistry extends TenantRegistry {

	@Override
	public Uni<TenantManager.Tenant> getTenantByVirtualHost(String virtualHost) {

		var tenant = new TenantManager.Tenant(
			"test.openk9.local", "public", "openk9", "openk9", "public");

		return Uni.createFrom().item(tenant);

	}

}
