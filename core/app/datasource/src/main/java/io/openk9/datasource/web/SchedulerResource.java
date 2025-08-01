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

import io.openk9.datasource.service.SchedulerService;
import io.openk9.datasource.web.dto.StatusResponse;

import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import org.jboss.resteasy.reactive.RestResponse;

@ApplicationScoped
@Path("/schedulers")
@RolesAllowed("k9-admin")
public class SchedulerResource {

	@Path("/{schedulerId}/getDeletedContentIds")
	@GET
	public Uni<List<String>> getDeletedContentIds(@PathParam("schedulerId") long schedulerId) {
		return schedulerService.getDeletedContentIds(schedulerId);
	}

	@Path("/{schedulerId}/closeScheduling")
	@POST
	public Uni<Void> closeScheduling(@PathParam("schedulerId") long schedulerId) {
		return schedulerService.closeScheduling(
			routingContext.get("_tenantId"), schedulerId);
	}

	@Path("/{schedulerId}/cancelScheduling")
	@POST
	public Uni<Void> cancelScheduling(@PathParam("schedulerId") long schedulerId) {
		return schedulerService.cancelScheduling(
			routingContext.get("_tenantId"), schedulerId);
	}

	@Path("/{schedulerId}/rerouteScheduling")
	@POST
	public Uni<Void> rerouteScheduling(@PathParam("schedulerId") long schedulerId) {
		return schedulerService.rereouteScheduling(
			routingContext.get("_tenantId"), schedulerId);
	}

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
