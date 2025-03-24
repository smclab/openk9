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

import io.openk9.datasource.Initializer;
import io.openk9.datasource.service.DatasourceService;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DataIndexGraphqlTest {

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
		assertFalse(dataIndexR.getBoolean("knnIndex"));

	}

}
