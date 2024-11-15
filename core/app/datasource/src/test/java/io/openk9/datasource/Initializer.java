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

package io.openk9.datasource;

import io.openk9.datasource.graphql.dto.DatasourceConnectionDTO;
import io.openk9.datasource.graphql.dto.PipelineWithItemsDTO;
import io.openk9.datasource.plugindriver.WireMockPluginDriver;
import io.openk9.datasource.service.CreateConnection;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.EnrichItemService;
import io.openk9.datasource.service.TenantInitializerService;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class Initializer {

	private static final Logger log = Logger.getLogger(Initializer.class);

	@Inject
	TenantInitializerService initializerService;

	@Inject
	DatasourceService datasourceService;

	@Inject
	EnrichItemService enrichItemService;

	public void initDb(@Observes Startup startup) {

		log.info("Init public tenant with default data.");

		var bucketId = initializerService.createDefault("public")
			.await().indefinitely();

		log.infof("New tenant initialized with id %s.", bucketId);

		log.info("Create a new Connection.");

		datasourceService.createDatasourceConnection(
			DatasourceConnectionDTO.builder()
				.name(CreateConnection.DATASOURCE_NAME)
				.description(CreateConnection.DATASOURCE_DESCRIPTION)
				.jsonConfig(CreateConnection.DATASOURCE_JSON_CONFIG)
				.scheduling(CreateConnection.SCHEDULING)
				.pluginDriver(CreateConnection.PLUGIN_DRIVER_DTO
					.toBuilder()
					.jsonConfig(JsonObject.of(
						"host", WireMockPluginDriver.HOST,
						"port", WireMockPluginDriver.PORT,
						"secure", false
					).encode())
					.build()
				)
				.pipeline(PipelineWithItemsDTO.builder()
					.name(CreateConnection.PIPELINE_NAME)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(2L)
						.weight(1)
						.build())
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(3L)
						.weight(2)
						.build())
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(4L)
						.weight(3)
						.build())
					.build()
				)
				.build()
		).await().indefinitely();

	}

}
