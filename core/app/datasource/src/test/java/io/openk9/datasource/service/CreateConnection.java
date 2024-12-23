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
import io.openk9.datasource.model.VectorIndex;
import io.openk9.datasource.model.dto.PluginDriverDTO;
import io.openk9.datasource.model.dto.VectorIndexDTO;
import io.openk9.datasource.model.init.PluginDrivers;
import io.openk9.datasource.plugindriver.WireMockPluginDriver;

import java.util.Set;

public class CreateConnection {
	public static final String DATASOURCE_NAME = "My New Connection";
	public static final String DATASOURCE_DESCRIPTION = "A new datasource connection";
	public static final int REINDEX_RATE = 1;
	public static final boolean REINDEXABLE = false;
	public static final String REINDEXING = "0 0 * ? * * *";
	public static final boolean SCHEDULABLE = true;
	public static final String DATASOURCE_JSON_CONFIG = "{}";
	public static final String SCHEDULING = "0 0 * ? * * *";
	public static final PluginDriverDTO PLUGIN_DRIVER_DTO =
		PluginDrivers.getPluginDriverDTO(Preset.CRAWLER);
	public static final String PIPELINE_NAME = "Connection Pipeline";
	public static final long FIRST_ITEM_ID = 34L;
	public static final long SECOND_ITEM_ID = 65L;
	public static final PipelineWithItemsDTO PIPELINE_WITH_ITEMS_DTO =
		PipelineWithItemsDTO.builder()
			.name(PIPELINE_NAME + " with items")
			.item(PipelineWithItemsDTO.ItemDTO.builder()
				.enrichItemId(FIRST_ITEM_ID)
				.weight(13)
				.build()
			)
			.item(PipelineWithItemsDTO.ItemDTO.builder()
				.enrichItemId(SECOND_ITEM_ID)
				.weight(54)
				.build()
			)
			.build();
	public static final long PLUGIN_DRIVER_ID = 100L;
	public static final long PIPELINE_ID = 10L;
	public static final long DATASOURCE_ID = Long.MAX_VALUE;
	public static final long DATA_INDEX_ID = 1111L;
	public static final long VECTOR_INDEX_ID = 2199L;

	public static final DatasourceConnectionDTO NEW_ENTITIES_VECTOR_DTO =
		DatasourceConnectionDTO.builder()
			.name(DATASOURCE_NAME + " new entities vector dto")
			.description(DATASOURCE_DESCRIPTION)
			.reindexRate(REINDEX_RATE)
			.reindexing(REINDEXING)
			.reindexable(REINDEXABLE)
			.schedulable(SCHEDULABLE)
			.jsonConfig(DATASOURCE_JSON_CONFIG)
			.scheduling(SCHEDULING)
			.pluginDriver(PLUGIN_DRIVER_DTO)
			.pipeline(PIPELINE_WITH_ITEMS_DTO)
			.vectorIndexConfigurations(VectorIndexDTO
				.ConfigurationsDTO.builder()
				.chunkType(VectorIndex.ChunkType.DEFAULT)
				.textEmbeddingField("$.rawContent")
				.titleField("$.title")
				.urlField("$.url")
				.build()
			)
			.build();

	public static final DatasourceConnectionDTO NEW_ENTITIES_BASE_DTO =
		DatasourceConnectionDTO.builder()
			.name(DATASOURCE_NAME + "new entities base dto")
		.description(DATASOURCE_DESCRIPTION)
		.reindexRate(REINDEX_RATE)
		.reindexing(REINDEXING)
		.reindexable(REINDEXABLE)
		.schedulable(SCHEDULABLE)
		.jsonConfig(DATASOURCE_JSON_CONFIG)
		.scheduling(SCHEDULING)
		.pluginDriver(PLUGIN_DRIVER_DTO)
		.pipeline(PIPELINE_WITH_ITEMS_DTO)
		.build();

	public static final DatasourceConnectionDTO PRE_EXIST_PLUGIN_NEW_PIPELINE_DTO =
		DatasourceConnectionDTO.builder()
			.name(DATASOURCE_NAME + "pre exist plugin new pipeline dto")
			.description(DATASOURCE_DESCRIPTION)
			.reindexRate(REINDEX_RATE)
			.reindexing(REINDEXING)
			.reindexable(REINDEXABLE)
			.schedulable(SCHEDULABLE)
			.jsonConfig(DATASOURCE_JSON_CONFIG)
			.scheduling(SCHEDULING)
			.pluginDriverId(PLUGIN_DRIVER_ID)
			.pipeline(PIPELINE_WITH_ITEMS_DTO)
			.build();

	public static final DatasourceConnectionDTO AMBIGUOUS_DTO = DatasourceConnectionDTO.builder()
		.name(DATASOURCE_NAME + " ambiguous dto")
		.description(DATASOURCE_DESCRIPTION)
		.reindexRate(REINDEX_RATE)
		.reindexing(REINDEXING)
		.reindexable(REINDEXABLE)
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
			.name(DATASOURCE_NAME + "new plugin preexist pipeline dto")
			.description(DATASOURCE_DESCRIPTION)
			.reindexRate(REINDEX_RATE)
			.reindexing(REINDEXING)
			.reindexable(REINDEXABLE)
			.schedulable(SCHEDULABLE)
			.jsonConfig(DATASOURCE_JSON_CONFIG)
			.scheduling(SCHEDULING)
			.pluginDriver(PLUGIN_DRIVER_DTO)
			.pipelineId(PIPELINE_ID)
			.build();

	public static final DatasourceConnectionDTO NEW_PLUGIN_NO_PIPELINE_DTO =
		DatasourceConnectionDTO.builder()
			.name(DATASOURCE_NAME + "new plugin no pipeline dto")
			.description(DATASOURCE_DESCRIPTION)
			.reindexRate(REINDEX_RATE)
			.reindexing(REINDEXING)
			.reindexable(REINDEXABLE)
			.schedulable(SCHEDULABLE)
			.jsonConfig(DATASOURCE_JSON_CONFIG)
			.scheduling(SCHEDULING)
			.pluginDriver(PLUGIN_DRIVER_DTO)
			.build();

	public static final DatasourceConnectionDTO NO_PLUGIN_NO_PIPELINE_DTO =
		DatasourceConnectionDTO.builder()
			.name(DATASOURCE_NAME + "no plugin no pipeline dto")
			.description(DATASOURCE_DESCRIPTION)
			.reindexRate(REINDEX_RATE)
			.reindexing(REINDEXING)
			.reindexable(REINDEXABLE)
			.schedulable(SCHEDULABLE)
			.jsonConfig(DATASOURCE_JSON_CONFIG)
			.scheduling(SCHEDULING)
			.build();

	public static PluginDriver PLUGIN_DRIVER;
	public static EnrichPipeline PIPELINE;
	public static Datasource DATASOURCE;
	public static DataIndex DATAINDEX;
	public static VectorIndex VECTORINDEX;

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
		datasource.setName(NEW_ENTITIES_BASE_DTO.getName());
		datasource.setPluginDriver(PLUGIN_DRIVER);
		datasource.setEnrichPipeline(PIPELINE);
		DATASOURCE = datasource;

		var dataIndex = new DataIndex();
		dataIndex.setId(DATA_INDEX_ID);
		dataIndex.setName(DATASOURCE_NAME + "-data-index");
		dataIndex.setDatasource(DATASOURCE);
		DATAINDEX = dataIndex;

		var vectorIndex = new VectorIndex();
		vectorIndex.setId(VECTOR_INDEX_ID);
		vectorIndex.setName(DATASOURCE_NAME + "-vector-index");
		vectorIndex.setDataIndex(dataIndex);
		vectorIndex.setJsonConfig("{}");
		vectorIndex.setTextEmbeddingField("$.rawContent");
		vectorIndex.setTitleField("$.title");
		vectorIndex.setUrlField("$.url");
		vectorIndex.setChunkType(VectorIndex.ChunkType.DEFAULT);
		VECTORINDEX = vectorIndex;

	}

}
