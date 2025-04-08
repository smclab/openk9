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

import com.google.protobuf.Struct;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.openk9.client.grpc.common.StructUtils;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.LargeLanguageModel;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.dto.base.BucketDTO;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;
import io.openk9.datasource.model.dto.base.ModelTypeDTO;
import io.openk9.datasource.model.dto.base.RAGConfigurationDTO;
import io.openk9.datasource.model.projection.BucketLargeLanguageModel;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.EmbeddingModelService;
import io.openk9.datasource.service.LargeLanguageModelService;
import io.openk9.datasource.service.RAGConfigurationService;
import io.openk9.searcher.grpc.GetEmbeddingModelConfigurationsRequest;
import io.openk9.searcher.grpc.GetLLMConfigurationsRequest;
import io.openk9.searcher.grpc.GetRAGConfigurationsRequest;
import io.openk9.searcher.grpc.Searcher;
import io.openk9.tenantmanager.grpc.TenantManager;
import io.openk9.tenantmanager.grpc.TenantResponse;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

import static io.openk9.datasource.Initializer.EMBEDDING_MODEL_DEFAULT_PRIMARY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.times;

@QuarkusTest
public class SearcherGrpcTest {

	private static final String ENTITY_NAME_PREFIX = "SearcherGrpcTest - ";
	private static final String BUCKET_ONE = ENTITY_NAME_PREFIX + "Bucket 1";
	private static final int CHUNK_WINDOW = 1500;
	public static final String PROMPT_TEST = "Test prompt";
	private static final String RAG_CHAT_ONE = ENTITY_NAME_PREFIX + "Rag configuration CHAT 1";
	private static final String RAG_SEARCH_ONE = ENTITY_NAME_PREFIX + "Rag configuration SEARCH 1";
	private static final String RAG_CHAT_TOOL_ONE = ENTITY_NAME_PREFIX + "Rag configuration CHAT_TOOL 1";
	private static final String EMBEDDING_MODEL_ONE = ENTITY_NAME_PREFIX + "Embedding model 1 ";
	private static final String EM_API_KEY = "EMST.asdfkaslf01432kl4l1";
	private static final String EM_API_URL = "http://EMST.embeddingapi.local";
	private static final String EM_JSON_CONFIG = "{\n" +
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
	private static final String EM_MODEL = "model";
	private static final String EM_TYPE = "type";
	private static final int EM_VECTOR_SIZE = 1330;
	private static final Bucket BUCKET = new Bucket();
	private static final Bucket BUCKET_RETRIEVE_TYPE_NULL = new Bucket();
	private static final String BLANK_STRING = "";
	private static final String LLM_API_KEY = "api_key";
	private static final String LLM_API_URL = "api_url";
	private static final String LLM_JSON_CONFIG = "{testField: \"test\"}";
	private static final boolean REFORMULATE = true;
	private static final Struct STRUCT_JSON_CONFIG = StructUtils.fromJson(LLM_JSON_CONFIG);
	private static final String SCHEMA_NAME = "public";
	private static final LargeLanguageModel LARGE_LANGUAGE_MODEL = new LargeLanguageModel();
	private static final String VIRTUAL_HOST = "test.openk9.local";
	private static final Logger log = Logger.getLogger(SearcherGrpcTest.class);


	static {
		BUCKET.setRetrieveType(Bucket.RetrieveType.HYBRID);
		LARGE_LANGUAGE_MODEL.setApiKey(LLM_API_KEY);
		LARGE_LANGUAGE_MODEL.setApiUrl(LLM_API_URL);
		LARGE_LANGUAGE_MODEL.setJsonConfig(LLM_JSON_CONFIG);
	}

	@GrpcClient
	Searcher searcher;

	@Inject
	BucketService bucketService;

	@Inject
	EmbeddingModelService embeddingModelService;

	@InjectMock
	LargeLanguageModelService largeLanguageModelService;

	@Inject
	RAGConfigurationService ragConfigurationService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@InjectMock
	@GrpcClient("tenantmanager")
	TenantManager tenantManager;

	@BeforeEach
	void setup() {
		// EmbeddingModel
		createEmbeddingModelOne();
		getEmbeddingModelOne();
		enableEmbeddingModelOne();

		// RAGConfiguration
		createBucketOne();
		createRAGConfiguration(RAG_CHAT_ONE, RAGType.CHAT);
		createRAGConfiguration(RAG_CHAT_TOOL_ONE, RAGType.CHAT_TOOL);
		createRAGConfiguration(RAG_SEARCH_ONE, RAGType.SEARCH);

		bindRAGConfigurationToBucket(getBucketOne(), getRAGConfiguration(RAG_CHAT_ONE));
		bindRAGConfigurationToBucket(getBucketOne(), getRAGConfiguration(RAG_SEARCH_ONE));
		bindRAGConfigurationToBucket(getBucketOne(), getRAGConfiguration(RAG_CHAT_TOOL_ONE));
	}

	@Test
	@RunOnVertxContext
	void should_get_llm_configurations(UniAsserter asserter) {

		BDDMockito.given(tenantManager.findTenant(notNull()))
			.willReturn(Uni.createFrom().item(
				TenantResponse.newBuilder().setSchemaName(SCHEMA_NAME).build()));
		BDDMockito.given(largeLanguageModelService.fetchCurrentLLMAndBucket(anyString()))
			.willReturn(Uni.createFrom().item(
				new BucketLargeLanguageModel(BUCKET, LARGE_LANGUAGE_MODEL)));

		asserter.assertThat(
			() -> searcher.getLLMConfigurations(GetLLMConfigurationsRequest.newBuilder()
				.setVirtualHost(VIRTUAL_HOST)
				.build()
			),
			response -> {
				BDDMockito.then(largeLanguageModelService)
					.should(times(1))
					.fetchCurrentLLMAndBucket(anyString());

				Assertions.assertEquals(LLM_API_KEY, response.getApiKey());
				Assertions.assertEquals(LLM_API_URL, response.getApiUrl());
				Assertions.assertEquals(STRUCT_JSON_CONFIG, response.getJsonConfig());
				Assertions.assertEquals(
					Bucket.RetrieveType.HYBRID.name(), response.getRetrieveType());
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_get_llm_configurations_retrieve_type_null(UniAsserter asserter) {

		BDDMockito.given(tenantManager.findTenant(notNull()))
			.willReturn(Uni.createFrom().item(
				TenantResponse.newBuilder().setSchemaName(SCHEMA_NAME).build()));
		BDDMockito.given(largeLanguageModelService.fetchCurrentLLMAndBucket(anyString()))
			.willReturn(Uni.createFrom().item(
				new BucketLargeLanguageModel(BUCKET_RETRIEVE_TYPE_NULL, LARGE_LANGUAGE_MODEL)));

		asserter.assertThat(
			() -> searcher.getLLMConfigurations(GetLLMConfigurationsRequest.newBuilder()
				.setVirtualHost(VIRTUAL_HOST)
				.build()
			),
			response -> {
				BDDMockito.then(largeLanguageModelService)
					.should(times(1))
					.fetchCurrentLLMAndBucket(anyString());

				log.info(String.format("getLLMConfigurations response: %s", response));

				Assertions.assertEquals(LLM_API_KEY, response.getApiKey());
				Assertions.assertEquals(LLM_API_URL, response.getApiUrl());
				Assertions.assertEquals(STRUCT_JSON_CONFIG, response.getJsonConfig());
				Assertions.assertEquals(BLANK_STRING, response.getRetrieveType());
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_fail_on_get_llm_configurations(UniAsserter asserter) {

		BDDMockito.given(tenantManager.findTenant(notNull()))
			.willReturn(Uni.createFrom().item(
				TenantResponse.newBuilder().setSchemaName(SCHEMA_NAME).build()));
		BDDMockito.given(largeLanguageModelService.fetchCurrentLLMAndBucket(anyString()))
			.willReturn(Uni.createFrom().failure(InternalServiceMockException::new));

		asserter.assertFailedWith(
			() -> searcher.getLLMConfigurations(GetLLMConfigurationsRequest.newBuilder()
				.setVirtualHost(VIRTUAL_HOST)
				.build()
			),
				throwable -> failureAssertions(throwable)
			);
	}

	@Test
	@RunOnVertxContext
	void should_fail_on_get_llm_configurations_llm_null(UniAsserter asserter) {

		BDDMockito.given(tenantManager.findTenant(notNull()))
			.willReturn(Uni.createFrom().item(
				TenantResponse.newBuilder().setSchemaName(SCHEMA_NAME).build()));
		BDDMockito.given(largeLanguageModelService.fetchCurrentLLMAndBucket(anyString()))
			.willReturn(Uni.createFrom().item(
				new BucketLargeLanguageModel(BUCKET, null)));

		asserter.assertFailedWith(
			() -> searcher.getLLMConfigurations(GetLLMConfigurationsRequest.newBuilder()
				.setVirtualHost(VIRTUAL_HOST)
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
	void should_get_embedding_model_configurations(UniAsserter asserter) {
		asserter.assertThat(
			() -> searcher.getEmbeddingModelConfigurations(
				GetEmbeddingModelConfigurationsRequest.newBuilder()
					.setVirtualHost(VIRTUAL_HOST)
					.build()
			),
			response -> {

				log.info(String.format("getEmbeddingModelConfigurations response: %s", response));

				Assertions.assertEquals(EM_API_URL, response.getApiUrl());
				Assertions.assertEquals(EM_API_KEY, response.getApiKey());
				Assertions.assertEquals(
					StructUtils.fromJson(EM_JSON_CONFIG),
					response.getJsonConfig());
				assertEquals(EM_TYPE, response.getModelType().getType());
				assertEquals(EM_MODEL, response.getModelType().getModel());
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_fail_trying_get_missing_rag_configurations(UniAsserter asserter) {
		asserter.assertFailedWith(
			() -> searcher.getRAGConfigurations(
				GetRAGConfigurationsRequest.newBuilder()
					.setVirtualHost(VIRTUAL_HOST)
					.setRagType(io.openk9.searcher.grpc.RAGType.CHAT)
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
					.setRagType(io.openk9.searcher.grpc.RAGType.SEARCH)
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
					.setRagType(io.openk9.searcher.grpc.RAGType.CHAT_TOOL)
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

	@Test
	@RunOnVertxContext
	void should_get_rag_configurations(UniAsserter asserter) {
		// enable bucketOne
		asserter.execute(() ->
			sessionFactory.withTransaction(session ->
				bucketService.findByName(session, BUCKET_ONE)
					.flatMap(bucketOne ->
						bucketService.enableTenant(session, bucketOne.getId())
					)
			)
		);

		asserter.assertThat(
			() -> searcher.getRAGConfigurations(
				GetRAGConfigurationsRequest.newBuilder()
					.setVirtualHost(VIRTUAL_HOST)
					.setRagType(io.openk9.searcher.grpc.RAGType.CHAT)
					.build()
			),
			response -> {
				log.info(String.format(
					"getRAGConfigurations %s response: %s",
					io.openk9.searcher.grpc.RAGType.CHAT.name(),
					response.toString()
				));

				assertEquals(RAG_CHAT_ONE, response.getName());
				assertEquals(CHUNK_WINDOW, response.getChunkWindow());
				assertEquals(PROMPT_TEST, response.getPrompt());
				assertEquals(PROMPT_TEST, response.getPromptNoRag());
				assertEquals(PROMPT_TEST, response.getRagToolDescription());
				assertEquals(PROMPT_TEST, response.getRephrasePrompt());
				assertEquals(REFORMULATE, response.getReformulate());
			}
		);

		asserter.assertThat(
			() -> searcher.getRAGConfigurations(
				GetRAGConfigurationsRequest.newBuilder()
					.setVirtualHost(VIRTUAL_HOST)
					.setRagType(io.openk9.searcher.grpc.RAGType.SEARCH)
					.build()
			),
			response -> {
				log.info(String.format(
					"getRAGConfigurations %s response: %s",
					io.openk9.searcher.grpc.RAGType.SEARCH.name(),
					response.toString()
				));

				assertEquals(RAG_SEARCH_ONE, response.getName());
				assertEquals(CHUNK_WINDOW, response.getChunkWindow());
				assertEquals(PROMPT_TEST, response.getPrompt());
				assertEquals(PROMPT_TEST, response.getPromptNoRag());
				assertEquals(PROMPT_TEST, response.getRagToolDescription());
				assertEquals(PROMPT_TEST, response.getRephrasePrompt());
				assertEquals(REFORMULATE, response.getReformulate());
			}
		);

		asserter.assertThat(
			() -> searcher.getRAGConfigurations(
				GetRAGConfigurationsRequest.newBuilder()
					.setVirtualHost(VIRTUAL_HOST)
					.setRagType(io.openk9.searcher.grpc.RAGType.CHAT_TOOL)
					.build()
			),
			response -> {
				log.info(String.format(
					"getRAGConfigurations %s response: %s",
					io.openk9.searcher.grpc.RAGType.CHAT_TOOL.name(),
					response.toString()
				));

				assertEquals(RAG_CHAT_TOOL_ONE, response.getName());
				assertEquals(CHUNK_WINDOW, response.getChunkWindow());
				assertEquals(PROMPT_TEST, response.getPrompt());
				assertEquals(PROMPT_TEST, response.getPromptNoRag());
				assertEquals(PROMPT_TEST, response.getRagToolDescription());
				assertEquals(PROMPT_TEST, response.getRephrasePrompt());
				assertEquals(REFORMULATE, response.getReformulate());
			}
		);
	}

	@AfterEach
	void tearDown() {
		// EmbeddingModel
		enableEmbeddingModelDefaultPrimary();
		removeEmbeddingModelOne();

		// enable default bucket
		enableBucket(getBucketDefault());

		// RAGConfiguration
		removeBucketOne();
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

	private EmbeddingModel createEmbeddingModelOne() {
		var dto = EmbeddingModelDTO
			.builder()
			.name(EMBEDDING_MODEL_ONE)
			.apiUrl(EM_API_URL)
			.apiKey(EM_API_KEY)
			.vectorSize(EM_VECTOR_SIZE)
			.jsonConfig(EM_JSON_CONFIG)
			.modelType(
				ModelTypeDTO
					.builder()
					.type(EM_TYPE)
					.model(EM_MODEL)
					.build()
			)
			.build();

		return sessionFactory.withTransaction(
			session -> embeddingModelService.create(session, dto)
		)
		.await()
		.indefinitely();
	}

	private void createRAGConfiguration(String name, RAGType type) {
		RAGConfigurationDTO dto = RAGConfigurationDTO.builder()
			.name(name)
			.type(type)
			.chunkWindow(CHUNK_WINDOW)
			.prompt(PROMPT_TEST)
			.promptNoRag(PROMPT_TEST)
			.ragToolDescription(PROMPT_TEST)
			.rephrasePrompt(PROMPT_TEST)
			.reformulate(REFORMULATE)
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
				s -> embeddingModelService.findByName(s, EMBEDDING_MODEL_DEFAULT_PRIMARY)
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
