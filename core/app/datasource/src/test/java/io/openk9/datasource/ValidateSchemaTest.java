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
import io.openk9.datasource.plugindriver.WireMockPluginDriver;
import io.openk9.datasource.service.CreateConnection;
import io.openk9.datasource.service.DatasourceService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ValidateSchemaTest {

	@Inject
	DatasourceService datasourceService;

	@Test
	@RunOnVertxContext
	void validate(UniAsserter asserter) {
		asserter.assertThat(
			() -> datasourceService.createDatasourceConnection(
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
						.build())
					.build()
			),
			datasourceResponse -> Assertions.assertTrue(datasourceResponse.getEntity().getId() > 0L)
		);
	}

}
