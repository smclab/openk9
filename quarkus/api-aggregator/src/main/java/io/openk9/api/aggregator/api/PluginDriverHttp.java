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

import io.openk9.api.aggregator.client.dto.InvokeDataParserDTO;
import io.openk9.api.aggregator.client.dto.PluginDriverDTO;
import io.openk9.api.aggregator.client.dto.PluginDriverDTOList;
import io.openk9.api.aggregator.client.dto.PluginInfoResponseDTO;
import io.openk9.api.aggregator.client.dto.SchedulerEnabledDTO;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;

public interface PluginDriverHttp {

	@RolesAllowed({"plugin-driver-manager-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v1/plugin-driver/invoke-data-parser")
	Uni<Void> invokeDataParser(InvokeDataParserDTO invokeDataParserDTO);

	@RolesAllowed({"plugin-driver-manager-read", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v1/plugin-driver/scheduler-enabled/{serviceDriverName}")
	Uni<SchedulerEnabledDTO> schedulerEnabled(@PathParam("serviceDriverName") String serviceDriverName);

	@RolesAllowed({"plugin-driver-manager-read", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v1/plugin-driver/{serviceDriverName}")
	Uni<PluginDriverDTO> getPluginDriver(@PathParam("serviceDriverName") String serviceDriverName);

	@RolesAllowed({"plugin-driver-manager-read", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v1/plugin-driver/")
	Uni<PluginDriverDTOList> getPluginDriverList(
		Collection<String> serviceDriverNames);

	@RolesAllowed({"plugin-driver-manager-read", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v1/plugin-driver/")
	Uni<PluginDriverDTOList> getPluginDriverList();

	@PermitAll
	@GET
	@Path("/v1/plugin")
	Uni<List<PluginInfoResponseDTO>> getPluginInfo();

	@PermitAll
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/plugins/{pluginName}/static/build/{resourceName}")
	Uni<Response> getPluginResource(
		@PathParam("pluginName") String pluginName,
		@PathParam("resourceName") String resourceName,
		@QueryParam("t") Long t
	);
	
}
