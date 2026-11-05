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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.validation.ValidationException;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.IndexTemplateUtils;
import io.openk9.datasource.Initializer;
import io.openk9.datasource.index.model.EmbeddingComponentTemplate;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.dto.base.DataIndexDTO;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import org.apache.http.util.EntityUtils;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensearch.client.Request;
import org.opensearch.client.RestHighLevelClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the single validation gate every {@link DataIndex} creation goes
 * through: a knn index is refused when the tenant has no active embedding
 * model, while a plain index is unaffected by the absence of a model.
 */
@QuarkusTest
class CreateDataIndexGateTest {

	private static final String KNN_DATA_INDEX = "cdigt.knn-data-index";
	private static final String PLAIN_DATA_INDEX = "cdigt.plain-data-index";
	private static final String TENANT_ID = "public";
	private static final String MANUAL_DOC_TYPE = "cdigt_manual";
	private static final String WEB_DOC_TYPE = "cdigt_web";

	@Inject
	DataIndexService dataIndexService;

	@Inject
	DatasourceService datasourceService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	DocTypeService docTypeService;

	@Inject
	EmbeddingModelService embeddingModelService;

	@Inject
	RestHighLevelClient restHighLevelClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@AfterEach
	void tearDown() {
		enableEmbeddingModel();
		deleteDataIndexQuietly(KNN_DATA_INDEX);
		deleteDataIndexQuietly(PLAIN_DATA_INDEX);
		deleteDocTypeQuietly(MANUAL_DOC_TYPE);
		deleteDocTypeQuietly(WEB_DOC_TYPE);
	}

	@Test
	@DisplayName("Should refuse a knn dataIndex when no embedding model is active")
	void should_refuse_knn_data_index_without_active_embedding_model() {
		// no embedding model is active on the tenant
		disableEmbeddingModel();

		// the creation is refused
		var exception = assertThrows(
			ValidationException.class,
			() -> createDataIndex(KNN_DATA_INDEX, true)
		);

		// the error says which of the two things is missing
		assertTrue(
			exception.getMessage().contains("no active embedding model"),
			exception.getMessage()
		);

		// and no orphan index template is left behind
		assertNull(IndexTemplateUtils.getIndexTemplate(
			restHighLevelClient, TENANT_ID, KNN_DATA_INDEX));
	}

	@Test
	@DisplayName("Should link the current embedding model on a knn dataIndex")
	void should_create_knn_data_index_with_active_embedding_model() {
		// the tenant has an active embedding model
		var embeddingModel = EntitiesUtils.getEntity(
			Initializer.EMBEDDING_MODEL_DEFAULT_PRIMARY,
			embeddingModelService,
			sessionFactory
		);

		// a knn dataIndex is created
		var dataIndex = createDataIndex(KNN_DATA_INDEX, true);

		// its index template composes the current model component template
		var indexTemplate = IndexTemplateUtils.getIndexTemplate(
			restHighLevelClient, TENANT_ID, dataIndex.getName());

		assertNotNull(indexTemplate);
		assertTrue(
			indexTemplate.composedOf().contains(componentTemplateName(embeddingModel)),
			"the index template does not compose the embedding component template"
		);
	}

	@Test
	@DisplayName("Should rewrite the embedding component template of an older model")
	void should_rewrite_the_component_template_on_a_knn_data_index()
		throws IOException {

		// the model was enabled when the chunk mappings had no binary
		var embeddingModel = EntitiesUtils.getEntity(
			Initializer.EMBEDDING_MODEL_DEFAULT_PRIMARY,
			embeddingModelService,
			sessionFactory
		);
		var componentTemplateName = componentTemplateName(embeddingModel);

		var putRequest =
			new Request("PUT", "/_component_template/" + componentTemplateName);
		putRequest.setJsonEntity(new JsonObject()
			.put("template", new JsonObject()
				.put("settings", new JsonObject().put("index.knn", true))
				.put("mappings", new JsonObject()
					.put("properties", new JsonObject()
						.put("vector", new JsonObject()
							.put("type", "knn_vector")
							.put("dimension", embeddingModel.getVectorSize())))))
			.encode());
		restHighLevelClient.getLowLevelClient().performRequest(putRequest);

		// a knn dataIndex is created
		createDataIndex(KNN_DATA_INDEX, true);

		// the component template has the binary fields again
		var response = restHighLevelClient.getLowLevelClient().performRequest(
			new Request("GET", "/_component_template/" + componentTemplateName));
		var mappings = new JsonObject(
				EntityUtils.toString(response.getEntity()))
			.getJsonArray("component_templates")
			.getJsonObject(0)
			.getJsonObject("component_template")
			.getJsonObject("template")
			.getJsonObject("mappings");

		assertTrue(
			mappings.getJsonObject("properties").containsKey("binary"),
			"the component template was not rewritten: " + mappings);
	}

	@Test
	@DisplayName("Should create a plain dataIndex when no embedding model is active")
	void should_create_plain_data_index_without_active_embedding_model() {
		// no embedding model is active on the tenant
		disableEmbeddingModel();

		// a non knn dataIndex is created without errors
		var dataIndex = createDataIndex(PLAIN_DATA_INDEX, false);

		// and its index template composes nothing
		var indexTemplate = IndexTemplateUtils.getIndexTemplate(
			restHighLevelClient, TENANT_ID, dataIndex.getName());

		assertNotNull(indexTemplate);
		assertTrue(
			indexTemplate.composedOf().isEmpty(),
			"the index template of a plain dataIndex composes something"
		);
	}

	@Test
	@DisplayName("Should refuse a header that holds the embeddingDocTypeField")
	void should_refuse_the_embedding_field_in_the_header() {
		// the field to embed is also asked as a header field
		var fixture = headerFixture();

		var dto = headerDataIndex(
			fixture, List.of(fixture.titleId(), fixture.contentId()));

		// the creation is refused
		var exception = assertThrows(
			ValidationException.class,
			() -> createDataIndex(dto)
		);

		// the error names the field
		assertTrue(
			exception.getMessage().contains(docTypeFieldName(fixture.contentId())),
			exception.getMessage()
		);
	}

	@Test
	@DisplayName("Should refuse a header field id that matches no docTypeField")
	void should_refuse_an_unknown_header_field_id() {
		// one existing field and one id that matches no field
		var fixture = headerFixture();
		var unknownId = Long.MAX_VALUE;

		var dto = headerDataIndex(fixture, List.of(fixture.titleId(), unknownId));

		// the creation is refused
		var exception = assertThrows(
			ValidationException.class,
			() -> createDataIndex(dto)
		);

		// the error names the unknown id
		assertTrue(
			exception.getMessage().contains(String.valueOf(unknownId)),
			exception.getMessage()
		);
	}

	@Test
	@DisplayName("Should refuse a null header field id")
	void should_refuse_a_null_header_field_id() {
		// a null among the header field ids
		var fixture = headerFixture();

		var dto = headerDataIndex(fixture, Arrays.asList(fixture.titleId(), null));

		// the creation is refused with a validation error, not a generic one
		var exception = assertThrows(
			ValidationException.class,
			() -> createDataIndex(dto)
		);

		assertTrue(
			exception.getMessage().contains("null"),
			exception.getMessage()
		);
	}

	@Test
	@DisplayName("Should refuse a sub-field of a field that is not an object")
	void should_refuse_a_sub_field_in_the_header() {
		// title.keyword is a multi-field: the payload has no such path
		var fixture = headerFixture();

		var dto = headerDataIndex(fixture, List.of(fixture.titleKeywordId()));

		var exception = assertThrows(
			ValidationException.class,
			() -> createDataIndex(dto)
		);

		// the error names the field by its path
		assertTrue(
			exception.getMessage().contains(MANUAL_DOC_TYPE + ".title.keyword"),
			exception.getMessage()
		);
	}

	@Test
	@DisplayName("Should refuse a field of a docType the dataIndex does not hold")
	void should_refuse_a_field_of_another_doc_type_in_the_header() {
		// the web docType is not linked to the dataIndex
		var fixture = headerFixture();

		var dto = headerDataIndex(
			fixture, List.of(fixture.titleId(), fixture.webTitleId()));

		var exception = assertThrows(
			ValidationException.class,
			() -> createDataIndex(dto)
		);

		// the error names the field of the other docType, not the valid one
		var message = exception.getMessage();

		assertTrue(message.contains(WEB_DOC_TYPE + ".title"), message);
		assertFalse(message.contains(MANUAL_DOC_TYPE + ".title"), message);
	}

	@Test
	@DisplayName("Should refuse a header on a dataIndex with no docType")
	void should_refuse_a_header_without_doc_types() {
		// a header field, but no docType on the dataIndex
		var fixture = headerFixture();

		var dto = DataIndexDTO.builder()
			.name(KNN_DATA_INDEX)
			.knnIndex(true)
			.embeddingDocTypeFieldId(fixture.contentId())
			.chunkHeaderFieldIds(List.of(fixture.titleId()))
			.build();

		var exception = assertThrows(
			ValidationException.class,
			() -> createDataIndex(dto)
		);

		assertTrue(
			exception.getMessage().contains(MANUAL_DOC_TYPE + ".title"),
			exception.getMessage()
		);
	}

	@Test
	@DisplayName("Should refuse object and I18N fields in the header")
	void should_refuse_object_and_i18n_fields_in_the_header() {
		// an object and an I18N field hold no single value
		var fixture = headerFixture();

		var dto = headerDataIndex(
			fixture, List.of(fixture.metaId(), fixture.labelId()));

		var exception = assertThrows(
			ValidationException.class,
			() -> createDataIndex(dto)
		);

		// both fields are named, with their type
		var message = exception.getMessage();

		assertTrue(message.contains(MANUAL_DOC_TYPE + ".meta"), message);
		assertTrue(message.contains("OBJECT"), message);
		assertTrue(message.contains(MANUAL_DOC_TYPE + ".label"), message);
		assertTrue(message.contains("I18N"), message);
	}

	@Test
	@DisplayName("Should accept a child of an object field in the header")
	void should_accept_a_child_of_an_object_field_in_the_header() {
		// meta.author is in the payload as a value
		var fixture = headerFixture();

		var dataIndex = createDataIndex(
			headerDataIndex(fixture, List.of(fixture.metaAuthorId())));

		assertEquals(
			List.of(fixture.metaAuthorId()), chunkHeaderFieldIds(dataIndex));
	}

	@Test
	@DisplayName("Should accept a language of an I18N field in the header")
	void should_accept_a_language_of_an_i18n_field_in_the_header() {
		// label.i18n.it_IT is in the payload as a value, like a child of an object
		var fixture = headerFixture();

		var dataIndex = createDataIndex(
			headerDataIndex(fixture, List.of(fixture.labelItalianId())));

		assertEquals(
			List.of(fixture.labelItalianId()), chunkHeaderFieldIds(dataIndex));
	}

	@Test
	@DisplayName("Should keep the header fields in the order they were chosen")
	void should_persist_the_header_in_the_chosen_order() {
		// two header fields in reverse id order
		var fixture = headerFixture();
		var headerFieldIds = List.of(fixture.categoryId(), fixture.titleId());

		var dataIndex = createDataIndex(headerDataIndex(fixture, headerFieldIds));

		// the header is read back in the same order
		assertEquals(headerFieldIds, chunkHeaderFieldIds(dataIndex));
	}

	@Test
	@DisplayName("Should keep a header field from being deleted")
	void should_keep_a_header_field_from_being_deleted() {
		// a header field of a saved dataIndex
		var fixture = headerFixture();
		var headerFieldId = fixture.titleId();

		createDataIndex(headerDataIndex(fixture, List.of(headerFieldId)));

		// its deletion fails on the foreign key
		assertThrows(
			Exception.class,
			() -> docTypeFieldService.deleteById(headerFieldId).await().indefinitely()
		);

		// and the field is still there
		assertNotNull(docTypeFieldName(headerFieldId));
	}

	/**
	 * Creates the docTypes the header tests choose their fields from: a
	 * manual with the text to embed, value fields, a keyword sub-field, an
	 * object with a child and an I18N field with a language; and a web docType
	 * the dataIndexes of the tests do not hold.
	 */
	private HeaderFixture headerFixture() {
		var manualId = createDocType(MANUAL_DOC_TYPE);
		var contentId = addField(manualId, "content", FieldType.TEXT);
		var titleId = addField(manualId, "title", FieldType.TEXT);
		var titleKeywordId = addSubField(titleId, "keyword", FieldType.KEYWORD);
		var categoryId = addField(manualId, "category", FieldType.KEYWORD);
		var metaId = addField(manualId, "meta", FieldType.OBJECT);
		var metaAuthorId = addSubField(metaId, "author", FieldType.KEYWORD);
		var labelId = addField(manualId, "label", FieldType.I18N);
		var labelI18nId = addSubField(labelId, "i18n", FieldType.OBJECT);
		var labelItalianId = addSubField(labelI18nId, "it_IT", FieldType.TEXT);

		var webId = createDocType(WEB_DOC_TYPE);
		var webTitleId = addField(webId, "title", FieldType.TEXT);

		return new HeaderFixture(
			manualId, contentId, titleId, titleKeywordId, categoryId, metaId,
			metaAuthorId, labelId, labelItalianId, webTitleId);
	}

	private DataIndexDTO headerDataIndex(
		HeaderFixture fixture, List<Long> headerFieldIds) {

		return DataIndexDTO.builder()
			.name(KNN_DATA_INDEX)
			.knnIndex(true)
			.docTypeIds(Set.of(fixture.manualDocTypeId()))
			.embeddingDocTypeFieldId(fixture.contentId())
			.chunkHeaderFieldIds(headerFieldIds)
			.build();
	}

	private List<Long> chunkHeaderFieldIds(DataIndex dataIndex) {
		return dataIndexService.getChunkHeaderFields(dataIndex.getId())
			.await()
			.indefinitely()
			.stream()
			.map(DocTypeField::getId)
			.toList();
	}

	private long createDocType(String name) {
		return docTypeService.create(DocTypeDTO.builder().name(name).build())
			.await()
			.indefinitely()
			.getId();
	}

	private long addField(long docTypeId, String fieldName, FieldType fieldType) {
		return docTypeService.addDocTypeField(docTypeId, fieldDTO(fieldName, fieldType))
			.await()
			.indefinitely()
			.right
			.getId();
	}

	private long addSubField(long parentId, String fieldName, FieldType fieldType) {
		return docTypeFieldService.createSubField(parentId, fieldDTO(fieldName, fieldType))
			.await()
			.indefinitely()
			.getId();
	}

	private static DocTypeFieldDTO fieldDTO(String fieldName, FieldType fieldType) {
		return DocTypeFieldDTO.builder()
			.name(fieldName)
			.fieldName(fieldName)
			.fieldType(fieldType)
			.build();
	}

	private void deleteDocTypeQuietly(String name) {
		try {
			var docType = EntitiesUtils.getEntity(name, docTypeService, sessionFactory);

			docTypeService.deleteById(docType.getId()).await().indefinitely();
		}
		catch (Exception ignored) {
		}
	}

	private String docTypeFieldName(long id) {
		return sessionFactory.withTransaction((s, t) -> s
				.find(DocTypeField.class, id)
				.map(DocTypeField::getName)
			)
			.await()
			.indefinitely();
	}

	private DataIndex createDataIndex(DataIndexDTO dto) {
		var datasource = EntitiesUtils.getEntity(
			Initializer.INIT_DATASOURCE_CONNECTION, datasourceService, sessionFactory);

		return sessionFactory.withTransaction((s, t) ->
				dataIndexService.create(s, datasource.getId(), dto)
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

	private String componentTemplateName(EmbeddingModel embeddingModel) {
		return new EmbeddingComponentTemplate(
			TENANT_ID,
			embeddingModel.getName(),
			embeddingModel.getVectorSize(),
			embeddingModel.getVectorDataType()
		).getName();
	}

	private DataIndex createDataIndex(String name, boolean knnIndex) {
		var datasource = EntitiesUtils.getEntity(
			Initializer.INIT_DATASOURCE_CONNECTION, datasourceService, sessionFactory);

		var dtoBuilder = DataIndexDTO.builder()
			.name(name)
			.knnIndex(knnIndex);

		if (knnIndex) {
			dtoBuilder.embeddingDocTypeFieldId(anyDocTypeFieldId());
		}

		return sessionFactory.withTransaction((s, t) ->
				dataIndexService.create(s, datasource.getId(), dtoBuilder.build())
			)
			.await()
			.indefinitely();
	}

	private void deleteDataIndexQuietly(String name) {
		try {
			var dataIndex = EntitiesUtils.getEntity(name, dataIndexService, sessionFactory);

			dataIndexService.deleteById(dataIndex.getId()).await().indefinitely();
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

	private void enableEmbeddingModel() {
		var embeddingModel = EntitiesUtils.getEntity(
			Initializer.EMBEDDING_MODEL_DEFAULT_PRIMARY,
			embeddingModelService,
			sessionFactory
		);

		embeddingModelService.enable(embeddingModel.getId()).await().indefinitely();
	}

	private record HeaderFixture(
		long manualDocTypeId,
		long contentId,
		long titleId,
		long titleKeywordId,
		long categoryId,
		long metaId,
		long metaAuthorId,
		long labelId,
		long labelItalianId,
		long webTitleId
	) {}

}
