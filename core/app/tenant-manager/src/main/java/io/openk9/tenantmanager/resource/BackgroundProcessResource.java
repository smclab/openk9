package io.openk9.tenantmanager.resource;

import io.openk9.tenantmanager.model.BackgroundProcess;
import io.openk9.tenantmanager.service.BackgroundProcessService;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;

@Path("/tenant-manager/background-process")
public class BackgroundProcessResource {

	@GET
	@Path("/status/{status}")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<List<BackgroundProcess>> findAllBackgroundProcess(
		@PathParam("status") BackgroundProcess.Status status) {
		return backgroundProcessService.findAllBackgroundProcessByStatus(status);
	}

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<List<BackgroundProcess>> findAllBackgroundProcess() {
		return backgroundProcessService.findAllBackgroundProcess();
	}

	@GET
	@Path("/id/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<BackgroundProcess> findBackgroundProcessById(UUID id) {
		return backgroundProcessService.findBackgroundProcessById(id);
	}

	@Inject
	BackgroundProcessService backgroundProcessService;

}
