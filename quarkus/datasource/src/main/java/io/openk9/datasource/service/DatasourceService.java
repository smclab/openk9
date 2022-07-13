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
import io.openk9.datasource.model.EntityIndex;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.dto.DatasourceDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DatasourceService extends BaseK9EntityService<Datasource, DatasourceDTO> {
	 DatasourceService(DatasourceMapper mapper) {
		 this.mapper = mapper;
	}

	public Uni<EntityIndex> getEntityIndex(long datasourceId) {
		return findById(datasourceId).map(Datasource::getEntityIndex);
	}

	public Uni<DataIndex> getDataIndex(long datasourceId) {
		return findById(datasourceId).map(Datasource::getDataIndex);
	}

	public Uni<EnrichPipeline> getEnrichPipeline(long datasourceId) {
		return findById(datasourceId).map(Datasource::getEnrichPipeline);
	}

	public Uni<Datasource> setEntityIndex(long datasourceId, long entityIndexId) {
		return findById(datasourceId)
			.flatMap(datasource -> entityIndexService.findById(entityIndexId)
				.flatMap(entityIndex -> {
					datasource.setEntityIndex(entityIndex);
					return persist(datasource);
				}));
	}

	public Uni<Datasource> unsetEntityIndex(long datasourceId) {
		return findById(datasourceId)
			.flatMap(datasource -> {
				datasource.setEntityIndex(null);
				return persist(datasource);
			});
	}

	public Uni<Datasource> setDataIndex(long datasourceId, long dataIndexId) {
		return findById(datasourceId)
			.flatMap(datasource -> dataIndexService.findById(dataIndexId)
				.flatMap(dataIndex -> {
					datasource.setDataIndex(dataIndex);
					return persist(datasource);
				}));
	}

	public Uni<Datasource> unsetDataIndex(long datasourceId) {
		return findById(datasourceId)
			.flatMap(datasource -> {
				datasource.setDataIndex(null);
				return persist(datasource);
			});
	}

	public Uni<Datasource> setEnrichPipeline(long datasourceId, long enrichPipelineId) {
		return findById(datasourceId)
			.flatMap(datasource -> enrichPipelineService.findById(enrichPipelineId)
				.flatMap(enrichPipeline -> {
					datasource.setEnrichPipeline(enrichPipeline);
					return persist(datasource);
				}));
	}

	public Uni<Datasource> unsetEnrichPipeline(long datasourceId) {
		return findById(datasourceId)
			.flatMap(datasource -> {
				datasource.setEnrichPipeline(null);
				return persist(datasource);
			});
	}

	public Uni<PluginDriver> getPluginDriver(long datasourceId) {
		return findById(datasourceId).map(Datasource::getPluginDriver);
	}

	public Uni<Datasource> setPluginDriver(long datasourceId, long pluginDriverId) {
		return findById(datasourceId)
			.flatMap(datasource -> pluginDriverService.findById(pluginDriverId)
				.flatMap(pluginDriver -> {
					datasource.setPluginDriver(pluginDriver);
					return persist(datasource);
				}));
	}

	public Uni<Datasource> unsetPluginDriver(long datasourceId) {
		return findById(datasourceId)
			.flatMap(datasource -> {
				datasource.setPluginDriver(null);
				return persist(datasource);
			});
	}

	@Inject
	EntityIndexService entityIndexService;

	@Inject
	DataIndexService dataIndexService;

	@Inject
	EnrichPipelineService enrichPipelineService;

	@Inject
	PluginDriverService pluginDriverService;


}
