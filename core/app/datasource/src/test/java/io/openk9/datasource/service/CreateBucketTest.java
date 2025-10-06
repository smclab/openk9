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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.Initializer;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.dto.request.BucketWithListsDTO;
import io.openk9.datasource.model.util.K9Entity;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
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
	RAGConfigurationService ragService;

	@Inject
	SuggestionCategoryService suggestionCategoryService;

	@Inject
	TabService tabService;

	@BeforeEach
	void setup() {
		// Retrieves lists
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
			EntitiesUtils.getRAGConfiguration(RAG_CHAT_ONE, ragService, sessionFactory);
		var ragConfigurationChatToolOne =
			EntitiesUtils.getRAGConfiguration(RAG_CHAT_TOOL_ONE, ragService, sessionFactory);
		var ragConfigurationSimpleOne =
			EntitiesUtils.getRAGConfiguration(RAG_SIMPLE_GENERATE_ONE, ragService, sessionFactory);

		// Creates Bucket two
		BucketWithListsDTO dtoBucketTwo = BucketWithListsDTO.builder()
			.name(BUCKET_TWO_NAME)
			.refreshOnDate(true)
			.refreshOnQuery(true)
			.refreshOnTab(true)
			.refreshOnSuggestionCategory(true)
			.retrieveType(Bucket.RetrieveType.TEXT)
			.datasourceIds(datasourceIds)
			.suggestionCategoryIds(suggestionCategorieIds)
			.tabIds(tabIds)
			.ragConfigurationChat(ragConfigurationChatOne.getId())
			.ragConfigurationChatTool(ragConfigurationChatToolOne.getId())
			.ragConfigurationSimpleGenerate(ragConfigurationSimpleOne.getId())
			.build();

		EntitiesUtils.createBucket(sessionFactory, bucketService, dtoBucketTwo);
	}

	@Test
	void should_create_bucket_with_lists() {

		var ragConfigurationChat =
			EntitiesUtils.getRAGConfiguration(
				RAG_CHAT_ONE, ragService, sessionFactory);
		var ragConfigurationChatTool =
			EntitiesUtils.getRAGConfiguration(
				RAG_CHAT_TOOL_ONE, ragService, sessionFactory);
		var ragConfigurationSimple =
			EntitiesUtils.getRAGConfiguration(
				RAG_SIMPLE_GENERATE_ONE, ragService, sessionFactory);

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
			.retrieveType(Bucket.RetrieveType.TEXT)
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

		// Removes bucketOne
		EntitiesUtils.cleanBucket(bucketService, bucket);
		EntitiesUtils.removeEntity(bucket.getName(), bucketService, sessionFactory);
	}

	@Test
	void should_patch_bucket_with_lists() {

		// Retrieve ragConfigurations one
		var ragConfigurationChatOne =
			EntitiesUtils.getRAGConfiguration(
				RAG_CHAT_ONE, ragService, sessionFactory);
		var ragConfigurationChatToolOne =
			EntitiesUtils.getRAGConfiguration(
				RAG_CHAT_TOOL_ONE, ragService, sessionFactory);
		var ragConfigurationSimpleOne =
			EntitiesUtils.getRAGConfiguration(
				RAG_SIMPLE_GENERATE_ONE, ragService, sessionFactory);

		// Retrieve ragConfigurations two
		var ragConfigurationChatTwo =
			EntitiesUtils.getRAGConfiguration(
				RAG_CHAT_TWO, ragService, sessionFactory);
		var ragConfigurationChatToolTwo =
			EntitiesUtils.getRAGConfiguration(
				RAG_CHAT_TOOL_TWO, ragService, sessionFactory);
		var ragConfigurationSimpleTwo =
			EntitiesUtils.getRAGConfiguration(
				RAG_SIMPLE_GENERATE_TWO, ragService, sessionFactory);

		var bucketTwo = getBucketAndFetch(BUCKET_TWO_NAME);

		// RAGConfiguration initial check
		assertEquals(ragConfigurationChatOne.getId(), bucketTwo.getRagConfigurationChat().getId());
		assertEquals(
			ragConfigurationChatToolOne.getId(), bucketTwo.getRagConfigurationChatTool().getId());
		assertEquals(
			ragConfigurationSimpleOne.getId(),
			bucketTwo.getRagConfigurationSimpleGenerate().getId());

		bucketService.patch(bucketTwo.getId(), BucketWithListsDTO.builder()
			.name(bucketTwo.getName())
			.refreshOnDate(true)
			.refreshOnQuery(true)
			.refreshOnTab(true)
			.refreshOnSuggestionCategory(true)
			.retrieveType(Bucket.RetrieveType.TEXT)
			.tabIds(Set.of())
			.ragConfigurationChat(ragConfigurationChatTwo.getId())
			.ragConfigurationChatTool(ragConfigurationChatToolTwo.getId())
			.ragConfigurationSimpleGenerate(ragConfigurationSimpleTwo.getId())
			.build()
		).await().indefinitely();

		var patched = getBucketAndFetch(bucketTwo.getName());

		assertFalse(bucketTwo.getTabs().isEmpty());

		assertTrue(patched.getTabs().isEmpty());

		// RAGConfiguration patched bucketTwo check
		assertEquals(ragConfigurationChatTwo.getId(), patched.getRagConfigurationChat().getId());
		assertEquals(
			ragConfigurationChatToolTwo.getId(), patched.getRagConfigurationChatTool().getId());
		assertEquals(
			ragConfigurationSimpleTwo.getId(), patched.getRagConfigurationSimpleGenerate().getId());
	}

	@Test
	void should_update_bucket_with_lists() {

		// Retrieve ragConfigurations one
		var ragConfigurationChatOne =
			EntitiesUtils.getRAGConfiguration(
				RAG_CHAT_ONE, ragService, sessionFactory);
		var ragConfigurationChatToolOne =
			EntitiesUtils.getRAGConfiguration(
				RAG_CHAT_TOOL_ONE, ragService, sessionFactory);
		var ragConfigurationSimpleOne =
			EntitiesUtils.getRAGConfiguration(
				RAG_SIMPLE_GENERATE_ONE, ragService, sessionFactory);

		// Retrieve ragConfigurations two
		var ragConfigurationChatTwo =
			EntitiesUtils.getRAGConfiguration(
				RAG_CHAT_TWO, ragService, sessionFactory);
		var ragConfigurationChatToolTwo =
			EntitiesUtils.getRAGConfiguration(
				RAG_CHAT_TOOL_TWO, ragService, sessionFactory);
		var ragConfigurationSimpleTwo =
			EntitiesUtils.getRAGConfiguration(
				RAG_SIMPLE_GENERATE_TWO, ragService, sessionFactory);

		var bucketTwo = EntitiesUtils.getEntity(BUCKET_TWO_NAME, bucketService, sessionFactory);

		// RAGConfiguration initial check
		assertEquals(ragConfigurationChatOne.getId(), bucketTwo.getRagConfigurationChat().getId());
		assertEquals(
			ragConfigurationChatToolOne.getId(), bucketTwo.getRagConfigurationChatTool().getId());
		assertEquals(
			ragConfigurationSimpleOne.getId(),
			bucketTwo.getRagConfigurationSimpleGenerate().getId());

		var suggestionCategoryIds = suggestionCategoryService
			.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		bucketService.update(bucketTwo.getId(), BucketWithListsDTO.builder()
			.name(bucketTwo.getName())
			.refreshOnDate(false)
			.refreshOnQuery(false)
			.refreshOnTab(false)
			.refreshOnSuggestionCategory(true)
			.suggestionCategoryIds(suggestionCategoryIds)
			.retrieveType(Bucket.RetrieveType.KNN)
			.ragConfigurationChat(ragConfigurationChatTwo.getId())
			.ragConfigurationChatTool(ragConfigurationChatToolTwo.getId())
			.ragConfigurationSimpleGenerate(ragConfigurationSimpleTwo.getId())
			.build()
		).await().indefinitely();

		var updated = getBucketAndFetch(bucketTwo.getName());

		assertTrue(updated.getTabs().isEmpty());
		assertEquals(suggestionCategoryIds.size(), updated.getSuggestionCategories().size());
		assertTrue(updated.getDatasources().isEmpty());
		assertFalse(updated.getRefreshOnDate());
		assertEquals(Bucket.RetrieveType.KNN, updated.getRetrieveType());


		// RAGConfiguration updated bucketTwo check
		assertEquals(ragConfigurationChatTwo.getId(), updated.getRagConfigurationChat().getId());
		assertEquals(
			ragConfigurationChatToolTwo.getId(), updated.getRagConfigurationChatTool().getId());
		assertEquals(
			ragConfigurationSimpleTwo.getId(), updated.getRagConfigurationSimpleGenerate().getId());
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

	private Bucket getBucketAndFetch(String bucketName) {
		return sessionFactory.withTransaction((s, t) ->
			bucketService.findByName(s, bucketName)
				.call(entity -> Mutiny.fetch(entity.getTabs()))
				.call(entity -> Mutiny.fetch(entity.getSuggestionCategories()))
				.call(entity -> Mutiny.fetch(entity.getDatasources()))
		).await().indefinitely();
	}
}

