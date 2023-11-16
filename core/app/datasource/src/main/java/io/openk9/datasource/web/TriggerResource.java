package io.openk9.datasource.web;

import io.openk9.datasource.listener.SchedulerInitializer;
import io.openk9.datasource.service.SchedulerService;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/v1/trigger")
@RolesAllowed("k9-admin")
public class TriggerResource {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@ActivateRequestContext
	public Uni<List<SchedulerService.DatasourceJobStatus>> trigger(TriggerResourceRequest dto) {
		List<Long> datasourceIds = dto.datasourceIds;
		String tenantId = routingContext.get("_tenantId");

		return schedulerService
			.getStatusByDatasources(datasourceIds)
			.call(() -> schedulerInitializer
				.get()
				.triggerJobs(tenantId, dto.getDatasourceIds())
			);

	}

	@Inject
	Instance<SchedulerInitializer> schedulerInitializer;

	@Inject
	RoutingContext routingContext;

	@Inject
	SchedulerService schedulerService;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TriggerResourceRequest {
		private List<Long> datasourceIds;
	}

}
