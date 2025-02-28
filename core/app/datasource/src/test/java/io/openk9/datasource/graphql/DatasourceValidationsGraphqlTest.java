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

import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.dto.DatasourceDTO;
import io.openk9.datasource.service.DatasourceService;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import jakarta.inject.Inject;
import jakarta.json.JsonValue;
import jakarta.persistence.NoResultException;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.HashMap;
import java.util.Set;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class DatasourceValidationsGraphqlTest {

	private static final String ENTITY_NAME_PREFIX = "DatasourceValidationsGraphqlTest - ";

	private static final String CRON_2_MINUTES = "0 */2 * ? * * *";
	private static final String CRON_ERROR_MESSAGE = "is not a valid cron expression";
	private static final String DATASOURCE = "datasource";
	private static final String DATASOURCE_DTO = "datasourceDTO";
	private static final String DATASOURCE_ONE_NAME = ENTITY_NAME_PREFIX + "Datasource 1";
	private static final String DATASOURCE_TWO_NAME = ENTITY_NAME_PREFIX + "Datasource 2";
	private static final String DESCRIPTION = "description";
	private static final String DESCRIPTION_DEFAULT = "Default description";
	private static final String ENTITY = "entity";
	private static final String FIELD = "field";
	private static final String FIELD_VALIDATORS = "fieldValidators";
	private static final String ID = "id";
	private static final String MESSAGE = "message";
	private static final String NAME = "name";
	private static final String PATCH = "patch";
	private static final String PURGING = "purging";
	private static final String REINDEXING = "reindexing";
	private static final String SCHEDULING = "scheduling";
	private static final Logger log = Logger.getLogger(DatasourceValidationsGraphqlTest.class);

	@Inject
	DatasourceService datasourceService;

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@BeforeEach
	void setup() {
		createDatasourceTwo();
	}

	@Test
	void should_create_datasource_one_with_valid_cron()
		throws ExecutionException, InterruptedException {

		var validCronScheduling = "0 0 1 * * ?";
		var validCronReindexing = "0 */30 * ? * * *";
		var validCronPurging = "0 0/5 8-10 ? * MON-FRI";

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					DATASOURCE,
					args(
						arg(
							DATASOURCE_DTO,
							inputObject(
								prop(NAME, DATASOURCE_ONE_NAME),
								prop(DESCRIPTION, DESCRIPTION_DEFAULT),
								prop(SCHEDULING, validCronScheduling),
								prop(REINDEXING, validCronReindexing),
								prop(PURGING, validCronPurging)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response: %s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var datasource =
			response.getData().getJsonObject(DATASOURCE);

		assertNotNull(datasource);

		assertTrue(datasource.isNull(FIELD_VALIDATORS));

		// Check if the datasource have the cron fields passed in the creation mutation
		var datasourceOne = getDatasourceOne();

		assertEquals(DESCRIPTION_DEFAULT, datasourceOne.getDescription());
		assertFalse(datasourceOne.getSchedulable());
		assertEquals(validCronScheduling, datasourceOne.getScheduling());
		assertFalse(datasourceOne.getReindexable());
		assertEquals(validCronReindexing, datasourceOne.getReindexing());
		assertFalse(datasourceOne.getPurgeable());
		assertEquals(validCronPurging, datasourceOne.getPurging());

		// remove datasourceOne
		removeDatasourceOne();
	}

	@Test
	void should_have_validator_fields_error_creating_datasource_one_with_null_empty_and_no_cron_fields()
		throws ExecutionException, InterruptedException {

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					DATASOURCE,
					args(
						arg(
							DATASOURCE_DTO,
							inputObject(
								prop(NAME, DATASOURCE_ONE_NAME),
								prop(SCHEDULING, "null"),
								prop(REINDEXING, ""),
								prop(PURGING, "not_valid_cron")
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response: %s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var datasource =
			response.getData().getJsonObject(DATASOURCE);

		assertNotNull(datasource);

		var fieldValidators = datasource.getJsonArray(FIELD_VALIDATORS);

		assertNotNull(fieldValidators);

		// Check if the number of errors is exactly 3
		assertEquals(3, fieldValidators.size());

		// Collect field validator errors
		var errorMap = new HashMap<String, String>();

		for (JsonValue fieldValidator : fieldValidators) {
			var validator = fieldValidator.asJsonObject();

			var field = validator.getString(FIELD);
			var message = validator.getString(MESSAGE);

			errorMap.put(field, message);
		}

		// Check if all fields have generated an error
		assertEquals(Set.of(SCHEDULING, REINDEXING, PURGING), errorMap.keySet());

		// Check if the error comes from cron validation for each field
		assertEquals(CRON_ERROR_MESSAGE, errorMap.get(SCHEDULING));
		assertEquals(CRON_ERROR_MESSAGE, errorMap.get(REINDEXING));
		assertEquals(CRON_ERROR_MESSAGE, errorMap.get(PURGING));

		// The datasource must not be present in the database
		assertThrows(NoResultException.class, this::getDatasourceOne);
	}

	@Test
	void should_have_validator_fields_error_patching_datasource_two_with_null_and_empty_fields()
		throws ExecutionException, InterruptedException {

		var datasourceTwo = getDatasourceTwo();

		// Check original values
		assertEquals(DESCRIPTION_DEFAULT, datasourceTwo.getDescription());
		assertFalse(datasourceTwo.getSchedulable());
		assertEquals(CRON_2_MINUTES, datasourceTwo.getScheduling());
		assertFalse(datasourceTwo.getReindexable());
		assertEquals(CRON_2_MINUTES, datasourceTwo.getReindexing());
		assertFalse(datasourceTwo.getPurgeable());
		assertEquals(CRON_2_MINUTES, datasourceTwo.getPurging());

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					DATASOURCE,
					args(
						arg(ID, datasourceTwo.getId()),
						arg(PATCH, true),
						arg(
							DATASOURCE_DTO,
							inputObject(
								prop(NAME, DATASOURCE_TWO_NAME),
								prop(DESCRIPTION, null),
								prop(SCHEDULING, null),
								prop(REINDEXING, "")
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response: %s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var datasource =
			response.getData().getJsonObject(DATASOURCE);

		assertNotNull(datasource);

		var fieldValidators = datasource.getJsonArray(FIELD_VALIDATORS);

		assertNotNull(fieldValidators);

		// Check if the number of errors is exactly 1, and is for the reindex field
		assertEquals(1, fieldValidators.size());

		var validator = fieldValidators.getFirst().asJsonObject();

		var field = validator.getString(FIELD);
		var message = validator.getString(MESSAGE);

		assertEquals(REINDEXING, field);
		assertEquals(CRON_ERROR_MESSAGE, message);

		// Check if the datasourceTwo has not changed
		var datasourceTwoPatched = getDatasourceTwo();

		assertEquals(DESCRIPTION_DEFAULT, datasourceTwoPatched.getDescription());
		assertFalse(datasourceTwo.getSchedulable());
		assertEquals(CRON_2_MINUTES, datasourceTwoPatched.getScheduling());
		assertFalse(datasourceTwo.getReindexable());
		assertEquals(CRON_2_MINUTES, datasourceTwoPatched.getReindexing());
		assertFalse(datasourceTwo.getPurgeable());
		assertEquals(CRON_2_MINUTES, datasourceTwoPatched.getPurging());

	}

	@AfterEach
	void tearDown() {
		removeDatasourceTwo();
	}

	private void createDatasourceTwo() {
		DatasourceDTO dto = DatasourceDTO.builder()
			.name(DATASOURCE_TWO_NAME)
			.description(DESCRIPTION_DEFAULT)
			.schedulable(false)
			.scheduling(CRON_2_MINUTES)
			.reindexable(false)
			.reindexing(CRON_2_MINUTES)
			.purgeable(false)
			.purging(CRON_2_MINUTES)
			.build();

		sessionFactory.withTransaction(
			(s, transaction) ->
				datasourceService.create(dto)
		)
		.await()
		.indefinitely();
	}

	private Datasource getDatasourceOne() {
		return sessionFactory.withTransaction(
			(s, transaction) ->
				datasourceService.findByName(s,DATASOURCE_ONE_NAME)
		)
		.await()
		.indefinitely();
	}

	private Datasource getDatasourceTwo() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.findByName(s,DATASOURCE_TWO_NAME)
			)
			.await()
			.indefinitely();
	}

	private void removeDatasourceOne() {
		var datasource = getDatasourceOne();

		sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.deleteById(datasource.getId())
			)
			.await()
			.indefinitely();
	}

	private void removeDatasourceTwo() {
		var datasource = getDatasourceTwo();

		sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.deleteById(datasource.getId())
			)
			.await()
			.indefinitely();
	}
}
