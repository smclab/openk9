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
import static io.smallrye.graphql.client.core.InputObject.inputObject;
import static io.smallrye.graphql.client.core.InputObjectField.prop;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutionException;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.dto.base.DatasourceDTO;
import io.openk9.datasource.service.DatasourceService;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatasourcePurgeGraphqlTest {

	private static final String ENTITY_NAME_PREFIX = "DatasourcePurgeGraphqlTest - ";

	private static final String CRON_5_MINUTES = "0 */5 * ? * * *";
	private static final String CRON_2_MINUTES = "0 */2 * ? * * *";
	private static final String DATASOURCE = "datasource";
	private static final String DATASOURCE_DTO = "datasourceDTO";
	private static final String DATASOURCE_ONE_NAME = ENTITY_NAME_PREFIX + "Datasource 1";
	private static final String DATASOURCE_TWO_NAME = ENTITY_NAME_PREFIX + "Datasource 2";
	private static final String DATASOURCE_THREE_NAME = ENTITY_NAME_PREFIX + "Datasource 3";
	private static final String DESCRIPTION = "description";
	private static final String DESCRIPTION_DEFAULT = "Default description";
	private static final String DESCRIPTION_ONE = "Description 1";
	private static final String DURATION_5_DAYS = "5d";
	private static final String ENTITY = "entity";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String PATCH = "patch";
	private static final String PURGE_MAX_AGE = "purgeMaxAge";
	private static final String PURGE_MAX_AGE_DEFAULT_VALUE = "2d";
	private static final String PURGEABLE = "purgeable";
	private static final boolean PURGEABLE_DEFAULT_VALUE = false;
	private static final String PURGING = "purging";
	private static final String PURGING_DEFAULT_VALUE = "0 0 1 * * ?";
	private static final String REINDEXABLE = "reindexable";
	private static final boolean REINDEXABLE_DEFAULT_VALUE = false;
	private static final String REINDEXING = "reindexing";
	private static final String REINDEXING_DEFAULT_VALUE = "0 0 1 * * ?";
	private static final String SCHEDULABLE = "schedulable";
	private static final boolean SCHEDULABLE_DEFAULT_VALUE = false;
	private static final String SCHEDULING = "scheduling";
	private static final String SCHEDULING_DEFAULT_VALUE = "0 */30 * ? * * *";
	private static final Logger log = Logger.getLogger(DatasourcePurgeGraphqlTest.class);

	@Inject
	DatasourceService datasourceService;

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	@Order(1)
	void init() {
		createDatasourceThree();
	}

	@Test
	@Order(2)
	void should_create_datasource_one_with_default_purge_fields()
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
								prop(NAME, DATASOURCE_ONE_NAME)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response: %s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var datasourceOne = getDatasourceOne();

		assertEquals(SCHEDULING_DEFAULT_VALUE, datasourceOne.getScheduling());
		assertFalse(datasourceOne.getSchedulable());
		assertEquals(REINDEXING_DEFAULT_VALUE, datasourceOne.getReindexing());
		assertFalse(datasourceOne.getReindexable());
		assertEquals(PURGEABLE_DEFAULT_VALUE, datasourceOne.getPurgeable());
		assertEquals(PURGING_DEFAULT_VALUE, datasourceOne.getPurging());
		assertEquals(PURGE_MAX_AGE_DEFAULT_VALUE, datasourceOne.getPurgeMaxAge());

	}

	@Test
	@Order(3)
	void should_patch_datasource_one_purge_fields()
		throws ExecutionException, InterruptedException {

		var datasourceOne = getDatasourceOne();

		assertEquals(SCHEDULING_DEFAULT_VALUE, datasourceOne.getScheduling());
		assertEquals(REINDEXING_DEFAULT_VALUE, datasourceOne.getReindexing());
		assertEquals(PURGEABLE_DEFAULT_VALUE, datasourceOne.getPurgeable());
		assertEquals(PURGING_DEFAULT_VALUE, datasourceOne.getPurging());
		assertEquals(PURGE_MAX_AGE_DEFAULT_VALUE, datasourceOne.getPurgeMaxAge());

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					DATASOURCE,
					args(
						arg(ID,datasourceOne.getId()),
						arg(PATCH, true),
						arg(
							DATASOURCE_DTO,
							inputObject(
								prop(NAME, DATASOURCE_ONE_NAME),
								prop(SCHEDULABLE, false),
								prop(SCHEDULING, CRON_2_MINUTES),
								prop(REINDEXABLE, false),
								prop(REINDEXING, CRON_2_MINUTES),
								prop(PURGEABLE, true),
								prop(PURGING, CRON_2_MINUTES),
								prop(PURGE_MAX_AGE, DURATION_5_DAYS)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var datasourceOnePatched = getDatasourceOne();

		assertEquals(CRON_2_MINUTES, datasourceOnePatched.getScheduling());
		assertEquals(CRON_2_MINUTES, datasourceOnePatched.getReindexing());
		assertEquals(true, datasourceOnePatched.getPurgeable());
		assertEquals(CRON_2_MINUTES, datasourceOnePatched.getPurging());
		assertEquals(DURATION_5_DAYS, datasourceOnePatched.getPurgeMaxAge());
	}

	@Test
	@Order(4)
	void should_create_datasource_two_with_custom_purge_fields()
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
								prop(NAME, DATASOURCE_TWO_NAME),
								prop(DESCRIPTION, DESCRIPTION_DEFAULT),
								prop(SCHEDULABLE, true),
								prop(SCHEDULING, CRON_2_MINUTES),
								prop(REINDEXABLE, true),
								prop(REINDEXING, CRON_2_MINUTES),
								prop(PURGEABLE, true),
								prop(PURGING, CRON_2_MINUTES),
								prop(PURGE_MAX_AGE, DURATION_5_DAYS)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response: %s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var datasourceTwo = getDatasourceTwo();

		assertEquals(DESCRIPTION_DEFAULT, datasourceTwo.getDescription());
		assertTrue(datasourceTwo.getSchedulable());
		assertEquals(CRON_2_MINUTES, datasourceTwo.getScheduling());
		assertTrue(datasourceTwo.getReindexable());
		assertEquals(CRON_2_MINUTES, datasourceTwo.getReindexing());
		assertTrue(datasourceTwo.getPurgeable());
		assertEquals(CRON_2_MINUTES, datasourceTwo.getPurging());
		assertEquals(DURATION_5_DAYS, datasourceTwo.getPurgeMaxAge());

	}

	@Test
	@Order(5)
	void should_update_datasource_two()
		throws ExecutionException, InterruptedException {

		var datasourceTwo = getDatasourceTwo();

		assertEquals(DESCRIPTION_DEFAULT, datasourceTwo.getDescription());
		assertTrue(datasourceTwo.getSchedulable());
		assertEquals(CRON_2_MINUTES, datasourceTwo.getScheduling());
		assertTrue(datasourceTwo.getReindexable());
		assertEquals(CRON_2_MINUTES, datasourceTwo.getReindexing());
		assertEquals(true, datasourceTwo.getPurgeable());
		assertEquals(CRON_2_MINUTES, datasourceTwo.getPurging());
		assertEquals(DURATION_5_DAYS, datasourceTwo.getPurgeMaxAge());

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					DATASOURCE,
					args(
						arg(ID,datasourceTwo.getId()),
						arg(PATCH, false),
						arg(
							DATASOURCE_DTO,
							inputObject(
								prop(NAME, DATASOURCE_TWO_NAME),
								prop(DESCRIPTION, DESCRIPTION_ONE),
								prop(SCHEDULABLE, false),
								prop(SCHEDULING, CRON_5_MINUTES),
								prop(REINDEXABLE, false),
								prop(REINDEXING, CRON_5_MINUTES),
								prop(PURGEABLE, PURGEABLE_DEFAULT_VALUE),
								prop(PURGING, PURGING_DEFAULT_VALUE),
								prop(PURGE_MAX_AGE, PURGE_MAX_AGE_DEFAULT_VALUE)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var datasourceTwoUpdated = getDatasourceTwo();

		assertEquals(DESCRIPTION_ONE, datasourceTwoUpdated.getDescription());
		assertFalse(datasourceTwoUpdated.getSchedulable());
		assertEquals(CRON_5_MINUTES, datasourceTwoUpdated.getScheduling());
		assertFalse(datasourceTwoUpdated.getReindexable());
		assertEquals(CRON_5_MINUTES, datasourceTwoUpdated.getReindexing());
		assertEquals(PURGEABLE_DEFAULT_VALUE, datasourceTwoUpdated.getPurgeable());
		assertEquals(PURGING_DEFAULT_VALUE, datasourceTwoUpdated.getPurging());
		assertEquals(PURGE_MAX_AGE_DEFAULT_VALUE, datasourceTwoUpdated.getPurgeMaxAge());
	}

	@Test
	@Order(6)
	void should_update_datasource_two_with_null_fields_reset_to_default_value()
		throws ExecutionException, InterruptedException {

		var datasourceTwo = getDatasourceTwo();

		assertEquals(DESCRIPTION_ONE, datasourceTwo.getDescription());
		assertFalse(datasourceTwo.getSchedulable());
		assertEquals(CRON_5_MINUTES, datasourceTwo.getScheduling());
		assertFalse(datasourceTwo.getReindexable());
		assertEquals(CRON_5_MINUTES, datasourceTwo.getReindexing());
		assertEquals(PURGEABLE_DEFAULT_VALUE, datasourceTwo.getPurgeable());
		assertEquals(PURGING_DEFAULT_VALUE, datasourceTwo.getPurging());
		assertEquals(PURGE_MAX_AGE_DEFAULT_VALUE, datasourceTwo.getPurgeMaxAge());

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					DATASOURCE,
					args(
						arg(ID,datasourceTwo.getId()),
						arg(PATCH, false),
						arg(
							DATASOURCE_DTO,
							inputObject(
								prop(NAME, DATASOURCE_TWO_NAME),
								prop(DESCRIPTION, null),
								prop(SCHEDULABLE, null),
								prop(SCHEDULING, null),
								prop(REINDEXABLE, null),
								prop(REINDEXING, null),
								prop(PURGEABLE, null),
								prop(PURGING, null),
								prop(PURGE_MAX_AGE, null)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var datasourceTwoUpdated = getDatasourceTwo();

		assertNull(datasourceTwoUpdated.getDescription());
		assertFalse(datasourceTwoUpdated.getSchedulable());
		assertEquals(SCHEDULING_DEFAULT_VALUE, datasourceTwoUpdated.getScheduling());
		assertFalse(datasourceTwoUpdated.getReindexable());
		assertEquals(REINDEXING_DEFAULT_VALUE, datasourceTwoUpdated.getReindexing());
		assertEquals(PURGEABLE_DEFAULT_VALUE, datasourceTwoUpdated.getPurgeable());
		assertEquals(PURGING_DEFAULT_VALUE, datasourceTwoUpdated.getPurging());
		assertEquals(PURGE_MAX_AGE_DEFAULT_VALUE, datasourceTwoUpdated.getPurgeMaxAge());
	}

	@Test
	@Order(7)
	void should_patch_datasource_three_without_params_keep_original_values()
		throws ExecutionException, InterruptedException {

		var datasourceThree = getDatasourceThree();

		// Check original values
		assertEquals(DESCRIPTION_DEFAULT, datasourceThree.getDescription());
		assertTrue(datasourceThree.getSchedulable());
		assertEquals(CRON_2_MINUTES, datasourceThree.getScheduling());
		assertTrue(datasourceThree.getReindexable());
		assertEquals(CRON_2_MINUTES, datasourceThree.getReindexing());
		assertTrue(datasourceThree.getPurgeable());
		assertEquals(CRON_2_MINUTES, datasourceThree.getPurging());
		assertEquals(DURATION_5_DAYS, datasourceThree.getPurgeMaxAge());

		// Patch datasource
		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					DATASOURCE,
					args(
						arg(ID, datasourceThree.getId()),
						arg(PATCH, true),
						arg(
							DATASOURCE_DTO,
							inputObject(
								prop(NAME, DATASOURCE_THREE_NAME)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response: %s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var datasourceThreePatched = getDatasourceThree();

		// Check if the patched datasource keeps its original values
		assertEquals(DESCRIPTION_DEFAULT, datasourceThreePatched.getDescription());
		assertTrue(datasourceThreePatched.getSchedulable());
		assertEquals(CRON_2_MINUTES, datasourceThreePatched.getScheduling());
		assertTrue(datasourceThreePatched.getReindexable());
		assertEquals(CRON_2_MINUTES, datasourceThreePatched.getReindexing());
		assertTrue(datasourceThreePatched.getPurgeable());
		assertEquals(CRON_2_MINUTES, datasourceThreePatched.getPurging());
		assertEquals(DURATION_5_DAYS, datasourceThreePatched.getPurgeMaxAge());

	}

	@Test
	@Order(8)
	void should_patch_datasource_three_with_null_fields_keep_original_values()
		throws ExecutionException, InterruptedException {

		var datasourceThree = getDatasourceThree();

		// Check original values
		assertEquals(DESCRIPTION_DEFAULT, datasourceThree.getDescription());
		assertTrue(datasourceThree.getSchedulable());
		assertEquals(CRON_2_MINUTES, datasourceThree.getScheduling());
		assertTrue(datasourceThree.getReindexable());
		assertEquals(CRON_2_MINUTES, datasourceThree.getReindexing());
		assertTrue(datasourceThree.getPurgeable());
		assertEquals(CRON_2_MINUTES, datasourceThree.getPurging());
		assertEquals(DURATION_5_DAYS, datasourceThree.getPurgeMaxAge());

		// Patch datasource
		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					DATASOURCE,
					args(
						arg(ID, datasourceThree.getId()),
						arg(PATCH, true),
						arg(
							DATASOURCE_DTO,
							inputObject(
								prop(NAME, DATASOURCE_THREE_NAME),
								prop(DESCRIPTION, null),
								prop(SCHEDULABLE, null),
								prop(SCHEDULING, null),
								prop(REINDEXABLE, null),
								prop(REINDEXING, null),
								prop(PURGEABLE, null),
								prop(PURGING, null),
								prop(PURGE_MAX_AGE, null)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response: %s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var datasourceThreePatched = getDatasourceThree();

		// Check if the patched datasource keeps its original values
		assertEquals(DESCRIPTION_DEFAULT, datasourceThreePatched.getDescription());
		assertTrue(datasourceThreePatched.getSchedulable());
		assertEquals(CRON_2_MINUTES, datasourceThreePatched.getScheduling());
		assertTrue(datasourceThreePatched.getReindexable());
		assertEquals(CRON_2_MINUTES, datasourceThreePatched.getReindexing());
		assertTrue(datasourceThreePatched.getPurgeable());
		assertEquals(CRON_2_MINUTES, datasourceThreePatched.getPurging());
		assertEquals(DURATION_5_DAYS, datasourceThreePatched.getPurgeMaxAge());

	}

	@Test
	@Order(9)
	void should_update_datasource_three_without_params_reset_to_default_values()
		throws ExecutionException, InterruptedException {

		var datasourceThree = getDatasourceThree();

		// Check original values
		assertTrue(datasourceThree.getSchedulable());
		assertEquals(CRON_2_MINUTES, datasourceThree.getScheduling());
		assertTrue(datasourceThree.getReindexable());
		assertEquals(CRON_2_MINUTES, datasourceThree.getReindexing());
		assertTrue(datasourceThree.getPurgeable());
		assertEquals(CRON_2_MINUTES, datasourceThree.getPurging());
		assertEquals(DURATION_5_DAYS, datasourceThree.getPurgeMaxAge());

		// Patch datasource
		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					DATASOURCE,
					args(
						arg(ID, datasourceThree.getId()),
						arg(PATCH, false),
						arg(
							DATASOURCE_DTO,
							inputObject(
								prop(NAME, DATASOURCE_THREE_NAME)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response: %s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var datasourceThreeUpdated = getDatasourceThree();

		// Check if the updated datasource resets its values to the default
		// or null if the specific field does not have a default.
		assertNull(datasourceThreeUpdated.getDescription());
		assertEquals(SCHEDULABLE_DEFAULT_VALUE, datasourceThreeUpdated.getSchedulable());
		assertEquals(SCHEDULING_DEFAULT_VALUE, datasourceThreeUpdated.getScheduling());
		assertEquals(REINDEXABLE_DEFAULT_VALUE, datasourceThreeUpdated.getReindexable());
		assertEquals(REINDEXING_DEFAULT_VALUE, datasourceThreeUpdated.getReindexing());
		assertEquals(PURGEABLE_DEFAULT_VALUE, datasourceThreeUpdated.getPurgeable());
		assertEquals(PURGING_DEFAULT_VALUE, datasourceThreeUpdated.getPurging());
		assertEquals(PURGE_MAX_AGE_DEFAULT_VALUE, datasourceThreeUpdated.getPurgeMaxAge());

	}

	@Test
	@Order(10)
	void tearDown() {
		var datasourceOne = getDatasourceOne();
		var datasourceTwo = getDatasourceTwo();
		var datasourceThree = getDatasourceThree();

		sessionFactory.withTransaction(
			(s, transaction) ->
				datasourceService.deleteById(datasourceOne.getId())
		)
		.await()
		.indefinitely();

		sessionFactory.withTransaction(
			(s, transaction) ->
				datasourceService.deleteById(datasourceTwo.getId())
		)
		.await()
		.indefinitely();

		sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.deleteById(datasourceThree.getId())
			)
			.await()
			.indefinitely();

		assertThrows(NoResultException.class, this::getDatasourceOne);
		assertThrows(NoResultException.class, this::getDatasourceTwo);
		assertThrows(NoResultException.class, this::getDatasourceThree);

	}

	private void createDatasourceThree() {
		DatasourceDTO dto = DatasourceDTO.builder()
			.name(DATASOURCE_THREE_NAME)
			.description(DESCRIPTION_DEFAULT)
			.schedulable(true)
			.scheduling(CRON_2_MINUTES)
			.reindexable(true)
			.reindexing(CRON_2_MINUTES)
			.purgeable(true)
			.purging(CRON_2_MINUTES)
			.purgeMaxAge(DURATION_5_DAYS)
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

	private Datasource getDatasourceThree() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					datasourceService.findByName(s,DATASOURCE_THREE_NAME)
			)
			.await()
			.indefinitely();
	}
}
