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
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.List;

@CircuitBreaker
@Path("/v1/index")
@ApplicationScoped
@ActivateRequestContext
@RolesAllowed("k9-admin")
public class ReindexResource {

	@POST
	@Path("/reindex")
	public Uni<List<ReindexResponseDto>> reindex(ReindexRequestDto dto) {
		return schedulerInitializer
			.get()
			.triggerJobs(ctir.resolveCurrentTenantIdentifier(), dto.getDatasourceIds(), true)
			.map(list -> list
				.stream()
				.map(datasourceId -> ReindexResponseDto.of(datasourceId, true))
				.toList()
			);
	}

	@Inject
	Instance<SchedulerInitializer> schedulerInitializer;

	@Inject
	CurrentTenantIdentifierResolver ctir;

	@Data
	@AllArgsConstructor(staticName = "of")
	@NoArgsConstructor(staticName = "of")
	public static class ReindexResponseDto {
		private long datasourceId;
		private boolean status;
	}

	@Data
	public static class ReindexRequestDto {
		private List<Long> datasourceIds;
	}

}
