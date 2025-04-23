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

import io.openk9.datasource.Initializer;
import io.openk9.datasource.service.DatasourceService;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

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
public class DataIndexGraphqlTest {

	private static final String DATA_INDEX = "dataIndex";
	private static final String DATA_INDICES = "dataIndices";
	private static final String DATASOURCE = "datasource";
	private static final String EDGES = "edges";
	private static final String ID = "id";
	private static final String KNN_INDEX = "knnIndex";
	private static final String NAME = "name";
	private static final String NODE = "node";
	private static final String RESPONSE = "response";
	private static final String TENANT_ID = "public";

	@Inject
	DatasourceService datasourceService;

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Test
	void should_return_knnDataIndex()
		throws ExecutionException, InterruptedException {

		var dataIndex = datasourceService.findByName(
				TENANT_ID, Initializer.INIT_DATASOURCE_CONNECTION)
			.flatMap(datasource -> datasourceService.getDataIndex(datasource))
			.await().indefinitely();

		var query = document(
			operation(
				OperationType.QUERY,
				field(
					DATA_INDEX,
					args(
						arg(ID, dataIndex.getId())
					),
					field(ID),
					field(NAME),
					field(KNN_INDEX)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		System.out.println(RESPONSE);
		System.out.println(response);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var dataIndexR = response.getData().getJsonObject(DATA_INDEX);

		assertNotNull(dataIndexR);
		assertEquals(
			dataIndex.getId(), Long.parseLong(dataIndexR.getString(ID)));
		assertFalse(dataIndexR.getBoolean(KNN_INDEX));

	}

	@Test
	void should_retrieve_datasource()
		throws ExecutionException, InterruptedException {

		var dataIndex = datasourceService.findByName(
				TENANT_ID, Initializer.INIT_DATASOURCE_CONNECTION)
			.flatMap(datasource -> datasourceService.getDataIndex(datasource))
			.await().indefinitely();

		var query = document(
			operation(
				OperationType.QUERY,
				field(
					DATA_INDEX,
					args(
						arg(ID, dataIndex.getId())
					),
					field(ID),
					field(NAME),
					field(
						DATASOURCE,
						field(ID),
						field(NAME)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		System.out.println(RESPONSE);
		System.out.println(response);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var dataIndexR = response.getData().getJsonObject(DATA_INDEX);

		assertNotNull(dataIndexR);
		assertEquals(
			dataIndex.getId(), Long.parseLong(dataIndexR.getString(ID)));

		var datasourceR = dataIndexR.getJsonObject(DATASOURCE);

		assertEquals(Initializer.INIT_DATASOURCE_CONNECTION, datasourceR.getString(NAME));

	}

	@Test
	void should_get_all_dataindices_and_retrieve_datasource()
		throws ExecutionException, InterruptedException {

		var dataIndex = datasourceService.findByName(
				TENANT_ID, Initializer.INIT_DATASOURCE_CONNECTION)
			.flatMap(datasource -> datasourceService.getDataIndex(datasource))
			.await().indefinitely();

		var query = document(
			operation(
				OperationType.QUERY,
				field(
					DATA_INDICES,
					field(EDGES,
						field(NODE,
							field(ID),
							field(NAME),
							field(
								DATASOURCE,
								field(ID),
								field(NAME)
							)
						)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		System.out.println(RESPONSE);
		System.out.println(response);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var dataIndicesR = response.getData().getJsonObject(DATA_INDICES);
		assertNotNull(dataIndicesR);

		var edges = dataIndicesR.getJsonArray(EDGES);
		assertFalse(edges.isEmpty());

		var firstNode = edges.getFirst().asJsonObject().getJsonObject(NODE);
		assertEquals(
			dataIndex.getId(), Long.parseLong(firstNode.getString(ID)));

		var datasourceR = firstNode.getJsonObject(DATASOURCE);

		assertEquals(Initializer.INIT_DATASOURCE_CONNECTION, datasourceR.getString(NAME));

	}

}
