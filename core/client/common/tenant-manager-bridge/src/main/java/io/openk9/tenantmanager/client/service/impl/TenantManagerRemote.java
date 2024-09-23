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

package io.openk9.tenantmanager.client.service.impl;

import io.openk9.api.tenantmanager.TenantManager;
import io.openk9.tenantmanager.grpc.TenantRequest;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Startup
public class TenantManagerRemote implements TenantManager {
	@Override
	public Uni<Tenant> getTenantByVirtualHost(String virtualHost) {
		return tenantManager.findTenant(
			TenantRequest.newBuilder()
				.setVirtualHost(virtualHost)
				.build())
			.map(response -> new Tenant(
				response.getVirtualHost(),
				response.getSchemaName(),
				response.getClientId(),
				response.getClientSecret(),
				response.getRealmName())
			);
	}

	@GrpcClient("tenantmanager")
	io.openk9.tenantmanager.grpc.TenantManager tenantManager;

}
