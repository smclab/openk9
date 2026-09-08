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

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.ValidationException;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.IndexTemplateUtils;
import io.openk9.datasource.Initializer;
import io.openk9.datasource.index.model.EmbeddingComponentTemplate;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.dto.base.DataIndexDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensearch.client.RestHighLevelClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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

	@Inject
	DataIndexService dataIndexService;

	@Inject
	DatasourceService datasourceService;

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

}
