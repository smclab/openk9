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

package io.openk9.api.aggregator.service;

import io.openk9.api.aggregator.model.Tenant;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.Blocking;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@ActivateRequestContext
public class TenantRegistry {

	public Optional<Tenant> getTenant(String name) {
		return Optional.ofNullable(getTenantNullable(name));
	}

	public Tenant getTenantNullable(String name) {
		return _tenantMap.get(name);
	}

	public Collection<Tenant> getTenantList() {
		return _tenantMap.values();
	}


	@Scheduled(every="30s")
	@Blocking
	void initializeTenantMap() {

		List<Tenant> listTenant =
			Tenant
				.<Tenant>list("active = true")
				.await()
				.indefinitely();

		Map<String, Tenant> map = new HashMap<>(listTenant.size());

		for (Tenant tenant : listTenant) {
			map.put(tenant.getVirtualHost(), tenant);
		}

		_tenantMap.clear();

		_tenantMap.putAll(map);

	}

	private final Map<String, Tenant> _tenantMap =
		new ConcurrentHashMap<>();

}
