package io.openk9.datasource.web;

import io.openk9.datasource.listener.SchedulerInitializer;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/v1/trigger")
public class TriggerResource {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@ActivateRequestContext
	public Uni<List<Long>> trigger(TriggerResourceRequest dto) {

		return schedulerInitializer
			.get()
			.triggerJobs(dto.getDatasourceIds());

	}

	@Inject
	Instance<SchedulerInitializer> schedulerInitializer;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TriggerResourceRequest {
		private List<Long> datasourceIds;
	}

}
