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

import java.util.List;
import java.util.Objects;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import io.openk9.datasource.listener.SchedulerInitializer;
import io.openk9.datasource.service.SchedulerService;

import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Path("/v1/trigger")
@RolesAllowed("k9-admin")
public class TriggerResource {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@ActivateRequestContext
	public Uni<List<SchedulerService.DatasourceJobStatus>> trigger(TriggerResourceRequest dto) {
		List<Long> datasourceIds = dto.datasourceIds;
		var startFromFirst = Objects.requireNonNullElse(dto.startFromFirst, false);

		String tenantId = routingContext.get("_tenantId");

		return schedulerService
			.getStatusByDatasources(datasourceIds)
			.call(() -> schedulerInitializer
				.get()
				.triggerJobs(tenantId, datasourceIds, startFromFirst)
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
	public static class TriggerResourceRequest {
		private List<Long> datasourceIds;
		private Boolean startFromFirst;
	}

}
