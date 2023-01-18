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
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.dto.DatasourceDTO;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class DatasourceGraphqlResource {

	@Query
	public Uni<Connection<Datasource>> getDatasources(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before, 
		@Description("fetching only the first certain number of nodes") Integer first, 
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return datasourceService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	public Uni<EnrichPipeline> enrichPipeline(
		@Source Datasource datasource) {
		return datasourceService.getEnrichPipeline(datasource.getId());
	}

	public Uni<DataIndex> dataIndex(@Source Datasource datasource) {
		return datasourceService.getDataIndex(datasource.getId());
	}

	public Uni<PluginDriver> pluginDriver(@Source Datasource datasource) {
		return datasourceService.getPluginDriver(datasource.getId());
	}

	@Query
	public Uni<Datasource> getDatasource(@Id long id) {
		return datasourceService.findById(id);
	}

	public Uni<Response<Datasource>> patchDatasource(@Id long id, DatasourceDTO datasourceDTO) {
		return datasourceService.getValidator().patch(id, datasourceDTO);
	}

	public Uni<Response<Datasource>> updateDatasource(@Id long id, DatasourceDTO datasourceDTO) {
		return datasourceService.getValidator().update(id, datasourceDTO);
	}

	public Uni<Response<Datasource>> createDatasource(DatasourceDTO datasourceDTO) {
		return datasourceService.getValidator().create(datasourceDTO);
	}

	@Mutation
	public Uni<Response<Datasource>> datasource(
		@Id Long id, DatasourceDTO datasourceDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createDatasource(datasourceDTO);
		} else {
			return patch
				? patchDatasource(id, datasourceDTO)
				: updateDatasource(id, datasourceDTO);
		}

	}

	@Mutation
	public Uni<Datasource> deleteDatasource(@Id long datasourceId) {
		return datasourceService.deleteById(datasourceId);
	}

	@Mutation
	public Uni<Tuple2<Datasource, DataIndex>> bindDataIndexToDatasource(
		@Id @Name("datasourceId") long datasourceId,
		@Id @Name("dataIndexId") long dataIndexId) {
		return datasourceService.setDataIndex(datasourceId, dataIndexId);
	}

	@Mutation
	public Uni<Datasource> unbindDataIndexFromDatasource(
		@Id @Name("datasourceId") long datasourceId) {
		return datasourceService.unsetDataIndex(datasourceId);
	}

	@Mutation
	public Uni<Tuple2<Datasource, EnrichPipeline>> bindEnrichPipelineToDatasource(
		@Id @Name("datasourceId") long datasourceId,
		@Id @Name("enrichPipelineId") long enrichPipelineId) {
		return datasourceService.setEnrichPipeline(datasourceId, enrichPipelineId);
	}

	@Mutation
	public Uni<Datasource> unbindEnrichPipelineToDatasource(
		@Id @Name("datasourceId") long datasourceId) {
		return datasourceService.unsetEnrichPipeline(datasourceId);
	}

	@Mutation
	public Uni<Tuple2<Datasource, PluginDriver>> bindPluginDriverToDatasource(
		@Id @Name("datasourceId") long datasourceId,
		@Id @Name("pluginDriverId") long pluginDriverId) {
		return datasourceService.setPluginDriver(datasourceId, pluginDriverId);
	}

	@Mutation
	public Uni<Datasource> unbindPluginDriverToDatasource(
		@Id @Name("datasourceId") long datasourceId) {
		return datasourceService.unsetPluginDriver(datasourceId);
	}

	@Mutation
	public Uni<Tuple2<Datasource,PluginDriver>> createDatasourceAndAddPluginDriver(
		DatasourceDTO datasourceDTO, @Id long id){

		return datasourceService.createDatasourceAndAddPluginDriver(
			datasourceDTO, id);
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