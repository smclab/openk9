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
import io.openk9.datasource.listener.SchedulerInitializer;
import io.openk9.datasource.model.Datasource;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@CircuitBreaker
@Path("/v1/index")
@ApplicationScoped
@ActivateRequestContext
public class ReindexResource {

	@POST
	@Path("/reindex")
	public Uni<List<ReindexResponseDto>> reindex(ReindexRequestDto dto) {

		return sf.withTransaction(s -> {

			Uni<List<Datasource>> datasourceList =
				s.find(
					Datasource.class,
					(Object[])dto.getDatasourceIds().toArray(Long[]::new)
				);

			return datasourceList
				.flatMap(list -> {

					for (Datasource datasource : list) {

						datasource.setLastIngestionDate(
							OffsetDateTime.ofInstant(
								Instant.ofEpochMilli(0),
								ZoneOffset.UTC
							)
						);

						datasource.setDataIndex(null);
					}

					return s.persistAll(list.toArray(Object[]::new))
						.map(__ -> list);

				})
				.flatMap(list -> schedulerInitializer
					.get()
					.triggerJobs(
						list
							.stream()
							.map(Datasource::getId)
							.collect(Collectors.toList())))
				.map(e -> dto.getDatasourceIds()
					.stream()
					.map(id -> ReindexResponseDto.of(id, true))
					.collect(Collectors.toList()));

		});

	}

	@Inject
	Mutiny.SessionFactory sf;

	@Inject
	Instance<SchedulerInitializer> schedulerInitializer;

}
