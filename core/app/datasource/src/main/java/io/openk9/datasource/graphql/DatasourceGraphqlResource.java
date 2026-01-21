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

import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.graphql.SortBy;
import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.web.Response;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.dto.base.DatasourceDTO;
import io.openk9.datasource.model.dto.request.CreateDatasourceDTO;
import io.openk9.datasource.model.dto.request.UpdateDatasourceDTO;
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

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class DatasourceGraphqlResource {

	@Inject
	DatasourceService datasourceService;

	@Mutation
	public Uni<Tuple2<Datasource, DataIndex>> bindDataIndexToDatasource(
		@Id @Name("datasourceId") long datasourceId,
		@Id @Name("dataIndexId") long dataIndexId) {
		return datasourceService.setDataIndex(datasourceId, dataIndexId);
	}

	@Mutation
	public Uni<Tuple2<Datasource, EnrichPipeline>> bindEnrichPipelineToDatasource(
		@Id @Name("datasourceId") long datasourceId,
		@Id @Name("enrichPipelineId") long enrichPipelineId) {
		return datasourceService.setEnrichPipeline(datasourceId, enrichPipelineId);
	}

	@Mutation
	public Uni<Tuple2<Datasource, PluginDriver>> bindPluginDriverToDatasource(
		@Id @Name("datasourceId") long datasourceId,
		@Id @Name("pluginDriverId") long pluginDriverId) {
		return datasourceService.setPluginDriver(datasourceId, pluginDriverId);
	}

	public Uni<Response<Datasource>> createDatasource(DatasourceDTO datasourceDTO) {
		return datasourceService.getValidator().create(datasourceDTO);
	}

	@Mutation
	public Uni<Tuple2<Datasource,PluginDriver>> createDatasourceAndAddPluginDriver(
		DatasourceDTO datasourceDTO, @Id long id){

		return datasourceService.createDatasourceAndAddPluginDriver(
			datasourceDTO, id);
	}

	@Mutation
	public Uni<Response<Datasource>> createDatasourceConnection(
		CreateDatasourceDTO datasourceConnection) {

		return datasourceService.createDatasourceConnection(datasourceConnection);
	}

	public Uni<DataIndex> dataIndex(@Source Datasource datasource) {
		return datasourceService.getDataIndex(datasource.getId());
	}

	public Uni<Connection<DataIndex>> dataIndexes(
		@Source Datasource datasource,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities")
		@DefaultValue("false") boolean notEqual) {

		return datasourceService.getDataIndexConnection(
			datasource.getId(), after, before, first, last, searchText, sortByList, notEqual);
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

	@Mutation
	public Uni<Datasource> deleteDatasource(@Id long datasourceId, String datasourceName) {
		return datasourceService.deleteById(datasourceId, datasourceName);
	}

	public Uni<EnrichPipeline> enrichPipeline(
		@Source Datasource datasource) {
		return datasourceService.getEnrichPipeline(datasource.getId());
	}

	@Query
	public Uni<Datasource> getDatasource(@Id long id) {
		return datasourceService.findById(id);
	}

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

	public Uni<Response<Datasource>> patchDatasource(@Id long id, DatasourceDTO datasourceDTO) {
		return datasourceService.getValidator().patch(id, datasourceDTO);
	}

	public Uni<PluginDriver> pluginDriver(@Source Datasource datasource) {
		return datasourceService.getPluginDriver(datasource.getId());
	}

	public Uni<Connection<Scheduler>> schedulers(
		@Source Datasource datasource,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities")
		@DefaultValue("false") boolean notEqual) {

		return datasourceService.getSchedulerConnection(
			datasource.getId(), after, before, first, last, searchText, sortByList, notEqual);
	}

	@Mutation
	public Uni<Datasource> unbindDataIndexFromDatasource(
		@Id @Name("datasourceId") long datasourceId) {
		return datasourceService.unsetDataIndex(datasourceId);
	}

	@Mutation
	public Uni<Datasource> unbindEnrichPipelineToDatasource(
		@Id @Name("datasourceId") long datasourceId) {
		return datasourceService.unsetEnrichPipeline(datasourceId);
	}

	@Mutation
	public Uni<Datasource> unbindPluginDriverToDatasource(
		@Id @Name("datasourceId") long datasourceId) {
		return datasourceService.unsetPluginDriver(datasourceId);
	}

	public Uni<Response<Datasource>> updateDatasource(@Id long id, DatasourceDTO datasourceDTO) {
		return datasourceService.getValidator().update(id, datasourceDTO);
	}

	@Mutation
	public Uni<Response<Datasource>> updateDatasourceConnection(
		UpdateDatasourceDTO datasourceConnection) {

		return datasourceService.updateDatasourceConnection(datasourceConnection);
	}

}