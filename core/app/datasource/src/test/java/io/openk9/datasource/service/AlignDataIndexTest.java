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

import java.io.IOException;
import java.util.Set;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;

import io.openk9.datasource.Initializer;
import io.openk9.datasource.index.IndexService;
import io.openk9.datasource.index.model.IndexName;
import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.TokenFilter;
import io.openk9.datasource.model.Tokenizer;
import io.openk9.datasource.model.dto.base.DataIndexDTO;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.base.TokenFilterDTO;
import io.openk9.datasource.model.dto.base.TokenizerDTO;
import io.openk9.datasource.model.dto.request.AnalyzerWithListsDTO;
import io.openk9.datasource.model.dto.request.DocTypeFieldWithAnalyzerDTO;
import io.openk9.datasource.service.DataIndexService.IndexAlignment;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch.generic.Requests;

/**
 * Proves that a searchAnalyzer bound to a docTypeField after the index was born
 * becomes effective without reindexing, and covers what the alignment does when
 * it cannot get there: an index that does not exist yet, one that is being
 * written to, one that may not be closed, and one OpenSearch refuses.
 */
@QuarkusTest
public class AlignDataIndexTest {

	// the prefix is snake_case, and not the class name, because these names end
	// up as OpenSearch identifiers: index name, analyzer name, settings keys
	private static final String ENTITY_NAME_PREFIX = "aditest_";

	private static final String ANALYZER_NAME = ENTITY_NAME_PREFIX + "synonym_search";
	private static final String DATA_INDEX_NAME = ENTITY_NAME_PREFIX + "data_index";
	private static final String DOC_TYPE_NAME = ENTITY_NAME_PREFIX + "web";
	private static final String FIELD_PATH = DOC_TYPE_NAME + ".title";
	private static final String SCHEMA_NAME = "public";

	@Inject
	AnalyzerService analyzerService;
	@Inject
	DataIndexService dataIndexService;
	@Inject
	DatasourceService datasourceService;
	@Inject
	DocTypeFieldService docTypeFieldService;
	@Inject
	DocTypeService docTypeService;
	@Inject
	Mutiny.SessionFactory sessionFactory;
	@Inject
	OpenSearchClient openSearchClient;
	@Inject
	SchedulerService schedulerService;
	@Inject
	TokenFilterService tokenFilterService;
	@Inject
	TokenizerService tokenizerService;

	private Analyzer analyzer;
	private DataIndex dataIndex;
	private long datasourceId;
	private DocType docType;
	private DocTypeField docTypeField;
	private Long previousDataIndexId;
	private TokenFilter tokenFilter;
	private Tokenizer tokenizer;

	@BeforeEach
	void setUp() {
		// an analyzer that expands "notebook" into "laptop" at query time only
		tokenizer = tokenizerService.create(TokenizerDTO.builder()
			.name(ENTITY_NAME_PREFIX + "standard")
			.type("standard")
			.jsonConfig("{\"type\":\"standard\"}")
			.build()
		).await().indefinitely();

		tokenFilter = tokenFilterService.create(TokenFilterDTO.builder()
			.name(ENTITY_NAME_PREFIX + "synonym")
			.type("synonym")
			.jsonConfig(
				"{\"type\":\"synonym\",\"synonyms\":[\"notebook, laptop\"]}")
			.build()
		).await().indefinitely();

		analyzer = analyzerService.create(AnalyzerWithListsDTO.builder()
			.name(ANALYZER_NAME)
			.type("custom")
			.tokenizerId(tokenizer.getId())
			.tokenFilterIds(Set.of(tokenFilter.getId()))
			.build()
		).await().indefinitely();

		// a docType whose field carries no searchAnalyzer yet
		docType = docTypeService.create(DocTypeDTO.builder()
			.name(DOC_TYPE_NAME)
			.build()
		).await().indefinitely();

		docTypeField = docTypeService.addDocTypeField(
			docType.getId(),
			DocTypeFieldWithAnalyzerDTO.builder()
				.name("title")
				.fieldName("title")
				.fieldType(FieldType.TEXT)
				.build()
		).await().indefinitely().right;

		var datasource = getDefaultDatasource();

		datasourceId = datasource.getId();
		previousDataIndexId = getCurrentDataIndexId();

		dataIndex = createDataIndex(DATA_INDEX_NAME);

		// only the dataIndex a datasource points at is aligned
		bind(dataIndex.getId());
	}

	@AfterEach
	void tearDown() {
		// the datasource has to let go of the dataIndex before it can be deleted
		if (previousDataIndexId != null) {
			bind(previousDataIndexId);
		}
		else {
			datasourceService.unsetDataIndex(datasourceId).await().indefinitely();
		}

		// deleting the dataIndex also drops the index and its template
		dataIndexService.deleteById(dataIndex.getId()).await().indefinitely();

		docTypeFieldService.deleteById(docTypeField.getId())
			.await().indefinitely();
		docTypeService.deleteById(docType.getId()).await().indefinitely();
		analyzerService.deleteById(analyzer.getId()).await().indefinitely();
		tokenFilterService.deleteById(tokenFilter.getId())
			.await().indefinitely();
		tokenizerService.deleteById(tokenizer.getId()).await().indefinitely();
	}

	@Test
	void should_apply_the_search_analyzer_without_reindexing() throws IOException {
		var indexName = IndexName.from(SCHEMA_NAME, dataIndex);

		// 1. the first write materializes the index from the template, so the
		// document is analyzed with the mapping as it is now
		indexDocument(indexName, "laptop computer");

		Assertions.assertEquals(
			0L, countMatches(indexName, "notebook"),
			"without the search analyzer the query must not match");

		// 2. the searchAnalyzer is bound only now, when the index already holds
		// a document: this is the case the API has to cover
		docTypeFieldService
			.bindSearchAnalyzer(docTypeField.getId(), analyzer.getId())
			.await().indefinitely();

		// 3. one call does both halves in the order OpenSearch imposes: the
		// analyzer into the settings, closing the index, then the mapping
		var alignments = dataIndexService
			.alignDataIndexes(docType.getId(), true)
			.await().indefinitely();

		Assertions.assertEquals(1, alignments.size());
		Assertions.assertEquals(
			IndexAlignment.Status.APPLIED, alignments.getFirst().status());

		// 4. the proof: the document indexed at step 1 is matched by a query
		// that only the new search analyzer can satisfy, with no reindexing
		Assertions.assertEquals(
			1L, countMatches(indexName, "notebook"),
			"the search analyzer must apply to the documents already indexed");
	}

	@Test
	void should_be_idempotent() throws IOException {
		var indexName = IndexName.from(SCHEMA_NAME, dataIndex);

		indexDocument(indexName, "laptop computer");

		docTypeFieldService
			.bindSearchAnalyzer(docTypeField.getId(), analyzer.getId())
			.await().indefinitely();

		align();

		// the second time the index already carries the analyzers, so it is not
		// closed again and nothing changes
		Assertions.assertEquals(IndexAlignment.Status.APPLIED, align().status());

		Assertions.assertEquals(1L, countMatches(indexName, "notebook"));
	}

	@Test
	void should_ask_before_closing_an_index() throws IOException {
		var indexName = IndexName.from(SCHEMA_NAME, dataIndex);

		indexDocument(indexName, "laptop computer");

		docTypeFieldService
			.bindSearchAnalyzer(docTypeField.getId(), analyzer.getId())
			.await().indefinitely();

		// 1. the index does not carry the analyzer yet, so taking it means
		// closing the index, and nobody has allowed that
		var refused = align(false);

		Assertions.assertEquals(
			IndexAlignment.Status.CLOSE_REQUIRED, refused.status());
		Assertions.assertTrue(
			refused.reason().contains("closed"),
			"the caller must read why he is being asked: " + refused.reason());
		Assertions.assertEquals(
			0L, countMatches(indexName, "notebook"),
			"nothing must have reached the live index");

		// 2. the same call, allowed, closes the index and applies everything
		Assertions.assertEquals(IndexAlignment.Status.APPLIED, align(true).status());

		Assertions.assertEquals(1L, countMatches(indexName, "notebook"));
	}

	@Test
	void should_ask_before_closing_for_static_settings() {
		indexDocument(IndexName.from(SCHEMA_NAME, dataIndex), "laptop computer");

		var custom = JsonObject.of(
			"analysis", JsonObject.of(
				"filter", JsonObject.of(
					ENTITY_NAME_PREFIX + "custom_filter",
					JsonObject.of("type", "lowercase"))));

		// an analysis definition is static, and OpenSearch refuses it on an
		// open index: the refusal applies nothing, so asking now costs nothing
		var refused = updateSettings(custom, false);

		Assertions.assertEquals(
			IndexAlignment.Status.CLOSE_REQUIRED, refused.status());
		Assertions.assertFalse(
			getCustomSettings().contains("custom_filter"),
			"nothing must be recorded when nothing reached the index");

		Assertions.assertEquals(
			IndexAlignment.Status.APPLIED, updateSettings(custom, true).status());
		Assertions.assertTrue(getCustomSettings().contains("custom_filter"));
	}

	@Test
	void should_only_update_the_template_without_an_index() throws IOException {
		var indexName = IndexName.from(SCHEMA_NAME, dataIndex);

		docTypeFieldService
			.bindSearchAnalyzer(docTypeField.getId(), analyzer.getId())
			.await().indefinitely();

		// a dataIndex nothing has been written to has no index, only a template
		Assertions.assertEquals(IndexAlignment.Status.TEMPLATE_ONLY, align().status());

		// the first write materializes the index from that template, with the
		// search analyzer already in place
		indexDocument(indexName, "laptop computer");

		Assertions.assertEquals(
			1L, countMatches(indexName, "notebook"),
			"the index must be born with the search analyzer in place");
	}

	@Test
	void should_report_a_refusal_as_it_is() throws IOException {
		var indexName = IndexName.from(SCHEMA_NAME, dataIndex);

		indexDocument(indexName, "laptop computer");

		// changing the type of a field that is already indexed can never be
		// applied to a live index, and OpenSearch rejects the whole document
		docTypeFieldService.patch(
			docTypeField.getId(),
			DocTypeFieldDTO.builder()
				.name("title")
				.fieldName("title")
				.fieldType(FieldType.KEYWORD)
				.build(),
			"title"
		).await().indefinitely();

		var alignment = align();

		Assertions.assertEquals(IndexAlignment.Status.FAILED, alignment.status());
		Assertions.assertTrue(
			alignment.reason().contains("cannot be changed from type"),
			"the refusal of OpenSearch must be reported as is: "
				+ alignment.reason());
	}

	@Test
	void should_skip_an_index_that_is_being_written_to() throws IOException {
		indexDocument(IndexName.from(SCHEMA_NAME, dataIndex), "laptop computer");

		docTypeFieldService
			.bindSearchAnalyzer(docTypeField.getId(), analyzer.getId())
			.await().indefinitely();

		var scheduler = createRunningScheduler();

		try {
			var alignment = align();

			Assertions.assertEquals(
				IndexAlignment.Status.SKIPPED, alignment.status(),
				"closing the index would have broken the ingestion");
			Assertions.assertTrue(
				alignment.reason().contains("TRIGGER_RUNNING"),
				"the caller must read why the index was skipped: "
					+ alignment.reason());
		}
		finally {
			schedulerService.deleteById(scheduler.getId()).await().indefinitely();
		}
	}

	@Test
	void should_leave_alone_the_indices_a_reindex_left_behind() {
		var orphan = createDataIndex(ENTITY_NAME_PREFIX + "orphan");

		try {
			var alignments = dataIndexService
				.alignDataIndexes(docType.getId(), true)
				.await().indefinitely();

			Assertions.assertEquals(
				1, alignments.size(),
				"an orphan is neither searched nor written, so it is not aligned");
			Assertions.assertEquals(
				dataIndex.getId(), alignments.getFirst().dataIndexId());
		}
		finally {
			dataIndexService.deleteById(orphan.getId()).await().indefinitely();
		}
	}

	@Test
	void should_align_a_data_index_that_becomes_the_current_one()
		throws IOException {

		// born before the searchAnalyzer exists, and left out of every
		// alignment while it is not the index the datasource points at
		var orphan = createDataIndex(ENTITY_NAME_PREFIX + "rollback");

		docTypeFieldService
			.bindSearchAnalyzer(docTypeField.getId(), analyzer.getId())
			.await().indefinitely();

		align();

		try {
			Assertions.assertFalse(
				getTemplate(IndexName.from(SCHEMA_NAME, orphan)).contains(ANALYZER_NAME),
				"the orphan must still be out of date");

			// pointing the datasource at it is the moment its being out of date
			// starts to matter
			bind(orphan.getId());

			Assertions.assertTrue(
				getTemplate(IndexName.from(SCHEMA_NAME, orphan)).contains(ANALYZER_NAME),
				"becoming the current index must align it");
		}
		finally {
			bind(dataIndex.getId());

			dataIndexService.deleteById(orphan.getId()).await().indefinitely();
		}
	}

	@Test
	void should_apply_and_record_custom_settings() throws IOException {
		var indexName = IndexName.from(SCHEMA_NAME, dataIndex);

		indexDocument(indexName, "laptop computer");

		var alignment = updateSettings(JsonObject.of(
			"index", JsonObject.of("max_result_window", 12345)));

		Assertions.assertEquals(IndexAlignment.Status.APPLIED, alignment.status());

		Assertions.assertTrue(
			getLiveSettings(indexName).contains("12345"),
			"the settings must reach the live index");
		Assertions.assertTrue(
			getCustomSettings().contains("12345"),
			"and be recorded, because a reindex inherits them");
		Assertions.assertTrue(
			getTemplate(indexName).contains("12345"),
			"and be declared by the template");
	}

	@Test
	void should_keep_what_the_settings_document_does_not_name() {
		indexDocument(IndexName.from(SCHEMA_NAME, dataIndex), "laptop computer");

		updateSettings(JsonObject.of(
			"index", JsonObject.of("max_result_window", 12345)));

		// a second document that does not name the first setting
		updateSettings(JsonObject.of(
			"index", JsonObject.of("refresh_interval", "30s")));

		var recorded = getCustomSettings();

		Assertions.assertTrue(
			recorded.contains("12345"),
			"a key the document does not name is left alone: " + recorded);
		Assertions.assertTrue(recorded.contains("30s"), recorded);

		// only null takes a setting back to its default
		updateSettings(new JsonObject().put(
			"index", new JsonObject().putNull("refresh_interval")));

		recorded = getCustomSettings();

		Assertions.assertTrue(recorded.contains("12345"), recorded);
		Assertions.assertFalse(
			recorded.contains("30s"),
			"null must remove the setting from what is recorded: " + recorded);
		Assertions.assertFalse(
			recorded.contains("null"),
			"a recorded null would end up in the generated index template: "
				+ recorded);
	}

	@Test
	void should_refuse_settings_that_name_a_derived_definition() {
		docTypeFieldService
			.bindSearchAnalyzer(docTypeField.getId(), analyzer.getId())
			.await().indefinitely();

		var refused = Assertions.assertThrows(
			ValidationException.class,
			() -> updateSettings(JsonObject.of(
				"analysis", JsonObject.of(
					"analyzer", JsonObject.of(
						ANALYZER_NAME, JsonObject.of("type", "whitespace")))))
		);

		Assertions.assertTrue(
			refused.getMessage().contains(ANALYZER_NAME),
			"the caller must read which definition is not his: "
				+ refused.getMessage());

		// a definition the docTypes do not know is nobody else's
		Assertions.assertEquals(
			IndexAlignment.Status.TEMPLATE_ONLY,
			updateSettings(JsonObject.of(
				"analysis", JsonObject.of(
					"filter", JsonObject.of(
						ENTITY_NAME_PREFIX + "custom_filter",
						JsonObject.of("type", "lowercase")))
			)).status()
		);
	}

	private IndexAlignment align() {
		return align(true);
	}

	private IndexAlignment align(boolean closeIfNeeded) {
		return dataIndexService.alignDataIndex(dataIndex.getId(), closeIfNeeded)
			.await().indefinitely();
	}

	private void bind(long dataIndexId) {
		datasourceService.setDataIndex(datasourceId, dataIndexId)
			.await().indefinitely();
	}

	private long countMatches(IndexName indexName, String text)
		throws IOException {

		return openSearchClient.search(
				request -> request
					.index(indexName.toString())
					.query(query -> query
						.match(match -> match
							.field(FIELD_PATH)
							.query(value -> value.stringValue(text))
						)
					),
				Void.class
			)
			.hits()
			.total()
			.value();
	}

	private DataIndex createDataIndex(String name) {
		return dataIndexService.create(
			datasourceId,
			DataIndexDTO.builder()
				.name(name)
				.docTypeIds(Set.of(docType.getId()))
				.build()
		).await().indefinitely().getEntity();
	}

	private Scheduler createRunningScheduler() {
		var scheduler = new Scheduler();

		scheduler.setScheduleId(ENTITY_NAME_PREFIX + "schedule");
		scheduler.setDatasource(getDefaultDatasource());
		scheduler.setStatus(Scheduler.SchedulerStatus.RUNNING);
		scheduler.setReindex(false);

		return sessionFactory.withTransaction(
				(session, transaction) -> schedulerService.create(scheduler))
			.await().indefinitely();
	}

	private Long getCurrentDataIndexId() {
		return sessionFactory.withTransaction(session -> datasourceService
				.findById(session, datasourceId)
				.flatMap(datasource -> session.fetch(datasource.getDataIndex()))
			)
			.await()
			.indefinitely()
			.getId();
	}

	private String getCustomSettings() {
		return dataIndexService.getCustomSettings(dataIndex.getId())
			.await().indefinitely();
	}

	private Datasource getDefaultDatasource() {
		return sessionFactory.withTransaction(session -> datasourceService
				.findByName(session, Initializer.INIT_DATASOURCE_CONNECTION)
			)
			.await()
			.indefinitely();
	}

	private String getLiveSettings(IndexName indexName) throws IOException {
		return send(String.format("/%s/_settings", indexName));
	}

	private String getTemplate(IndexName indexName) throws IOException {
		return send(String.format(
			"/_index_template/%s%s", indexName, IndexService.TEMPLATE_SUFFIX));
	}

	private void indexDocument(IndexName indexName, String title) {
		var document = JsonObject.of(
			DOC_TYPE_NAME, JsonObject.of("title", title));

		try {
			openSearchClient.index(request -> request
				.index(indexName.toString())
				.document(document.getMap())
				.refresh(Refresh.True)
			);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private String send(String endpoint) throws IOException {
		var request = Requests.builder()
			.endpoint(endpoint)
			.method("GET")
			.build();

		try (var response = openSearchClient.generic().execute(request)) {
			return response.getBody()
				.map(content -> content.bodyAsString())
				.orElse("");
		}
	}

	private IndexAlignment updateSettings(JsonObject settings) {
		return updateSettings(settings, true);
	}

	private IndexAlignment updateSettings(
		JsonObject settings, boolean closeIfNeeded) {

		return dataIndexService
			.updateIndexSettings(
				dataIndex.getId(), settings.encode(), closeIfNeeded)
			.await().indefinitely();
	}

}
