package io.openk9.datasource.web;

import io.openk9.datasource.service.SchedulerService;
import io.smallrye.mutiny.Uni;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

@ApplicationScoped
@Path("/schedulers")
@RolesAllowed("k9-admin")
public class SchedulerResource {

	@Path("/{schedulerId}")
	@GET
	public Uni<List<String>> getDeletedContentIds(@PathParam("schedulerId") long schedulerId) {
		return schedulerService.getDiff(schedulerId);
	}

	@Inject
	SchedulerService schedulerService;

}
