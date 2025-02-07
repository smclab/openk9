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
import io.openk9.datasource.graphql.dto.DatasourceConnectionDTO.DatasourceConnectionDTOBuilder;
import io.openk9.datasource.graphql.dto.PipelineWithItemsDTO;
import io.openk9.datasource.grpc.Preset;
import io.openk9.datasource.model.dto.PluginDriverDTO.PluginDriverDTOBuilder;
import io.openk9.datasource.model.init.PluginDrivers;
import io.openk9.datasource.plugindriver.WireMockPluginDriver;

import io.vertx.core.json.JsonObject;

public class DatasourceConnectionObjects {

	public static final String DESCRIPTION = "A new datasource connection";
	public static final String JSON_CONFIG = "{}";
	public static final boolean REINDEXABLE = false;
	public static final String REINDEXING = "0 0 * ? * * *";
	public static final boolean SCHEDULABLE = true;
	public static final String SCHEDULING = "0 0 * ? * * *";

	public static final DatasourceConnectionDTOBuilder<?, ?>
		NEW_PLUGIN_PRE_EXIST_PIPELINE_DTO_BUILDER =
		DATASOURCE_CONNECTION_DTO_BUILDER()
			.name("NEW_PLUGIN_PRE_EXIST_PIPELINE_DTO")
			.pluginDriver(PLUGIN_DRIVER_DTO_BUILDER()
				.name("NEW_PLUGIN_PRE_EXIST_PIPELINE_PLUGIN")
				.build());
	public static final DatasourceConnectionDTO NEW_PLUGIN_NO_PIPELINE_DTO =
		DATASOURCE_CONNECTION_DTO_BUILDER()
			.name("NEW_PLUGIN_NO_PIPELINE_DATASOURCE")
			.pluginDriver(PLUGIN_DRIVER_DTO_BUILDER()
				.name("NEW_PLUGIN_NO_PIPELINE_PLUGIN")
				.build())
			.build();
	public static final DatasourceConnectionDTO NO_PLUGIN_NO_PIPELINE_DTO =
		DATASOURCE_CONNECTION_DTO_BUILDER()
			.name("NO_PLUGIN_NO_PIPELINE_DATASOURCE")
			.build();
	public static final DatasourceConnectionDTO NEW_ENTITIES_VECTOR_DTO =
		DATASOURCE_CONNECTION_DTO_BUILDER()
			.name("NEW_ENTITIES_VECTOR_DATASOURCE")
			.pluginDriver(PLUGIN_DRIVER_DTO_BUILDER()
				.name("NEW_ENTITIES_VECTOR_PLUGIN")
				.build())
			.pipeline(PipelineWithItemsDTO.builder()
				.name("NEW_ENTITIES_VECTOR_PIPELINE")
				.build())
			.build();
	public static final DatasourceConnectionDTOBuilder<?, ?> NEW_ENTITIES_BASE_DTO_BUILDER =
		DATASOURCE_CONNECTION_DTO_BUILDER()
			.name("NEW_ENTITIES_BASE_DTO")
			.pluginDriver(PLUGIN_DRIVER_DTO_BUILDER()
				.name("NEW_ENTITIES_BASE_PLUGIN")
				.build());
	public static final DatasourceConnectionDTOBuilder<?, ?>
		PRE_EXIST_PLUGIN_NEW_PIPELINE_DTO_BUILDER =
		DATASOURCE_CONNECTION_DTO_BUILDER()
			.name("PRE_EXIST_PLUGIN_NEW_PIPELINE_DATASOURCE");
	public static final DatasourceConnectionDTO AMBIGUOUS_DTO =
		DatasourceConnectionDTO.builder()
			.name("AMBIGUOUS_DATASOURCE")
			.description(DESCRIPTION)
			.reindexing(REINDEXING)
			.reindexable(REINDEXABLE)
			.schedulable(SCHEDULABLE)
			.jsonConfig(JSON_CONFIG)
			.scheduling(SCHEDULING)
			.pluginDriverId(Long.MAX_VALUE)
			.pluginDriver(PLUGIN_DRIVER_DTO_BUILDER()
				.name("AMBIGUOUS_PLUGIN")
				.build())
			.pipelineId(Long.MAX_VALUE)
			.pipeline(PipelineWithItemsDTO.builder()
				.name("AMBIGUOUS_PIPELINE")
				.build())
			.build();

	public static DatasourceConnectionDTOBuilder<?, ?> DATASOURCE_CONNECTION_DTO_BUILDER() {
		return DatasourceConnectionDTO.builder()
			.description(DESCRIPTION)
			.reindexable(REINDEXABLE)
			.reindexing(REINDEXING)
			.schedulable(SCHEDULABLE)
			.scheduling(SCHEDULING)
			.jsonConfig(JSON_CONFIG);
	}

	public static PluginDriverDTOBuilder<?, ?> PLUGIN_DRIVER_DTO_BUILDER() {
		return PluginDrivers.getPluginDriverDTO(Preset.CRAWLER)
			.toBuilder()
			.jsonConfig(JsonObject.of(
				"host", WireMockPluginDriver.HOST,
				"port", WireMockPluginDriver.PORT,
				"secure", false
			).encode());
	}


}
