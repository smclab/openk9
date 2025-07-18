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

package io.openk9.datasource.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import io.openk9.client.grpc.common.StructUtils;
import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.Initializer;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.LargeLanguageModel;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.dto.base.BucketDTO;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;
import io.openk9.datasource.model.dto.base.LargeLanguageModelDTO;
import io.openk9.datasource.model.dto.base.ProviderModelDTO;
import io.openk9.datasource.model.dto.request.CreateRAGConfigurationDTO;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.EmbeddingModelService;
import io.openk9.datasource.service.LargeLanguageModelService;
import io.openk9.datasource.service.RAGConfigurationService;
import io.openk9.searcher.grpc.GetLLMConfigurationsRequest;
import io.openk9.searcher.grpc.GetRAGConfigurationsRequest;
import io.openk9.searcher.grpc.Searcher;

import com.google.protobuf.Struct;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class SearcherGrpcTest {

	private static final String ENTITY_NAME_PREFIX = "SearcherGrpcTest - ";

	private static final Bucket BUCKET = new Bucket();
	private static final String BUCKET_ONE = ENTITY_NAME_PREFIX + "Bucket 1";
	private static final String BUCKET_TWO = ENTITY_NAME_PREFIX + "Bucket 2";
	private static final int CHUNK_WINDOW = 1500;
	private static final int CONTEXT_WINDOW_VALUE = 1300;
	private static final String EMBEDDING_MODEL_ONE = ENTITY_NAME_PREFIX + "Embedding model 1 ";
	private static final String EM_API_KEY = "EMST.asdfkaslf01432kl4l1";
	private static final String EM_API_URL = "http://EMST.embeddingapi.local";
	private static final int EM_VECTOR_SIZE = 1330;
	private static final String JSON_CONFIG = "{\n" +
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
	private static final String JSON_CONFIG_SHORT = "{testField: \"test\"}";
	private static final LargeLanguageModel LARGE_LANGUAGE_MODEL = new LargeLanguageModel();
	private static final String LLM_ONE_NAME = ENTITY_NAME_PREFIX + "Large language model 1 ";
	private static final String LLM_API_KEY = "api_key";
	private static final String LLM_API_URL = "api_url";
	private static final String MODEL = "model";
	private static final String PROMPT_TEST = "Test prompt";
	private static final String PROVIDER = "provider";
	private static final String RAG_CHAT_ONE = ENTITY_NAME_PREFIX + "Rag configuration CHAT 1";
	private static final String RAG_SEARCH_ONE = ENTITY_NAME_PREFIX + "Rag configuration SEARCH 1";
	private static final String RAG_CHAT_TOOL_ONE = ENTITY_NAME_PREFIX + "Rag configuration CHAT_TOOL 1";
	private static final boolean REFORMULATE = true;
	private static final Struct STRUCT_JSON_CONFIG = StructUtils.fromJson(JSON_CONFIG);
	private static final Struct STRUCT_JSON_CONFIG_SHORT = StructUtils.fromJson(JSON_CONFIG_SHORT);
	private static final String SCHEMA_NAME = "public";
	private static final String VIRTUAL_HOST = "test.openk9.local";
	private static final Logger log = Logger.getLogger(SearcherGrpcTest.class);


	static {
		BUCKET.setRetrieveType(Bucket.RetrieveType.HYBRID);
		LARGE_LANGUAGE_MODEL.setApiKey(LLM_API_KEY);
		LARGE_LANGUAGE_MODEL.setApiUrl(LLM_API_URL);
		LARGE_LANGUAGE_MODEL.setJsonConfig(JSON_CONFIG_SHORT);
	}

	@GrpcClient
	Searcher searcher;

	@Inject
	BucketService bucketService;

	@Inject
	EmbeddingModelService embeddingModelService;

	@Inject
	LargeLanguageModelService largeLanguageModelService;

	@Inject
	RAGConfigurationService ragConfigurationService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@BeforeEach
	void setup() {
		// EmbeddingModel
		createEmbeddingModelOne();
		enableEmbeddingModelOne();

		// Bucket
		createBucketOne();
		enableBucket(getBucketOne());

		// LargeLanguageModel
		createLargeLanguageModelOne();
		enableLargeLanguageModelOne();

		// RAGConfiguration

		EntitiesUtils.createRAGConfiguration(
			ragConfigurationService,
			CreateRAGConfigurationDTO.builder()
				.name(RAG_CHAT_ONE)
				.type(RAGType.CHAT_RAG)
				.chunkWindow(CHUNK_WINDOW)
				.prompt(PROMPT_TEST)
				.promptNoRag(PROMPT_TEST)
				.ragToolDescription(PROMPT_TEST)
				.rephrasePrompt(PROMPT_TEST)
				.reformulate(REFORMULATE)
				.jsonConfig(JSON_CONFIG)
				.build()
		);
		EntitiesUtils.createRAGConfiguration(
			ragConfigurationService,
			CreateRAGConfigurationDTO.builder()
				.name(RAG_CHAT_TOOL_ONE)
				.type(RAGType.CHAT_RAG_TOOL)
				.chunkWindow(CHUNK_WINDOW)
				.prompt(PROMPT_TEST)
				.promptNoRag(PROMPT_TEST)
				.ragToolDescription(PROMPT_TEST)
				.rephrasePrompt(PROMPT_TEST)
				.reformulate(REFORMULATE)
				.jsonConfig(JSON_CONFIG)
				.build()
		);
		EntitiesUtils.createRAGConfiguration(
			ragConfigurationService,
			CreateRAGConfigurationDTO.builder()
				.name(RAG_SEARCH_ONE)
				.type(RAGType.SIMPLE_GENERATE)
				.chunkWindow(CHUNK_WINDOW)
				.prompt(PROMPT_TEST)
				.promptNoRag(PROMPT_TEST)
				.ragToolDescription(PROMPT_TEST)
				.rephrasePrompt(PROMPT_TEST)
				.reformulate(REFORMULATE)
				.jsonConfig(JSON_CONFIG)
				.build()
		);

		bindRAGConfigurationToBucket(getBucketOne(), getRAGConfiguration(RAG_CHAT_ONE));
		bindRAGConfigurationToBucket(getBucketOne(), getRAGConfiguration(RAG_SEARCH_ONE));
		bindRAGConfigurationToBucket(getBucketOne(), getRAGConfiguration(RAG_CHAT_TOOL_ONE));
	}

	@Test
	@RunOnVertxContext
	void should_get_llm_configurations(UniAsserter asserter) {
		asserter.assertThat(
			() -> searcher.getLLMConfigurations(GetLLMConfigurationsRequest.newBuilder()
				.setVirtualHost(VIRTUAL_HOST)
				.build()
			),
			response -> {

				Assertions.assertEquals(LLM_API_KEY, response.getApiKey());
				Assertions.assertEquals(LLM_API_URL, response.getApiUrl());
				Assertions.assertEquals(STRUCT_JSON_CONFIG_SHORT, response.getJsonConfig());
				Assertions.assertEquals(
					Bucket.RetrieveType.TEXT.name(), response.getRetrieveType());
				Assertions.assertEquals(PROVIDER, response.getProviderModel().getProvider());
				Assertions.assertEquals(MODEL, response.getProviderModel().getModel());
				Assertions.assertEquals(CONTEXT_WINDOW_VALUE, response.getContextWindow());
				assertTrue(response.getRetrieveCitations());
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_get_rag_configurations(UniAsserter asserter) {
		asserter.assertThat(
			() -> searcher.getRAGConfigurations(
				GetRAGConfigurationsRequest.newBuilder()
					.setVirtualHost(VIRTUAL_HOST)
					.setRagType(io.openk9.searcher.grpc.RAGType.CHAT_RAG)
					.build()
			),
			response -> {
				log.info(String.format(
					"getRAGConfigurations %s response: %s",
					io.openk9.searcher.grpc.RAGType.CHAT_RAG.name(),
					response.toString()
				));

				assertEquals(RAG_CHAT_ONE, response.getName());
				assertEquals(CHUNK_WINDOW, response.getChunkWindow());
				assertEquals(PROMPT_TEST, response.getPrompt());
				assertEquals(PROMPT_TEST, response.getPromptNoRag());
				assertEquals(PROMPT_TEST, response.getRagToolDescription());
				assertEquals(PROMPT_TEST, response.getRephrasePrompt());
				assertEquals(REFORMULATE, response.getReformulate());
				assertEquals(STRUCT_JSON_CONFIG, response.getJsonConfig());
			}
		);

		asserter.assertThat(
			() -> searcher.getRAGConfigurations(
				GetRAGConfigurationsRequest.newBuilder()
					.setVirtualHost(VIRTUAL_HOST)
					.setRagType(io.openk9.searcher.grpc.RAGType.SIMPLE_GENERATE)
					.build()
			),
			response -> {
				log.info(String.format(
					"getRAGConfigurations %s response: %s",
					io.openk9.searcher.grpc.RAGType.SIMPLE_GENERATE.name(),
					response.toString()
				));

				assertEquals(RAG_SEARCH_ONE, response.getName());
				assertEquals(CHUNK_WINDOW, response.getChunkWindow());
				assertEquals(PROMPT_TEST, response.getPrompt());
				assertEquals(PROMPT_TEST, response.getPromptNoRag());
				assertEquals(PROMPT_TEST, response.getRagToolDescription());
				assertEquals(PROMPT_TEST, response.getRephrasePrompt());
				assertEquals(REFORMULATE, response.getReformulate());
				assertEquals(STRUCT_JSON_CONFIG, response.getJsonConfig());
			}
		);

		asserter.assertThat(
			() -> searcher.getRAGConfigurations(
				GetRAGConfigurationsRequest.newBuilder()
					.setVirtualHost(VIRTUAL_HOST)
					.setRagType(io.openk9.searcher.grpc.RAGType.CHAT_RAG_TOOL)
					.build()
			),
			response -> {
				log.info(String.format(
					"getRAGConfigurations %s response: %s",
					io.openk9.searcher.grpc.RAGType.CHAT_RAG_TOOL.name(),
					response.toString()
				));

				assertEquals(RAG_CHAT_TOOL_ONE, response.getName());
				assertEquals(CHUNK_WINDOW, response.getChunkWindow());
				assertEquals(PROMPT_TEST, response.getPrompt());
				assertEquals(PROMPT_TEST, response.getPromptNoRag());
				assertEquals(PROMPT_TEST, response.getRagToolDescription());
				assertEquals(PROMPT_TEST, response.getRephrasePrompt());
				assertEquals(REFORMULATE, response.getReformulate());
				assertEquals(STRUCT_JSON_CONFIG, response.getJsonConfig());
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_fail_trying_get_missing_rag_configurations(UniAsserter asserter) {

		// create a Bucket without any RAGConfigurations and enables it
		asserter.execute(() ->
			sessionFactory.withTransaction(session ->
				bucketService.create(
					session,
					BucketDTO.builder()
						.name(BUCKET_TWO)
						.refreshOnSuggestionCategory(false)
						.refreshOnTab(false)
						.refreshOnDate(false)
						.refreshOnQuery(false)
						.retrieveType(Bucket.RetrieveType.TEXT)
						.build()
				)
				.flatMap(bucketTwo ->
					bucketService.enableTenant(session, bucketTwo.getId())
				)
			)
		);

		asserter.assertFailedWith(
			() -> searcher.getRAGConfigurations(
				GetRAGConfigurationsRequest.newBuilder()
					.setVirtualHost(VIRTUAL_HOST)
					.setRagType(io.openk9.searcher.grpc.RAGType.CHAT_RAG)
					.build()
			),
			throwable -> {
				Assertions.assertInstanceOf(StatusRuntimeException.class, throwable);

				var exception = (StatusRuntimeException) throwable;

				Assertions.assertEquals(
					Status.Code.NOT_FOUND, exception.getStatus().getCode());
			});

		asserter.assertFailedWith(
			() -> searcher.getRAGConfigurations(
				GetRAGConfigurationsRequest.newBuilder()
					.setVirtualHost(VIRTUAL_HOST)
					.setRagType(io.openk9.searcher.grpc.RAGType.SIMPLE_GENERATE)
					.build()
			),
			throwable -> {
				Assertions.assertInstanceOf(StatusRuntimeException.class, throwable);

				var exception = (StatusRuntimeException) throwable;

				Assertions.assertEquals(
					Status.Code.NOT_FOUND, exception.getStatus().getCode());
			});

		asserter.assertFailedWith(
			() -> searcher.getRAGConfigurations(
				GetRAGConfigurationsRequest.newBuilder()
					.setVirtualHost(VIRTUAL_HOST)
					.setRagType(io.openk9.searcher.grpc.RAGType.CHAT_RAG_TOOL)
					.build()
			),
			throwable -> {
				Assertions.assertInstanceOf(StatusRuntimeException.class, throwable);

				var exception = (StatusRuntimeException) throwable;

				Assertions.assertEquals(
					Status.Code.NOT_FOUND, exception.getStatus().getCode());
			});
	}

	@Test
	@RunOnVertxContext
	void should_fail_trying_get_rag_configurations_with_missing_rag_type(UniAsserter asserter) {
		asserter.assertFailedWith(
			() -> searcher.getRAGConfigurations(
				GetRAGConfigurationsRequest.newBuilder()
					.setVirtualHost(VIRTUAL_HOST)
					.build()
			),
			throwable -> {
				Assertions.assertInstanceOf(StatusRuntimeException.class, throwable);

				var exception = (StatusRuntimeException) throwable;

				Assertions.assertEquals(
					Status.Code.INVALID_ARGUMENT, exception.getStatus().getCode());
			});
	}

	@AfterEach
	void tearDown() {
		// EmbeddingModel
		enableEmbeddingModelDefaultPrimary();
		removeEmbeddingModelOne();

		// LargeLanguageModel
		enableLargeLanguageModelDefaultPrimary();
		removeLargeLanguageModelOne();

		// Bucket
		enableBucket(getBucketDefault());
		removeBucketOne();

		// RAGConfiguration
		removeRAGConfiguration(RAG_CHAT_ONE);
		removeRAGConfiguration(RAG_SEARCH_ONE);
		removeRAGConfiguration(RAG_CHAT_TOOL_ONE);
	}

	private static <T> void failureAssertions(Throwable throwable) {

		Assertions.assertInstanceOf(StatusRuntimeException.class, throwable);

		var exception = (StatusRuntimeException) throwable;

		assertTrue(exception
			.getMessage()
			.contains(InternalServiceMockException.class.getName())
		);
	}

	private void bindRAGConfigurationToBucket(Bucket bucket, RAGConfiguration ragConfiguration) {
		bucketService.bindRAGConfiguration(bucket.getId(), ragConfiguration.getId())
			.await()
			.indefinitely();
	}

	private void createBucketOne() {
		BucketDTO dto = BucketDTO.builder()
			.name(BUCKET_ONE)
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

	private EmbeddingModel createEmbeddingModelOne() {
		var dto = EmbeddingModelDTO
			.builder()
			.name(EMBEDDING_MODEL_ONE)
			.apiUrl(EM_API_URL)
			.apiKey(EM_API_KEY)
			.vectorSize(EM_VECTOR_SIZE)
			.jsonConfig(JSON_CONFIG)
			.providerModel(
				ProviderModelDTO
					.builder()
					.provider(PROVIDER)
					.model(MODEL)
					.build()
			)
			.build();

		return sessionFactory.withTransaction(
			session -> embeddingModelService.create(session, dto)
		)
		.await()
		.indefinitely();
	}

	private LargeLanguageModel createLargeLanguageModelOne() {
		var dto = LargeLanguageModelDTO
			.builder()
			.name(LLM_ONE_NAME)
			.apiUrl(LLM_API_URL)
			.apiKey(LLM_API_KEY)
			.jsonConfig(JSON_CONFIG_SHORT)
			.contextWindow(CONTEXT_WINDOW_VALUE)
			.retrieveCitations(true)
			.providerModel(
				ProviderModelDTO
					.builder()
					.provider(PROVIDER)
					.model(MODEL)
					.build()
			)
			.build();

		return sessionFactory.withTransaction(
				session -> largeLanguageModelService.create(session, dto)
			)
			.await()
			.indefinitely();
	}

	private void createRAGConfiguration(String name, RAGType type) {
		CreateRAGConfigurationDTO dto = CreateRAGConfigurationDTO.builder()
			.name(name)
			.type(type)
			.chunkWindow(CHUNK_WINDOW)
			.prompt(PROMPT_TEST)
			.promptNoRag(PROMPT_TEST)
			.ragToolDescription(PROMPT_TEST)
			.rephrasePrompt(PROMPT_TEST)
			.reformulate(REFORMULATE)
			.jsonConfig(JSON_CONFIG)
			.build();

		sessionFactory.withTransaction(
				session -> ragConfigurationService.create(session, dto)
			)
			.await()
			.indefinitely();
	}

	private void enableBucket(Bucket bucket) {
		sessionFactory.withTransaction(
			session ->
				bucketService.enableTenant(session, bucket.getId())
		)
		.await()
		.indefinitely();
	}

	private EmbeddingModel enableEmbeddingModelDefaultPrimary() {
		var embeddingModel = getEmbeddingModelDefaultPrimary();

		return sessionFactory.withTransaction(
				session -> embeddingModelService.enable(session, embeddingModel.getId())
			)
			.await()
			.indefinitely();
	}

	private EmbeddingModel enableEmbeddingModelOne() {
		var embeddingModel = getEmbeddingModelOne();

		return sessionFactory.withTransaction(
			session -> embeddingModelService.enable(session, embeddingModel.getId())
		)
		.await()
		.indefinitely();
	}

	private void enableLargeLanguageModelDefaultPrimary() {
		var languageModel = getLargeLanguageModelDefaultPrimary();

		sessionFactory.withTransaction(
				session -> largeLanguageModelService.enable(session, languageModel.getId())
			)
			.await()
			.indefinitely();
	}

	private void enableLargeLanguageModelOne() {
		var languageModel = getLargeLanguageModelOne();

		sessionFactory.withTransaction(
				session -> largeLanguageModelService.enable(session, languageModel.getId())
			)
			.await()
			.indefinitely();
	}

	private Bucket getBucketDefault() {
		return sessionFactory.withTransaction(
				session ->
					bucketService.findByName(
						session,
						io.openk9.datasource.model.init.Bucket.INSTANCE.getName())
			)
			.await()
			.indefinitely();
	}

	private Bucket getBucketOne() {
		return sessionFactory.withTransaction(
				session ->
					bucketService.findByName(session, BUCKET_ONE)
			)
			.await()
			.indefinitely();
	}

	private EmbeddingModel getEmbeddingModelDefaultPrimary() {
		return sessionFactory.withTransaction(
				s ->
					embeddingModelService.findByName(s, Initializer.EMBEDDING_MODEL_DEFAULT_PRIMARY)
			)
			.await()
			.indefinitely();
	}

	private EmbeddingModel getEmbeddingModelOne() {
		return sessionFactory.withTransaction(
				s -> embeddingModelService.findByName(s, EMBEDDING_MODEL_ONE)
			)
			.await()
			.indefinitely();
	}

	private LargeLanguageModel getLargeLanguageModelDefaultPrimary() {
		return sessionFactory.withTransaction(
				s ->
					largeLanguageModelService.findByName(s, Initializer.LLM_DEFAULT_PRIMARY)
			)
			.await()
			.indefinitely();
	}

	private LargeLanguageModel getLargeLanguageModelOne() {
		return sessionFactory.withTransaction(
				s -> largeLanguageModelService.findByName(s, LLM_ONE_NAME)
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

	private EmbeddingModel removeEmbeddingModelOne() {
		var embeddingModel = getEmbeddingModelOne();

		return sessionFactory.withTransaction(
			SCHEMA_NAME,
			(session, transaction) ->
			embeddingModelService.deleteById(session, embeddingModel.getId())
		)
		.await()
		.indefinitely();
	}

	private LargeLanguageModel removeLargeLanguageModelOne() {
		var largeLanguageModel = getLargeLanguageModelOne();

		return sessionFactory.withTransaction(
				SCHEMA_NAME,
				(session, transaction) ->
					largeLanguageModelService.deleteById(session, largeLanguageModel.getId())
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
}
