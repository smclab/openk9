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
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.service.DatasourceService;
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
import java.util.List;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class DatasourceGraphqlResource {

	@Query
	public Uni<List<Datasource>> getDatasources(
		@Name("limit") @DefaultValue("20") int limit,
		@Name("offset") @DefaultValue("0") int offset,
		@Name("sortBy") @DefaultValue("createDate") String sortBy,
		@Name("sortType") @DefaultValue("ASC") SortType sortType) {
		return datasourceService.findAll(limit, offset, sortBy, sortType);
	}

	@Query
	public Uni<Datasource> getDatasource(long id) {
		return datasourceService.findById(id);
	}

	@Mutation
	public Uni<Datasource> patchDatasource(Datasource datasource) {
		return datasourceService.patch(datasource);
	}

	@Mutation
	public Uni<Datasource> updateDatasource(Datasource datasource) {
		return datasourceService.update(datasource);
	}

	@Mutation
	public Uni<Datasource> createDatasource(Datasource datasource) {
		return datasourceService.persist(datasource);
	}

	@Mutation
	public Uni<Datasource> deleteDatasource(int datasourceId) {
		return datasourceService.deleteById(datasourceId);
	}

	@Subscription
	public Multi<Datasource> datasourceCreated() {
		return datasourceService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Datasource> datasourceDeleted() {
		return datasourceService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Datasource> datasourceUpdated() {
		return datasourceService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	DatasourceService datasourceService;

}