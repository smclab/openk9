package io.openk9.datasource.graphql;

import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.service.DatasourceService;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import org.glassfish.jaxb.core.v2.TODO;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.lang.reflect.InvocationTargetException;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatasourcePurgeGraphqlTest {

	public static final String DATASOURCE = "datasource";
	public static final String DATASOURCE_DTO = "datasourceDTO";
	public static final String CRON_5_MINUTES = "0 */5 * ? * * *";
	public static final String CRON_2_MINUTES = "0 */2 * ? * * *";
	public static final String DURATION_5_DAYS = "5d";
	private static final String ENTITY_NAME_PREFIX = "DatasourcePurgeGraphqlTest - ";

	private static final String DATASOURCE_ONE_NAME = ENTITY_NAME_PREFIX + "Datasource 1";
	private static final String DATASOURCE_TWO_NAME = ENTITY_NAME_PREFIX + "Datasource 2";
	private static final String ENTITY = "entity";
	private static final String ID = "id";
	private static final String NAME = "name";
	public static final String PATCH = "patch";
	public static final String PURGEABLE = "purgeable";
	private static final boolean PURGEABLE_DEFAULT_VALUE = false;
	public static final String PURGING = "purging";
	public static final String PURGE_MAX_AGE = "purgeMaxAge";
	private static final String PURGING_DEFAULT_VALUE = "0 */10 * ? * *";
	private static final String PURGE_MAX_AGE_DEFAULT_VALUE = "2d";
	public static final String SCHEDULABLE = "schedulable";
	public static final String SCHEDULING = "scheduling";
	public static final String REINDEXABLE = "reindexable";
	public static final String REINDEXING = "reindexing";
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
								prop(NAME, DATASOURCE_ONE_NAME),
								prop(SCHEDULABLE, false),
								prop(SCHEDULING, CRON_5_MINUTES),
								prop(REINDEXABLE, false),
								prop(REINDEXING, CRON_5_MINUTES)
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

		assertEquals(CRON_5_MINUTES, datasourceOne.getScheduling());
		assertEquals(CRON_5_MINUTES, datasourceOne.getReindexing());
		assertEquals(PURGEABLE_DEFAULT_VALUE, datasourceOne.getPurgeable());
		assertEquals(PURGING_DEFAULT_VALUE, datasourceOne.getPurging());
		assertEquals(PURGE_MAX_AGE_DEFAULT_VALUE, datasourceOne.getPurgeMaxAge());

	}

	@Test
	@Order(2)
	void should_patch_datasource_one_purge_fields()
		throws ExecutionException, InterruptedException {

		var datasourceOne = getDatasourceOne();

		assertEquals(CRON_5_MINUTES, datasourceOne.getScheduling());
		assertEquals(CRON_5_MINUTES, datasourceOne.getReindexing());
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
	@Order(3)
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

		log.info(String.format("Response: %s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var datasourceTwo = getDatasourceTwo();

		assertEquals(CRON_2_MINUTES, datasourceTwo.getScheduling());
		assertEquals(CRON_2_MINUTES, datasourceTwo.getReindexing());
		assertEquals(true, datasourceTwo.getPurgeable());
		assertEquals(CRON_2_MINUTES, datasourceTwo.getPurging());
		assertEquals(DURATION_5_DAYS, datasourceTwo.getPurgeMaxAge());

	}

	@Test
	@Order(4)
	void should_update_datasource_two_purge_fields()
		throws ExecutionException, InterruptedException {

		var datasourceTwo = getDatasourceTwo();

		assertEquals(CRON_2_MINUTES, datasourceTwo.getScheduling());
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

		assertEquals(CRON_5_MINUTES, datasourceTwoUpdated.getScheduling());
		assertEquals(CRON_5_MINUTES, datasourceTwoUpdated.getReindexing());
		assertEquals(PURGEABLE_DEFAULT_VALUE, datasourceTwoUpdated.getPurgeable());
		assertEquals(PURGING_DEFAULT_VALUE, datasourceTwoUpdated.getPurging());
		assertEquals(PURGE_MAX_AGE_DEFAULT_VALUE, datasourceTwoUpdated.getPurgeMaxAge());
	}

	@Test
	@Order(5)
	void should_update_datasource_two_with_null_purge_fields()
		throws ExecutionException, InterruptedException {

		var datasourceTwo = getDatasourceTwo();

		assertEquals(CRON_5_MINUTES, datasourceTwo.getScheduling());
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
								prop(SCHEDULABLE, false),
								prop(SCHEDULING, CRON_5_MINUTES),
								prop(REINDEXABLE, false),
								prop(REINDEXING, CRON_5_MINUTES),
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

		assertEquals(CRON_5_MINUTES, datasourceTwoUpdated.getScheduling());
		assertEquals(CRON_5_MINUTES, datasourceTwoUpdated.getReindexing());
		assertNull(datasourceTwoUpdated.getPurgeable());
		assertNull(datasourceTwoUpdated.getPurging());
		assertNull(datasourceTwoUpdated.getPurgeMaxAge());
	}

	@Test
	@Order(6)
	void tearDown() {
		var datasourceOne = getDatasourceOne();
		var datasourceTwo = getDatasourceTwo();

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

		assertThrows(NoResultException.class, this::getDatasourceOne);
		assertThrows(NoResultException.class, this::getDatasourceTwo);

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
}
