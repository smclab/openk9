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

import io.openk9.api.aggregator.client.dto.EnrichItemDTO;
import io.openk9.api.aggregator.client.dto.EnrichItemRequestDTO;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

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

public interface EnrichItemHttp {

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v2/enrichItem/count")
	public Uni<Long> enrichItemCount();

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/enrichItem/filter/count")
	public Uni<Long> enrichItemCountFilter(EnrichItemRequestDTO dto);

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v2/enrichItem/{id}")
	public Uni<EnrichItemDTO> enrichItemFindById(@PathParam("id") long id);

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/enrichItem/filter")
	public Uni<List<EnrichItemDTO>> enrichItemFilter(EnrichItemRequestDTO dto);

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v2/enrichItem")
	public Uni<List<EnrichItemDTO>> enrichItemFindAll(
		@QueryParam("sort") List<String> enrichItemSortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("20") int pageSize
	);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/enrichItem")
	public Uni<EnrichItemDTO> enrichItemCreate(EnrichItemRequestDTO dto);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/enrichItem/{id}")
	public Uni<EnrichItemDTO> enrichItemUpdate(
		@PathParam("id") long id, EnrichItemRequestDTO dto);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@PATCH
	@Path("/v2/enrichItem/{id}")
	public Uni<EnrichItemDTO> enrichItemPatch(
		@PathParam("id") long id, EnrichItemRequestDTO dto);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@DELETE
	@Path("/v2/enrichItem/{id}")
	public Uni<Response> enrichItemDeleteById(@PathParam("id") long id);
	
}
