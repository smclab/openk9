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

import io.openk9.datasource.mapper.DatasourceMapper;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.dto.DatasourceDTO;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DatasourceService extends BaseK9EntityService<Datasource, DatasourceDTO> {
	 DatasourceService(DatasourceMapper mapper) {
		 this.mapper = mapper;
	}

	public Uni<DataIndex> getDataIndex(Datasource datasource) {
		return withTransaction(
			s -> s.fetch(datasource.getDataIndex()));
	}

	public Uni<DataIndex> getDataIndex(long datasourceId) {
		return withTransaction(
			() -> findById(datasourceId).flatMap(this::getDataIndex));
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

	@Inject
	DataIndexService dataIndexService;

	@Inject
	EnrichPipelineService enrichPipelineService;

	@Inject
	PluginDriverService pluginDriverService;

	@Override
	public Class<Datasource> getEntityClass() {
		return Datasource.class;
	}

}
