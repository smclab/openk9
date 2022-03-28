package io.openk9.datasource.web;

import io.openk9.datasource.listener.SchedulerInitializer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Traced
@Path("/v1/trigger")
@ApplicationScoped
public class TriggerResource {

	@POST
	@Consumes("application/json")
	public Uni<List<Long>> reindex(TriggerRequestDto dto) {

		List<Uni<Long>> triggers = new ArrayList<>();

		for (long datasourceId : dto.getDatasourceIds()) {
			triggers.add(
				_schedulerInitializer
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
	Instance<SchedulerInitializer> _schedulerInitializer;

	@Data
	@NoArgsConstructor
	@RegisterForReflection
	@AllArgsConstructor
	public static class TriggerRequestDto {
		private long[] datasourceIds;
	}

}
