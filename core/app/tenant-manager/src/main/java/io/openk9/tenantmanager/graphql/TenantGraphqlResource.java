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

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.model.EntityServiceValidatorWrapper;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.tenantmanager.dto.TenantDTO;
import io.openk9.tenantmanager.mapper.TenantMapper;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.service.TenantService;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
@RolesAllowed("admin")
public class TenantGraphqlResource {

	@Query
	public Uni<Connection<Tenant>> getTenants(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return tenantService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<Tenant> getTenant(@Id long id) {
		return tenantService.findById(id);
	}

	@Mutation
	public Uni<Response<Tenant>> tenant(
		@Id Long id, TenantDTO tenantDTO,
		@DefaultValue("false") boolean patch) {

		EntityServiceValidatorWrapper<Tenant, TenantDTO> validator =
			tenantService.getValidator();

		if (id != null) {
			if (patch) {
				return validator.patch(id, tenantDTO);
			}
			else {
				return validator.update(id, tenantDTO);
			}
		}

		return validator.create(tenantDTO);

	}

	@Inject
	TenantService tenantService;

	@Inject
	TenantMapper tenantMapper;

}
