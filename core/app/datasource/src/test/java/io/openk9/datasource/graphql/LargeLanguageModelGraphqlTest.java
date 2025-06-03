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

import io.openk9.datasource.model.LargeLanguageModel;
import io.openk9.datasource.model.dto.base.LargeLanguageModelDTO;
import io.openk9.datasource.model.dto.base.ProviderModelDTO;
import io.openk9.datasource.service.LargeLanguageModelService;
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
public class LargeLanguageModelGraphqlTest {

	private static final String API_KEY = "apiKey";
	private static final String API_KEY_VALUE = "apiKeyValue";
	private static final String API_KEY_VALUE_UPDATED = "apiKeyValue_updated";
	private static final String API_URL = "apiUrl";
	private static final String API_URL_VALUE = "large-language-model.local";
	private static final String API_URL_VALUE_UPDATED = "large-language-model.local_updated";
	private static final String CONTEXT_WINDOW = "contextWindow";
	private static final int CONTEXT_WINDOW_DEFAULT_VALUE = 1500;
	private static final int CONTEXT_WINDOW_VALUE_UPDATED = 3000;
	private static final String ENTITY_NAME_PREFIX = "LargeLanguageModelGraphqlTest - ";
	private static final String LARGE_LANGUAGE_MODEL = "largeLanguageModel";
	private static final String LARGE_LANGUAGE_MODEL_DTO = "largeLanguageModelDTO";
	private static final String LARGE_LANGUAGE_MODEL_ONE_NAME = ENTITY_NAME_PREFIX + "Large language model 1 ";
	private static final String LARGE_LANGUAGE_MODEL_TWO_NAME = ENTITY_NAME_PREFIX + "Large language model 2 ";
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
	private static final String PROVIDER_VALUE = "provider_value";
	private static final String PROVIDER_VALUE_UPDATED = "provider_updated";
	private static final String RETRIEVE_CITATIONS = "retrieveCitations";

	private static final Logger log = Logger.getLogger(LargeLanguageModelGraphqlTest.class);

	@Inject
	LargeLanguageModelService largeLanguageModelService;

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@BeforeEach
	void setup() {
		createLargeLanguageModelTwo();
	}

	@Test
	void should_create_large_language_model_one() throws ExecutionException, InterruptedException {

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					LARGE_LANGUAGE_MODEL,
					args(
						arg(
							LARGE_LANGUAGE_MODEL_DTO,
							inputObject(
								prop(NAME, LARGE_LANGUAGE_MODEL_ONE_NAME),
								prop(API_URL, API_URL_VALUE),
								prop(API_KEY, API_KEY_VALUE),
								prop(JSON_CONFIG, JSON_CONFIG_EMPTY),
								prop(CONTEXT_WINDOW, CONTEXT_WINDOW_DEFAULT_VALUE),
								prop(RETRIEVE_CITATIONS, true),
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
						field(CONTEXT_WINDOW),
						field(RETRIEVE_CITATIONS),
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

		var largeLanguageModelResponse =
			response.getData().getJsonObject(LARGE_LANGUAGE_MODEL);

		assertNotNull(largeLanguageModelResponse);

		assertTrue(largeLanguageModelResponse.isNull(FIELD_VALIDATORS));

		// check the largeLanguageModel fields
		var largeLanguageModelOne = getLargeLanguageModelOne();

		assertEquals(API_KEY_VALUE, largeLanguageModelOne.getApiKey());
		assertEquals(API_URL_VALUE, largeLanguageModelOne.getApiUrl());
		assertEquals(JSON_CONFIG_EMPTY, largeLanguageModelOne.getJsonConfig());
		assertEquals(CONTEXT_WINDOW_DEFAULT_VALUE, largeLanguageModelOne.getContextWindow());
		assertTrue(largeLanguageModelOne.getRetrieveCitations());
		assertEquals(PROVIDER_VALUE, largeLanguageModelOne.getProviderModel().getProvider());
		assertEquals(MODEL_VALUE, largeLanguageModelOne.getProviderModel().getModel());

		// remove the largeLanguageModelOne
		removeLargeLanguageModelOne();
	}

	@Test
	void should_fail_create_large_language_model_one_with_no_providerModel() throws ExecutionException, InterruptedException {

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					LARGE_LANGUAGE_MODEL,
					args(
						arg(
							LARGE_LANGUAGE_MODEL_DTO,
							inputObject(
								prop(NAME, LARGE_LANGUAGE_MODEL_ONE_NAME),
								prop(API_URL, API_URL_VALUE),
								prop(API_KEY, API_KEY_VALUE),
								prop(JSON_CONFIG, JSON_CONFIG_EMPTY),
								prop(CONTEXT_WINDOW, CONTEXT_WINDOW_DEFAULT_VALUE),
								prop(RETRIEVE_CITATIONS, true)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(API_URL),
						field(API_KEY),
						field(CONTEXT_WINDOW),
						field(RETRIEVE_CITATIONS),
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

		var errors = response.getErrors();

		assertEquals(1, errors.size());

		String errorMessage = errors.getFirst().getMessage();

		assertTrue(errorMessage.contains(
			String.format("is missing required fields '[%s]'", PROVIDER_MODEL)));
	}

	@Test
	void should_fail_create_large_language_model_one_with_no_model_and_provider() throws ExecutionException, InterruptedException {

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					LARGE_LANGUAGE_MODEL,
					args(
						arg(
							LARGE_LANGUAGE_MODEL_DTO,
							inputObject(
								prop(NAME, LARGE_LANGUAGE_MODEL_ONE_NAME),
								prop(API_URL, API_URL_VALUE),
								prop(API_KEY, API_KEY_VALUE),
								prop(JSON_CONFIG, JSON_CONFIG_EMPTY),
								prop(CONTEXT_WINDOW, CONTEXT_WINDOW_DEFAULT_VALUE),
								prop(RETRIEVE_CITATIONS, true),
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
						field(CONTEXT_WINDOW),
						field(RETRIEVE_CITATIONS),
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

		var errors = response.getErrors();

		assertEquals(1, errors.size());

		String errorMessage = errors.getFirst().getMessage();

		assertTrue(errorMessage.contains(
			String.format("is missing required fields '[%s, %s]'", MODEL, PROVIDER)));
	}

	@Test
	void should_update_large_language_model_two() throws ExecutionException, InterruptedException {

		var largeLanguageModelTwo = getLargeLanguageModelTwo();

		// check initial state
		assertEquals(API_URL_VALUE, largeLanguageModelTwo.getApiUrl());
		assertEquals(API_KEY_VALUE, largeLanguageModelTwo.getApiKey());
		assertEquals(JSON_CONFIG_EMPTY, largeLanguageModelTwo.getJsonConfig());
		assertEquals(CONTEXT_WINDOW_DEFAULT_VALUE, largeLanguageModelTwo.getContextWindow());
		assertTrue(largeLanguageModelTwo.getRetrieveCitations());
		assertEquals(PROVIDER_VALUE, largeLanguageModelTwo.getProviderModel().getProvider());
		assertEquals(MODEL_VALUE, largeLanguageModelTwo.getProviderModel().getModel());

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					LARGE_LANGUAGE_MODEL,
					args(
						arg(ID, largeLanguageModelTwo.getId()),
						arg(PATCH, false),
						arg(
							LARGE_LANGUAGE_MODEL_DTO,
							inputObject(
								prop(NAME, LARGE_LANGUAGE_MODEL_TWO_NAME),
								prop(API_URL, API_URL_VALUE_UPDATED),
								prop(API_KEY, API_KEY_VALUE_UPDATED),
								prop(JSON_CONFIG, JSON_CONFIG_UPDATED),
								prop(CONTEXT_WINDOW, CONTEXT_WINDOW_VALUE_UPDATED),
								prop(RETRIEVE_CITATIONS, false),
								prop(
									PROVIDER_MODEL,
									inputObject(
										prop(PROVIDER, PROVIDER_VALUE_UPDATED),
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
						field(CONTEXT_WINDOW),
						field(RETRIEVE_CITATIONS),
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

		var largeLanguageModelResponse =
			response.getData().getJsonObject(LARGE_LANGUAGE_MODEL);

		assertNotNull(largeLanguageModelResponse);

		assertTrue(largeLanguageModelResponse.isNull(FIELD_VALIDATORS));

		// check the largeLanguageModel fields
		var largeLanguageModelTwoUpdated = getLargeLanguageModelTwo();

		assertEquals(API_KEY_VALUE_UPDATED, largeLanguageModelTwoUpdated.getApiKey());
		assertEquals(API_URL_VALUE_UPDATED, largeLanguageModelTwoUpdated.getApiUrl());
		assertEquals(JSON_CONFIG_UPDATED, largeLanguageModelTwoUpdated.getJsonConfig());
		assertEquals(CONTEXT_WINDOW_VALUE_UPDATED, largeLanguageModelTwoUpdated.getContextWindow());
		assertFalse(largeLanguageModelTwoUpdated.getRetrieveCitations());
		assertEquals(PROVIDER_VALUE_UPDATED, largeLanguageModelTwoUpdated.getProviderModel().getProvider());
		assertEquals(MODEL_VALUE_UPDATED, largeLanguageModelTwoUpdated.getProviderModel().getModel());
	}

	@Test
	void should_patch_large_language_model_two() throws ExecutionException, InterruptedException {

		var largeLanguageModelTwo = getLargeLanguageModelTwo();

		// check initial state
		assertEquals(API_URL_VALUE, largeLanguageModelTwo.getApiUrl());
		assertEquals(API_KEY_VALUE, largeLanguageModelTwo.getApiKey());
		assertEquals(JSON_CONFIG_EMPTY, largeLanguageModelTwo.getJsonConfig());
		assertEquals(CONTEXT_WINDOW_DEFAULT_VALUE, largeLanguageModelTwo.getContextWindow());
		assertTrue(largeLanguageModelTwo.getRetrieveCitations());
		assertEquals(PROVIDER_VALUE, largeLanguageModelTwo.getProviderModel().getProvider());
		assertEquals(MODEL_VALUE, largeLanguageModelTwo.getProviderModel().getModel());

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					LARGE_LANGUAGE_MODEL,
					args(
						arg(ID, largeLanguageModelTwo.getId()),
						arg(PATCH, true),
						arg(
							LARGE_LANGUAGE_MODEL_DTO,
							inputObject(
								prop(NAME, LARGE_LANGUAGE_MODEL_TWO_NAME),
								prop(API_URL, API_URL_VALUE_UPDATED),
								prop(API_KEY, API_KEY_VALUE_UPDATED),
								prop(JSON_CONFIG, JSON_CONFIG_UPDATED),
								prop(CONTEXT_WINDOW, CONTEXT_WINDOW_VALUE_UPDATED),
								prop(RETRIEVE_CITATIONS, false),
								prop(
									PROVIDER_MODEL,
									inputObject(
										prop(PROVIDER, PROVIDER_VALUE_UPDATED),
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
						field(CONTEXT_WINDOW),
						field(RETRIEVE_CITATIONS),
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

		var largeLanguageModelResponse =
			response.getData().getJsonObject(LARGE_LANGUAGE_MODEL);

		assertNotNull(largeLanguageModelResponse);

		assertTrue(largeLanguageModelResponse.isNull(FIELD_VALIDATORS));

		// check the largeLanguageModel fields
		var largeLanguageModelTwoUpdated = getLargeLanguageModelTwo();

		assertEquals(API_KEY_VALUE_UPDATED, largeLanguageModelTwoUpdated.getApiKey());
		assertEquals(API_URL_VALUE_UPDATED, largeLanguageModelTwoUpdated.getApiUrl());
		assertEquals(JSON_CONFIG_UPDATED, largeLanguageModelTwoUpdated.getJsonConfig());
		assertEquals(CONTEXT_WINDOW_VALUE_UPDATED, largeLanguageModelTwoUpdated.getContextWindow());
		assertFalse(largeLanguageModelTwoUpdated.getRetrieveCitations());
		assertEquals(PROVIDER_VALUE_UPDATED, largeLanguageModelTwoUpdated.getProviderModel().getProvider());
		assertEquals(MODEL_VALUE_UPDATED, largeLanguageModelTwoUpdated.getProviderModel().getModel());
	}

	@AfterEach
	void tearDown() {
		removeLargeLanguageModelTwo();
	}

	private LargeLanguageModel createLargeLanguageModelTwo() {
		var dto = LargeLanguageModelDTO.builder()
			.name(LARGE_LANGUAGE_MODEL_TWO_NAME)
			.apiKey(API_KEY_VALUE)
			.apiUrl(API_URL_VALUE)
			.jsonConfig(JSON_CONFIG_EMPTY)
			.providerModel(
				ProviderModelDTO.builder()
					.provider(PROVIDER_VALUE)
					.model(MODEL_VALUE)
					.build()
			)
			.contextWindow(CONTEXT_WINDOW_DEFAULT_VALUE)
			.retrieveCitations(true)
			.build();

		return sessionFactory.withTransaction(
				session -> largeLanguageModelService.create(session, dto)
			)
			.await()
			.indefinitely();
	}

	private LargeLanguageModel getLargeLanguageModelOne() {
		return sessionFactory.withTransaction(
				session ->
					largeLanguageModelService.findByName(session, LARGE_LANGUAGE_MODEL_ONE_NAME)
			)
			.await()
			.indefinitely();
	}

	private LargeLanguageModel getLargeLanguageModelTwo() {
		return sessionFactory.withTransaction(
				session ->
					largeLanguageModelService.findByName(session, LARGE_LANGUAGE_MODEL_TWO_NAME)
			)
			.await()
			.indefinitely();
	}

	private void removeLargeLanguageModelOne() {
		var largeLanguageModel = getLargeLanguageModelOne();

		sessionFactory.withTransaction(
				session ->
					largeLanguageModelService.deleteById(session, largeLanguageModel.getId())
			)
			.await()
			.indefinitely();
	}

	private void removeLargeLanguageModelTwo() {
		var largeLanguageModel = getLargeLanguageModelTwo();

		sessionFactory.withTransaction(
				session ->
					largeLanguageModelService.deleteById(session, largeLanguageModel.getId())
			)
			.await()
			.indefinitely();
	}
}
