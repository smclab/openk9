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

package io.openk9.datasource.service;

import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;

import io.openk9.tenantmanager.grpc.TenantListResponse;
import io.openk9.tenantmanager.grpc.TenantManager;
import io.openk9.tenantmanager.grpc.TenantRequest;
import io.openk9.tenantmanager.grpc.TenantResponse;

import com.google.protobuf.Empty;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class TenantRegistryImpl implements TenantRegistry {

	@GrpcClient
	TenantManager tenantManager;

	@Override
	public Uni<String> getTenantId(String virtualHost) {
		return tenantManager.findTenant(TenantRequest.newBuilder()
				.setVirtualHost(virtualHost)
				.build())
			.map(TenantResponse::getSchemaName);
	}

	@Override
	public Uni<List<String>> getTenantIdList() {
		return tenantManager.findTenantList(Empty.newBuilder().build())
			.map(TenantListResponse::getTenantResponseList)
			.map(tenantResponses -> tenantResponses.stream()
				.map(TenantResponse::getSchemaName)
				.toList()
			);
	}
}
