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

import io.openk9.datasource.graphql.dto.DatasourceConnectionDTO;
import io.openk9.datasource.graphql.dto.PipelineWithItemsDTO;
import io.openk9.datasource.grpc.Preset;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.dto.PluginDriverDTO;
import io.openk9.datasource.model.init.PluginDrivers;
import io.openk9.datasource.plugindriver.WireMockPluginDriver;

import java.util.Set;

public class CreateConnection {
	public static final String DATASOURCE_NAME = "My New Connection";
	public static final String DATASOURCE_DESCRIPTION = "A new datasource connection";
	public static final int REINDEX_RATE = 1;
	public static final boolean SCHEDULABLE = true;
	public static final String DATASOURCE_JSON_CONFIG = "{}";
	public static final String SCHEDULING = "0 0 * ? * * *";
	public static final PluginDriverDTO PLUGIN_DRIVER_DTO =
		PluginDrivers.getPluginDriverDTO(Preset.CRAWLER);
	public static final PipelineWithItemsDTO PIPELINE_WITH_ITEMS_DTO =
		PipelineWithItemsDTO.builder()
			.item(PipelineWithItemsDTO.ItemDTO.builder()
				.enrichItemId(34L)
				.weight(13)
				.build()
			)
			.item(PipelineWithItemsDTO.ItemDTO.builder()
				.enrichItemId(65L)
				.weight(54)
				.build()
			)
			.build();
	public static final long PLUGIN_DRIVER_ID = 100L;
	public static final long PIPELINE_ID = 10L;
	public static final long DATASOURCE_ID = 12312L;
	public static final long DATA_INDEX_ID = 1111L;

	public static final DatasourceConnectionDTO NEW_ENTITIES_DTO = DatasourceConnectionDTO.builder()
		.name(DATASOURCE_NAME)
		.description(DATASOURCE_DESCRIPTION)
		.reindexRate(REINDEX_RATE)
		.schedulable(SCHEDULABLE)
		.jsonConfig(DATASOURCE_JSON_CONFIG)
		.scheduling(SCHEDULING)
		.pluginDriver(PLUGIN_DRIVER_DTO)
		.pipeline(PIPELINE_WITH_ITEMS_DTO)
		.build();
	public static final DatasourceConnectionDTO PRE_EXIST_PLUGIN_NEW_PIPELINE_DTO =
		DatasourceConnectionDTO.builder()
			.name(DATASOURCE_NAME)
			.description(DATASOURCE_DESCRIPTION)
			.reindexRate(REINDEX_RATE)
			.schedulable(SCHEDULABLE)
			.jsonConfig(DATASOURCE_JSON_CONFIG)
			.scheduling(SCHEDULING)
			.pluginDriverId(PLUGIN_DRIVER_ID)
			.pipeline(PIPELINE_WITH_ITEMS_DTO)
			.build();
	public static final DatasourceConnectionDTO AMBIGUOUS_DTO = DatasourceConnectionDTO.builder()
		.name(DATASOURCE_NAME)
		.description(DATASOURCE_DESCRIPTION)
		.reindexRate(REINDEX_RATE)
		.schedulable(SCHEDULABLE)
		.jsonConfig(DATASOURCE_JSON_CONFIG)
		.scheduling(SCHEDULING)
		.pluginDriverId(PLUGIN_DRIVER_ID)
		.pluginDriver(PLUGIN_DRIVER_DTO)
		.pipelineId(PIPELINE_ID)
		.pipeline(PIPELINE_WITH_ITEMS_DTO)
		.build();
	public static final DatasourceConnectionDTO NEW_PLUGIN_PRE_EXIST_PIPELINE_DTO =
		DatasourceConnectionDTO.builder()
			.name(DATASOURCE_NAME)
			.description(DATASOURCE_DESCRIPTION)
			.reindexRate(REINDEX_RATE)
			.schedulable(SCHEDULABLE)
			.jsonConfig(DATASOURCE_JSON_CONFIG)
			.scheduling(SCHEDULING)
			.pluginDriver(PLUGIN_DRIVER_DTO)
			.pipelineId(PIPELINE_ID)
			.build();

	public static final DatasourceConnectionDTO NEW_PLUGIN_NO_PIPELINE_DTO =
		DatasourceConnectionDTO.builder()
			.name(DATASOURCE_NAME)
			.description(DATASOURCE_DESCRIPTION)
			.reindexRate(REINDEX_RATE)
			.schedulable(SCHEDULABLE)
			.jsonConfig(DATASOURCE_JSON_CONFIG)
			.scheduling(SCHEDULING)
			.pluginDriver(PLUGIN_DRIVER_DTO)
			.build();

	public static final DatasourceConnectionDTO NO_PLUGIN_NO_PIPELINE_DTO =
		DatasourceConnectionDTO.builder()
			.name(DATASOURCE_NAME)
			.description(DATASOURCE_DESCRIPTION)
			.reindexRate(REINDEX_RATE)
			.schedulable(SCHEDULABLE)
			.jsonConfig(DATASOURCE_JSON_CONFIG)
			.scheduling(SCHEDULING)
			.build();
	public static PluginDriver PLUGIN_DRIVER;
	public static EnrichPipeline PIPELINE;
	public static Datasource DATASOURCE;
	public static DataIndex DATAINDEX;

	static {
		var pluginDriver = new PluginDriver();
		pluginDriver.setId(PLUGIN_DRIVER_ID);
		pluginDriver.setName(PLUGIN_DRIVER_DTO.getName());
		pluginDriver.setJsonConfig(String
			.format(
				"{\"host\": \"%s\", \"port\": \"%s\", \"secure\": \"%s\"}",
				WireMockPluginDriver.HOST,
				WireMockPluginDriver.PORT,
				false
			)
		);
		PLUGIN_DRIVER = pluginDriver;

		var enrichPipeline = new EnrichPipeline();
		enrichPipeline.setId(PIPELINE_ID);
		enrichPipeline.setName(PIPELINE_WITH_ITEMS_DTO.getName());
		enrichPipeline.setEnrichPipelineItems(Set.of());
		PIPELINE = enrichPipeline;

		var datasource = new Datasource();
		datasource.setId(DATASOURCE_ID);
		datasource.setName(NEW_ENTITIES_DTO.getName());
		datasource.setPluginDriver(PLUGIN_DRIVER);
		datasource.setEnrichPipeline(PIPELINE);
		DATASOURCE = datasource;

		var dataIndex = new DataIndex();
		dataIndex.setId(DATA_INDEX_ID);
		dataIndex.setName(DATASOURCE_NAME + " DataIndex");
		dataIndex.setDatasource(DATASOURCE);
		DATAINDEX = dataIndex;
	}

}
