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

import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.dto.base.RAGConfigurationDTO;
import io.openk9.datasource.service.RAGConfigurationService;
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
public class RAGConfigurationGraphqlTest {

	private static final String CHUNK_WINDOW = "chunkWindow";
	private static final String ENTITY_NAME_PREFIX = "RAGConfigurationGraphqlTest - ";
	private static final int CHUNK_WINDOW_VALUE = 1500;
	private static final int CHUNK_WINDOW_VALUE_UPDATED = 3000;
	private static final Integer DEFAULT_CHUNK_WINDOW = 0;
	private static final String DEFAULT_PROMPT_EMPTY_STRING = "";
	private static final Boolean DEFAULT_REFORMULATE = false;
	private static final String ENTITY = "entity";
	private static final String FIELD = "field";
	private static final String FIELD_VALIDATORS = "fieldValidators";
	private static final String ID = "id";
	private static final String MESSAGE = "message";
	private static final String NAME = "name";
	private static final String PATCH = "patch";
	private static final String PROMPT = "prompt";
	private static final String PROMPT_EMPTY = "";
	private static final String PROMPT_EXAMPLE = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris sit" +
		" amet diam a lorem aliquam pellentesque. Morbi dapibus porttitor quam, id porta elit ultrices vel." +
		" Donec eget ex rutrum, rutrum lectus eget, molestie libero. Nunc at commodo odio. Proin tempus ipsum ac" +
		" lectus mattis, vitae porttitor turpis interdum. Etiam vitae mi sit amet diam efficitur dapibus. Nulla" +
		" egestas, tellus maximus fringilla tincidunt, sapien urna dictum quam, quis consectetur metus urna vel mi." +
		" Proin eleifend, mi pulvinar semper dapibus, massa mi vestibulum est, sit amet finibus odio augue a elit.\n" +
		"\n" +
		"Donec in hendrerit metus, interdum egestas neque. Praesent eget eros sit amet ipsum congue sollicitudin." +
		" Curabitur sit amet tincidunt enim. Phasellus consequat vulputate hendrerit. Morbi in aliquam diam. Morbi" +
		" sem dui, fringilla blandit consectetur ut, imperdiet nec orci. Nulla non quam et velit lacinia maximus.\n" +
		"\n" +
		"Fusce posuere egestas dapibus. Orci varius natoque penatibus et magnis dis parturient montes, nascetur" +
		" ridiculus mus. Nullam sit amet venenatis massa, eget luctus velit. Pellentesque quis blandit sem, ut" +
		" hendrerit purus. Duis nunc purus, accumsan non aliquet non, viverra a odio. Vivamus sed nunc ullamcorper," +
		" volutpat lacus eget, accumsan odio. Integer tincidunt lectus non justo scelerisque hendrerit. Maecenas" +
		" pellentesque gravida magna sed fringilla. Donec fringilla quam eget massa elementum, at pulvinar velit" +
		" facilisis. Donec finibus ipsum sed justo faucibus, sollicitudin vestibulum diam suscipit. Etiam non sem" +
		" vel mi imperdiet ultricies vel sed metus. Quisque luctus massa magna, a mollis eros pretium gravida." +
		" Nam quis libero metus. Cras tellus turpis, imperdiet at consectetur vel, lobortis vitae enim.\n" +
		"\n" +
		"Nullam vitae eros ac eros sagittis fermentum ut facilisis augue. Nunc euismod ultricies tellus. Aliquam" +
		" erat volutpat. Nunc rhoncus ligula arcu, sed mollis mauris scelerisque lacinia. Pellentesque habitant" +
		" morbi tristique senectus et netus et malesuada fames ac turpis egestas. Cras pretium nibh sed dapibus" +
		" pellentesque. Nulla at sem sem. Aenean scelerisque suscipit mauris, eu porta magna luctus a. Vestibulum" +
		" nisl turpis, congue vel libero faucibus, ultricies aliquet elit. Donec at diam nec odio egestas euismod." +
		" Sed eu vulputate orci. Aenean mauris turpis, maximus vel dapibus a, luctus vitae sapien. Aliquam erat volutpat.\n" +
		"\n" +
		"Sed tellus ex, dignissim in eros in, iaculis commodo mauris. Vestibulum ornare leo vel sapien maximus, " +
		"a auctor leo hendrerit. Aenean sapien urna, vestibulum ac ex iaculis, venenatis accumsan elit. Nam luctus" +
		" faucibus nibh et fermentum. Pellentesque ipsum tortor, volutpat eu porta nec, imperdiet in elit. Proin" +
		" pellentesque neque tincidunt enim bibendum bibendum. Vestibulum ante ipsum primis in faucibus orci luctus " +
		"et ultrices posuere cubilia curae; Morbi consequat et leo sed faucibus.";
	private static final String PROMPT_NO_RAG = "promptNoRag";
	private static final String RAG_CONFIGURATION = "ragConfiguration";
	private static final String RAG_CONFIGURATION_DTO = "ragConfigurationDTO";
	private static final String RAG_CONFIGURATION_ONE_NAME = ENTITY_NAME_PREFIX + "RAG Configuration 1 ";
	private static final String RAG_CONFIGURATION_TWO_NAME = ENTITY_NAME_PREFIX + "RAG Configuration 2 ";
	private static final String TYPE = "type";
	private static final String REPHRASE_PROMPT = "rephrasePrompt";
	private static final String RAG_TOOL_DESCRIPTION = "ragToolDescription";
	private static final String REFORMULATE = "reformulate";
	private static final Logger log = Logger.getLogger(RAGConfigurationGraphqlTest.class);

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	RAGConfigurationService ragConfigurationService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@BeforeEach
	void setup() {
		createRAGConfigurationTwo();
	}

	@Test
	void should_create_rag_configuration_one() throws ExecutionException, InterruptedException {

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					RAG_CONFIGURATION,
					args(
						arg(
							RAG_CONFIGURATION_DTO,
							inputObject(
								prop(NAME, RAG_CONFIGURATION_ONE_NAME),
								prop(TYPE, RAGType.CHAT),
								prop(PROMPT, PROMPT_EXAMPLE),
								prop(REPHRASE_PROMPT, PROMPT_EXAMPLE),
								prop(PROMPT_NO_RAG, PROMPT_EXAMPLE),
								prop(RAG_TOOL_DESCRIPTION, PROMPT_EXAMPLE),
								prop(CHUNK_WINDOW, CHUNK_WINDOW_VALUE),
								prop(REFORMULATE, true)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(TYPE),
						field(PROMPT),
						field(REPHRASE_PROMPT),
						field(PROMPT_NO_RAG),
						field(RAG_TOOL_DESCRIPTION),
						field(CHUNK_WINDOW),
						field(REFORMULATE)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response:\n%s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var ragConfigurationResponse = response.getData().getJsonObject(RAG_CONFIGURATION);

		assertNotNull(ragConfigurationResponse);
		assertTrue(ragConfigurationResponse.isNull(FIELD_VALIDATORS));

		var ragConfigurationOne = getRagConfigurationOne();

		assertEquals(RAG_CONFIGURATION_ONE_NAME, ragConfigurationOne.getName());
		assertEquals(RAGType.CHAT, ragConfigurationOne.getType());
		assertEquals(PROMPT_EXAMPLE, ragConfigurationOne.getPrompt());
		assertEquals(PROMPT_EXAMPLE, ragConfigurationOne.getRephrasePrompt());
		assertEquals(PROMPT_EXAMPLE, ragConfigurationOne.getPromptNoRag());
		assertEquals(PROMPT_EXAMPLE, ragConfigurationOne.getRagToolDescription());
		assertEquals(CHUNK_WINDOW_VALUE, ragConfigurationOne.getChunkWindow());
		assertTrue(ragConfigurationOne.getReformulate());

		removeRAGConfigurationOne();
	}

	@Test
	void should_create_rag_configuration_one_with_only_name_and_type() throws ExecutionException, InterruptedException {

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					RAG_CONFIGURATION,
					args(
						arg(
							RAG_CONFIGURATION_DTO,
							inputObject(
								prop(NAME, RAG_CONFIGURATION_ONE_NAME),
								prop(TYPE, RAGType.CHAT)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(TYPE),
						field(PROMPT),
						field(REPHRASE_PROMPT),
						field(PROMPT_NO_RAG),
						field(RAG_TOOL_DESCRIPTION),
						field(CHUNK_WINDOW),
						field(REFORMULATE)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response:\n%s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var ragConfigurationResponse = response.getData().getJsonObject(RAG_CONFIGURATION);

		assertNotNull(ragConfigurationResponse);
		assertTrue(ragConfigurationResponse.isNull(FIELD_VALIDATORS));

		var ragConfigurationOne = getRagConfigurationOne();

		assertEquals(RAG_CONFIGURATION_ONE_NAME, ragConfigurationOne.getName());
		assertEquals(RAGType.CHAT, ragConfigurationOne.getType());
		assertEquals(DEFAULT_PROMPT_EMPTY_STRING, ragConfigurationOne.getPrompt());
		assertEquals(DEFAULT_PROMPT_EMPTY_STRING, ragConfigurationOne.getRephrasePrompt());
		assertEquals(DEFAULT_PROMPT_EMPTY_STRING, ragConfigurationOne.getPromptNoRag());
		assertEquals(DEFAULT_PROMPT_EMPTY_STRING, ragConfigurationOne.getRagToolDescription());
		assertEquals(DEFAULT_CHUNK_WINDOW, ragConfigurationOne.getChunkWindow());
		assertEquals(DEFAULT_REFORMULATE, ragConfigurationOne.getReformulate());

		removeRAGConfigurationOne();
	}

	@Test
	void should_patch_rag_configuration_two() throws ExecutionException, InterruptedException {

		// check initial state
		var initialRagConfigurationTwo = getRagConfigurationTwo();

		assertEquals(RAG_CONFIGURATION_TWO_NAME, initialRagConfigurationTwo.getName());
		assertEquals(RAGType.CHAT, initialRagConfigurationTwo.getType());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getPrompt());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getRephrasePrompt());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getPromptNoRag());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getRagToolDescription());
		assertEquals(CHUNK_WINDOW_VALUE, initialRagConfigurationTwo.getChunkWindow());
		assertTrue(initialRagConfigurationTwo.getReformulate());

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					RAG_CONFIGURATION,
					args(
						arg(ID, initialRagConfigurationTwo.getId()),
						arg(PATCH, true),
						arg(
							RAG_CONFIGURATION_DTO,
							inputObject(
								prop(NAME, RAG_CONFIGURATION_TWO_NAME),
								prop(TYPE, RAGType.SEARCH),
								prop(PROMPT, PROMPT_EMPTY),
								prop(REPHRASE_PROMPT, PROMPT_EMPTY),
								prop(PROMPT_NO_RAG, PROMPT_EMPTY),
								prop(RAG_TOOL_DESCRIPTION, PROMPT_EMPTY),
								prop(CHUNK_WINDOW, CHUNK_WINDOW_VALUE_UPDATED),
								prop(REFORMULATE, false)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(TYPE),
						field(PROMPT),
						field(REPHRASE_PROMPT),
						field(PROMPT_NO_RAG),
						field(RAG_TOOL_DESCRIPTION),
						field(CHUNK_WINDOW),
						field(REFORMULATE)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response:\n%s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var ragConfigurationResponse = response.getData().getJsonObject(RAG_CONFIGURATION);

		assertNotNull(ragConfigurationResponse);
		assertTrue(ragConfigurationResponse.isNull(FIELD_VALIDATORS));

		var ragConfigurationTwo = getRagConfigurationTwo();

		assertEquals(RAG_CONFIGURATION_TWO_NAME, ragConfigurationTwo.getName());
		// type is an immutable field
		assertEquals(RAGType.CHAT, ragConfigurationTwo.getType());
		assertEquals(PROMPT_EMPTY, ragConfigurationTwo.getPrompt());
		assertEquals(PROMPT_EMPTY, ragConfigurationTwo.getRephrasePrompt());
		assertEquals(PROMPT_EMPTY, ragConfigurationTwo.getPromptNoRag());
		assertEquals(PROMPT_EMPTY, ragConfigurationTwo.getRagToolDescription());
		assertEquals(CHUNK_WINDOW_VALUE_UPDATED, ragConfigurationTwo.getChunkWindow());
		assertFalse(ragConfigurationTwo.getReformulate());
	}

	@Test
	void should_patch_rag_configuration_two_with_no_fields() throws ExecutionException, InterruptedException {

		// check initial state
		var initialRagConfigurationTwo = getRagConfigurationTwo();

		assertEquals(RAG_CONFIGURATION_TWO_NAME, initialRagConfigurationTwo.getName());
		assertEquals(RAGType.CHAT, initialRagConfigurationTwo.getType());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getPrompt());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getRephrasePrompt());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getPromptNoRag());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getRagToolDescription());
		assertEquals(CHUNK_WINDOW_VALUE, initialRagConfigurationTwo.getChunkWindow());
		assertTrue(initialRagConfigurationTwo.getReformulate());

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					RAG_CONFIGURATION,
					args(
						arg(ID, initialRagConfigurationTwo.getId()),
						arg(PATCH, true),
						arg(
							RAG_CONFIGURATION_DTO,
							inputObject(
								prop(NAME, RAG_CONFIGURATION_TWO_NAME),
								prop(TYPE, RAGType.CHAT)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(TYPE),
						field(PROMPT),
						field(REPHRASE_PROMPT),
						field(PROMPT_NO_RAG),
						field(RAG_TOOL_DESCRIPTION),
						field(CHUNK_WINDOW),
						field(REFORMULATE)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response:\n%s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var ragConfigurationResponse = response.getData().getJsonObject(RAG_CONFIGURATION);

		assertNotNull(ragConfigurationResponse);
		assertTrue(ragConfigurationResponse.isNull(FIELD_VALIDATORS));

		var ragConfigurationTwo = getRagConfigurationTwo();

		assertEquals(RAG_CONFIGURATION_TWO_NAME, ragConfigurationTwo.getName());
		assertEquals(RAGType.CHAT, ragConfigurationTwo.getType());
		assertEquals(PROMPT_EXAMPLE, ragConfigurationTwo.getPrompt());
		assertEquals(PROMPT_EXAMPLE, ragConfigurationTwo.getRephrasePrompt());
		assertEquals(PROMPT_EXAMPLE, ragConfigurationTwo.getPromptNoRag());
		assertEquals(PROMPT_EXAMPLE, ragConfigurationTwo.getRagToolDescription());
		assertEquals(CHUNK_WINDOW_VALUE, ragConfigurationTwo.getChunkWindow());
		assertTrue(ragConfigurationTwo.getReformulate());
	}

	@Test
	void should_update_rag_configuration_two() throws ExecutionException, InterruptedException {

		// check initial state
		var initialRagConfigurationTwo = getRagConfigurationTwo();

		assertEquals(RAG_CONFIGURATION_TWO_NAME, initialRagConfigurationTwo.getName());
		assertEquals(RAGType.CHAT, initialRagConfigurationTwo.getType());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getPrompt());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getRephrasePrompt());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getPromptNoRag());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getRagToolDescription());
		assertEquals(CHUNK_WINDOW_VALUE, initialRagConfigurationTwo.getChunkWindow());
		assertTrue(initialRagConfigurationTwo.getReformulate());

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					RAG_CONFIGURATION,
					args(
						arg(ID, initialRagConfigurationTwo.getId()),
						arg(PATCH, false),
						arg(
							RAG_CONFIGURATION_DTO,
							inputObject(
								prop(NAME, RAG_CONFIGURATION_TWO_NAME),
								prop(TYPE, RAGType.SEARCH),
								prop(PROMPT, PROMPT_EMPTY),
								prop(REPHRASE_PROMPT, PROMPT_EMPTY),
								prop(PROMPT_NO_RAG, PROMPT_EMPTY),
								prop(RAG_TOOL_DESCRIPTION, PROMPT_EMPTY),
								prop(CHUNK_WINDOW, CHUNK_WINDOW_VALUE_UPDATED),
								prop(REFORMULATE, false)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(TYPE),
						field(PROMPT),
						field(REPHRASE_PROMPT),
						field(PROMPT_NO_RAG),
						field(RAG_TOOL_DESCRIPTION),
						field(CHUNK_WINDOW),
						field(REFORMULATE)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response:\n%s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var ragConfigurationResponse = response.getData().getJsonObject(RAG_CONFIGURATION);

		assertNotNull(ragConfigurationResponse);
		assertTrue(ragConfigurationResponse.isNull(FIELD_VALIDATORS));

		var ragConfigurationTwo = getRagConfigurationTwo();

		assertEquals(RAG_CONFIGURATION_TWO_NAME, ragConfigurationTwo.getName());
		// type is an immutable field
		assertEquals(RAGType.CHAT, ragConfigurationTwo.getType());
		assertEquals(PROMPT_EMPTY, ragConfigurationTwo.getPrompt());
		assertEquals(PROMPT_EMPTY, ragConfigurationTwo.getRephrasePrompt());
		assertEquals(PROMPT_EMPTY, ragConfigurationTwo.getPromptNoRag());
		assertEquals(PROMPT_EMPTY, ragConfigurationTwo.getRagToolDescription());
		assertEquals(CHUNK_WINDOW_VALUE_UPDATED, ragConfigurationTwo.getChunkWindow());
		assertFalse(ragConfigurationTwo.getReformulate());
	}

	@Test
	void should_update_rag_configuration_two_with_no_fields() throws ExecutionException, InterruptedException {

		// check initial state
		var initialRagConfigurationTwo = getRagConfigurationTwo();

		assertEquals(RAG_CONFIGURATION_TWO_NAME, initialRagConfigurationTwo.getName());
		assertEquals(RAGType.CHAT, initialRagConfigurationTwo.getType());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getPrompt());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getRephrasePrompt());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getPromptNoRag());
		assertEquals(PROMPT_EXAMPLE, initialRagConfigurationTwo.getRagToolDescription());
		assertEquals(CHUNK_WINDOW_VALUE, initialRagConfigurationTwo.getChunkWindow());
		assertTrue(initialRagConfigurationTwo.getReformulate());

		var mutation = document(
			operation(
				OperationType.MUTATION,
				field(
					RAG_CONFIGURATION,
					args(
						arg(ID, initialRagConfigurationTwo.getId()),
						arg(PATCH, false),
						arg(
							RAG_CONFIGURATION_DTO,
							inputObject(
								prop(NAME, RAG_CONFIGURATION_TWO_NAME),
								prop(TYPE, RAGType.CHAT)
							)
						)
					),
					field(ENTITY,
						field(ID),
						field(NAME),
						field(TYPE),
						field(PROMPT),
						field(REPHRASE_PROMPT),
						field(PROMPT_NO_RAG),
						field(RAG_TOOL_DESCRIPTION),
						field(CHUNK_WINDOW),
						field(REFORMULATE)
					),
					field(FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(mutation);

		log.info(String.format("Response:\n%s", response));

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var ragConfigurationResponse = response.getData().getJsonObject(RAG_CONFIGURATION);

		assertNotNull(ragConfigurationResponse);
		assertTrue(ragConfigurationResponse.isNull(FIELD_VALIDATORS));

		var ragConfigurationTwo = getRagConfigurationTwo();

		assertEquals(RAG_CONFIGURATION_TWO_NAME, ragConfigurationTwo.getName());
		assertEquals(RAGType.CHAT, ragConfigurationTwo.getType());
		assertEquals(DEFAULT_PROMPT_EMPTY_STRING, ragConfigurationTwo.getPrompt());
		assertEquals(DEFAULT_PROMPT_EMPTY_STRING, ragConfigurationTwo.getRephrasePrompt());
		assertEquals(DEFAULT_PROMPT_EMPTY_STRING, ragConfigurationTwo.getPromptNoRag());
		assertEquals(DEFAULT_PROMPT_EMPTY_STRING, ragConfigurationTwo.getRagToolDescription());
		assertEquals(DEFAULT_CHUNK_WINDOW, ragConfigurationTwo.getChunkWindow());
		assertEquals(DEFAULT_REFORMULATE, ragConfigurationTwo.getReformulate());
	}

	@AfterEach
	void tearDown() {
		removeRAGConfigurationTwo();
	}

	private RAGConfiguration createRAGConfigurationTwo() {
		var dto = RAGConfigurationDTO.builder()
			.name(RAG_CONFIGURATION_TWO_NAME)
			.type(RAGType.CHAT)
			.rephrasePrompt(PROMPT_EXAMPLE)
			.prompt(PROMPT_EXAMPLE)
			.promptNoRag(PROMPT_EXAMPLE)
			.ragToolDescription(PROMPT_EXAMPLE)
			.chunkWindow(CHUNK_WINDOW_VALUE)
			.reformulate(true)
			.build();

		return sessionFactory.withTransaction(
			session ->
				ragConfigurationService.create(session, dto)
		)
		.await()
		.indefinitely();
	}

	private RAGConfiguration getRagConfigurationOne() {
		return sessionFactory.withTransaction(
			session ->
				ragConfigurationService.findByName(session, RAG_CONFIGURATION_ONE_NAME)
		)
		.await()
		.indefinitely();
	}

	private RAGConfiguration getRagConfigurationTwo() {
		return sessionFactory.withTransaction(
				session ->
					ragConfigurationService.findByName(session, RAG_CONFIGURATION_TWO_NAME)
			)
			.await()
			.indefinitely();
	}

	private RAGConfiguration removeRAGConfigurationOne() {
		var ragConfiguration = getRagConfigurationOne();

		return sessionFactory.withTransaction(
				session ->
					ragConfigurationService.deleteById(session, ragConfiguration.getId())
			)
			.await()
			.indefinitely();
	}

	private RAGConfiguration removeRAGConfigurationTwo() {
		var ragConfiguration = getRagConfigurationTwo();

		return sessionFactory.withTransaction(
				session ->
					ragConfigurationService.deleteById(session, ragConfiguration.getId())
			)
			.await()
			.indefinitely();
	}
}
