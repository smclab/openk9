package io.openk9.datasource.web;

import io.openk9.datasource.dto.ReindexRequestDto;
import io.openk9.datasource.dto.ReindexResponseDto;
import io.openk9.datasource.listener.SchedulerInitializer;
import io.openk9.datasource.model.Datasource;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/v1/index")
@ApplicationScoped
public class ReindexResource {

	@POST
	@Path("/reindex")
	public Uni<List<ReindexResponseDto>> reindex(ReindexRequestDto dto) {

		return Panache.withTransaction(() ->
			Datasource
				.<Datasource>list("datasourceId in ?1", dto.getDatasourceIds())
				.flatMap(datasourceList -> {

					List<Uni<?>> unis = new ArrayList<>();

					for (Datasource datasource : datasourceList) {

						datasource.setLastIngestionDate(Instant.EPOCH);

						unis.add(datasource.persist());

						unis.add(
							_schedulerInitializer.get().triggerJob(
								datasource.getDatasourceId(), datasource.getName())
								.map(unused ->
									ReindexResponseDto.of(
										datasource.getDatasourceId(),
										true)
								)
						);

					}

					return Uni
						.combine()
						.all()
						.unis(unis)
						.combinedWith(list ->
							list
								.stream()
								.filter(o -> o instanceof ReindexResponseDto)
								.map(o -> (ReindexResponseDto)o)
								.collect(Collectors.toList())
						);
				})
		);

	}

	@Inject
	Instance<SchedulerInitializer> _schedulerInitializer;

	@Inject
	Logger _logger;

}
