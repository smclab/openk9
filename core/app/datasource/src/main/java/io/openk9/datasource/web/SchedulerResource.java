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
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
@Path("/schedulers")
public class SchedulerResource {

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "List of deleted content ids returned",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = List.class),
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

	@APIResponses(value = {
		@APIResponse(
			responseCode = "204",
			description = "Scheduling closed"
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/{schedulerId}/closeScheduling")
	@POST
	public Uni<Void> closeScheduling(
		@Parameter(description = "Scheduler entity ID")
		@PathParam("schedulerId") long schedulerId) {

		return schedulerService.closeScheduling(
			routingContext.get("_tenantId"), schedulerId);
	}

	@APIResponses(value = {
		@APIResponse(
			responseCode = "204",
			description = "Scheduling cancelled"
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/{schedulerId}/cancelScheduling")
	@POST
	public Uni<Void> cancelScheduling(
		@Parameter(description = "Scheduler entity ID")
		@PathParam("schedulerId") long schedulerId) {

		return schedulerService.cancelScheduling(
			routingContext.get("_tenantId"), schedulerId);
	}

	@APIResponses(value = {
		@APIResponse(
			responseCode = "204",
			description = "Scheduling rerouted successfully"
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@Path("/{schedulerId}/rerouteScheduling")
	@POST
	public Uni<Void> rerouteScheduling(
		@Parameter(description = "Scheduler entity ID")
		@PathParam("schedulerId") long schedulerId) {
		return schedulerService.rereouteScheduling(
			routingContext.get("_tenantId"), schedulerId);
	}

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "Health statud for each scheduled Datasource Indexing Job.",
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
