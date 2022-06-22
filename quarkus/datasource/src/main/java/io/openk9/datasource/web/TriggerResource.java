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
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@CircuitBreaker
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
