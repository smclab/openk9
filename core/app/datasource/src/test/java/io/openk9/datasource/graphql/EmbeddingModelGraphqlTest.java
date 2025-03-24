package io.openk9.datasource.graphql;

import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.dto.EmbeddingModelDTO;
import io.openk9.datasource.service.EmbeddingModelService;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
public class EmbeddingModelGraphqlTest {

	private static final String API_URL = "apiUrl";
	private static final String API_KEY = "apiKey";
	private static final String EMBEDDING_MODEL = "embeddingModel";
	private static final String EMBEDDING_MODEL_DTO = "embeddingModelDTO";
	private static final String EMBEDDING_MODEL_LOCAL = "embedding-model.local";
	private static final String EMBEDDING_MODEL_LOCAL_UPDATED = "embedding-model.local_updated";
	private static final String ENTITY_NAME_PREFIX = "EmbeddingModelGraphqlTest - ";
	private static final String EMBEDDING_MODEL_ONE_NAME = ENTITY_NAME_PREFIX + "Embedding model 1 ";
	private static final String EMBEDDING_MODEL_TWO_NAME = ENTITY_NAME_PREFIX + "Embedding model 2 ";
	private static final String ENTITY = "entity";
	private static final String FIELD = "field";
	private static final String FIELD_VALIDATORS = "fieldValidators";
	private static final String ID = "id";
	private static final String MESSAGE = "message";
	private static final String MODEL = "model";
	private static final String MODEL_VALUE = "model";
	private static final String MODEL_VALUE_UPDATED = "model_updated";
	private static final String NAME = "name";
	private static final String PATCH = "patch";
	private static final String SECRET_KEY = "secret-key";
	private static final String SECRET_KEY_UPDATED = "secret-key_updated";
	private static final String TYPE = "type";
	private static final String TYPE_VALUE = "type";
	private static final String TYPE_VALUE_UPDATED = "type_updated";
	private static final String VECTOR_SIZE = "vectorSize";
	private static final int VECTOR_SIZE_DEFAULT_VALUE = 1500;
	private static final int VECTOR_SIZE_VALUE_UPDATED = 3000;
	private static final Logger log = Logger.getLogger(EmbeddingModelGraphqlTest.class);

	@Inject
	EmbeddingModelService embeddingModelService;

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@BeforeEach
	void setup() {
		createEmbeddingModelTwo();
	}

	@Test
	void should_create_embedding_model_one() throws ExecutionException, InterruptedException {

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					EMBEDDING_MODEL,
					args(
						arg(
							EMBEDDING_MODEL_DTO,
							inputObject(
								prop(NAME, EMBEDDING_MODEL_ONE_NAME),
								prop(API_URL, EMBEDDING_MODEL_LOCAL),
								prop(API_KEY, SECRET_KEY),
								prop(VECTOR_SIZE, VECTOR_SIZE_DEFAULT_VALUE),
								prop(TYPE, TYPE_VALUE),
								prop(MODEL, MODEL_VALUE)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(API_URL),
						field(API_KEY),
						field(VECTOR_SIZE),
						field(TYPE),
						field(MODEL)
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

		var embeddingModelResponse =
			response.getData().getJsonObject(EMBEDDING_MODEL);

		assertNotNull(embeddingModelResponse);

		assertTrue(embeddingModelResponse.isNull(FIELD_VALIDATORS));

		// check if the embeddingModel have the type and model fields
		var embeddingModelOne = getEmbeddingModelOne();

		assertEquals(TYPE_VALUE, embeddingModelOne.getType());
		assertEquals(MODEL_VALUE, embeddingModelOne.getModel());

		// remove the embeddingModelOne
		removeEmbeddingModelOne();
	}

	@Test
	void should_update_embedding_model_two() throws ExecutionException, InterruptedException {

		var embeddingModelTwo = getEmbeddingModelTwo();

		// check initial state
		assertEquals(EMBEDDING_MODEL_LOCAL, embeddingModelTwo.getApiUrl());
		assertEquals(SECRET_KEY, embeddingModelTwo.getApiKey());
		assertEquals(VECTOR_SIZE_DEFAULT_VALUE, embeddingModelTwo.getVectorSize());
		assertEquals(TYPE_VALUE, embeddingModelTwo.getType());
		assertEquals(MODEL_VALUE, embeddingModelTwo.getModel());

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					EMBEDDING_MODEL,
					args(
						arg(ID, embeddingModelTwo.getId()),
						arg(PATCH, false),
						arg(
							EMBEDDING_MODEL_DTO,
							inputObject(
								prop(NAME, EMBEDDING_MODEL_TWO_NAME),
								prop(API_URL, EMBEDDING_MODEL_LOCAL_UPDATED),
								prop(API_KEY, SECRET_KEY_UPDATED),
								prop(VECTOR_SIZE, VECTOR_SIZE_VALUE_UPDATED),
								prop(TYPE, TYPE_VALUE_UPDATED),
								prop(MODEL, MODEL_VALUE_UPDATED)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(API_URL),
						field(API_KEY),
						field(VECTOR_SIZE),
						field(TYPE),
						field(MODEL)
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

		var embeddingModelResponse =
			response.getData().getJsonObject(EMBEDDING_MODEL);

		assertNotNull(embeddingModelResponse);

		assertTrue(embeddingModelResponse.isNull(FIELD_VALIDATORS));

		// check if the embeddingModel has been correctly updated
		var embeddingModelUpdated = getEmbeddingModelTwo();

		assertEquals(EMBEDDING_MODEL_LOCAL_UPDATED, embeddingModelUpdated.getApiUrl());
		assertEquals(SECRET_KEY_UPDATED, embeddingModelUpdated.getApiKey());
		assertEquals(VECTOR_SIZE_VALUE_UPDATED, embeddingModelUpdated.getVectorSize());
		assertEquals(TYPE_VALUE_UPDATED, embeddingModelUpdated.getType());
		assertEquals(MODEL_VALUE_UPDATED, embeddingModelUpdated.getModel());
	}

	@Test
	void should_patched_embedding_model_two() throws ExecutionException, InterruptedException {

		var embeddingModelTwo = getEmbeddingModelTwo();

		// check initial state
		assertEquals(EMBEDDING_MODEL_LOCAL, embeddingModelTwo.getApiUrl());
		assertEquals(SECRET_KEY, embeddingModelTwo.getApiKey());
		assertEquals(VECTOR_SIZE_DEFAULT_VALUE, embeddingModelTwo.getVectorSize());
		assertEquals(TYPE_VALUE, embeddingModelTwo.getType());
		assertEquals(MODEL_VALUE, embeddingModelTwo.getModel());

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					EMBEDDING_MODEL,
					args(
						arg(ID, embeddingModelTwo.getId()),
						arg(PATCH, true),
						arg(
							EMBEDDING_MODEL_DTO,
							inputObject(
								prop(NAME, EMBEDDING_MODEL_TWO_NAME),
								prop(API_URL, EMBEDDING_MODEL_LOCAL_UPDATED),
								prop(API_KEY, SECRET_KEY_UPDATED),
								prop(VECTOR_SIZE, VECTOR_SIZE_VALUE_UPDATED),
								prop(TYPE, TYPE_VALUE_UPDATED),
								prop(MODEL, MODEL_VALUE_UPDATED)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(TYPE),
						field(MODEL)
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

		var embeddingModelResponse =
			response.getData().getJsonObject(EMBEDDING_MODEL);

		assertNotNull(embeddingModelResponse);

		assertTrue(embeddingModelResponse.isNull(FIELD_VALIDATORS));

		// check if the embeddingModel has been correctly updated
		var embeddingModelPatched = getEmbeddingModelTwo();

		assertEquals(EMBEDDING_MODEL_LOCAL_UPDATED, embeddingModelPatched.getApiUrl());
		assertEquals(SECRET_KEY_UPDATED, embeddingModelPatched.getApiKey());
		assertEquals(VECTOR_SIZE_VALUE_UPDATED, embeddingModelPatched.getVectorSize());
		assertEquals(TYPE_VALUE_UPDATED, embeddingModelPatched.getType());
		assertEquals(MODEL_VALUE_UPDATED, embeddingModelPatched.getModel());
	}

	@AfterEach
	void tearDown() {
		removeEmbeddingModelTwo();
	}

	private void createEmbeddingModelTwo() {
		var dto = EmbeddingModelDTO
			.builder()
			.name(EMBEDDING_MODEL_TWO_NAME)
			.apiUrl(EMBEDDING_MODEL_LOCAL)
			.apiKey(SECRET_KEY)
			.vectorSize(VECTOR_SIZE_DEFAULT_VALUE)
			.type(TYPE)
			.model(MODEL)
			.build();

		embeddingModelService.create(dto)
			.await()
			.indefinitely();
	}

	private EmbeddingModel getEmbeddingModelOne() {
		return sessionFactory.withTransaction(
			s -> embeddingModelService.findByName(s, EMBEDDING_MODEL_ONE_NAME)
		)
		.await()
		.indefinitely();
	}

	private EmbeddingModel getEmbeddingModelTwo() {
		return sessionFactory.withTransaction(
				s -> embeddingModelService.findByName(s, EMBEDDING_MODEL_TWO_NAME)
			)
			.await()
			.indefinitely();
	}

	private void removeEmbeddingModelOne() {
		var embeddingModel = getEmbeddingModelOne();

		sessionFactory.withTransaction(
				session -> embeddingModelService.deleteById(embeddingModel.getId())
			)
			.await()
			.indefinitely();
	}

	private void removeEmbeddingModelTwo() {
		var embeddingModel = getEmbeddingModelTwo();

		sessionFactory.withTransaction(
				session -> embeddingModelService.deleteById(embeddingModel.getId())
			)
			.await()
			.indefinitely();
	}
}
