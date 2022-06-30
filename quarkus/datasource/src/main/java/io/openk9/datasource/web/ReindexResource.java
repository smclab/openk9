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

import io.openk9.datasource.dto.ReindexRequestDto;
import io.openk9.datasource.dto.ReindexResponseDto;
import io.openk9.datasource.index.DatasourceIndexService;
import io.openk9.datasource.listener.SchedulerInitializer;
import io.openk9.datasource.model.Datasource;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@CircuitBreaker
@Path("/v1/index")
@ApplicationScoped
public class ReindexResource {

	@POST
	@Path("/reindex")
	public Uni<List<ReindexResponseDto>> reindex(ReindexRequestDto dto) {

		return Panache.withTransaction(() ->
			Datasource
				.<Datasource>list(
					"active = ?1 and datasourceId in ?2",
					true, dto.getDatasourceIds())
				.flatMap(datasourceList -> {

					List<Uni<ReindexResponseDto>> unis = new ArrayList<>();

					for (Datasource datasource : datasourceList) {

						datasource.setLastIngestionDate(Instant.EPOCH);

						unis.add(
							datasource.persist()
								.call(() -> datasourceIndexService.reindex(datasource))
								.replaceWithNull()
								.flatMap((ignore) ->
									_schedulerInitializer.get().triggerJob(
											datasource.getDatasourceId(), datasource.getName())
										.map(unused ->
											ReindexResponseDto.of(

												datasource.getDatasourceId(),
												true)
										)
								)
								.replaceIfNullWith(
									() -> ReindexResponseDto.of(
										datasource.getDatasourceId(),
										false))
							);

					}

					return Uni
						.join()
						.all(unis)
						.andFailFast();
				}))
			.runSubscriptionOn(Infrastructure.getDefaultWorkerPool());

	}

	@Inject
	Instance<SchedulerInitializer> _schedulerInitializer;

	@Inject
	Logger _logger;

	@Inject
	DatasourceIndexService datasourceIndexService;

}
