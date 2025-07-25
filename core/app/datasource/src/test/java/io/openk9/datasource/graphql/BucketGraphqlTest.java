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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.dto.request.BucketWithListsDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.RAGConfigurationService;
import io.openk9.datasource.service.SuggestionCategoryService;
import io.openk9.datasource.service.TabService;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class BucketGraphqlTest {

	private static final String ENTITY_NAME_PREFIX = "BucketGraphqlTest - ";

	private static final String BUCKET_ONE_NAME = ENTITY_NAME_PREFIX + "Bucket 1";
	private static final String BUCKET_TWO_NAME = ENTITY_NAME_PREFIX + "Bucket 2";
	private static final String BUCKET_WITH_LISTS = "bucketWithLists";
	private static final String BUCKET_WITH_LISTS_DTO = "bucketWithListsDTO";
	private static final String DATASOURCE_IDS = "datasourceIds";
	private static final String EDGES = "edges";
	private static final String NODE = "node";
	private static final String RAG_CHAT_ONE = ENTITY_NAME_PREFIX + "Rag configuration CHAT 1";
	private static final String RAG_CHAT_TWO = ENTITY_NAME_PREFIX + "Rag configuration CHAT 2";
	private static final String RAG_CHAT_TOOL_ONE = ENTITY_NAME_PREFIX + "Rag configuration CHAT_TOOL 1";
	private static final String RAG_CHAT_TOOL_TWO = ENTITY_NAME_PREFIX + "Rag configuration CHAT_TOOL 2";
	private static final String RAG_CONFIGURATION_CHAT = "ragConfigurationChat";
	private static final String RAG_CONFIGURATION_CHAT_TOOL = "ragConfigurationChatTool";
	private static final String RAG_CONFIGURATION_SIMPLE_GENERATE = "ragConfigurationSimpleGenerate";
	private static final String RAG_SIMPLE_GENERATE_ONE = ENTITY_NAME_PREFIX + "Rag configuration SIMPLE_GENERATE 1";
	private static final String RAG_SIMPLE_GENERATE_TWO = ENTITY_NAME_PREFIX + "Rag configuration SIMPLE_GENERATE 2";
	private static final String ENTITY = "entity";
	private static final String FIELD = "field";
	private static final String FIELD_VALIDATORS = "fieldValidators";
	private static final String ID = "id";
	private static final String MESSAGE = "message";
	private static final String NAME = "name";
	private static final String PATCH = "patch";
	private static final String REFRESH_ON_DATE = "refreshOnDate";
	private static final String REFRESH_ON_QUERY = "refreshOnQuery";
	private static final String REFRESH_ON_TAB = "refreshOnTab";
	private static final String REFRESH_ON_SUGGESTION_CATEGORY = "refreshOnSuggestionCategory";
	private static final String RETRIEVE_TYPE = "retrieveType";
	private static final String TAB_IDS = "tabIds";
	private static final String SUGGESTION_CATEGORY_IDS = "suggestionCategoryIds";
	private static final String SUGGESTION_CATEGORIES = "suggestionCategories";

	@Inject
	BucketService bucketService;

	@Inject
	DatasourceService datasourceService;

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	RAGConfigurationService ragService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	SuggestionCategoryService suggestionCategoryService;

	@Inject
	TabService tabService;

	@BeforeEach
	void setup() {
		// Creates RAGConfigurations
		EntitiesUtils.createRAGConfiguration(
			ragService, RAG_CHAT_ONE, RAGType.CHAT_RAG);
		EntitiesUtils.createRAGConfiguration(
			ragService, RAG_CHAT_TWO, RAGType.CHAT_RAG);
		EntitiesUtils.createRAGConfiguration(
			ragService, RAG_CHAT_TOOL_ONE, RAGType.CHAT_RAG_TOOL);
		EntitiesUtils.createRAGConfiguration(
			ragService, RAG_CHAT_TOOL_TWO, RAGType.CHAT_RAG_TOOL);
		EntitiesUtils.createRAGConfiguration(
			ragService, RAG_SIMPLE_GENERATE_ONE, RAGType.SIMPLE_GENERATE);
		EntitiesUtils.createRAGConfiguration(
			ragService, RAG_SIMPLE_GENERATE_TWO, RAGType.SIMPLE_GENERATE);

		// Retrieves RAGConfigurations one
		var ragConfigurationChatOne =
			EntitiesUtils.getEntity(RAG_CHAT_ONE, ragService, sessionFactory);
		var ragConfigurationChatToolOne =
			EntitiesUtils.getEntity(RAG_CHAT_TOOL_ONE, ragService, sessionFactory);
		var ragConfigurationSimpleOne =
			EntitiesUtils.getEntity(RAG_SIMPLE_GENERATE_ONE, ragService, sessionFactory);

		// Creates Bucket two
		BucketWithListsDTO dtoBucketTwo = BucketWithListsDTO.builder()
			.name(BUCKET_TWO_NAME)
			.refreshOnDate(true)
			.refreshOnQuery(true)
			.refreshOnTab(true)
			.refreshOnSuggestionCategory(true)
			.retrieveType(Bucket.RetrieveType.TEXT)
			.ragConfigurationChat(ragConfigurationChatOne.getId())
			.ragConfigurationChatTool(ragConfigurationChatToolOne.getId())
			.ragConfigurationSimpleGenerate(ragConfigurationSimpleOne.getId())
			.build();

		EntitiesUtils.createBucket(sessionFactory, bucketService, dtoBucketTwo);
	}

	@Test
	void should_create_bucket_with_lists_via_graphql() {

		// Retrieves RAGConfigurations one
		var ragConfigurationChatOne =
			EntitiesUtils.getEntity(RAG_CHAT_ONE, ragService, sessionFactory);
		var ragConfigurationChatToolOne =
			EntitiesUtils.getEntity(RAG_CHAT_TOOL_ONE, ragService, sessionFactory);
		var ragConfigurationSimpleOne =
			EntitiesUtils.getEntity(RAG_SIMPLE_GENERATE_ONE, ragService, sessionFactory);

		var datasourceIds = datasourceService.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		var suggestionCategorieIds = suggestionCategoryService.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		var tabIds = tabService.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		var query = document(
			operation(
				OperationType.MUTATION,
				field(
					BUCKET_WITH_LISTS,
					args(
						arg(
							BUCKET_WITH_LISTS_DTO,
							inputObject(
								prop(NAME, BUCKET_ONE_NAME),
								prop(REFRESH_ON_DATE, true),
								prop(REFRESH_ON_QUERY, true),
								prop(REFRESH_ON_TAB, true),
								prop(REFRESH_ON_SUGGESTION_CATEGORY, true),
								prop(RETRIEVE_TYPE, Bucket.RetrieveType.TEXT),
								prop(DATASOURCE_IDS, datasourceIds),
								prop(TAB_IDS, tabIds),
								prop(SUGGESTION_CATEGORY_IDS, suggestionCategorieIds),
								prop(RAG_CONFIGURATION_CHAT, ragConfigurationChatOne.getId()),
								prop(
									RAG_CONFIGURATION_CHAT_TOOL,
									ragConfigurationChatToolOne.getId()
								),
								prop(
									RAG_CONFIGURATION_SIMPLE_GENERATE,
									ragConfigurationSimpleOne.getId()
								)
							)
						)
					),
					field(
						ENTITY,
						field(ID),
						field(NAME)
					),
					field(
						FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		try {

			var response = graphQLClient.executeSync(query);

			System.out.println(response);

			var jsonObject = response.getData().getJsonObject(BUCKET_WITH_LISTS);

			assertFalse(jsonObject.isNull(ENTITY));
			assertTrue(jsonObject.isNull(FIELD_VALIDATORS));

			var created = EntitiesUtils.getEntity(BUCKET_ONE_NAME, bucketService, sessionFactory);

			// RAGConfiguration created bucket check
			assertEquals(
				ragConfigurationChatOne.getId(), created.getRagConfigurationChat().getId());
			assertEquals(
				ragConfigurationChatToolOne.getId(), created.getRagConfigurationChatTool().getId());
			assertEquals(
				ragConfigurationSimpleOne.getId(),
				created.getRagConfigurationSimpleGenerate().getId());

			// Removes bucketOne
			EntitiesUtils.cleanBucket(bucketService, created);
			EntitiesUtils.removeEntity( BUCKET_ONE_NAME, bucketService, sessionFactory);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Test
	void should_patch_bucket_with_lists_via_graphql() {

		// Retrieves RAGConfigurations one
		var ragConfigurationChatOne =
			EntitiesUtils.getEntity(RAG_CHAT_ONE, ragService, sessionFactory);
		var ragConfigurationChatToolOne =
			EntitiesUtils.getEntity(RAG_CHAT_TOOL_ONE, ragService, sessionFactory);
		var ragConfigurationSimpleOne =
			EntitiesUtils.getEntity(RAG_SIMPLE_GENERATE_ONE, ragService, sessionFactory);

		// Retrieve ragConfigurations two
		var ragConfigurationChatTwo =
			EntitiesUtils.getEntity(
				RAG_CHAT_TWO, ragService, sessionFactory);
		var ragConfigurationChatToolTwo =
			EntitiesUtils.getEntity(
				RAG_CHAT_TOOL_TWO, ragService, sessionFactory);
		var ragConfigurationSimpleTwo =
			EntitiesUtils.getEntity(
				RAG_SIMPLE_GENERATE_TWO, ragService, sessionFactory);

		var bucketTwo = EntitiesUtils.getEntity(BUCKET_TWO_NAME, bucketService, sessionFactory);

		// RAGConfiguration initial check
		assertEquals(ragConfigurationChatOne.getId(), bucketTwo.getRagConfigurationChat().getId());
		assertEquals(
			ragConfigurationChatToolOne.getId(), bucketTwo.getRagConfigurationChatTool().getId());
		assertEquals(
			ragConfigurationSimpleOne.getId(),
			bucketTwo.getRagConfigurationSimpleGenerate().getId());

		var datasourceIds = datasourceService.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		var suggestionCategorieIds = suggestionCategoryService.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		var query = document(
			operation(
				OperationType.MUTATION,
				field(
					BUCKET_WITH_LISTS,
					args(
						arg(ID, bucketTwo.getId()),
						arg(PATCH, true),
						arg(
							BUCKET_WITH_LISTS_DTO,
							inputObject(
								prop(NAME, bucketTwo.getName()),
								prop(REFRESH_ON_DATE, true),
								prop(REFRESH_ON_QUERY, true),
								prop(REFRESH_ON_TAB, true),
								prop(REFRESH_ON_SUGGESTION_CATEGORY, true),
								prop(RETRIEVE_TYPE, Bucket.RetrieveType.TEXT),
								prop(SUGGESTION_CATEGORY_IDS, suggestionCategorieIds
									.stream()
									.limit(1)
									.collect(Collectors.toSet())
								),
								prop(DATASOURCE_IDS, datasourceIds),
								prop(TAB_IDS, List.of()),
								prop(RAG_CONFIGURATION_CHAT, ragConfigurationChatTwo.getId()),
								prop(
									RAG_CONFIGURATION_CHAT_TOOL,
									ragConfigurationChatToolTwo.getId()
								),
								prop(
									RAG_CONFIGURATION_SIMPLE_GENERATE,
									ragConfigurationSimpleTwo.getId()
								)
							)
						)
					),
					field(
						ENTITY,
						field(ID),
						field(NAME),
						field(
							SUGGESTION_CATEGORIES,
							field(
								EDGES,
								field(
									NODE,
									field(ID)
								)
							)
						)
					),
					field(
						FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);

		try {

			var response = graphQLClient.executeSync(query);

			var jsonObject = response.getData().getJsonObject(BUCKET_WITH_LISTS);

			assertFalse(jsonObject.isNull(ENTITY));
			assertTrue(jsonObject.isNull(FIELD_VALIDATORS));

			var patched =
				EntitiesUtils.getEntity(bucketTwo.getName(), bucketService, sessionFactory);

			// RAGConfiguration patched bucketTwo check
			assertEquals(
				ragConfigurationChatTwo.getId(), patched.getRagConfigurationChat().getId());
			assertEquals(
				ragConfigurationChatToolTwo.getId(), patched.getRagConfigurationChatTool().getId());
			assertEquals(
				ragConfigurationSimpleTwo.getId(),
				patched.getRagConfigurationSimpleGenerate().getId());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@AfterEach
	void tearDown() {
		// Removes Bucket two
		var bucketTwo = EntitiesUtils.getEntity(BUCKET_TWO_NAME, bucketService, sessionFactory);
		EntitiesUtils.cleanBucket(bucketService, bucketTwo);
		EntitiesUtils.removeEntity(bucketTwo.getName(), bucketService, sessionFactory);

		// Removes RAGConfigurations
		EntitiesUtils.removeEntity(RAG_CHAT_ONE, ragService, sessionFactory);
		EntitiesUtils.removeEntity(RAG_CHAT_TWO, ragService, sessionFactory);
		EntitiesUtils.removeEntity(RAG_CHAT_TOOL_ONE, ragService, sessionFactory);
		EntitiesUtils.removeEntity(RAG_CHAT_TOOL_TWO, ragService, sessionFactory);
		EntitiesUtils.removeEntity(RAG_SIMPLE_GENERATE_ONE, ragService, sessionFactory);
		EntitiesUtils.removeEntity(RAG_SIMPLE_GENERATE_TWO, ragService, sessionFactory);
	}
}
