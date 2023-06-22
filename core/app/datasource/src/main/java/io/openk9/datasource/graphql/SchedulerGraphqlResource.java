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

package io.openk9.datasource.graphql;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.service.SchedulerService;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class SchedulerGraphqlResource {

	@Query
	public Uni<Connection<Scheduler>> getSchedulers(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before, 
		@Description("fetching only the first certain number of nodes") Integer first, 
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return schedulerService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<Scheduler> getScheduler(@Id long id) {
		return schedulerService.findById(id);
	}

	public Uni<Datasource> datasource(@Source Scheduler scheduler) {
		return schedulerService.getDatasource(scheduler);
	}

	public Uni<DataIndex> oldDataIndex(@Source Scheduler scheduler) {
		return schedulerService.getOldDataIndex(scheduler);
	}

	public Uni<DataIndex> newDataIndex(@Source Scheduler scheduler) {
		return schedulerService.getNewDataIndex(scheduler);
	}

	@Inject
	SchedulerService schedulerService;

}