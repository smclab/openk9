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

package io.openk9.datasource.listener;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionException;

import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.IndexTemplateUtils;
import io.openk9.datasource.Initializer;
import io.openk9.datasource.index.model.EmbeddingComponentTemplate;
import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.dto.base.AnalyzerDTO;
import io.openk9.datasource.model.dto.base.DataIndexDTO;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;
import io.openk9.datasource.model.dto.request.DocTypeFieldWithAnalyzerDTO;
import io.openk9.datasource.pipeline.service.dto.SchedulingType;
import io.openk9.datasource.pipeline.service.mapper.SchedulerMapper;
import io.openk9.datasource.service.AnalyzerService;
import io.openk9.datasource.service.DataIndexService;
import io.openk9.datasource.service.DatasourceConnectionObjects;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.DocTypeService;
import io.openk9.datasource.service.EmbeddingModelService;
import io.openk9.datasource.service.PluginDriverService;
import io.openk9.datasource.service.SchedulerService;
import io.openk9.ml.grpc.EmbeddingOuterClass;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensearch.client.Request;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.cluster.metadata.ComposableIndexTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the reindex creation path end to end: the scheduler builds the new
 * {@link DataIndex} inheriting from the one it replaces, and the dataIndex
 * service creates it, regenerating the index template from the tenant active
 * embedding model and refusing what it cannot produce coherently.
 */
@QuarkusTest
class ReindexDataIndexTest {

	private static final String ANALYZED_DOC_TYPE = "rdit_analyzed";
	private static final String ANALYZER = "rdit_analyzer";
	private static final String ANALYZER_TYPE_SETTING =
		"index.analysis.analyzer." + ANALYZER + ".type";
	private static final int CHUNK_WINDOW_SIZE = 3;
	private static final String DATA_INDEX = "rdit.data-index";
	private static final String DATASOURCE = "rdit.datasource";
	private static final String EMBEDDING_JSON_CONFIG = "{\"batch\": 8}";
	private static final String REPLICAS_SETTING = "index.number_of_replicas";
	private static final String SECONDARY_EMBEDDING_MODEL =
		"Test embedding model disabled";
	private static final String SETTINGS = "{\"index\": {\"number_of_replicas\": 2}}";
	private static final String TENANT_ID = "public";
	private static final String TEST_EMBEDDING_MODEL = "rdit.embedding-model";
	private static final int TEST_VECTOR_SIZE = 1024;

	@Inject
	AnalyzerService analyzerService;

	@Inject
	DataIndexService dataIndexService;

	@Inject
	DatasourceService datasourceService;

	@Inject
	DocTypeService docTypeService;

	@Inject
	EmbeddingModelService embeddingModelService;

	@Inject
	PluginDriverService pluginDriverService;

	@Inject
	RestHighLevelClient restHighLevelClient;

	@Inject
	SchedulerMapper schedulerMapper;

	@Inject
	SchedulerService schedulerService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@AfterEach
	void tearDown() {
		enableEmbeddingModel(Initializer.EMBEDDING_MODEL_DEFAULT_PRIMARY);

		deleteQuietly(() -> {
			var datasource = EntitiesUtils.getEntity(
				DATASOURCE, datasourceService, sessionFactory);

			datasourceService.deleteById(datasource.getId()).await().indefinitely();
		});

		deleteQuietly(() -> embeddingModelService
			.deleteById(getEmbeddingModel(TEST_EMBEDDING_MODEL).getId())
			.await()
			.indefinitely()
		);

		deleteQuietly(() -> docTypeService
			.deleteById(EntitiesUtils
				.getEntity(ANALYZED_DOC_TYPE, docTypeService, sessionFactory)
				.getId())
			.await()
			.indefinitely()
		);

		deleteQuietly(() -> analyzerService
			.deleteById(EntitiesUtils
				.getEntity(ANALYZER, analyzerService, sessionFactory)
				.getId())
			.await()
			.indefinitely()
		);
	}

	@Test
	@DisplayName("Should inherit the indexing properties of the replaced dataIndex")
	void should_inherit_the_indexing_properties() {
		// a knn dataIndex configured away from the defaults is reindexed
		var datasourceId = createDatasource(knnDataIndex());

		var scheduler = reindex(datasourceId);

		// the new dataIndex carries the same configuration
		var newDataIndex = reload(scheduler.getNewDataIndex().getName());

		assertTrue(newDataIndex.getKnnIndex());
		assertEquals(
			EmbeddingOuterClass.ChunkType.CHUNK_TYPE_TEXT_SPLITTER,
			newDataIndex.getChunkType()
		);
		assertEquals(CHUNK_WINDOW_SIZE, newDataIndex.getChunkWindowSize());
		assertEquals(EMBEDDING_JSON_CONFIG, newDataIndex.getEmbeddingJsonConfig());
		assertEquals(SETTINGS, newDataIndex.getSettings());
		assertNotNull(newDataIndex.getEmbeddingDocTypeField());
		assertFalse(newDataIndex.getDocTypes().isEmpty());

		// and the inherited settings reach its index template
		assertEquals(
			"2",
			indexTemplate(newDataIndex).template().settings().get(REPLICAS_SETTING)
		);
	}

	@Test
	@DisplayName("Should keep scheduling the embedding after a reindex")
	void should_keep_scheduling_the_embedding() {
		// a knn dataIndex is reindexed
		var datasourceId = createDatasource(knnDataIndex());

		var scheduler = reindex(datasourceId);

		// the datasource is re-pointed at the new dataIndex, as the end of a
		// scheduling does
		datasourceService
			.setDataIndex(datasourceId, scheduler.getNewDataIndex().getId())
			.await()
			.indefinitely();

		// the next ordinary scheduling is still typed with the embedding
		assertEquals(
			SchedulingType.EMBEDDING,
			schedulerMapper.map(fetchDatasourceConnection(datasourceId))
		);
	}

	@Test
	@DisplayName("Should regenerate the index template from the current model")
	void should_regenerate_the_index_template_from_the_current_model() {
		// a knn dataIndex is created while a model is active
		var datasourceId = createDatasource(knnDataIndex());

		var previousModel =
			getEmbeddingModel(Initializer.EMBEDDING_MODEL_DEFAULT_PRIMARY);

		// the tenant switches to another model, of a different size
		enableEmbeddingModel(SECONDARY_EMBEDDING_MODEL);

		var currentModel = getEmbeddingModel(SECONDARY_EMBEDDING_MODEL);

		// the reindex regenerates the index template from the current model
		var scheduler = reindex(datasourceId);

		var composedOf =
			indexTemplate(reload(scheduler.getNewDataIndex().getName())).composedOf();

		assertTrue(
			composedOf.contains(componentTemplateName(currentModel)),
			"the new index template does not compose the current model"
		);
		assertFalse(
			composedOf.contains(componentTemplateName(previousModel)),
			"the new index template still composes the previous model"
		);
	}

	@Test
	@DisplayName("Should follow a property change on the same active model")
	void should_follow_a_property_change_on_the_same_active_model() {
		// a knn dataIndex created against an active model
		var embeddingModel = createAndEnableEmbeddingModel();
		var datasourceId = createDatasource(knnDataIndex());

		// the vector type of the same model changes, which rewrites its
		// component template
		embeddingModelService.update(
				embeddingModel.getId(),
				EmbeddingModelDTO.builder()
					.name(TEST_EMBEDDING_MODEL)
					.vectorSize(TEST_VECTOR_SIZE)
					.vectorDataType(EmbeddingModel.VectorDataType.BYTE)
					.build()
			)
			.await()
			.indefinitely();

		// the reindex keeps composing the same component template, referred to
		// by name, so the new index follows the change on its own
		var scheduler = reindex(datasourceId);

		var componentTemplate = componentTemplateName(embeddingModel);

		assertTrue(
			indexTemplate(reload(scheduler.getNewDataIndex().getName()))
				.composedOf()
				.contains(componentTemplate),
			"the new index template does not compose the active model"
		);
		var componentTemplateJson = componentTemplate(componentTemplate);

		assertTrue(
			componentTemplateJson.contains("\"data_type\":\"byte\""),
			componentTemplateJson
		);
	}

	@Test
	@DisplayName("Should give a vector field to an index template that had none")
	void should_link_the_component_template_a_stale_template_never_had() {
		// a knn dataIndex whose index template composes no embedding component
		// template, the state left behind by a creation with no model active
		var datasourceId = createDatasource(knnDataIndex());

		clearComposedOf(DATA_INDEX);

		// the reindex links the component template of the active model
		var scheduler = reindex(datasourceId);

		var embeddingModel =
			getEmbeddingModel(Initializer.EMBEDDING_MODEL_DEFAULT_PRIMARY);

		assertTrue(
			indexTemplate(reload(scheduler.getNewDataIndex().getName()))
				.composedOf()
				.contains(componentTemplateName(embeddingModel)),
			"the new index template does not compose the active model"
		);
	}

	@Test
	@DisplayName("Should keep the analyzers of the docTypes across a reindex")
	void should_keep_the_analyzers_of_the_doc_types_across_a_reindex() {
		// a docType whose field carries an analyzer, and a knn dataIndex on it
		// whose settings know nothing of the analysis
		var analyzer = createAnalyzer("standard");

		createAnalyzedDocType(analyzer.getId());

		var datasourceId = createDatasource(knnDataIndex());

		// the analyzer reaches the index template next to the settings requested
		var created = indexTemplate(reload(DATA_INDEX)).template().settings();

		assertEquals("standard", created.get(ANALYZER_TYPE_SETTING));
		assertEquals("2", created.get(REPLICAS_SETTING));

		// the analyzer changes, and the reindex follows the docTypes as they
		// are now, not the settings recorded at creation
		analyzerService.update(
				analyzer.getId(),
				AnalyzerDTO.builder()
					.name(ANALYZER)
					.type("whitespace")
					.jsonConfig("{\"type\": \"whitespace\"}")
					.build()
			)
			.await()
			.indefinitely();

		var scheduler = reindex(datasourceId);

		var reindexed = indexTemplate(reload(scheduler.getNewDataIndex().getName()))
			.template()
			.settings();

		assertEquals("whitespace", reindexed.get(ANALYZER_TYPE_SETTING));
		assertEquals("2", reindexed.get(REPLICAS_SETTING));
	}

	@Test
	@DisplayName("Should create the index template of a dataIndex without docTypes")
	void should_create_the_index_template_without_doc_types() {
		// the replaced dataIndex has no docTypes
		var datasourceId = createDatasource(DataIndexDTO.builder().knnIndex(false));

		var scheduler = reindex(datasourceId);

		// the new dataIndex has an index template all the same
		var newDataIndex = reload(scheduler.getNewDataIndex().getName());

		assertTrue(newDataIndex.getDocTypes().isEmpty());
		assertNotNull(indexTemplate(newDataIndex));
	}

	@Test
	@DisplayName("Should reindex a plain dataIndex with no embedding model active")
	void should_reindex_a_plain_data_index_without_an_active_model() {
		// a plain dataIndex on a tenant with no active embedding model
		var datasourceId = createDatasource(
			DataIndexDTO.builder()
				.knnIndex(false)
				.docTypeIds(allDocTypeIds())
		);

		disableEmbeddingModel();

		// the reindex succeeds and stays plain
		var scheduler = reindex(datasourceId);

		var newDataIndex = reload(scheduler.getNewDataIndex().getName());

		assertFalse(newDataIndex.getKnnIndex());
		assertTrue(indexTemplate(newDataIndex).composedOf().isEmpty());
	}

	@Test
	@DisplayName("Should refuse the reindex when no embedding model is active")
	void should_refuse_the_reindex_without_an_active_model() {
		// a knn dataIndex on a tenant whose embedding model has been disabled
		var datasourceId = createDatasource(knnDataIndex());

		disableEmbeddingModel();

		var scheduler = newReindexScheduler(datasourceId);

		// the reindex fails instead of degrading to an index without vectors
		var exception = assertThrows(
			CompletionException.class,
			() -> JobSchedulerService.persistScheduler(TENANT_ID, scheduler).join()
		);

		assertTrue(
			exception.getCause().getMessage().contains("no active embedding model"),
			exception.getCause().getMessage()
		);

		// and neither a dataIndex nor an orphan index template is left behind
		assertEquals(1, countDataIndexes(datasourceId));
		assertNull(IndexTemplateUtils.getIndexTemplate(
			restHighLevelClient,
			TENANT_ID,
			scheduler.getNewDataIndex().getName()
		));
	}

	@Test
	@DisplayName("Should refuse the reindex of a knn index with no embedding field")
	void should_refuse_the_reindex_without_the_embedding_field() {
		// a knn dataIndex whose embedding field went missing in the database
		var datasourceId = createDatasource(knnDataIndex());

		clearEmbeddingDocTypeField(DATA_INDEX);

		var scheduler = newReindexScheduler(datasourceId);

		// the reindex fails instead of scheduling an embedding that produces
		// no vector at all
		var exception = assertThrows(
			CompletionException.class,
			() -> JobSchedulerService.persistScheduler(TENANT_ID, scheduler).join()
		);

		assertTrue(
			exception.getCause().getMessage().contains("embeddingDocTypeField"),
			exception.getCause().getMessage()
		);

		// and neither a dataIndex nor an orphan index template is left behind
		assertEquals(1, countDataIndexes(datasourceId));
		assertNull(IndexTemplateUtils.getIndexTemplate(
			restHighLevelClient,
			TENANT_ID,
			scheduler.getNewDataIndex().getName()
		));
	}

	@Test
	@DisplayName("Should record a refused reindex as a scheduling in FAILURE")
	void should_record_a_refused_reindex_as_a_failure() {
		// a knn dataIndex on a tenant whose embedding model has been disabled
		var datasourceId = createDatasource(knnDataIndex());

		disableEmbeddingModel();

		var scheduler = newReindexScheduler(datasourceId);

		// the reindex is refused
		var exception = assertThrows(
			CompletionException.class,
			() -> JobSchedulerService.persistScheduler(TENANT_ID, scheduler).join()
		);

		// so the scheduling is recorded in FAILURE, with the reason
		var failed = JobSchedulerService.persistScheduler(
				TENANT_ID,
				JobScheduler.failedScheduler(scheduler, exception.getCause())
			)
			.join();

		var recorded =
			schedulerService.findById(failed.getId()).await().indefinitely();

		assertEquals(Scheduler.SchedulerStatus.FAILURE, recorded.getStatus());
		assertTrue(
			recorded.getErrorDescription().contains("no active embedding model"),
			recorded.getErrorDescription()
		);
	}

	@Test
	@DisplayName("Should keep ingesting on a knn index with the model disabled")
	void should_keep_ingesting_on_a_knn_index_with_the_model_disabled() {
		// an ordinary ingestion creates no dataIndex, so it is unaffected by
		// the absence of an active model
		var datasourceId = createDatasource(knnDataIndex());

		disableEmbeddingModel();

		var datasource = fetchDatasourceConnection(datasourceId);

		var scheduler = new Scheduler();
		scheduler.setScheduleId(UUID.randomUUID().toString());
		scheduler.setDatasource(datasource);
		scheduler.setOldDataIndex(datasource.getDataIndex());
		scheduler.setStatus(Scheduler.SchedulerStatus.RUNNING);
		scheduler.setReindex(false);

		var persisted =
			JobSchedulerService.persistScheduler(TENANT_ID, scheduler).join();

		assertNotNull(persisted.getId());
		assertNull(persisted.getNewDataIndex());
	}

	private Set<Long> allDocTypeIds() {
		return sessionFactory.withTransaction((s, t) -> s
				.createQuery("select dt.id from DocType dt", Long.class)
				.getResultList()
				.map(Set::copyOf)
			)
			.await()
			.indefinitely();
	}

	private long anyDocTypeFieldId() {
		return sessionFactory.withTransaction((s, t) -> s
				.createQuery("select f.id from DocTypeField f order by f.id", Long.class)
				.getResultList()
				.map(List::getFirst)
			)
			.await()
			.indefinitely();
	}

	private void clearComposedOf(String dataIndexName) {
		var existing = IndexTemplateUtils.getIndexTemplate(
			restHighLevelClient, TENANT_ID, dataIndexName);

		IndexTemplateUtils.putIndexTemplate(
			restHighLevelClient, TENANT_ID, dataIndexName,
			new ComposableIndexTemplate(
				existing.indexPatterns(),
				existing.template(),
				List.of(),
				existing.priority(),
				existing.version(),
				existing.metadata()
			)
		);
	}

	private void clearEmbeddingDocTypeField(String dataIndexName) {
		sessionFactory.withTransaction((s, t) -> s
				.createNativeQuery(
					"UPDATE data_index SET embedding_doc_type_field_id = NULL"
					+ " WHERE name = :name")
				.setParameter("name", dataIndexName)
				.executeUpdate()
			)
			.await()
			.indefinitely();
	}

	private String componentTemplate(String componentTemplateName) {
		try {
			var response = restHighLevelClient
				.getLowLevelClient()
				.performRequest(new Request(
					"GET", "/_component_template/" + componentTemplateName));

			return new String(response.getEntity().getContent().readAllBytes());
		}
		catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private String componentTemplateName(EmbeddingModel embeddingModel) {
		return new EmbeddingComponentTemplate(
			TENANT_ID,
			embeddingModel.getName(),
			embeddingModel.getVectorSize(),
			embeddingModel.getVectorDataType()
		).getName();
	}

	private long countDataIndexes(long datasourceId) {
		return sessionFactory.withTransaction((s, t) -> s
				.createQuery(
					"select count(di) from DataIndex di"
					+ " where di.datasource.id = :datasourceId",
					Long.class
				)
				.setParameter("datasourceId", datasourceId)
				.getSingleResult()
			)
			.await()
			.indefinitely();
	}

	private Analyzer createAnalyzer(String type) {
		return analyzerService.create(AnalyzerDTO.builder()
				.name(ANALYZER)
				.type(type)
				.jsonConfig(String.format("{\"type\": \"%s\"}", type))
				.build()
			)
			.await()
			.indefinitely();
	}

	private void createAnalyzedDocType(long analyzerId) {
		var docType = docTypeService.create(DocTypeDTO.builder()
				.name(ANALYZED_DOC_TYPE)
				.build()
			)
			.await()
			.indefinitely();

		docTypeService.addDocTypeField(
				docType.getId(),
				DocTypeFieldWithAnalyzerDTO.builder()
					.name("content")
					.fieldName("content")
					.fieldType(FieldType.TEXT)
					.analyzerId(analyzerId)
					.build()
			)
			.await()
			.indefinitely();
	}

	private EmbeddingModel createAndEnableEmbeddingModel() {
		var embeddingModel = embeddingModelService.create(EmbeddingModelDTO.builder()
				.name(TEST_EMBEDDING_MODEL)
				.apiUrl("https://api.acmeai.com/v1/embeddings")
				.apiKey("secret-key")
				.vectorSize(TEST_VECTOR_SIZE)
				.build()
			)
			.await()
			.indefinitely();

		disableEmbeddingModel();

		embeddingModelService.enable(embeddingModel.getId()).await().indefinitely();

		return embeddingModel;
	}

	private long createDatasource(DataIndexDTO.DataIndexDTOBuilder<?, ?> dataIndex) {
		var pluginDriver = pluginDriverService
			.findByName(TENANT_ID, Initializer.INIT_DATASOURCE_PLUGIN)
			.await()
			.indefinitely();

		var response = datasourceService.createDatasourceConnection(
				DatasourceConnectionObjects.DATASOURCE_CONNECTION_DTO_BUILDER()
					.name(DATASOURCE)
					.pluginDriverId(pluginDriver.getId())
					.dataIndex(dataIndex.name(DATA_INDEX).build())
					.build()
			)
			.await()
			.indefinitely();

		assertNotNull(
			response.getEntity(), String.valueOf(response.getFieldValidators()));

		return response.getEntity().getId();
	}

	private void deleteQuietly(Runnable deletion) {
		try {
			deletion.run();
		}
		catch (Exception ignored) {
		}
	}

	private void disableEmbeddingModel() {
		sessionFactory.withTransaction((s, t) -> s
				.createNativeQuery("UPDATE tenant_binding SET embedding_model_id = NULL")
				.executeUpdate()
			)
			.await()
			.indefinitely();
	}

	private void enableEmbeddingModel(String name) {
		disableEmbeddingModel();

		embeddingModelService.enable(getEmbeddingModel(name).getId())
			.await()
			.indefinitely();
	}

	private Datasource fetchDatasourceConnection(long datasourceId) {
		return JobSchedulerService
			.fetchDatasourceConnection(TENANT_ID, datasourceId)
			.join();
	}

	private EmbeddingModel getEmbeddingModel(String name) {
		return EntitiesUtils.getEntity(name, embeddingModelService, sessionFactory);
	}

	private ComposableIndexTemplate indexTemplate(DataIndex dataIndex) {
		var indexTemplate = IndexTemplateUtils.getIndexTemplate(
			restHighLevelClient, TENANT_ID, dataIndex.getName());

		assertNotNull(
			indexTemplate,
			String.format("no index template for %s", dataIndex.getName())
		);

		return indexTemplate;
	}

	private DataIndexDTO.DataIndexDTOBuilder<?, ?> knnDataIndex() {
		return DataIndexDTO.builder()
			.knnIndex(true)
			.embeddingDocTypeFieldId(anyDocTypeFieldId())
			.chunkType(EmbeddingOuterClass.ChunkType.CHUNK_TYPE_TEXT_SPLITTER)
			.chunkWindowSize(CHUNK_WINDOW_SIZE)
			.embeddingJsonConfig(EMBEDDING_JSON_CONFIG)
			.settings(SETTINGS)
			.docTypeIds(allDocTypeIds());
	}

	/**
	 * Reproduces what the job scheduler does when a reindex starts: it builds
	 * the new dataIndex inheriting from the current one.
	 */
	private Scheduler newReindexScheduler(long datasourceId) {
		var datasource = fetchDatasourceConnection(datasourceId);

		var scheduler = new Scheduler();
		scheduler.setScheduleId(UUID.randomUUID().toString());
		scheduler.setDatasource(datasource);
		scheduler.setOldDataIndex(datasource.getDataIndex());
		scheduler.setStatus(Scheduler.SchedulerStatus.RUNNING);
		scheduler.setReindex(true);
		scheduler.setNewDataIndex(JobScheduler.newDataIndex(
			datasourceId + "-data-" + scheduler.getScheduleId(),
			datasource,
			datasource.getDataIndex()
		));

		return scheduler;
	}

	private Scheduler reindex(long datasourceId) {
		return JobSchedulerService
			.persistScheduler(TENANT_ID, newReindexScheduler(datasourceId))
			.join();
	}

	private DataIndex reload(String name) {
		return sessionFactory.withTransaction((s, t) ->
				dataIndexService.findByName(s, name)
					.call(dataIndex -> Mutiny.fetch(dataIndex.getDocTypes()))
					.call(dataIndex -> Mutiny.fetch(dataIndex.getEmbeddingDocTypeField()))
			)
			.await()
			.indefinitely();
	}

}
