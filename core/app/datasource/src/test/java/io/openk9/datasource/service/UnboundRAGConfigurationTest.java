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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.dto.base.BucketDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class UnboundRAGConfigurationTest {

	private static final String ENTITY_NAME_PREFIX = "UnboundRAGConfigurationTest - ";

	private static final String BUCKET_ONE_NAME = ENTITY_NAME_PREFIX + "Bucket 1";
	private static final String BUCKET_TWO_NAME = ENTITY_NAME_PREFIX + "Bucket 2";
	private static final String RAG_CHAT_ONE = ENTITY_NAME_PREFIX + "RAG Configuration chat 1";
	private static final String RAG_CHAT_TWO = ENTITY_NAME_PREFIX + "RAG Configuration chat 2";
	private static final String RAG_CHAT_THREE = ENTITY_NAME_PREFIX + "RAG Configuration chat 3";
	private static final String RAG_SIMPLE_GENERATE_ONE = ENTITY_NAME_PREFIX + "RAG Configuration Simple Generate 1";
	private static final String RAG_SIMPLE_GENERATE_TWO = ENTITY_NAME_PREFIX + "RAG Configuration Simple Generate 2";
	private static final String RAG_SIMPLE_GENERATE_THREE = ENTITY_NAME_PREFIX + "RAG Configuration Simple Generate 3";
	private static final String RAG_CHAT_TOOL_ONE = ENTITY_NAME_PREFIX + "RAG Configuration chat tool 1";
	private static final String RAG_CHAT_TOOL_TWO = ENTITY_NAME_PREFIX + "RAG Configuration chat tool 2";
	private static final String RAG_CHAT_TOOL_THREE = ENTITY_NAME_PREFIX + "RAG Configuration chat tool 3";

	private int allChatCount = 0;
	private int allChatToolCount = 0;
	private int allSimpleGenerateCount = 0;

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

		EntitiesUtils.createRAGConfiguration(
			ragConfigurationService,
			RAG_CHAT_ONE,
			RAGType.CHAT_RAG
		);
		EntitiesUtils.createRAGConfiguration(
			ragConfigurationService,
			RAG_CHAT_TWO,
			RAGType.CHAT_RAG
		);
		EntitiesUtils.createRAGConfiguration(
			ragConfigurationService,
			RAG_CHAT_THREE,
			RAGType.CHAT_RAG
		);
		allChatCount = 3;

		EntitiesUtils.createRAGConfiguration(
			ragConfigurationService,
			RAG_SIMPLE_GENERATE_ONE,
			RAGType.SIMPLE_GENERATE
		);
		EntitiesUtils.createRAGConfiguration(
			ragConfigurationService,
			RAG_SIMPLE_GENERATE_TWO,
			RAGType.SIMPLE_GENERATE
		);
		EntitiesUtils.createRAGConfiguration(
			ragConfigurationService,
			RAG_SIMPLE_GENERATE_THREE,
			RAGType.SIMPLE_GENERATE
		);
		allSimpleGenerateCount = 3;

		EntitiesUtils.createRAGConfiguration(
			ragConfigurationService,
			RAG_CHAT_TOOL_ONE,
			RAGType.CHAT_RAG_TOOL
		);
		EntitiesUtils.createRAGConfiguration(
			ragConfigurationService,
			RAG_CHAT_TOOL_TWO,
			RAGType.CHAT_RAG_TOOL
		);
		EntitiesUtils.createRAGConfiguration(
			ragConfigurationService,
			RAG_CHAT_TOOL_THREE,
			RAGType.CHAT_RAG_TOOL
		);
		allChatToolCount = 3;
	}

	@Test
	void should_bind_and_unbind_rag_configurations_to_bucket_one() {
		var bucket = getBucketOne();
		var ragConfigurationChatOne = getRAGConfiguration(RAG_CHAT_ONE);
		var ragConfigurationChatTwo = getRAGConfiguration(RAG_CHAT_TWO);
		var ragConfigurationChatToolOne = getRAGConfiguration(RAG_CHAT_TOOL_ONE);
		var ragConfigurationSimpleGenerateOne = getRAGConfiguration(RAG_SIMPLE_GENERATE_ONE);

		// initial check
		assertNull(bucket.getRagConfigurationChat());
		assertNull(bucket.getRagConfigurationChatTool());
		assertNull(bucket.getRagConfigurationSimpleGenerate());

		// bind one RAGConfiguration for each RAGType
		bindRAGConfigurationToBucket(bucket, ragConfigurationChatOne);
		bindRAGConfigurationToBucket(bucket, ragConfigurationChatToolOne);
		bindRAGConfigurationToBucket(bucket, ragConfigurationSimpleGenerateOne);

		bucket = getBucketOne();

		assertEquals(ragConfigurationChatOne.getId(), bucket.getRagConfigurationChat().getId());
		assertEquals(ragConfigurationChatToolOne.getId(), bucket.getRagConfigurationChatTool().getId());
		assertEquals(ragConfigurationSimpleGenerateOne.getId(), bucket.getRagConfigurationSimpleGenerate().getId());

		// override the CHAT RAGConfiguration binding
		bindRAGConfigurationToBucket(bucket, ragConfigurationChatTwo);

		bucket = getBucketOne();

		assertEquals(ragConfigurationChatTwo.getId(), bucket.getRagConfigurationChat().getId());

		// unbind CHAT_RAG RAGConfiguration and check bucket
		unbindRAGConfigurationToBucket(bucket, RAGType.CHAT_RAG);

		bucket = getBucketOne();

		assertNull(bucket.getRagConfigurationChat());
		assertEquals(ragConfigurationChatToolOne.getId(), bucket.getRagConfigurationChatTool().getId());
		assertEquals(ragConfigurationSimpleGenerateOne.getId(), bucket.getRagConfigurationSimpleGenerate().getId());

		// unbind CHAT_RAG_TOOL RAGConfiguration and check bucket
		unbindRAGConfigurationToBucket(bucket, RAGType.CHAT_RAG_TOOL);

		bucket = getBucketOne();

		assertNull(bucket.getRagConfigurationChat());
		assertNull(bucket.getRagConfigurationChatTool());
		assertEquals(ragConfigurationSimpleGenerateOne.getId(), bucket.getRagConfigurationSimpleGenerate().getId());

		// unbind SIMPLE_GENERATE RAGConfiguration and check bucket
		unbindRAGConfigurationToBucket(bucket, RAGType.SIMPLE_GENERATE);

		bucket = getBucketOne();

		assertNull(bucket.getRagConfigurationChat());
		assertNull(bucket.getRagConfigurationChatTool());
		assertNull(bucket.getRagConfigurationSimpleGenerate());
	}

	@Test
	void should_retrieve_all_rag_configuration_chat_from_empty_bucket() {

		var bucket = getBucketOne();
		var ragConfigurationChatOne = getRAGConfiguration(RAG_CHAT_ONE);
		var ragConfigurationChatTwo = getRAGConfiguration(RAG_CHAT_TWO);
		var ragConfigurationChatThree = getRAGConfiguration(RAG_CHAT_THREE);

		List<RAGConfiguration> unboundRAGConfigurations =
			ragConfigurationService.findUnboundRAGConfigurationByBucket(bucket.getId(), RAGType.CHAT_RAG)
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
			ragConfigurationService.findUnboundRAGConfigurationByBucket(bucketOne.getId(), RAGType.CHAT_RAG)
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
			ragConfigurationService.findUnboundRAGConfigurationByBucket(0L, RAGType.CHAT_RAG)
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
	void should_retrieve_all_rag_configuration_simple_generate_from_empty_bucket() {

		var bucket = getBucketOne();
		var ragConfigurationSimpleGenerateOne = getRAGConfiguration(RAG_SIMPLE_GENERATE_ONE);
		var ragConfigurationSimpleGenerateTwo = getRAGConfiguration(RAG_SIMPLE_GENERATE_TWO);
		var ragConfigurationSimpleGenerateThree = getRAGConfiguration(RAG_SIMPLE_GENERATE_THREE);

		List<RAGConfiguration> unboundRAGConfigurations =
			ragConfigurationService.findUnboundRAGConfigurationByBucket(
				bucket.getId(), RAGType.SIMPLE_GENERATE)
					.await()
					.indefinitely();

		assertFalse(unboundRAGConfigurations.isEmpty());
		assertEquals(allSimpleGenerateCount, unboundRAGConfigurations.size());
		// check list elements
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSimpleGenerateOne));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSimpleGenerateTwo));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSimpleGenerateThree));
	}

	@Test
	void should_retrieve_all_rag_configurations_simple_generate_excluding_the_one_associated_with_the_bucket_one(){

		var bucketOne = getBucketOne();
		var bucketTwo = getBucketTwo();
		var ragConfigurationSimpleGenerateOne = getRAGConfiguration(RAG_SIMPLE_GENERATE_ONE);
		var ragConfigurationSimpleGenerateTwo = getRAGConfiguration(RAG_SIMPLE_GENERATE_TWO);
		var ragConfigurationSimpleGenerateThree = getRAGConfiguration(RAG_SIMPLE_GENERATE_THREE);

		bindRAGConfigurationToBucket(bucketOne, ragConfigurationSimpleGenerateOne);

		// Associates a ragConfigurationSimpleGenerateTwo with another Bucket.
		bindRAGConfigurationToBucket(bucketTwo, ragConfigurationSimpleGenerateTwo);

		List<RAGConfiguration> unboundRAGConfigurations =
			ragConfigurationService.findUnboundRAGConfigurationByBucket(
				bucketOne.getId(), RAGType.SIMPLE_GENERATE)
					.await()
					.indefinitely();

		assertFalse(unboundRAGConfigurations.isEmpty());
		assertEquals(allSimpleGenerateCount - 1, unboundRAGConfigurations.size());
		// check list elements
		assertFalse(unboundRAGConfigurations.contains(ragConfigurationSimpleGenerateOne));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSimpleGenerateTwo));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSimpleGenerateThree));

	}

	@Test
	void should_retrieve_all_rag_configuration_simple_generate_from_missing_bucket() {

		var ragConfigurationSimpleGenerateOne = getRAGConfiguration(RAG_SIMPLE_GENERATE_ONE);
		var ragConfigurationSimpleGenerateTwo = getRAGConfiguration(RAG_SIMPLE_GENERATE_TWO);
		var ragConfigurationSimpleGenerateThree = getRAGConfiguration(RAG_SIMPLE_GENERATE_THREE);

		List<RAGConfiguration> unboundRAGConfigurations =
			ragConfigurationService.findUnboundRAGConfigurationByBucket(0L, RAGType.SIMPLE_GENERATE)
				.await()
				.indefinitely();

		assertFalse(unboundRAGConfigurations.isEmpty());
		assertEquals(allSimpleGenerateCount, unboundRAGConfigurations.size());
		// check list elements
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSimpleGenerateOne));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSimpleGenerateTwo));
		assertTrue(unboundRAGConfigurations.contains(ragConfigurationSimpleGenerateThree));
	}

	@Test
	void should_retrieve_all_rag_configuration_chat_tool_from_empty_bucket() {

		var bucket = getBucketOne();
		var ragConfigurationChatToolOne = getRAGConfiguration(RAG_CHAT_TOOL_ONE);
		var ragConfigurationChatToolTwo = getRAGConfiguration(RAG_CHAT_TOOL_TWO);
		var ragConfigurationChatToolThree = getRAGConfiguration(RAG_CHAT_TOOL_THREE);

		List<RAGConfiguration> unboundRAGConfigurations =
			ragConfigurationService.findUnboundRAGConfigurationByBucket(bucket.getId(), RAGType.CHAT_RAG_TOOL)
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
			ragConfigurationService.findUnboundRAGConfigurationByBucket(bucketOne.getId(), RAGType.CHAT_RAG_TOOL)
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
			ragConfigurationService.findUnboundRAGConfigurationByBucket(0L, RAGType.CHAT_RAG_TOOL)
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

		removeRAGConfiguration(RAG_SIMPLE_GENERATE_ONE);
		removeRAGConfiguration(RAG_SIMPLE_GENERATE_TWO);
		removeRAGConfiguration(RAG_SIMPLE_GENERATE_THREE);

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
			.retrieveType(Bucket.RetrieveType.TEXT)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.create(dto)
						.call(bucket -> Mutiny.fetch(bucket.getRagConfigurationChat()))
						.call(bucket -> Mutiny.fetch(bucket.getRagConfigurationChatTool()))
						.call(bucket -> Mutiny.fetch(bucket.getRagConfigurationSimpleGenerate()))
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
			.retrieveType(Bucket.RetrieveType.TEXT)
			.build();

		sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.create(dto)
						.call(bucket -> Mutiny.fetch(bucket.getRagConfigurationChat()))
						.call(bucket -> Mutiny.fetch(bucket.getRagConfigurationChatTool()))
						.call(bucket -> Mutiny.fetch(bucket.getRagConfigurationSimpleGenerate()))
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
