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

import io.openk9.datasource.listener.SchedulerInitializer;
import io.openk9.datasource.service.SchedulerService;
import io.openk9.datasource.web.dto.TriggerWithDateResourceDTO;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Path("/v2/trigger")
@RolesAllowed("k9-admin")
public class TriggerWithDateResource {

	/**
	 * Triggers the scheduling or reindexing for the specified datasource and ingestion parameters.
	 * <p>
	 * This method accepts a {@link TriggerV2ResourceDTO} object containing the datasource ID,
	 * reindex flag, and start ingestion date.
	 * It retrieves the status of the schedule that is trying to be started,
	 * and triggers the scheduled jobs if necessary.
	 *
	 * @param dto The {@link TriggerV2ResourceDTO} object containing the parameters for the job trigger.
	 * @return A {@link Uni} representing the status of the triggered jobs as a {@link SchedulerService.DatasourceJobStatus}.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@ActivateRequestContext
	public Uni<SchedulerService.DatasourceJobStatus> trigger(TriggerV2ResourceDTO dto) {

		TriggerWithDateResourceDTO withDateResourceDTO =
			TriggerWithDateResourceDTO.builder()
				.datasourceIds(List.of(dto.getDatasourceId()))
				.reindex(dto.isReindex())
				.startIngestionDate(dto.getStartIngestionDate())
				.build();

		List<Long> datasourceIds = withDateResourceDTO.getDatasourceIds();
		String tenantId = routingContext.get("_tenantId");

		return schedulerService
			.getStatusByDatasources(datasourceIds)
			.onItem()
			.transform(datasourceJobStatuses ->
				datasourceJobStatuses.stream().findFirst().orElse(null))
			.call(() -> schedulerInitializer
				.get()
				.triggerJobs(tenantId, withDateResourceDTO)
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
	public static class TriggerV2ResourceDTO {
		private long datasourceId;
		private boolean reindex;
		private OffsetDateTime startIngestionDate;
	}
}
