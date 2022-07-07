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

import io.openk9.datasource.bus.reindex.ReindexEvents;
import io.openk9.datasource.bus.reindex.ReindexMessage;
import io.openk9.datasource.dto.ReindexRequestDto;
import io.openk9.datasource.dto.ReindexResponseDto;
import io.openk9.datasource.model.Datasource;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@CircuitBreaker
@Path("/v1/index")
@ApplicationScoped
@ActivateRequestContext
public class ReindexResource {

	@POST
	@Path("/reindex")
	public Uni<List<ReindexResponseDto>> reindex(ReindexRequestDto dto) {

		return sf.withTransaction(
			t -> Datasource
				.<Datasource>stream(
					"datasourceId in ?2", dto.getDatasourceIds())
				.flatMap(datasource -> {

					datasource.setLastIngestionDate(
						OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC));

					return datasource.<Datasource>persistAndFlush().toMulti();

				})
				.call(datasource -> eventBus.request(
						ReindexEvents.REINDEX_STEP_1,
						ReindexMessage.of(
							datasource, ReindexEvents.REINDEX_STEP_2)))
				.map(datasource -> ReindexResponseDto.of(
					datasource.getId(),
					true))
				.collect()
				.asList());

	}

	@Inject
	Mutiny.SessionFactory sf;

	@Inject
	EventBus eventBus;

}
