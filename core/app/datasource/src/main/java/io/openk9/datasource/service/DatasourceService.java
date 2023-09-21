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

package io.openk9.datasource.service;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.mapper.DatasourceMapper;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.dto.DatasourceDTO;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class DatasourceService extends BaseK9EntityService<Datasource, DatasourceDTO> {
	 DatasourceService(DatasourceMapper mapper) {
		 this.mapper = mapper;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {Datasource_.NAME, Datasource_.DESCRIPTION};
	}

	public Uni<DataIndex> getDataIndex(Datasource datasource) {
		return withTransaction(
			s -> s.fetch(datasource.getDataIndex()));
	}

	public Uni<Set<DataIndex>> getDataIndexes(Datasource datasource) {
		return withTransaction(
			s -> s.fetch(datasource.getDataIndexes()));
	}

	public Uni<DataIndex> getDataIndex(long datasourceId) {
		return findById(datasourceId).flatMap(this::getDataIndex);
	}


	public Uni<List<DataIndex>> getDataIndexes(long datasourceId) {
		return withTransaction(
			() -> findById(datasourceId).flatMap(this::getDataIndexes).map(ArrayList::new)
		);
	}

	public Uni<Connection<DataIndex>> getDataIndexConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {
		return findJoinConnection(
			id, Datasource_.DATA_INDEXES, DataIndex.class,
			dataIndexService.getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual);
	}

	public Uni<Connection<Scheduler>> getSchedulerConnection(
		Long id, String after, String before, Integer first, Integer last, String searchText,
		Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			id, Datasource_.SCHEDULERS, Scheduler.class,
			schedulerService.getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual);
	}

	public Uni<EnrichPipeline> getEnrichPipeline(Datasource datasource) {
		return withTransaction(
			s -> Mutiny2.fetch(s, datasource.getEnrichPipeline()));
	}

	public Uni<EnrichPipeline> getEnrichPipeline(long datasourceId) {
		return withTransaction(
			() -> findById(datasourceId).flatMap(this::getEnrichPipeline));
	}

	public Uni<Tuple2<Datasource, DataIndex>> setDataIndex(long datasourceId, long dataIndexId) {
		return withTransaction(() -> findById(datasourceId)
			.onItem()
			.ifNotNull()
			.transformToUni(datasource -> dataIndexService.findById(dataIndexId)
				.onItem()
				.ifNotNull()
				.transformToUni(dataIndex -> {
					datasource.setDataIndex(dataIndex);
					return persist(datasource)
						.map(d -> Tuple2.of(d, dataIndex));
				})));
	}

	public Uni<Datasource> unsetDataIndex(long datasourceId) {
		return withTransaction(() -> findById(datasourceId)
			.onItem()
			.ifNotNull()
			.transformToUni(datasource -> {
				datasource.setDataIndex(null);
				return persist(datasource);
			}));
	}

	public Uni<Tuple2<Datasource, EnrichPipeline>> setEnrichPipeline(long datasourceId, long enrichPipelineId) {
		return withTransaction(() -> findById(datasourceId)
			.onItem()
			.ifNotNull()
			.transformToUni(datasource -> enrichPipelineService.findById(enrichPipelineId)
				.onItem()
				.ifNotNull()
				.transformToUni(enrichPipeline -> {
					datasource.setEnrichPipeline(enrichPipeline);
					return persist(datasource)
						.map(d -> Tuple2.of(d, enrichPipeline));
				})));
	}

	public Uni<Datasource> unsetEnrichPipeline(long datasourceId) {
		return withTransaction(() -> findById(datasourceId)
			.onItem()
			.ifNotNull()
			.transformToUni(datasource -> {
				datasource.setEnrichPipeline(null);
				return persist(datasource);
			}));
	}

	public Uni<PluginDriver> getPluginDriver(long datasourceId) {
		return findById(datasourceId).map(Datasource::getPluginDriver);
	}

	public Uni<Tuple2<Datasource, PluginDriver>> setPluginDriver(long datasourceId, long pluginDriverId) {
		return withTransaction(() -> findById(datasourceId)
			.onItem()
			.ifNotNull()
			.transformToUni(datasource -> pluginDriverService.findById(pluginDriverId)
				.flatMap(pluginDriver -> {
					datasource.setPluginDriver(pluginDriver);
					return persist(datasource)
						.map(d -> Tuple2.of(d, pluginDriver));
				})));
	}

	public Uni<Datasource> unsetPluginDriver(long datasourceId) {
		return withTransaction(() -> findById(datasourceId)
			.onItem()
			.ifNotNull()
			.transformToUni(datasource -> {
				datasource.setPluginDriver(null);
				return persist(datasource);
			}));
	}

	public Uni<Tuple2<Datasource, PluginDriver>> createDatasourceAndAddPluginDriver(
		DatasourceDTO datasourceDTO, long pluginDriverId) {
		 return withTransaction(() -> pluginDriverService.findById(pluginDriverId)
			 .onItem()
			 .ifNotNull()
			 .transformToUni(pluginDriver-> {
				 Datasource dataSource = mapper.create(datasourceDTO);
				 dataSource.setPluginDriver(pluginDriver);
				 return persist(dataSource).map(d -> Tuple2.of(d, pluginDriver));
			 }));
	}

	public Uni<Datasource> findDatasourceByIdWithPluginDriver(long datasourceId) {
		return withTransaction(
			(s) -> s.createQuery(
				"select d " +
				"from Datasource d " +
				"left join fetch d.pluginDriver where d.id = :id", Datasource.class)
			.setParameter("id", datasourceId)
			.getSingleResult()
		);
	}

	public Uni<List<DataIndex>> getDataIndexOrphans(long datasourceId) {
		return withTransaction((s) -> s.createQuery(
			"select di " +
				"from DataIndex di " +
				"inner join di.datasource d on di.datasource = d and d.dataIndex <> di " +
				"where d.id = :id", DataIndex.class)
			.setParameter("id", datasourceId)
			.getResultList()
		);
	}

	@Inject
	DataIndexService dataIndexService;

	@Inject
	EnrichPipelineService enrichPipelineService;

	@Inject
	PluginDriverService pluginDriverService;

	@Inject
	SchedulerService schedulerService;

	@Override
	public Class<Datasource> getEntityClass() {
		return Datasource.class;
	}

}
