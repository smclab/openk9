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

package io.openk9.tenantmanager.grpc;

import com.google.protobuf.Empty;
import io.openk9.tenantmanager.mapper.TenantMapper;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.service.TenantService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class TenantManagerGrpcService implements TenantManager {
	@Inject
	TenantService tenantService;
	@Inject
	TenantMapper tenantMapper;

	@Override
	@ActivateRequestContext
	public Uni<TenantResponse> findTenant(TenantRequest request) {

		Uni<Tenant> tenantUni = tenantService.findTenantByVirtualHost(
			request.getVirtualHost());

		return tenantUni
			.onItem()
			.ifNotNull()
			.transform(tenantMapper::toTenantResponse);

	}

	@Override
	public Uni<TenantListResponse> findTenantList(Empty request) {
		Uni<List<Tenant>> tenantUni = tenantService.findAllTenant();

		return tenantUni
			.onItem()
			.ifNotNull()
			.transform(list -> list
				.stream()
				.map(tenantMapper::toTenantResponse)
				.collect(
					Collectors.collectingAndThen(
						Collectors.toList(),
						collect -> TenantListResponse.newBuilder()
							.addAllTenantResponse(collect)
							.build()
					)
				)
			);
	}

}
