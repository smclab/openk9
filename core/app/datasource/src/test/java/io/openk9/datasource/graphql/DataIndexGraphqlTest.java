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

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutionException;
import jakarta.inject.Inject;

import io.openk9.datasource.model.dto.DataIndexDTO;
import io.openk9.datasource.plugindriver.WireMockPluginDriver;
import io.openk9.datasource.service.DataIndexService;
import io.openk9.datasource.service.DatasourceConnectionObjects;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.PluginDriverService;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DataIndexGraphqlTest {

	private static final String DATA_INDEX_DATASOURCE_GRAPHQL_TEST =
		"DataIndexDatasourceGraphqlTest";
	private static final String DATA_INDEX_PLUGIN_GRAPHQL_TEST = "DataIndexPluginGraphqlTest";
	private static final String TENANT_ID = "public";

	@Inject
	DatasourceService datasourceService;

	@Inject
	DataIndexService dataIndexService;

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	PluginDriverService pluginDriverService;

	@BeforeEach
	@RunOnVertxContext
	void setup(UniAsserter asserter) {

		asserter.assertThat(
			() -> datasourceService.createDatasourceConnection(
				DatasourceConnectionObjects.DATASOURCE_CONNECTION_DTO_BUILDER()
					.name(DATA_INDEX_DATASOURCE_GRAPHQL_TEST)
					.pluginDriver(DatasourceConnectionObjects.PLUGIN_DRIVER_DTO_BUILDER()
						.name(DATA_INDEX_PLUGIN_GRAPHQL_TEST)
						.jsonConfig(JsonObject.of(
							"host", WireMockPluginDriver.HOST,
							"port", WireMockPluginDriver.PORT,
							"secure", false
						).encode())
						.build()
					)
					.dataIndex(DataIndexDTO.builder()
						.embeddingJsonConfig("{}")
						.knnIndex(true)
						.build())
					.build()
			),
			ignore -> {}
		);

	}

	@Test
	void should_return_knnDataIndex()
		throws ExecutionException, InterruptedException {

		var dataIndex = datasourceService.findByName(
				TENANT_ID, DATA_INDEX_DATASOURCE_GRAPHQL_TEST)
			.flatMap(datasource -> datasourceService.getDataIndex(datasource))
			.await().indefinitely();

		var query = document(
			operation(
				OperationType.QUERY,
				field(
					"dataIndex",
					args(
						arg("id", dataIndex.getId())
					),
					field("id"),
					field("name"),
					field("knnIndex")
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		System.out.println("response");
		System.out.println(response);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var dataIndexR = response.getData().getJsonObject("dataIndex");

		assertNotNull(dataIndexR);
		assertEquals(
			dataIndex.getId(), Long.parseLong(dataIndexR.getString("id")));
		assertTrue(dataIndexR.getBoolean("knnIndex"));

	}

	@AfterEach
	@RunOnVertxContext
	void tearDown(UniAsserter asserter) {

		asserter.assertThat(
			() -> datasourceService.findByName(
					TENANT_ID, DATA_INDEX_DATASOURCE_GRAPHQL_TEST)
				.flatMap(datasource -> datasourceService
					.getDataIndex(datasource)
					.flatMap(dataIndex -> datasourceService.unsetDataIndex(
							datasource.getId())
						.flatMap(ignore -> dataIndexService
							.deleteById(TENANT_ID, dataIndex.getId()))
						.flatMap(ignore -> datasourceService
							.getPluginDriver(datasource.getId())
							.flatMap(pluginDriver -> datasourceService
								.unsetPluginDriver(datasource.getId())
								.flatMap(ignoree -> pluginDriverService
									.deleteById(pluginDriver.getId()))
							))
						.flatMap(ignore -> datasourceService
							.deleteById(datasource.getId()))
					)
				),
			ignore -> {}
		);
	}


}
