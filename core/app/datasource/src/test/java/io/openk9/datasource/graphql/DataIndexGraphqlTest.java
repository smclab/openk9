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
import io.openk9.datasource.service.DataIndexService;
import io.openk9.datasource.service.DatasourceService;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.core.InputObject;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.OpenSearchClient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.InputObject.inputObject;
import static io.smallrye.graphql.client.core.InputObjectField.prop;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class DataIndexGraphqlTest {

	// lowercase, as the name of a dataIndex names its index
	private static final String ENTITY_NAME_PREFIX = "dataindexgraphqltest-";
	private static final String CAT = "cat";
	private static final String DATA_INDEX = "dataIndex";
	private static final String DATA_INDEX_DTO = "dataIndexDTO";
	private static final String DATA_INDICES = "dataIndices";
	private static final String DATASOURCE = "datasource";
	private static final String DATASOURCE_ID = "datasourceId";
	private static final String DOC_TYPE_IDS = "docTypeIds";
	private static final String EDGES = "edges";
	private static final String ENTITY = "entity";
	private static final String ID = "id";
	private static final String KNN_INDEX = "knnIndex";
	private static final String NAME = "name";
	private static final String NODE = "node";
	private static final String RESPONSE = "response: %s";
	private static final String CUSTOM_SETTINGS = "customSettings";
	private static final String SETTINGS = "settings";
	private static final String STATUS = "status";
	private static final String TENANT_ID = "public";
	private static final String WARNINGS = "warnings";

	private static final Logger log = Logger.getLogger(DataIndexGraphqlTest.class);

	@Inject
	DataIndexService dataIndexService;

	@Inject
	DatasourceService datasourceService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	OpenSearchClient openSearchClient;

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

		log.info(String.format(RESPONSE, response));

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

		log.info(String.format(RESPONSE, response));

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

		log.info(String.format(RESPONSE, response));

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

	@Test
	void should_return_the_index_template_settings()
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
					field(SETTINGS)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		log.info(String.format(RESPONSE, response));

		// the settings field keeps answering the settings the index template
		// declares
		assertFalse(response.hasError());

		var settings =
			response.getData().getJsonObject(DATA_INDEX).getString(SETTINGS);

		assertNotNull(settings);
		assertFalse(settings.isBlank());
	}

	@Test
	void should_expose_only_the_two_settings_fields()
		throws ExecutionException, InterruptedException {

		var query = document(
			operation(
				OperationType.QUERY,
				field(
					"__type",
					args(
						arg("name", "DataIndex")
					),
					field("fields", field(NAME))
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		assertFalse(response.hasError());

		var fieldNames = response.getData()
			.getJsonObject("__type")
			.getJsonArray("fields")
			.stream()
			.map(field -> field.asJsonObject().getString(NAME))
			.filter(fieldName -> fieldName.toLowerCase().contains(SETTINGS))
			.sorted()
			.toList();

		// the two answer different questions and both are needed: settings tells
		// what the index template declares, which is the derived and the custom
		// merged, while customSettings tells what was recorded on the dataIndex,
		// which is the only document an operator can edit and send back
		assertEquals(List.of(CUSTOM_SETTINGS, SETTINGS), fieldNames);
	}

	@Test
	void should_return_cat()
		throws ExecutionException, InterruptedException, IOException {

		var dataIndex = datasourceService.findByName(
				TENANT_ID, Initializer.INIT_DATASOURCE_CONNECTION)
			.flatMap(datasource -> datasourceService.getDataIndex(datasource))
			.await().indefinitely();

		openSearchClient.indices().create(req ->
			req.index(TENANT_ID + "-" + dataIndex.getName().toLowerCase()));

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
						CAT,
						field(STATUS)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		log.info(String.format(RESPONSE, response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var dataIndexR = response.getData().getJsonObject(DATA_INDEX);

		assertNotNull(dataIndexR);
		assertEquals(
			dataIndex.getId(), Long.parseLong(dataIndexR.getString(ID)));
	}

	@Test
	void should_warn_when_created_without_doc_types()
		throws ExecutionException, InterruptedException {

		// a dataIndex created with no docTypeIds
		var name = ENTITY_NAME_PREFIX + "no-doc-types";

		var response = graphQLClient.executeSync(createDataIndexMutation(
			inputObject(
				prop(NAME, name),
				prop(KNN_INDEX, false)
			)
		));

		log.info(String.format(RESPONSE, response));

		try {
			assertFalse(response.hasError());

			var dataIndexR = response.getData().getJsonObject(DATA_INDEX);

			// the creation is not refused
			assertEquals(
				name, dataIndexR.getJsonObject(ENTITY).getString(NAME));

			// but the response says the configuration is incomplete
			var warnings = dataIndexR.getJsonArray(WARNINGS);

			assertEquals(1, warnings.size());
			assertEquals(
				DataIndexService.NO_DOC_TYPES_WARNING, warnings.getString(0));
		}
		finally {
			deleteDataIndexQuietly(name);
		}
	}

	@Test
	void should_not_warn_when_created_with_doc_types()
		throws ExecutionException, InterruptedException {

		// a dataIndex created with one docType
		var name = ENTITY_NAME_PREFIX + "with-doc-types";

		var response = graphQLClient.executeSync(createDataIndexMutation(
			inputObject(
				prop(NAME, name),
				prop(KNN_INDEX, false),
				prop(DOC_TYPE_IDS, List.of(anyDocTypeId()))
			)
		));

		log.info(String.format(RESPONSE, response));

		try {
			assertFalse(response.hasError());

			var dataIndexR = response.getData().getJsonObject(DATA_INDEX);

			assertEquals(
				name, dataIndexR.getJsonObject(ENTITY).getString(NAME));

			// nothing to warn about
			assertTrue(
				dataIndexR.isNull(WARNINGS)
				|| dataIndexR.getJsonArray(WARNINGS).isEmpty());
		}
		finally {
			deleteDataIndexQuietly(name);
		}
	}

	private Document createDataIndexMutation(InputObject dataIndexDTO) {

		var datasource = datasourceService.findByName(
				TENANT_ID, Initializer.INIT_DATASOURCE_CONNECTION)
			.await().indefinitely();

		return document(
			operation(
				OperationType.MUTATION,
				field(
					DATA_INDEX,
					args(
						arg(DATASOURCE_ID, datasource.getId()),
						arg(DATA_INDEX_DTO, dataIndexDTO)
					),
					field(
						ENTITY,
						field(ID),
						field(NAME)
					),
					field(WARNINGS)
				)
			)
		);
	}

	private long anyDocTypeId() {
		return sessionFactory.withTransaction((s, t) -> s
				.createQuery("select dt.id from DocType dt order by dt.id", Long.class)
				.getResultList()
				.map(List::getFirst)
			)
			.await()
			.indefinitely();
	}

	private void deleteDataIndexQuietly(String name) {
		try {
			var dataIndex = dataIndexService.findByName(TENANT_ID, name)
				.await().indefinitely();

			dataIndexService.deleteById(dataIndex.getId()).await().indefinitely();
		}
		catch (Exception ignored) {
		}
	}

}
