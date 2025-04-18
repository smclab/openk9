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

import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;
import io.openk9.datasource.model.dto.base.ProviderModelDTO;
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
	private static final String JSON_CONFIG = "jsonConfig";
	private static final String JSON_CONFIG_EMPTY = "{}";
	private static final String JSON_CONFIG_UPDATED = "{\n" +
		"  \"object1\": {\n" +
		"    \"id\": 1,\n" +
		"    \"name\": \"Test Object 1\",\n" +
		"    \"value\": \"Sample Value 1\"\n" +
		"  },\n" +
		"  \"object2\": {\n" +
		"    \"id\": 2,\n" +
		"    \"name\": \"Test Object 2\",\n" +
		"    \"value\": \"Sample Value 2\"\n" +
		"  },\n" +
		"  \"configurations\": [\n" +
		"    {\n" +
		"      \"key\": \"config1\",\n" +
		"      \"enabled\": true\n" +
		"    },\n" +
		"    {\n" +
		"      \"key\": \"config2\",\n" +
		"      \"enabled\": false\n" +
		"    }\n" +
		"  ]\n" +
		"}";
	private static final String MESSAGE = "message";
	private static final String MODEL = "model";
	private static final String MODEL_VALUE = "model_value";
	private static final String MODEL_VALUE_UPDATED = "model_updated";
	private static final String NAME = "name";
	private static final String PATCH = "patch";
	private static final String PROVIDER = "provider";
	private static final String PROVIDER_MODEL = "providerModel";
	private static final String PROVIDER_UPDATED = "provider_updated";
	private static final String PROVIDER_VALUE = "provider_value";
	private static final String SECRET_KEY = "secret-key";
	private static final String SECRET_KEY_UPDATED = "secret-key_updated";
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
								prop(JSON_CONFIG, JSON_CONFIG_EMPTY),
								prop(
									PROVIDER_MODEL,
									inputObject(
										prop(PROVIDER, PROVIDER_VALUE),
										prop(MODEL, MODEL_VALUE)
									)
								)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(API_URL),
						field(API_KEY),
						field(VECTOR_SIZE),
						field(JSON_CONFIG),
						field(PROVIDER_MODEL,
							field(PROVIDER),
							field(MODEL)
						)
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

		// check if the embeddingModel have the provider and model fields
		var embeddingModelOne = getEmbeddingModelOne();

		assertEquals(PROVIDER_VALUE, embeddingModelOne.getProviderModel().getProvider());
		assertEquals(MODEL_VALUE, embeddingModelOne.getProviderModel().getModel());
		assertEquals(JSON_CONFIG_EMPTY, embeddingModelOne.getJsonConfig());

		// remove the embeddingModelOne
		removeEmbeddingModelOne();
	}

	@Test
	void should_fail_create_embedding_model_one_with_no_providerModel() throws ExecutionException, InterruptedException {

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
								prop(JSON_CONFIG, JSON_CONFIG_EMPTY)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(API_URL),
						field(API_KEY),
						field(VECTOR_SIZE),
						field(JSON_CONFIG),
						field(PROVIDER_MODEL,
							field(PROVIDER),
							field(MODEL)
						)
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

		assertTrue(response.hasError());

		var embeddingModelErrors = response.getErrors();

		assertEquals(1, embeddingModelErrors.size());

		String errorMessage = embeddingModelErrors.getFirst().getMessage();

		assertTrue(errorMessage.contains(
			String.format("is missing required fields '[%s]'", PROVIDER_MODEL)));
	}

	@Test
	void should_fail_create_embedding_model_one_with_no_model_and_provider() throws ExecutionException, InterruptedException {

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
								prop(JSON_CONFIG, JSON_CONFIG_EMPTY),
								prop(
									PROVIDER_MODEL,
									inputObject()
								)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(API_URL),
						field(API_KEY),
						field(VECTOR_SIZE),
						field(JSON_CONFIG),
						field(PROVIDER_MODEL,
							field(PROVIDER),
							field(MODEL)
						)
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

		assertTrue(response.hasError());

		var embeddingModelErrors = response.getErrors();

		assertEquals(1, embeddingModelErrors.size());

		String errorMessage = embeddingModelErrors.getFirst().getMessage();

		assertTrue(errorMessage.contains(
			String.format("is missing required fields '[%s, %s]'", MODEL, PROVIDER)));
	}

	@Test
	void should_update_embedding_model_two() throws ExecutionException, InterruptedException {

		var embeddingModelTwo = getEmbeddingModelTwo();

		// check initial state
		assertEquals(EMBEDDING_MODEL_LOCAL, embeddingModelTwo.getApiUrl());
		assertEquals(SECRET_KEY, embeddingModelTwo.getApiKey());
		assertEquals(VECTOR_SIZE_DEFAULT_VALUE, embeddingModelTwo.getVectorSize());
		assertEquals(PROVIDER_VALUE, embeddingModelTwo.getProviderModel().getProvider());
		assertEquals(MODEL_VALUE, embeddingModelTwo.getProviderModel().getModel());
		assertEquals(JSON_CONFIG_EMPTY, embeddingModelTwo.getJsonConfig());

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
								prop(JSON_CONFIG, JSON_CONFIG_UPDATED),
								prop(
									PROVIDER_MODEL,
									inputObject(
										prop(PROVIDER, PROVIDER_UPDATED),
										prop(MODEL, MODEL_VALUE_UPDATED)
									)
								)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(API_URL),
						field(API_KEY),
						field(VECTOR_SIZE),
						field(JSON_CONFIG),
						field(PROVIDER_MODEL,
							field(PROVIDER),
							field(MODEL)
						)
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
		assertEquals(PROVIDER_UPDATED, embeddingModelUpdated.getProviderModel().getProvider());
		assertEquals(MODEL_VALUE_UPDATED, embeddingModelUpdated.getProviderModel().getModel());
		assertEquals(JSON_CONFIG_UPDATED, embeddingModelUpdated.getJsonConfig());
	}

	@Test
	void should_patch_embedding_model_two() throws ExecutionException, InterruptedException {

		var embeddingModelTwo = getEmbeddingModelTwo();

		// check initial state
		assertEquals(EMBEDDING_MODEL_LOCAL, embeddingModelTwo.getApiUrl());
		assertEquals(SECRET_KEY, embeddingModelTwo.getApiKey());
		assertEquals(VECTOR_SIZE_DEFAULT_VALUE, embeddingModelTwo.getVectorSize());
		assertEquals(PROVIDER_VALUE, embeddingModelTwo.getProviderModel().getProvider());
		assertEquals(MODEL_VALUE, embeddingModelTwo.getProviderModel().getModel());
		assertEquals(JSON_CONFIG_EMPTY, embeddingModelTwo.getJsonConfig());

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
								prop(JSON_CONFIG, JSON_CONFIG_UPDATED),
								prop(
									PROVIDER_MODEL,
									inputObject(
										prop(PROVIDER, PROVIDER_UPDATED),
										prop(MODEL, MODEL_VALUE_UPDATED)
									)
								)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(JSON_CONFIG),
						field(PROVIDER_MODEL,
							field(PROVIDER),
							field(MODEL)
						)
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
		assertEquals(PROVIDER_UPDATED, embeddingModelPatched.getProviderModel().getProvider());
		assertEquals(MODEL_VALUE_UPDATED, embeddingModelPatched.getProviderModel().getModel());
		assertEquals(JSON_CONFIG_UPDATED, embeddingModelPatched.getJsonConfig());
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
			.jsonConfig(JSON_CONFIG_EMPTY)
			.providerModel(
				ProviderModelDTO
					.builder()
					.provider(PROVIDER_VALUE)
					.model(MODEL_VALUE)
					.build()
			)
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
