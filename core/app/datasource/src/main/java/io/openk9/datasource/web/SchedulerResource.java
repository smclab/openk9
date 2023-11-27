package io.openk9.datasource.web;

import io.openk9.datasource.service.SchedulerService;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

@ApplicationScoped
@Path("/schedulers")
@RolesAllowed("k9-admin")
public class SchedulerResource {

	@Path("/{schedulerId}/getDeletedContentIds")
	@GET
	public Uni<List<String>> getDeletedContentIds(@PathParam("schedulerId") long schedulerId) {
		return schedulerService.getDeletedContentIds(schedulerId);
	}

	@Path("/{schedulerId}/cancelSchedulation")
	@POST
	public Uni<Void> cancelSchedulation(@PathParam("schedulerId") long schedulerId) {
		return schedulerService.cancelSchedulation(
			routingContext.get("_tenantId"), schedulerId);
	}

	@Path("/{schedulerId}/rerouteSchedulation")
	@POST
	public Uni<Void> rerouteSchedulation(@PathParam("schedulerId") long schedulerId) {
		return schedulerService.rereouteSchedulation(
			routingContext.get("_tenantId"), schedulerId);
	}

	@Inject
	SchedulerService schedulerService;

	@Inject
	RoutingContext routingContext;

}
