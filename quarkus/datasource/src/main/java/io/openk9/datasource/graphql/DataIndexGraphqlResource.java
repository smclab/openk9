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

import io.openk9.datasource.graphql.util.SortType;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.dto.DataIndexDTO;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.service.DataIndexService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class DataIndexGraphqlResource {

	@Query
	public Uni<Page<DataIndex>> getDataIndices(
		@Name("limit") @DefaultValue("20") int limit,
		@Name("offset") @DefaultValue("0") int offset,
		@Name("sortBy") @DefaultValue("createDate") String sortBy,
		@Name("sortType") @DefaultValue("ASC") SortType sortType) {
		return dataIndexService.findAllPaginated(limit, offset, sortBy, sortType);
	}

	@Query
	public Uni<DataIndex> getDataIndex(long id) {
		return dataIndexService.findById(id);
	}

	@Mutation
	public Uni<DataIndex> patchDataIndex(long id, DataIndexDTO dataIndexDTO) {
		return dataIndexService.patch(id, dataIndexDTO);
	}

	@Mutation
	public Uni<DataIndex> updateDataIndex(long id, DataIndexDTO dataIndexDTO) {
		return dataIndexService.update(id, dataIndexDTO);
	}

	@Mutation
	public Uni<DataIndex> createDataIndex(DataIndexDTO dataIndexDTO) {
		return dataIndexService.persist(dataIndexDTO);
	}

	@Mutation
	public Uni<DataIndex> deleteDataIndex(long dataIndexId) {
		return dataIndexService.deleteById(dataIndexId);
	}

	@Subscription
	public Multi<DataIndex> dataIndexCreated() {
		return dataIndexService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<DataIndex> dataIndexDeleted() {
		return dataIndexService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<DataIndex> dataIndexUpdated() {
		return dataIndexService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	DataIndexService dataIndexService;

}