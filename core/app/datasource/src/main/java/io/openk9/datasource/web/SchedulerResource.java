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

package io.openk9.datasource.web;

import java.util.List;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.openk9.datasource.service.SchedulerService;
import io.openk9.datasource.web.dto.StatusResponse;
import io.openk9.datasource.web.dto.openapi.SchedulerDtoExamples;

import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
@Path("/schedulers")
@RolesAllowed("k9-admin")
public class SchedulerResource {

	@Operation(operationId = "getDeletedContentIds")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "200",
					description = "List of deleted content ids returned",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = Response.class),
									example = SchedulerDtoExamples.GET_DELETED_CONTENT_IDS_RESPONSE
							)
					}
			),
			@APIResponse(ref = "#/components/responses/bad-request"),
			@APIResponse(ref = "#/components/responses/not-found"),
			@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/{schedulerId}/getDeletedContentIds")
	@GET
	public Uni<List<String>> getDeletedContentIds(@PathParam("schedulerId") long schedulerId) {
		return schedulerService.getDeletedContentIds(schedulerId);
	}

	@Operation(operationId = "closeScheduling")
	@APIResponses(value = {
			@APIResponse(responseCode = "204", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "204",
					description = "Scheduling closed",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = Response.class)
							)
					}
			),
			@APIResponse(ref = "#/components/responses/bad-request"),
			@APIResponse(ref = "#/components/responses/not-found"),
			@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/{schedulerId}/closeScheduling")
	@POST
	public Uni<Void> closeScheduling(
			@Parameter(description = "Id of scheduling")
			@PathParam("schedulerId") long schedulerId) {
		return schedulerService.closeScheduling(
			routingContext.get("_tenantId"), schedulerId);
	}

	@Operation(operationId = "cancelScheduling")
	@APIResponses(value = {
			@APIResponse(responseCode = "204", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "204",
					description = "Scheduling cancelled",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = Response.class)
							)
					}
			),
			@APIResponse(ref = "#/components/responses/bad-request"),
			@APIResponse(ref = "#/components/responses/not-found"),
			@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/{schedulerId}/cancelScheduling")
	@POST
	public Uni<Void> cancelScheduling(
			@Parameter(description = "Id of scheduling")
			@PathParam("schedulerId") long schedulerId) {
		return schedulerService.cancelScheduling(
			routingContext.get("_tenantId"), schedulerId);
	}

	@Operation(operationId = "rerouteScheduling")
	@APIResponses(value = {
			@APIResponse(responseCode = "204", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "204",
					description = "Scheduling rerouted successfully",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = Response.class)
							)
					}
			),
			@APIResponse(ref = "#/components/responses/bad-request"),
			@APIResponse(ref = "#/components/responses/not-found"),
			@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/{schedulerId}/rerouteScheduling")
	@POST
	public Uni<Void> rerouteScheduling(
			@Parameter(description = "Id of scheduling")
			@PathParam("schedulerId") long schedulerId) {
		return schedulerService.rereouteScheduling(
			routingContext.get("_tenantId"), schedulerId);
	}

	@Operation(operationId = "status")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "200",
					description = "Status of scheduling system up",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = Response.class),
									example = SchedulerDtoExamples.STATUS_RESPONSE
							)
					}
			),
			@APIResponse(ref = "#/components/responses/bad-request"),
			@APIResponse(ref = "#/components/responses/not-found"),
			@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/status")
	@GET
	public Uni<RestResponse<StatusResponse>> status() {
		return schedulerService.getHealthStatusList()
			.map(StatusResponse::new)
			.map(statusResponse -> {
				var status = RestResponse.Status.OK;

				if (statusResponse.getErrors() > 0) {
					status = RestResponse.Status.INTERNAL_SERVER_ERROR;
				}

				return RestResponse.status(status, statusResponse);
			});
	}

	@Inject
	SchedulerService schedulerService;

	@Inject
	RoutingContext routingContext;

}
