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

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.VectorIndex;
import io.openk9.datasource.model.dto.DatasourceDTO;
import io.openk9.datasource.model.dto.VectorIndexDTO;
import io.openk9.datasource.service.CreateConnection;
import io.openk9.datasource.service.DataIndexService;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.VectorIndexService;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.concurrent.ExecutionException;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataIndexGraphqlTest {

	public static final String DATA_INDEX_DATASOURCE_GRAPHQL_TEST =
		"DataIndexDatasourceGraphqlTest";
	public static final String DATA_INDEX_GRAPHQL_TEST = "DataIndexGraphqlTest";
	public static final String VECTOR_INDEX_GRAPHQL_TEST = "VectorIndexGraphqlTest";

	@Inject
	DatasourceService datasourceService;

	@Inject
	DataIndexService dataIndexService;

	@Inject
	VectorIndexService vectorIndexService;

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Test
	@Order(1)
	void setup() {

		var datasource = datasourceService.create(DatasourceDTO.builder()
			.name(DATA_INDEX_DATASOURCE_GRAPHQL_TEST)
			.jsonConfig(CreateConnection.DATASOURCE_JSON_CONFIG)
			.scheduling(CreateConnection.SCHEDULING)
			.schedulable(false)
			.reindexing(CreateConnection.REINDEXING)
			.reindexable(false)
			.build()
		).await().indefinitely();

		var transientDataIndex = new DataIndex();
		transientDataIndex.setName(DATA_INDEX_GRAPHQL_TEST);
		transientDataIndex.setDatasource(datasource);

		var dataindex = dataIndexService.create(transientDataIndex)
			.await().indefinitely();

		dataindex.setTenant("public");

		var vectorIndex = vectorIndexService.create(VectorIndexDTO.builder()
			.name(VECTOR_INDEX_GRAPHQL_TEST)
			.configurations(VectorIndexDTO.ConfigurationsDTO.builder()
				.jsonConfig("{}")
				.chunkType(VectorIndex.ChunkType.DEFAULT)
				.chunkWindowSize(1)
				.metadataMapping("$")
				.textEmbeddingField("$.rawContent")
				.titleField("$.title")
				.urlField("$.url")
				.build()
			)
			.build()
		).await().indefinitely();

		dataIndexService.bindVectorDataIndex(
			dataindex.getId(), vectorIndex.getId()
		).await().indefinitely();

	}

	@Test
	@Order(2)
	void should_return_vectorIndex_when_queried_from_dataIndex()
	throws ExecutionException, InterruptedException {

		var dataIndex = dataIndexService.findByName("public", DATA_INDEX_GRAPHQL_TEST)
			.await().indefinitely();

		var vectorIndex = vectorIndexService.findByName("public", VECTOR_INDEX_GRAPHQL_TEST)
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
					field(
						"vectorIndex",
						field("id"),
						field("name")
					)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var dataIndexR = response.getData().getJsonObject("dataIndex");

		assertNotNull(dataIndexR);
		assertEquals(
			dataIndex.getId(), Long.parseLong(dataIndexR.getString("id")));

		var vectorIndexR = dataIndexR.getJsonObject("vectorIndex");

		assertNotNull(vectorIndexR);
		assertEquals(
			vectorIndex.getId(), Long.parseLong(vectorIndexR.getString("id")));

	}

	@Test
	@Order(3)
	void tearDown() {

		var datasource = datasourceService.findByName("public", DATA_INDEX_DATASOURCE_GRAPHQL_TEST)
			.await().indefinitely();

		var dataIndex = dataIndexService.findByName("public", DATA_INDEX_GRAPHQL_TEST)
			.await().indefinitely();

		var vectorIndex = vectorIndexService.findByName("public", VECTOR_INDEX_GRAPHQL_TEST)
			.await().indefinitely();

		dataIndexService.unbindVectorDataIndex(dataIndex.getId())
			.await().indefinitely();

		datasourceService.unsetDataIndex(datasource.getId())
			.await().indefinitely();

		vectorIndexService.deleteById(vectorIndex.getId())
			.await().indefinitely();

		dataIndexService.deleteById("public", dataIndex.getId())
			.await().indefinitely();

		datasourceService.deleteById(datasource.getId())
			.await().indefinitely();
	}


}
