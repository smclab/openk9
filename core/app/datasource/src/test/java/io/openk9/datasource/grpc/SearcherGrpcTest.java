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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Collectors;
import jakarta.inject.Inject;

import io.openk9.client.grpc.common.StructUtils;
import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.Initializer;
import io.openk9.datasource.model.Autocomplete;
import io.openk9.datasource.model.Autocorrection;
import io.openk9.datasource.model.BooleanOperator;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.LargeLanguageModel;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.SortType;
import io.openk9.datasource.model.SuggestMode;
import io.openk9.datasource.model.dto.base.AutocompleteDTO;
import io.openk9.datasource.model.dto.base.AutocorrectionDTO;
import io.openk9.datasource.model.dto.base.BucketDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;
import io.openk9.datasource.model.dto.base.LargeLanguageModelDTO;
import io.openk9.datasource.model.dto.base.ProviderModelDTO;
import io.openk9.datasource.model.dto.request.CreateRAGConfigurationDTO;
import io.openk9.datasource.service.AutocompleteService;
import io.openk9.datasource.service.AutocorrectionService;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.DocTypeFieldService;
import io.openk9.datasource.service.EmbeddingModelService;
import io.openk9.datasource.service.LargeLanguageModelService;
import io.openk9.datasource.service.RAGConfigurationService;
import io.openk9.searcher.grpc.AutocompleteConfigurationsRequest;
import io.openk9.searcher.grpc.AutocorrectionConfigurationsRequest;
import io.openk9.searcher.grpc.GetEmbeddingModelConfigurationsRequest;
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

	private static final String AUTOCOMPLETE_NAME_ONE = ENTITY_NAME_PREFIX + "Autocomplete 1";
	private static final String AUTOCORRECTION_NAME_ONE = ENTITY_NAME_PREFIX + "Autocorrection 1";
	private static final Bucket BUCKET = new Bucket();
	private static final String BUCKET_ONE = ENTITY_NAME_PREFIX + "Bucket 1";
	private static final String BUCKET_TWO = ENTITY_NAME_PREFIX + "Bucket 2";
	private static final int CHUNK_WINDOW = 1500;
	private static final int CONTEXT_WINDOW_VALUE = 1300;
	private static final String DOC_TYPE_FIELD_NAME_ONE = ENTITY_NAME_PREFIX + "Doc type field 1";
	private static final String DOC_TYPE_FIELD_NAME_TWO = ENTITY_NAME_PREFIX + "Doc type field 2";
	private static final String EMBEDDING_MODEL_ONE = ENTITY_NAME_PREFIX + "Embedding model 1 ";
	private static final String EM_API_KEY = "EMST.asdfkaslf01432kl4l1";
	private static final String EM_API_URL = "http://EMST.embeddingapi.local";
	private static final int EM_VECTOR_SIZE = 1330;
	private static final String FUZZINESS = "2";
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
	private static final int MAX_EDIT = 2;
	private static final int MIN_WORD_LENGTH = 3;
	private static final String MINIMUM_SHOULD_MATCH = "50%";
	private static final String MODEL = "model";
	private static final BooleanOperator OPERATOR = BooleanOperator.AND;
	private static final int PREFIX_LENGTH = 3;
	private static final String PROMPT_TEST = "Test prompt";
	private static final String PROVIDER = "provider";
	private static final String RAG_CHAT_ONE = ENTITY_NAME_PREFIX + "Rag configuration CHAT 1";
	private static final String RAG_SEARCH_ONE = ENTITY_NAME_PREFIX + "Rag configuration SEARCH 1";
	private static final String RAG_CHAT_TOOL_ONE = ENTITY_NAME_PREFIX + "Rag configuration CHAT_TOOL 1";
	private static final boolean REFORMULATE = true;
	private static final int RESULT_SIZE = 7;
	private static final Struct STRUCT_JSON_CONFIG = StructUtils.fromJson(JSON_CONFIG);
	private static final Struct STRUCT_JSON_CONFIG_SHORT = StructUtils.fromJson(JSON_CONFIG_SHORT);
	private static final String SCHEMA_NAME = "public";
	private static final String VIRTUAL_HOST = "test.openk9.local";

	private static final Logger log = Logger.getLogger(SearcherGrpcTest.class);

	private String docTypeFieldPath = "";
	private String defaultDataindexNames = "";

	static {
		BUCKET.setRetrieveType(Bucket.RetrieveType.HYBRID);
		LARGE_LANGUAGE_MODEL.setApiKey(LLM_API_KEY);
		LARGE_LANGUAGE_MODEL.setApiUrl(LLM_API_URL);
		LARGE_LANGUAGE_MODEL.setJsonConfig(JSON_CONFIG_SHORT);
	}

	@GrpcClient
	Searcher searcher;

	@Inject
	AutocompleteService autocompleteService;

	@Inject
	AutocorrectionService autocorrectionService;

	@Inject
	BucketService bucketService;

	@Inject
	DatasourceService datasourceService;

	@Inject
	DocTypeFieldService docTypeFieldService;

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

		// Datasource
		var defaultDatasource = EntitiesUtils.getDatasource(
			Initializer.INIT_DATASOURCE_CONNECTION, datasourceService, sessionFactory);
		defaultDataindexNames = defaultDatasource.getDataIndexes().stream()
			.map(dataIndex -> SCHEMA_NAME + "-" + dataIndex.getName())
			.collect(Collectors.joining(","));

		// Bucket
		createBucketOne();
		var bucketOne = EntitiesUtils.getEntity(
			BUCKET_ONE,
			bucketService,
			sessionFactory
		);
		enableBucket(bucketOne);
		bucketService.addDatasource(bucketOne.getId(), defaultDatasource.getId())
			.await()
			.indefinitely();

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

		bindRAGConfigurationToBucket(bucketOne, getRAGConfiguration(RAG_CHAT_ONE));
		bindRAGConfigurationToBucket(bucketOne, getRAGConfiguration(RAG_SEARCH_ONE));
		bindRAGConfigurationToBucket(bucketOne, getRAGConfiguration(RAG_CHAT_TOOL_ONE));

		// DocTypeField
		DocTypeFieldDTO fieldDtoOne = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_NAME_ONE)
			.fieldName("fieldOne")
			.fieldType(FieldType.SEARCH_AS_YOU_TYPE)
			.boost(3D)
			.build();
		DocTypeFieldDTO fieldDtoTwo = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_NAME_TWO)
			.fieldName("fieldTwo")
			.fieldType(FieldType.SEARCH_AS_YOU_TYPE)
			.build();

		EntitiesUtils.createEntity(fieldDtoOne, docTypeFieldService, sessionFactory);
		EntitiesUtils.createEntity(fieldDtoTwo, docTypeFieldService, sessionFactory);

		var allDocTypeFields = EntitiesUtils.getAllEntities(docTypeFieldService, sessionFactory);

		// Autocorrection
		var firstSampleTextField = allDocTypeFields.stream()
			.filter(field -> "sample".equalsIgnoreCase(field.getDocType().getName()))
			.filter(field -> FieldType.TEXT.equals(field.getFieldType()))
			.findFirst();

		var docTypeFieldId = 0L;

		if (firstSampleTextField.isPresent()) {
			docTypeFieldId = firstSampleTextField.get().getId();
			docTypeFieldPath = firstSampleTextField.get().getPath();
		}

		AutocorrectionDTO autocorrectionDTO = AutocorrectionDTO.builder()
			.name(AUTOCORRECTION_NAME_ONE)
			.autocorrectionDocTypeFieldId(docTypeFieldId)
			.sort(SortType.FREQUENCY)
			.suggestMode(SuggestMode.MISSING)
			.maxEdit(MAX_EDIT)
			.minWordLength(MIN_WORD_LENGTH)
			.prefixLength(PREFIX_LENGTH)
			.enableSearchWithCorrection(true)
			.build();

		EntitiesUtils.createEntity(
			autocorrectionDTO,
			autocorrectionService,
			sessionFactory
		);

		var autocorrectionOne =
			EntitiesUtils.getEntity(AUTOCORRECTION_NAME_ONE, autocorrectionService, sessionFactory);

		assertNotNull(autocorrectionOne.getAutocorrectionDocTypeField());

		bindAutocorrectionToBucket(bucketOne, autocorrectionOne);

		// Autocomplete
		var autocompleteFieldIds = allDocTypeFields.stream()
			.filter(DocTypeField::isAutocomplete)
			.map(DocTypeField::getId)
			.collect(Collectors.toSet());

		var autocompleteDTO = AutocompleteDTO.builder()
			.name(AUTOCOMPLETE_NAME_ONE)
			.fieldIds(autocompleteFieldIds)
			.resultSize(RESULT_SIZE)
			.fuzziness(FUZZINESS)
			.minimumShouldMatch(MINIMUM_SHOULD_MATCH)
			.operator(OPERATOR)
			.build();

		EntitiesUtils.createEntity(autocompleteDTO,autocompleteService, sessionFactory);

		var autocompleteOne =
			EntitiesUtils.getAutocomplete(
				AUTOCOMPLETE_NAME_ONE,
				autocompleteService,
				sessionFactory
			);

		assertNotNull(autocompleteOne.getFields());
		assertFalse(autocompleteOne.getFields().isEmpty());
		assertEquals(autocompleteFieldIds.size(), autocompleteOne.getFields().size());

		bindAutocompleteToBucket(bucketOne, autocompleteOne);
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
				Assertions.assertEquals(STRUCT_JSON_CONFIG, response.getJsonConfig());
				assertEquals(PROVIDER, response.getProviderModel().getProvider());
				assertEquals(MODEL, response.getProviderModel().getModel());
				assertEquals(EM_VECTOR_SIZE, response.getVectorSize());
			}
		);
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
	void should_get_autocomplete_configurations(UniAsserter asserter) {
		asserter.assertThat(
			() -> searcher.getAutocompleteConfigurations(
				AutocompleteConfigurationsRequest.newBuilder()
					.setVirtualHost(VIRTUAL_HOST)
					.build()
			),
			response -> {
				log.debug(String.format("Response on new line: \n%s\n", response));

				var indexNameList = response.getIndexNameList();

				String indexNames =
					String.join(",", indexNameList);

				assertTrue(defaultDataindexNames.equalsIgnoreCase(indexNames));
				assertFalse(response.getAllFields().isEmpty());
				assertEquals(2, response.getFieldList().size());
				assertEquals(RESULT_SIZE, response.getResultSize());
				assertEquals(FUZZINESS, response.getFuzziness());
				assertEquals(MINIMUM_SHOULD_MATCH, response.getMinimumShouldMatch());
				assertEquals(OPERATOR.name(), response.getOperator().name());
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_get_autocorrection_configurations(UniAsserter asserter) {
		asserter.assertThat(
			() -> searcher.getAutocorrectionConfigurations(
				AutocorrectionConfigurationsRequest.newBuilder()
					.setVirtualHost(VIRTUAL_HOST)
					.build()
			),
			response -> {
				log.debug(String.format("Response on new line: \n%s\n", response));

				var indexNameList = response.getIndexNameList();

				String indexNames =
					String.join(",", indexNameList);

				assertTrue(defaultDataindexNames.equalsIgnoreCase(indexNames));
				assertEquals(docTypeFieldPath, response.getField());
				assertEquals(SortType.FREQUENCY.name(), response.getSort().name());
				assertEquals(SuggestMode.MISSING.name(), response.getSuggestMode().name());
				assertEquals(MAX_EDIT, response.getMaxEdit());
				assertEquals(MIN_WORD_LENGTH, response.getMinWordLength());
				assertEquals(PREFIX_LENGTH, response.getPrefixLength());
				assertTrue(response.getEnableSearchWithCorrection());
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

		// Autocorrection
		unbindAutocorrectionToBucket(getBucketOne());
		EntitiesUtils.removeEntity(AUTOCORRECTION_NAME_ONE, autocorrectionService, sessionFactory);

		// Autocomplete
		unbindAutocompleteToBucket(getBucketOne());
		EntitiesUtils.removeEntity(AUTOCOMPLETE_NAME_ONE, autocompleteService, sessionFactory);

		// Bucket
		enableBucket(getBucketDefault());
		var bucketOne = EntitiesUtils.getEntity(BUCKET_ONE, bucketService, sessionFactory);
		EntitiesUtils.cleanBucket(bucketOne, bucketService);
		removeBucketOne();

		// RAGConfiguration
		EntitiesUtils.removeEntity(
			RAG_CHAT_ONE,
			ragConfigurationService,
			sessionFactory
		);
		EntitiesUtils.removeEntity(
			RAG_SEARCH_ONE,
			ragConfigurationService,
			sessionFactory
		);
		EntitiesUtils.removeEntity(
			RAG_CHAT_TOOL_ONE,
			ragConfigurationService,
			sessionFactory
		);

		EntitiesUtils.removeEntity(DOC_TYPE_FIELD_NAME_ONE, docTypeFieldService, sessionFactory);
		EntitiesUtils.removeEntity(DOC_TYPE_FIELD_NAME_TWO, docTypeFieldService, sessionFactory);
	}

	private void bindAutocompleteToBucket(Bucket bucket, Autocomplete autocomplete) {
		bucketService.bindAutocomplete(bucket.getId(), autocomplete.getId())
			.await()
			.indefinitely();
	}

	private void bindAutocorrectionToBucket(Bucket bucket, Autocorrection autocorrection) {
		bucketService.bindAutocorrection(bucket.getId(), autocorrection.getId())
			.await()
			.indefinitely();
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

	private void unbindAutocorrectionToBucket(Bucket bucket) {
		bucketService.unbindAutocorrection(bucket.getId())
			.await()
			.indefinitely();
	}

	private void unbindAutocompleteToBucket(Bucket bucket) {
		bucketService.unbindAutocomplete(bucket.getId())
			.await()
			.indefinitely();
	}
}
