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

import io.openk9.datasource.grpc.Preset;
import io.openk9.datasource.model.ResourceUri;
import io.openk9.datasource.model.dto.base.PluginDriverDTO.PluginDriverDTOBuilder;
import io.openk9.datasource.model.dto.request.CreateDatasourceDTO;
import io.openk9.datasource.model.dto.request.PipelineWithItemsDTO;
import io.openk9.datasource.model.init.PluginDrivers;
import io.openk9.datasource.plugindriver.WireMockPluginDriver;

public class DatasourceConnectionObjects {

	public static final String DESCRIPTION = "A new datasource connection";
	public static final String JSON_CONFIG = "{}";
	public static final boolean REINDEXABLE = false;
	public static final String REINDEXING = "0 0 * ? * * *";
	public static final boolean SCHEDULABLE = true;
	public static final String SCHEDULING = "0 0 * ? * * *";

	public static final CreateDatasourceDTO AMBIGUOUS_DTO =
		DATASOURCE_CONNECTION_DTO_BUILDER()
			.name("AMBIGUOUS_DATASOURCE")
			.description(DESCRIPTION)
			.pluginDriverId(Long.MAX_VALUE)
			.pipelineId(Long.MAX_VALUE)
			.pipeline(PipelineWithItemsDTO.builder()
				.name("AMBIGUOUS_PIPELINE")
				.build())
			.build();

	public static CreateDatasourceDTO.CreateDatasourceDTOBuilder<?, ?> DATASOURCE_CONNECTION_DTO_BUILDER() {
		return CreateDatasourceDTO.builder()
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
			.resourceUri(ResourceUri.builder()
				.baseUri(WireMockPluginDriver.HOST + ":" + WireMockPluginDriver.PORT)
				.path("/test")
				.build());
	}


}
