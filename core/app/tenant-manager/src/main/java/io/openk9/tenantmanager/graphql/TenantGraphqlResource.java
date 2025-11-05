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

package io.openk9.tenantmanager.graphql;

import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.graphql.SortBy;
import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.web.Response;
import io.openk9.tenantmanager.dto.TenantRequestDTO;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.mapper.TenantMapper;
import io.openk9.tenantmanager.service.TenantGraphQLRelayService;
import io.openk9.tenantmanager.service.TenantService;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class TenantGraphqlResource {

	@Query
	public Uni<Connection<TenantResponseDTO>> getTenants(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {

		return relayService.findConnection(
				after, before, first, last, searchText, sortByList)
			.map(connection -> connection.map(mapper::toTenantResponseDTO));
	}

	@Query
	public Uni<TenantResponseDTO> getTenant(@Id String id) {
		return tenantService.findById(Long.valueOf(id));
	}

	@Mutation
	public Uni<Response<TenantResponseDTO>> tenant(TenantRequestDTO tenantRequestDTO) {
		return tenantService.create(tenantRequestDTO);
	}

	@Inject
	TenantMapper mapper;
	@Inject
	TenantGraphQLRelayService relayService;
	@Inject
	TenantService tenantService;

}
