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

package io.openk9.api.aggregator.api;

import io.openk9.api.aggregator.client.dto.TenantDTO;
import io.openk9.api.aggregator.client.dto.TenantRequestDTO;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

public interface TenantHttp {

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v2/tenant/count")
	public Uni<Long> tenantCount();

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/tenant/filter/count")
	public Uni<Long> tenantCountFilter(TenantRequestDTO dto);

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v2/tenant/{id}")
	public Uni<TenantDTO> tenantFindById(@PathParam("id") long id);

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/tenant/filter")
	public Uni<List<TenantDTO>> tenantFilter(TenantRequestDTO dto);

	@PermitAll
	@GET
	@Path("/v2/tenant")
	public Uni<List<TenantDTO>> tenantFindAll(
		@QueryParam("sort") List<String> tenantSortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("20") int pageSize
	);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/tenant")
	public Uni<TenantDTO> tenantCreate(TenantRequestDTO dto);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/tenant/{id}")
	public Uni<TenantDTO> tenantUpdate(
		@PathParam("id") long id, TenantRequestDTO dto);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@PATCH
	@Path("/v2/tenant/{id}")
	public Uni<TenantDTO> tenantPatch(
		@PathParam("id") long id, TenantRequestDTO dto);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@DELETE
	@Path("/v2/tenant/{id}")
	public Uni<Response> tenantDeleteById(@PathParam("id") long id);
	
}
