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

package io.openk9.datasource.service;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.Initializer;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.dto.request.BucketWithListsDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Enum.gqlEnum;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.InputObject.inputObject;
import static io.smallrye.graphql.client.core.InputObjectField.prop;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateBucketTest {

	private static final String ENTITY_NAME_PREFIX = "CreateBucketTest - ";

	private static final String BUCKET_ONE_NAME = ENTITY_NAME_PREFIX + "Bucket 1";
	private static final String BUCKET_TWO_NAME = ENTITY_NAME_PREFIX + "Bucket 2";
	private static final String RAG_CHAT_ONE = ENTITY_NAME_PREFIX + "Rag configuration CHAT 1";
	private static final String RAG_CHAT_TWO = ENTITY_NAME_PREFIX + "Rag configuration CHAT 2";
	private static final String RAG_CHAT_TOOL_ONE = ENTITY_NAME_PREFIX + "Rag configuration CHAT_TOOL 1";
	private static final String RAG_CHAT_TOOL_TWO = ENTITY_NAME_PREFIX + "Rag configuration CHAT_TOOL 2";
	private static final String RAG_SIMPLE_GENERATE_ONE = ENTITY_NAME_PREFIX + "Rag configuration SIMPLE_GENERATE 1";
	private static final String RAG_SIMPLE_GENERATE_TWO = ENTITY_NAME_PREFIX + "Rag configuration SIMPLE_GENERATE 2";

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	BucketService bucketService;

	@Inject
	DatasourceService datasourceService;

	@Inject
	RAGConfigurationService ragConfigurationService;

	@Inject
	SuggestionCategoryService suggestionCategoryService;

	@Inject
	TabService tabService;

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Test
	@Order(1)
	void should_create_bucket_with_lists() {

		EntitiesUtils.createRAGConfiguration(
			sessionFactory, ragConfigurationService, RAG_CHAT_ONE, RAGType.CHAT_RAG);
		EntitiesUtils.createRAGConfiguration(
			sessionFactory, ragConfigurationService, RAG_CHAT_TOOL_ONE, RAGType.CHAT_RAG_TOOL);
		EntitiesUtils.createRAGConfiguration(
			sessionFactory, ragConfigurationService, RAG_SIMPLE_GENERATE_ONE,
			RAGType.SIMPLE_GENERATE);

		var ragConfigurationChat =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_ONE);
		var ragConfigurationChatTool =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_TOOL_ONE);
		var ragConfigurationSimple =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_SIMPLE_GENERATE_ONE);

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

		var bucket = bucketService.create(BucketWithListsDTO.builder()
			.name(BUCKET_ONE_NAME)
			.refreshOnDate(true)
			.refreshOnQuery(true)
			.refreshOnTab(true)
			.refreshOnSuggestionCategory(true)
			.retrieveType(Bucket.RetrieveType.MATCH)
			.datasourceIds(datasourceIds)
			.suggestionCategoryIds(suggestionCategorieIds)
			.tabIds(tabIds)
			.ragConfigurationChat(ragConfigurationChat.getId())
			.ragConfigurationChatTool(ragConfigurationChatTool.getId())
			.ragConfigurationSimpleGenerate(ragConfigurationSimple.getId())
			.build()
		).await().indefinitely();

		var bucketId = bucket.getId();

		var createdBucket = sessionFactory.withTransaction((s, t) ->
			bucketService.findById(s, bucketId)
				.call(entity -> Mutiny.fetch(entity.getDatasources()))
				.call(entity -> Mutiny.fetch(entity.getSuggestionCategories()))
				.call(entity -> Mutiny.fetch(entity.getTabs()))
		).await().indefinitely();

		assertEquals(BUCKET_ONE_NAME, createdBucket.getName());
		assertEquals(datasourceIds.size(), createdBucket.getDatasources().size());

		var dataSources = createdBucket.getDatasources().iterator();
		assertEquals(Initializer.INIT_DATASOURCE_CONNECTION, dataSources.next().getName());

		assertEquals(suggestionCategorieIds.size(), createdBucket.getSuggestionCategories().size());
		assertEquals(tabIds.size(), createdBucket.getTabs().size());

		// RAGConfiguration check
		assertEquals(ragConfigurationChat.getId(), bucket.getRagConfigurationChat().getId());
		assertEquals(
			ragConfigurationChatTool.getId(), bucket.getRagConfigurationChatTool().getId());
		assertEquals(
			ragConfigurationSimple.getId(), bucket.getRagConfigurationSimpleGenerate().getId());
	}

	@Test
	@Order(2)
	void should_patch_bucket_with_lists() {

		EntitiesUtils.createRAGConfiguration(
			sessionFactory, ragConfigurationService, RAG_CHAT_TWO, RAGType.CHAT_RAG);
		EntitiesUtils.createRAGConfiguration(
			sessionFactory, ragConfigurationService, RAG_CHAT_TOOL_TWO, RAGType.CHAT_RAG_TOOL);
		EntitiesUtils.createRAGConfiguration(
			sessionFactory, ragConfigurationService, RAG_SIMPLE_GENERATE_TWO,
			RAGType.SIMPLE_GENERATE);

		// Retrieve ragConfigurations one
		var ragConfigurationChatOne =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_ONE);
		var ragConfigurationChatToolOne =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_TOOL_ONE);
		var ragConfigurationSimpleOne =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_SIMPLE_GENERATE_ONE);

		// Retrieve ragConfigurations two
		var ragConfigurationChatTwo =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_TWO);
		var ragConfigurationChatToolTwo =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_TOOL_TWO);
		var ragConfigurationSimpleTwo =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_SIMPLE_GENERATE_TWO);

		var bucket = sessionFactory.withTransaction((s, t) ->
			bucketService.findByName(s, BUCKET_ONE_NAME)
				.call(entity -> Mutiny.fetch(entity.getTabs()))
		).await().indefinitely();

		// RAGConfiguration initial check
		assertEquals(ragConfigurationChatOne.getId(), bucket.getRagConfigurationChat().getId());
		assertEquals(
			ragConfigurationChatToolOne.getId(), bucket.getRagConfigurationChatTool().getId());
		assertEquals(
			ragConfigurationSimpleOne.getId(), bucket.getRagConfigurationSimpleGenerate().getId());

		bucketService.patch(bucket.getId(), BucketWithListsDTO.builder()
			.name(BUCKET_ONE_NAME)
			.refreshOnDate(true)
			.refreshOnQuery(true)
			.refreshOnTab(true)
			.refreshOnSuggestionCategory(true)
			.retrieveType(Bucket.RetrieveType.MATCH)
			.tabIds(Set.of())
			.ragConfigurationChat(ragConfigurationChatTwo.getId())
			.ragConfigurationChatTool(ragConfigurationChatToolTwo.getId())
			.ragConfigurationSimpleGenerate(ragConfigurationSimpleTwo.getId())
			.build()
		).await().indefinitely();

		var patched = sessionFactory.withTransaction((s, t) ->
			bucketService.findByName(s, BUCKET_ONE_NAME)
				.call(entity -> Mutiny.fetch(entity.getTabs()))
		).await().indefinitely();

		assertFalse(bucket.getTabs().isEmpty());

		assertTrue(patched.getTabs().isEmpty());

		// RAGConfiguration patched bucket check
		assertEquals(ragConfigurationChatTwo.getId(), patched.getRagConfigurationChat().getId());
		assertEquals(
			ragConfigurationChatToolTwo.getId(), patched.getRagConfigurationChatTool().getId());
		assertEquals(
			ragConfigurationSimpleTwo.getId(), patched.getRagConfigurationSimpleGenerate().getId());
	}

	@Test
	@Order(3)
	void should_update_bucket_with_lists() {

		// Retrieve ragConfigurations one
		var ragConfigurationChatOne =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_ONE);
		var ragConfigurationChatToolOne =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_TOOL_ONE);
		var ragConfigurationSimpleOne =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_SIMPLE_GENERATE_ONE);

		// Retrieve ragConfigurations two
		var ragConfigurationChatTwo =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_TWO);
		var ragConfigurationChatToolTwo =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_TOOL_TWO);
		var ragConfigurationSimpleTwo =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_SIMPLE_GENERATE_TWO);

		var bucket = sessionFactory.withTransaction((s, t) ->
			bucketService.findByName(s, BUCKET_ONE_NAME)
				.call(entity -> Mutiny.fetch(entity.getTabs()))
		).await().indefinitely();

		// RAGConfiguration initial check
		assertEquals(ragConfigurationChatTwo.getId(), bucket.getRagConfigurationChat().getId());
		assertEquals(
			ragConfigurationChatToolTwo.getId(), bucket.getRagConfigurationChatTool().getId());
		assertEquals(
			ragConfigurationSimpleTwo.getId(), bucket.getRagConfigurationSimpleGenerate().getId());

		var suggestionCategoryIds = suggestionCategoryService
			.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		bucketService.update(bucket.getId(), BucketWithListsDTO.builder()
			.name(BUCKET_ONE_NAME)
			.refreshOnDate(false)
			.refreshOnQuery(false)
			.refreshOnTab(false)
			.refreshOnSuggestionCategory(true)
			.suggestionCategoryIds(suggestionCategoryIds)
			.retrieveType(Bucket.RetrieveType.KNN)
			.ragConfigurationChat(ragConfigurationChatOne.getId())
			.ragConfigurationChatTool(ragConfigurationChatToolOne.getId())
			.ragConfigurationSimpleGenerate(ragConfigurationSimpleOne.getId())
			.build()
		).await().indefinitely();

		var updated = sessionFactory.withTransaction((s, t) ->
			bucketService.findByName(s, BUCKET_ONE_NAME)
				.call(entity -> Mutiny.fetch(entity.getTabs()))
				.call(entity -> Mutiny.fetch(entity.getSuggestionCategories()))
				.call(entity -> Mutiny.fetch(entity.getDatasources()))
		).await().indefinitely();

		assertTrue(updated.getTabs().isEmpty());
		assertEquals(suggestionCategoryIds.size(), updated.getSuggestionCategories().size());
		assertTrue(updated.getDatasources().isEmpty());
		assertFalse(updated.getRefreshOnDate());
		assertEquals(Bucket.RetrieveType.KNN, updated.getRetrieveType());


		// RAGConfiguration updated bucket check
		assertEquals(ragConfigurationChatOne.getId(), updated.getRagConfigurationChat().getId());
		assertEquals(
			ragConfigurationChatToolOne.getId(), updated.getRagConfigurationChatTool().getId());
		assertEquals(
			ragConfigurationSimpleOne.getId(), updated.getRagConfigurationSimpleGenerate().getId());
	}

	@Test
	@Order(4)
	void should_patch_bucket_with_lists_via_graphql() {

		// Retrieve ragConfigurations one
		var ragConfigurationChatOne =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_ONE);
		var ragConfigurationChatToolOne =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_TOOL_ONE);
		var ragConfigurationSimpleOne =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_SIMPLE_GENERATE_ONE);

		// Retrieve ragConfigurations two
		var ragConfigurationChatTwo =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_TWO);
		var ragConfigurationChatToolTwo =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_TOOL_TWO);
		var ragConfigurationSimpleTwo =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_SIMPLE_GENERATE_TWO);

		var bucket = sessionFactory.withTransaction((s, t) ->
			bucketService.findByName(s, BUCKET_ONE_NAME)
				.call(entity -> Mutiny.fetch(entity.getTabs()))
				.call(entity -> Mutiny.fetch(entity.getSuggestionCategories()))
				.call(entity -> Mutiny.fetch(entity.getDatasources()))
		).await().indefinitely();

		// RAGConfiguration initial check
		assertEquals(ragConfigurationChatOne.getId(), bucket.getRagConfigurationChat().getId());
		assertEquals(
			ragConfigurationChatToolOne.getId(), bucket.getRagConfigurationChatTool().getId());
		assertEquals(
			ragConfigurationSimpleOne.getId(), bucket.getRagConfigurationSimpleGenerate().getId());

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
					"bucketWithLists",
					args(
						arg("id", bucket.getId()),
						arg("patch", true),
						arg(
							"bucketWithListsDTO",
							inputObject(
								prop("name", BUCKET_ONE_NAME),
								prop("refreshOnDate", true),
								prop("refreshOnQuery", true),
								prop("refreshOnTab", true),
								prop("refreshOnSuggestionCategory", true),
								prop("retrieveType", gqlEnum("MATCH")),
								prop("suggestionCategoryIds", suggestionCategorieIds
									.stream()
									.limit(1)
									.collect(Collectors.toSet())
								),
								prop("datasourceIds", datasourceIds),
								prop("tabIds", List.of()),
								prop("ragConfigurationChat", ragConfigurationChatTwo.getId()),
								prop(
									"ragConfigurationChatTool",
									ragConfigurationChatToolTwo.getId()
								),
								prop(
									"ragConfigurationSimpleGenerate",
									ragConfigurationSimpleTwo.getId()
								)
							)
						)
					),
					field(
						"entity",
						field("id"),
						field("name"),
						field(
							"suggestionCategories",
							field(
								"edges",
								field(
									"node",
									field("id")
								)
							)
						)
					),
					field(
						"fieldValidators",
						field("field"),
						field("message")
					)
				)
			)
		);

		try {

			var response = graphQLClient.executeSync(query);

			var jsonObject = response.getData().getJsonObject("bucketWithLists");

			assertFalse(jsonObject.isNull("entity"));
			assertTrue(jsonObject.isNull("fieldValidators"));

			var patched = sessionFactory.withTransaction((s, t) ->
				bucketService.findByName(s, BUCKET_ONE_NAME)
					.call(entity -> Mutiny.fetch(entity.getTabs()))
			).await().indefinitely();

			// RAGConfiguration patched bucket check
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

	@Test
	void should_create_bucket_with_lists_via_graphql() {

		// Retrieve ragConfigurations one
		var ragConfigurationChatOne =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_ONE);
		var ragConfigurationChatToolOne =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_CHAT_TOOL_ONE);
		var ragConfigurationSimpleOne =
			EntitiesUtils.getRAGConfiguration(
				sessionFactory, ragConfigurationService, RAG_SIMPLE_GENERATE_ONE);

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
					"bucketWithLists",
					args(
						arg(
							"bucketWithListsDTO",
							inputObject(
								prop("name", BUCKET_TWO_NAME),
								prop("refreshOnDate", true),
								prop("refreshOnQuery", true),
								prop("refreshOnTab", true),
								prop("refreshOnSuggestionCategory", true),
								prop("retrieveType", gqlEnum("MATCH")),
								prop("datasourceIds", datasourceIds),
								prop("tabIds", tabIds),
								prop("suggestionCategoryIds", suggestionCategorieIds),
								prop("ragConfigurationChat", ragConfigurationChatOne.getId()),
								prop(
									"ragConfigurationChatTool",
									ragConfigurationChatToolOne.getId()
								),
								prop(
									"ragConfigurationSimpleGenerate",
									ragConfigurationSimpleOne.getId()
								)
							)
						)
					),
					field(
						"entity",
						field("id"),
						field("name")
					),
					field(
						"fieldValidators",
						field("field"),
						field("message")
					)
				)
			)
		);

		try {

			var response = graphQLClient.executeSync(query);

			System.out.println(response);

			var jsonObject = response.getData().getJsonObject("bucketWithLists");

			assertFalse(jsonObject.isNull("entity"));
			assertTrue(jsonObject.isNull("fieldValidators"));

			var patched = sessionFactory.withTransaction((s, t) ->
				bucketService.findByName(s, BUCKET_TWO_NAME)
					.call(entity -> Mutiny.fetch(entity.getTabs()))
			).await().indefinitely();

			// RAGConfiguration patched bucket check
			assertEquals(
				ragConfigurationChatOne.getId(), patched.getRagConfigurationChat().getId());
			assertEquals(
				ragConfigurationChatToolOne.getId(), patched.getRagConfigurationChatTool().getId());
			assertEquals(
				ragConfigurationSimpleOne.getId(),
				patched.getRagConfigurationSimpleGenerate().getId());

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}

