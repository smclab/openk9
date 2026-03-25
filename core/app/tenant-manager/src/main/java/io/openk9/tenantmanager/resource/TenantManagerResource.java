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

import java.util.NoSuchElementException;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.service.DuplicateVirtualHostException;
import io.openk9.tenantmanager.service.InvalidDeletionTokenException;
import io.openk9.tenantmanager.service.InvalidTenantNameException;
import io.openk9.tenantmanager.service.TenantNotFoundException;
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
			.transform(cause -> new WebApplicationException(cause, Response.Status.CONFLICT))
			.onFailure(InvalidTenantNameException.class)
			.transform(cause -> new WebApplicationException(cause, Response.Status.BAD_REQUEST));
	}

	@POST
	@Path("/{id}/tables")
	public Uni<CreateTablesResponse> createTables(@PathParam("id") Long id) {

		return provisioningService.populateSchema(id)
			.onFailure(NoSuchElementException.class)
			.transform(cause -> new WebApplicationException(cause, Response.Status.NOT_FOUND))
			.onFailure()
			.transform(WebApplicationException::new);
	}

	/**
	 * Requests a tenant deletion and returns a confirmation
	 * token.
	 */
	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<DeleteTenantResponse> deleteTenant(
		DeleteTenantRequest deleteTenantRequest) {

		return provisioningService
			.requestDeletion(deleteTenantRequest)
			.onFailure(TenantNotFoundException.class)
			.transform(cause ->
				new WebApplicationException(
					cause, Response.Status.BAD_REQUEST));
	}

	/**
	 * Confirms and executes a tenant deletion using the
	 * previously issued token.
	 */
	@DELETE
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<DeleteTenantResponse> deleteTenant(
		EffectiveDeleteTenantRequest request) {

		return provisioningService.delete(request)
			.onFailure(InvalidDeletionTokenException.class)
			.transform(cause ->
				new WebApplicationException(
					cause, Response.Status.FORBIDDEN));
	}

	@Inject
	TenantProvisioningService provisioningService;

}
