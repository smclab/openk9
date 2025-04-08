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

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.dto.base.BucketDTO;
import io.openk9.datasource.model.dto.base.RAGConfigurationDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class UnboundRAGConfigurationTest {

	private static final String ENTITY_NAME_PREFIX = "UnboundRAGConfigurationTest - ";

	private static final String BUCKET_ONE_NAME = ENTITY_NAME_PREFIX + "Bucket 1";
	private static final String BUCKET_TWO_NAME = ENTITY_NAME_PREFIX + "Bucket 2";
	private static final String RAG_CHAT_ONE = ENTITY_NAME_PREFIX + "RAG Configuration chat 1";
	private static final String RAG_CHAT_TWO = ENTITY_NAME_PREFIX + "RAG Configuration chat 2";
	private static final String RAG_CHAT_THREE = ENTITY_NAME_PREFIX + "RAG Configuration chat 3";
	private static final String RAG_SEARCH_ONE = ENTITY_NAME_PREFIX + "RAG Configuration search 1";
	private static final String RAG_SEARCH_TWO = ENTITY_NAME_PREFIX + "RAG Configuration search 2";
	private static final String RAG_SEARCH_THREE = ENTITY_NAME_PREFIX + "RAG Configuration search 3";
	private static final String RAG_CHAT_TOOL_ONE = ENTITY_NAME_PREFIX + "RAG Configuration chat tool 1";
	private static final String RAG_CHAT_TOOL_TWO = ENTITY_NAME_PREFIX + "RAG Configuration chat tool 2";
	private static final String RAG_CHAT_TOOL_THREE = ENTITY_NAME_PREFIX + "RAG Configuration chat tool 3";

	private static int allChatCount = 0;
	private static int allChatToolCount = 0;
	private static int allSearchCount = 0;

	@Inject
	BucketService bucketService;

	@Inject
	RAGConfigurationService ragConfigurationService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@BeforeEach
	void setup() {
		createBucketOne();
		createBucketTwo();

		createRAGConfiguration(RAG_CHAT_ONE, RAGType.CHAT);
		createRAGConfiguration(RAG_CHAT_TWO, RAGType.CHAT);
		createRAGConfiguration(RAG_CHAT_THREE, RAGType.CHAT);
		allChatCount = 3;

		createRAGConfiguration(RAG_SEARCH_ONE, RAGType.SEARCH);
		createRAGConfiguration(RAG_SEARCH_TWO, RAGType.SEARCH);
		createRAGConfiguration(RAG_SEARCH_THREE, RAGType.SEARCH);
		allSearchCount = 3;

		createRAGConfiguration(RAG_CHAT_TOOL_ONE, RAGType.CHAT_TOOL);
		createRAGConfiguration(RAG_CHAT_TOOL_TWO, RAGType.CHAT_TOOL);
		createRAGConfiguration(RAG_CHAT_TOOL_THREE, RAGType.CHAT_TOOL);
		allChatToolCount = 3;
	}

	@Test
	void should_bind_and_unbind_rag_configurations_to_bucket_one() {
		var bucket = getBucketOne();
		var ragConfigurationChatOne = getRAGConfiguration(RAG_CHAT_ONE);
		var ragConfigurationChatTwo = getRAGConfiguration(RAG_CHAT_TWO);
		var ragConfigurationChatToolOne = getRAGConfiguration(RAG_CHAT_TOOL_ONE);
		var ragConfigurationSearchOne = getRAGConfiguration(RAG_SEARCH_ONE);

		// initial check
		assertNull(bucket.getRagConfigurationChat());
		assertNull(bucket.getRagConfigurationChatTool());
		assertNull(bucket.getRagConfigurationSearch());

		// bind one RAGConfiguration for each RAGType
		bindRAGConfigurationToBucket(bucket, ragConfigurationChatOne);
		bindRAGConfigurationToBucket(bucket, ragConfigurationChatToolOne);
		bindRAGConfigurationToBucket(bucket, ragConfigurationSearchOne);

		bucket = getBucketOne();

		assertEquals(ragConfigurationChatOne.getId(), bucket.getRagConfigurationChat().getId());
		assertEquals(ragConfigurationChatToolOne.getId(), bucket.getRagConfigurationChatTool().getId());
		assertEquals(ragConfigurationSearchOne.getId(), bucket.getRagConfigurationSearch().getId());

		// override the CHAT RAGConfiguration binding
		bindRAGConfigurationToBucket(bucket, ragConfigurationChatTwo);

		bucket = getBucketOne();

		assertEquals(ragConfigurationChatTwo.getId(), bucket.getRagConfigurationChat().getId());

		// unbind CHAT RAGConfiguration and check bucket
		unbindRAGConfigurationToBucket(bucket, RAGType.CHAT);

		bucket = getBucketOne();

		assertNull(bucket.getRagConfigurationChat());
		assertEquals(ragConfigurationChatToolOne.getId(), bucket.getRagConfigurationChatTool().getId());
		assertEquals(ragConfigurationSearchOne.getId(), bucket.getRagConfigurationSearch().getId());

		// unbind CHAT_TOOL RAGConfiguration and check bucket
		unbindRAGConfigurationToBucket(bucket, RAGType.CHAT_TOOL);

		bucket = getBucketOne();

		assertNull(bucket.getRagConfigurationChat());
		assertNull(bucket.getRagConfigurationChatTool());
		assertEquals(ragConfigurationSearchOne.getId(), bucket.getRagConfigurationSearch().getId());

		// unbind SEARCH RAGConfiguration and check bucket
		unbindRAGConfigurationToBucket(bucket, RAGType.SEARCH);

		bucket = getBucketOne();

		assertNull(bucket.getRagConfigurationChat());
		assertNull(bucket.getRagConfigurationChatTool());
		assertNull(bucket.getRagConfigurationSearch());
	}

	@Test
	void should_retrieve_all_rag_configuration_chat_from_empty_bucket() {

		var bucket = getBucketOne();
		var ragConfigurationChatOne = getRAGConfiguration(RAG_CHAT_ONE);
		var ragConfigurationChatTwo = getRAGConfiguration(RAG_CHAT_TWO);
		var ragConfigurationChatThree = getRAGConfiguration(RAG_CHAT_THREE);

		List<RAGConfiguration> unboundRAGConfigurations =
			ragConfigurationService.findRAGConfigurationByBucket(bucket.getId(), RAGType.CHAT)
				.await()
				.indefinitely();

		assertFalse(unboundRAGConfigurations.isEmpty());
		assertEquals(allChatCount, unboundRAGConfigurations.size());
		// check list elements
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatOne));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatTwo));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatThree));
	}

	@Test
	void should_retrieve_all_rag_configurations_chat_excluding_the_one_associated_with_the_bucket_one(){

		var bucketOne = getBucketOne();
		var bucketTwo = getBucketTwo();
		var ragConfigurationChatOne = getRAGConfiguration(RAG_CHAT_ONE);
		var ragConfigurationChatTwo = getRAGConfiguration(RAG_CHAT_TWO);
		var ragConfigurationChatThree = getRAGConfiguration(RAG_CHAT_THREE);

		bindRAGConfigurationToBucket(bucketOne, ragConfigurationChatOne);

		// Associates a ragConfigurationChatTwo with another Bucket.
		bindRAGConfigurationToBucket(bucketTwo, ragConfigurationChatTwo);

		List<RAGConfiguration> unboundRAGConfigurations =
			ragConfigurationService.findRAGConfigurationByBucket(bucketOne.getId(), RAGType.CHAT)
				.await()
				.indefinitely();

		assertFalse(unboundRAGConfigurations.isEmpty());
		assertEquals(allChatCount - 1, unboundRAGConfigurations.size());
		// check list elements
		assertFalse(unboundRAGConfigurations.contains(ragConfigurationChatOne));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatTwo));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatThree));

	}

	@Test
	void should_retrieve_all_rag_configuration_chat_from_missing_bucket() {

		var ragConfigurationChatOne = getRAGConfiguration(RAG_CHAT_ONE);
		var ragConfigurationChatTwo = getRAGConfiguration(RAG_CHAT_TWO);
		var ragConfigurationChatThree = getRAGConfiguration(RAG_CHAT_THREE);

		List<RAGConfiguration> unboundRAGConfigurations =
			ragConfigurationService.findRAGConfigurationByBucket(0L, RAGType.CHAT)
				.await()
				.indefinitely();

		assertFalse(unboundRAGConfigurations.isEmpty());
		assertEquals(allChatCount, unboundRAGConfigurations.size());
		// check list elements
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatOne));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatTwo));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatThree));
	}

	@Test
	void should_retrieve_all_rag_configuration_search_from_empty_bucket() {

		var bucket = getBucketOne();
		var ragConfigurationSearchOne = getRAGConfiguration(RAG_SEARCH_ONE);
		var ragConfigurationSearchTwo = getRAGConfiguration(RAG_SEARCH_TWO);
		var ragConfigurationSearchThree = getRAGConfiguration(RAG_SEARCH_THREE);

		List<RAGConfiguration> unboundRAGConfigurations =
			ragConfigurationService.findRAGConfigurationByBucket(bucket.getId(), RAGType.SEARCH)
				.await()
				.indefinitely();

		assertFalse(unboundRAGConfigurations.isEmpty());
		assertEquals(allSearchCount, unboundRAGConfigurations.size());
		// check list elements
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSearchOne));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSearchTwo));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSearchThree));
	}

	@Test
	void should_retrieve_all_rag_configurations_search_excluding_the_one_associated_with_the_bucket_one(){

		var bucketOne = getBucketOne();
		var bucketTwo = getBucketTwo();
		var ragConfigurationSearchOne = getRAGConfiguration(RAG_SEARCH_ONE);
		var ragConfigurationSearchTwo = getRAGConfiguration(RAG_SEARCH_TWO);
		var ragConfigurationSearchThree = getRAGConfiguration(RAG_SEARCH_THREE);

		bindRAGConfigurationToBucket(bucketOne, ragConfigurationSearchOne);

		// Associates a ragConfigurationSearchTwo with another Bucket.
		bindRAGConfigurationToBucket(bucketTwo, ragConfigurationSearchTwo);

		List<RAGConfiguration> unboundRAGConfigurations =
			ragConfigurationService.findRAGConfigurationByBucket(bucketOne.getId(), RAGType.SEARCH)
				.await()
				.indefinitely();

		assertFalse(unboundRAGConfigurations.isEmpty());
		assertEquals(allSearchCount - 1, unboundRAGConfigurations.size());
		// check list elements
		assertFalse(unboundRAGConfigurations.contains(ragConfigurationSearchOne));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSearchTwo));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSearchThree));

	}

	@Test
	void should_retrieve_all_rag_configuration_search_from_missing_bucket() {

		var ragConfigurationSearchOne = getRAGConfiguration(RAG_SEARCH_ONE);
		var ragConfigurationSearchTwo = getRAGConfiguration(RAG_SEARCH_TWO);
		var ragConfigurationSearchThree = getRAGConfiguration(RAG_SEARCH_THREE);

		List<RAGConfiguration> unboundRAGConfigurations =
			ragConfigurationService.findRAGConfigurationByBucket(0L, RAGType.SEARCH)
				.await()
				.indefinitely();

		assertFalse(unboundRAGConfigurations.isEmpty());
		assertEquals(allSearchCount, unboundRAGConfigurations.size());
		// check list elements
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSearchOne));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSearchTwo));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSearchThree));
	}

	@Test
	void should_retrieve_all_rag_configuration_chat_tool_from_empty_bucket() {

		var bucket = getBucketOne();
		var ragConfigurationChatToolOne = getRAGConfiguration(RAG_CHAT_TOOL_ONE);
		var ragConfigurationChatToolTwo = getRAGConfiguration(RAG_CHAT_TOOL_TWO);
		var ragConfigurationChatToolThree = getRAGConfiguration(RAG_CHAT_TOOL_THREE);

		List<RAGConfiguration> unboundRAGConfigurations =
			ragConfigurationService.findRAGConfigurationByBucket(bucket.getId(), RAGType.CHAT_TOOL)
				.await()
				.indefinitely();

		assertFalse(unboundRAGConfigurations.isEmpty());
		assertEquals(allChatToolCount, unboundRAGConfigurations.size());
		// check list elements
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatToolOne));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatToolTwo));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatToolThree));
	}

	@Test
	void should_retrieve_all_rag_configurations_chat_tool_excluding_the_one_associated_with_the_bucket_one(){

		var bucketOne = getBucketOne();
		var bucketTwo = getBucketTwo();
		var ragConfigurationChatToolOne = getRAGConfiguration(RAG_CHAT_TOOL_ONE);
		var ragConfigurationChatToolTwo = getRAGConfiguration(RAG_CHAT_TOOL_TWO);
		var ragConfigurationChatToolThree = getRAGConfiguration(RAG_CHAT_TOOL_THREE);

		bindRAGConfigurationToBucket(bucketOne, ragConfigurationChatToolOne);

		// Associates a ragConfigurationChatToolTwo with another Bucket.
		bindRAGConfigurationToBucket(bucketTwo, ragConfigurationChatToolTwo);

		List<RAGConfiguration> unboundRAGConfigurations =
			ragConfigurationService.findRAGConfigurationByBucket(bucketOne.getId(), RAGType.CHAT_TOOL)
				.await()
				.indefinitely();

		assertFalse(unboundRAGConfigurations.isEmpty());
		assertEquals(allChatToolCount - 1, unboundRAGConfigurations.size());
		// check list elements
		assertFalse(unboundRAGConfigurations.contains(ragConfigurationChatToolOne));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatToolTwo));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatToolThree));

	}

	@Test
	void should_retrieve_all_rag_configuration_chat_tool_from_missing_bucket() {

		var ragConfigurationChatToolOne = getRAGConfiguration(RAG_CHAT_TOOL_ONE);
		var ragConfigurationChatToolTwo = getRAGConfiguration(RAG_CHAT_TOOL_TWO);
		var ragConfigurationChatToolThree = getRAGConfiguration(RAG_CHAT_TOOL_THREE);

		List<RAGConfiguration> unboundRAGConfigurations =
			ragConfigurationService.findRAGConfigurationByBucket(0L, RAGType.CHAT_TOOL)
				.await()
				.indefinitely();

		assertFalse(unboundRAGConfigurations.isEmpty());
		assertEquals(allChatCount, unboundRAGConfigurations.size());
		// check list elements
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatToolOne));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatToolTwo));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationChatToolThree));
	}

	@AfterEach
	void tearDown() {
		removeBucketOne();
		removeBucketTwo();

		removeRAGConfiguration(RAG_CHAT_ONE);
		removeRAGConfiguration(RAG_CHAT_TWO);
		removeRAGConfiguration(RAG_CHAT_THREE);

		removeRAGConfiguration(RAG_SEARCH_ONE);
		removeRAGConfiguration(RAG_SEARCH_TWO);
		removeRAGConfiguration(RAG_SEARCH_THREE);

		removeRAGConfiguration(RAG_CHAT_TOOL_ONE);
		removeRAGConfiguration(RAG_CHAT_TOOL_TWO);
		removeRAGConfiguration(RAG_CHAT_TOOL_THREE);
	}

	private void bindRAGConfigurationToBucket(Bucket bucket, RAGConfiguration ragConfiguration) {
		bucketService.bindRAGConfiguration(bucket.getId(), ragConfiguration.getId())
			.await()
			.indefinitely();
	}

	private void createBucketOne() {
		BucketDTO dto = BucketDTO.builder()
			.name(BUCKET_ONE_NAME)
			.refreshOnSuggestionCategory(false)
			.refreshOnTab(false)
			.refreshOnDate(false)
			.refreshOnQuery(false)
			.retrieveType(Bucket.RetrieveType.MATCH)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.create(dto)
						.call(bucket -> Mutiny.fetch(bucket.getRagConfigurationChat()))
						.call(bucket -> Mutiny.fetch(bucket.getRagConfigurationChatTool()))
						.call(bucket -> Mutiny.fetch(bucket.getRagConfigurationSearch()))
			)
			.await()
			.indefinitely();
	}

	private void createBucketTwo() {
		BucketDTO dto = BucketDTO.builder()
			.name(BUCKET_TWO_NAME)
			.refreshOnSuggestionCategory(false)
			.refreshOnTab(false)
			.refreshOnDate(false)
			.refreshOnQuery(false)
			.retrieveType(Bucket.RetrieveType.MATCH)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.create(dto)
						.call(bucket -> Mutiny.fetch(bucket.getRagConfigurationChat()))
						.call(bucket -> Mutiny.fetch(bucket.getRagConfigurationChatTool()))
						.call(bucket -> Mutiny.fetch(bucket.getRagConfigurationSearch()))
			)
			.await()
			.indefinitely();
	}

	private void createRAGConfiguration(String name, RAGType type) {
		RAGConfigurationDTO dto = RAGConfigurationDTO.builder()
			.name(name)
			.type(type)
			.build();

		sessionFactory.withTransaction(
				session -> ragConfigurationService.create(session, dto)
			)
			.await()
			.indefinitely();
	}

	private Bucket getBucketOne() {
		return sessionFactory.withTransaction(
			session ->
				bucketService.findByName(session, BUCKET_ONE_NAME)
		)
		.await()
		.indefinitely();
	}

	private Bucket getBucketTwo() {
		return sessionFactory.withTransaction(
			session ->
				bucketService.findByName(session, BUCKET_TWO_NAME)
		)
		.await()
		.indefinitely();
	}

	private RAGConfiguration getRAGConfiguration(String name) {
		return sessionFactory.withTransaction(
				session ->
					ragConfigurationService.findByName(session, name)
			)
			.await()
			.indefinitely();
	}

	private void removeBucketOne() {
		var bucket = getBucketOne();

		sessionFactory.withTransaction(
			session ->
				bucketService.deleteById(session, bucket.getId())
		)
		.await()
		.indefinitely();
	}

	private void removeBucketTwo() {
		var bucket = getBucketTwo();

		sessionFactory.withTransaction(
			session ->
				bucketService.deleteById(session, bucket.getId())
		)
		.await()
		.indefinitely();
	}

	private void removeRAGConfiguration(String name) {
		var ragConfiguration = getRAGConfiguration(name);

		sessionFactory.withTransaction(
				session ->
					ragConfigurationService.deleteById(session, ragConfiguration.getId())
			)
			.await()
			.indefinitely();
	}

	private void unbindRAGConfigurationToBucket(Bucket bucket, RAGType ragType) {
		bucketService.unbindRAGConfiguration(bucket.getId(), ragType)
			.await()
			.indefinitely();
	}
}
