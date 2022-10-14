package io.openk9.datasource.resource;

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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Path("/v1/trigger")
public class TriggerResource {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@ActivateRequestContext
	public Uni<List<Long>> trigger(TriggerResourceRequest dto) {

		List<Uni<Long>> triggers = new ArrayList<>();

		for (long datasourceId : dto.getDatasourceIds()) {
			triggers.add(
				schedulerInitializer
					.get()
					.triggerJob(datasourceId, String.valueOf(datasourceId))
					.map(unused -> datasourceId)
			);
		}

		return Uni
			.combine()
			.all()
			.unis(triggers)
			.combinedWith(Long.class, Function.identity());


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
