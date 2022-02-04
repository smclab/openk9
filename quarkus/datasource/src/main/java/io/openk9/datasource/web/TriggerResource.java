package io.openk9.datasource.web;

import io.openk9.datasource.listener.SchedulerInitializer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;

@Path("/v1/trigger")
@ApplicationScoped
public class TriggerResource {

	@POST
	@Consumes("application/json")
	public Uni<Void> reindex(TriggerRequestDto dto) {

		List<Uni<Void>> triggers = new ArrayList<>();

		for (long datasourceId : dto.getDatasourceIds()) {
			triggers.add(
				_schedulerInitializer
					.get()
					.triggerJob(datasourceId, String.valueOf(datasourceId))
			);
		}

		return Uni.combine().all().unis(triggers).discardItems();

	}

	@Inject
	Instance<SchedulerInitializer> _schedulerInitializer;

	@Data
	@NoArgsConstructor
	@RegisterForReflection
	@AllArgsConstructor
	public static class TriggerRequestDto {
		long[] datasourceIds;
	}

}
