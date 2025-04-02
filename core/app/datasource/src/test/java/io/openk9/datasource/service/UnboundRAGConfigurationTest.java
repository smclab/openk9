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
import io.openk9.datasource.model.dto.BucketDTO;
import io.openk9.datasource.model.dto.RAGConfigurationDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
public class UnboundRAGConfigurationTest {

	private static final String ENTITY_NAME_PREFIX = "UnboundRAGConfigurationTest - ";

	private static final String BUCKET_ONE_NAME = ENTITY_NAME_PREFIX + "Bucket 1";
	private static final String RAG_CONFIGURATION_CHAT_ONE_NAME = ENTITY_NAME_PREFIX + "RAG Configuration chat 1";
	private static final String RAG_CONFIGURATION_CHAT_TWO_NAME = ENTITY_NAME_PREFIX + "RAG Configuration chat 2";
	private static final String RAG_CONFIGURATION_SEARCH_NAME = ENTITY_NAME_PREFIX + "RAG Configuration search";
	private static final String RAG_CONFIGURATION_CHAT_TOOL_NAME = ENTITY_NAME_PREFIX + "RAG Configuration chat tool";

	@Inject
	BucketService bucketService;

	@Inject
	RAGConfigurationService ragConfigurationService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@BeforeEach
	void setup() {
		createBucketOne();
		createRAGConfigurationChatOne();
		createRAGConfigurationChatTwo();
		createRAGConfigurationSearch();
		createRAGConfigurationChatTool();
	}

	@Test
	void should_bind_and_unbind_rag_configurations_to_bucket_one() {
		var bucket = getBucketOne();
		var ragConfigurationChatOne = getRAGConfigurationChatOne();
		var ragConfigurationChatTool = getRAGConfigurationChatTool();
		var ragConfigurationSearch = getRAGConfigurationSearch();
		var ragConfigurationChatTwo = getRAGConfigurationChatTwo();

		// initial check
		assertNull(bucket.getRagConfigurationChat());
		assertNull(bucket.getRagConfigurationChatTool());
		assertNull(bucket.getRagConfigurationSearch());

		// bind one RAGConfiguration for each RAGType
		bindRAGConfigurationToBucket(bucket, ragConfigurationChatOne);
		bindRAGConfigurationToBucket(bucket, ragConfigurationChatTool);
		bindRAGConfigurationToBucket(bucket, ragConfigurationSearch);

		bucket = getBucketOne();

		assertEquals(ragConfigurationChatOne.getId(), bucket.getRagConfigurationChat().getId());
		assertEquals(ragConfigurationChatTool.getId(), bucket.getRagConfigurationChatTool().getId());
		assertEquals(ragConfigurationSearch.getId(), bucket.getRagConfigurationSearch().getId());

		// override the CHAT RAGConfiguration binding
		bindRAGConfigurationToBucket(bucket, ragConfigurationChatTwo);

		bucket = getBucketOne();

		assertEquals(ragConfigurationChatTwo.getId(), bucket.getRagConfigurationChat().getId());

		// unbind CHAT RAGConfiguration and check bucket
		unbindRAGConfigurationToBucket(bucket, RAGType.CHAT);

		bucket = getBucketOne();

		assertNull(bucket.getRagConfigurationChat());
		assertEquals(ragConfigurationChatTool.getId(), bucket.getRagConfigurationChatTool().getId());
		assertEquals(ragConfigurationSearch.getId(), bucket.getRagConfigurationSearch().getId());

		// unbind CHAT_TOOL RAGConfiguration and check bucket
		unbindRAGConfigurationToBucket(bucket, RAGType.CHAT_TOOL);

		bucket = getBucketOne();

		assertNull(bucket.getRagConfigurationChat());
		assertNull(bucket.getRagConfigurationChatTool());
		assertEquals(ragConfigurationSearch.getId(), bucket.getRagConfigurationSearch().getId());

		// unbind SEARCH RAGConfiguration and check bucket
		unbindRAGConfigurationToBucket(bucket, RAGType.SEARCH);

		bucket = getBucketOne();

		assertNull(bucket.getRagConfigurationChat());
		assertNull(bucket.getRagConfigurationChatTool());
		assertNull(bucket.getRagConfigurationSearch());
	}

	private void bindRAGConfigurationToBucket(Bucket bucket, RAGConfiguration ragConfiguration) {
		bucketService.bindRAGConfiguration(bucket.getId(), ragConfiguration.getId())
			.await()
			.indefinitely();
	}

	@AfterEach
	void tearDown() {
		removeBucketOne();
		removeRAGConfigurationChatOne();
		removeRAGConfigurationChatTwo();
		removeRAGConfigurationSearch();
		removeRAGConfigurationChatTool();
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

	private void createRAGConfigurationChatOne() {
		RAGConfigurationDTO dto = RAGConfigurationDTO.builder()
			.name(RAG_CONFIGURATION_CHAT_ONE_NAME)
			.type(RAGType.CHAT)
			.build();

		sessionFactory.withTransaction(
			session -> ragConfigurationService.create(session, dto)
		)
		.await()
		.indefinitely();
	}

	private void createRAGConfigurationChatTwo() {
		RAGConfigurationDTO dto = RAGConfigurationDTO.builder()
			.name(RAG_CONFIGURATION_CHAT_TWO_NAME)
			.type(RAGType.CHAT)
			.build();

		sessionFactory.withTransaction(
				session -> ragConfigurationService.create(session, dto)
			)
			.await()
			.indefinitely();
	}

	private void createRAGConfigurationChatTool() {
		RAGConfigurationDTO dto = RAGConfigurationDTO.builder()
			.name(RAG_CONFIGURATION_CHAT_TOOL_NAME)
			.type(RAGType.CHAT_TOOL)
			.build();

		sessionFactory.withTransaction(
				session -> ragConfigurationService.create(session, dto)
			)
			.await()
			.indefinitely();
	}

	private void createRAGConfigurationSearch() {
		RAGConfigurationDTO dto = RAGConfigurationDTO.builder()
			.name(RAG_CONFIGURATION_SEARCH_NAME)
			.type(RAGType.SEARCH)
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

	private RAGConfiguration getRAGConfigurationChatOne() {
		return sessionFactory.withTransaction(
			session ->
				ragConfigurationService.findByName(session, RAG_CONFIGURATION_CHAT_ONE_NAME)
		)
		.await()
		.indefinitely();
	}

	private RAGConfiguration getRAGConfigurationChatTwo() {
		return sessionFactory.withTransaction(
			session ->
				ragConfigurationService.findByName(session, RAG_CONFIGURATION_CHAT_TWO_NAME)
		)
		.await()
		.indefinitely();
	}

	private RAGConfiguration getRAGConfigurationSearch() {
		return sessionFactory.withTransaction(
				session ->
					ragConfigurationService.findByName(session, RAG_CONFIGURATION_SEARCH_NAME)
			)
			.await()
			.indefinitely();
	}

	private RAGConfiguration getRAGConfigurationChatTool
		() {
		return sessionFactory.withTransaction(
				session ->
					ragConfigurationService.findByName(session, RAG_CONFIGURATION_CHAT_TOOL_NAME)
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

	private void removeRAGConfigurationChatOne() {
		var ragConfiguration = getRAGConfigurationChatOne();

		sessionFactory.withTransaction(
			session ->
				ragConfigurationService.deleteById(session, ragConfiguration.getId())
		)
		.await()
		.indefinitely();
	}

	private void removeRAGConfigurationChatTwo() {
		var ragConfiguration = getRAGConfigurationChatTwo();

		sessionFactory.withTransaction(
				session ->
					ragConfigurationService.deleteById(session, ragConfiguration.getId())
			)
			.await()
			.indefinitely();
	}

	private void removeRAGConfigurationSearch() {
		var ragConfiguration = getRAGConfigurationSearch();

		sessionFactory.withTransaction(
				session ->
					ragConfigurationService.deleteById(session, ragConfiguration.getId())
			)
			.await()
			.indefinitely();
	}

	private void removeRAGConfigurationChatTool() {
		var ragConfiguration = getRAGConfigurationChatTool();

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
