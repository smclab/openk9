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

package io.openk9.tenantmanager.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.openk9.common.util.RandomGenerator;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.pipe.tenant.create.TenantManagerActorSystem;
import io.openk9.tenantmanager.pipe.tenant.delete.DeleteTenantActorSystem;
import io.openk9.tenantmanager.service.DuplicateVirtualHostException;
import io.openk9.tenantmanager.service.TenantDbService;
import io.openk9.tenantmanager.service.TenantProvisioningService;
import io.openk9.tenantmanager.service.dto.CreateTablesResponse;
import io.openk9.tenantmanager.service.dto.CreateTenantRequest;
import io.openk9.tenantmanager.service.dto.DeleteTenantRequest;
import io.openk9.tenantmanager.service.dto.DeleteTenantResponse;
import io.openk9.tenantmanager.service.dto.EffectiveDeleteTenantRequest;

import io.smallrye.mutiny.Uni;

@Path("/tenant-manager/tenant")
public class TenantManagerResource {

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<TenantResponseDTO> createTenant(
		CreateTenantRequest createTenantRequest) {

		return provisioningService.create(createTenantRequest)
			.onFailure(DuplicateVirtualHostException.class)
			.transform(cause -> new WebApplicationException(cause, Response.Status.CONFLICT));
	}

	@POST
	@Path("/{id}/tables")
	public Uni<CreateTablesResponse> createTables(@PathParam("id") Long id) {
		return provisioningService.populateSchema(id);
	}

	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<DeleteTenantResponse> deleteTenant(DeleteTenantRequest deleteTenantRequest) {
		return provisioningService.requestDeletion(deleteTenantRequest);
	}

	@DELETE
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<DeleteTenantResponse> deleteTenant(
		EffectiveDeleteTenantRequest effectiveDeleteTenantRequest) {
		return provisioningService.delete(effectiveDeleteTenantRequest);
	}

	@Inject
	TenantProvisioningService provisioningService;

}
