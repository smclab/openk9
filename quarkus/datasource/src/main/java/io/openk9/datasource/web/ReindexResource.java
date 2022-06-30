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
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.jboss.logging.Logger;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

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
	public Publisher<List<ReindexResponseDto>> reindex(ReindexRequestDto dto) {

		Uni<List<Datasource>> listDatasource = Datasource
			.<Datasource>list(
				"active = ?1 and datasourceId in ?2",
				true, dto.getDatasourceIds());

		Mono<List<Datasource>> listDatasourceMono =
			Mono.from(listDatasource.convert().toPublisher());

		return listDatasourceMono
			.flatMap(datasourceList -> {

				List<Mono<ReindexResponseDto>> monos = new ArrayList<>();

				for (Datasource datasource : datasourceList) {

					datasource.setLastIngestionDate(Instant.EPOCH);

					monos.add(
						Mono.from(datasource.persist().convert().toPublisher())
							.then(datasourceIndexService.reindex(datasource))
							.then(Mono.from(schedulerInitializer.get().triggerJob(
								datasource.getDatasourceId(), datasource.getName()).convert().toPublisher()))
							.map(ignore -> ReindexResponseDto.of(
								datasource.getDatasourceId(),
								true))
							.transform(mono -> {

								ReindexResponseDto fallback =
									ReindexResponseDto.of(
										datasource.getDatasourceId(),
										false);

								return mono
									.onErrorReturn(fallback)
									.defaultIfEmpty(fallback);

							})
					);

				}

				return Mono.zip(monos, objs -> {
					List<ReindexResponseDto> response = new ArrayList<>();
					for (Object obj : objs) {
						response.add((ReindexResponseDto) obj);
					}
					return response;
				});
			});

	}

	@Inject
	Instance<SchedulerInitializer> schedulerInitializer;

	@Inject
	Logger logger;

	@Inject
	DatasourceIndexService datasourceIndexService;

}
