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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

import io.openk9.datasource.Initializer;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.dto.base.DataIndexDTO;
import io.openk9.datasource.model.dto.request.PipelineWithItemsDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.service.exception.K9Error;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CreateDatasourceConnectionTest {

	private static final String DATASOURCE_NAME = "cdct.datasource";
	private static final String DATA_INDEX_NAME = "cdct.dataIndex";
	private static final String ENRICH_PIPELINE_NAME = "cdct.enrichPipeline";

	@Inject
	DatasourceService datasourceService;

	@InjectSpy(delegate = true)
	PluginDriverService pluginDriverService;

	@InjectSpy(delegate = true)
	EnrichPipelineService enrichPipelineService;

	@InjectSpy(delegate = true)
	DataIndexService dataIndexService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	private Long pluginDriverId;
	private Long enrichPipelineId;

	@BeforeEach
	void setup() {
		var pluginDriver = pluginDriverService
			.findByName("public", Initializer.INIT_DATASOURCE_PLUGIN)
			.await()
			.indefinitely();

		this.pluginDriverId = pluginDriver.getId();

		var enrichPipeline = enrichPipelineService.findByName(
				"public",
				Initializer.INIT_DATASOURCE_PIPELINE
			)
			.await()
			.indefinitely();

		this.enrichPipelineId = enrichPipeline.getId();

	}

	@Test
	void should_associate_pipeline() {

		var response = datasourceService.createDatasourceConnection(
			DatasourceConnectionObjects.DATASOURCE_CONNECTION_DTO_BUILDER()
				.name(DATASOURCE_NAME)
				.pluginDriverId(pluginDriverId)
				.pipelineId(enrichPipelineId)
				.dataIndex(DataIndexDTO.builder()
					.name(DATA_INDEX_NAME)
					.build())
				.build()
		).await().indefinitely();

		var datasourceId = response.getEntity().getId();

		var enrichPipeline = datasourceService.getEnrichPipeline(datasourceId)
			.await().indefinitely();

		Assertions.assertEquals(enrichPipelineId, enrichPipeline.getId());

		then(dataIndexService).should().create(
			anySession(),
			eq(datasourceId),
			argThat((DataIndexDTO dto) ->
					dto.getName().equals(DATA_INDEX_NAME))
		);

	}

	@Test
	void should_create_with_pipeline() {

		var response = datasourceService.createDatasourceConnection(
			DatasourceConnectionObjects.DATASOURCE_CONNECTION_DTO_BUILDER()
				.name(DATASOURCE_NAME)
				.pluginDriverId(pluginDriverId)
				.pipeline(PipelineWithItemsDTO.builder()
					.name(ENRICH_PIPELINE_NAME)
					.build()
				)
				.dataIndex(DataIndexDTO.builder()
					.name(DATA_INDEX_NAME)
					.build())
				.build()
		).await().indefinitely();

		var datasourceId = response.getEntity().getId();
		var enrichPipeline = datasourceService.getEnrichPipeline(datasourceId)
			.await().indefinitely();

		Assertions.assertEquals(ENRICH_PIPELINE_NAME, enrichPipeline.getName());

		then(dataIndexService).should().create(
			anySession(),
			eq(datasourceId),
			argThat((DataIndexDTO dto) ->
				dto.getName().equals(DATA_INDEX_NAME))
		);

	}

	@Test
	void should_fail_with_K9Error_when_transaction_exception() {

		Exception failure = null;
		try {
			datasourceService.createDatasourceConnection(
					DatasourceConnectionObjects.DATASOURCE_CONNECTION_DTO_BUILDER()
						.name(DATASOURCE_NAME)
						.pluginDriverId(Long.MAX_VALUE) // not exist
						.pipeline(PipelineWithItemsDTO.builder()
							.name(Initializer.INIT_DATASOURCE_PIPELINE) // already exist
							.build()
						)
						.dataIndex(DataIndexDTO.builder()
							.name(DATA_INDEX_NAME)
							.build())
						.build())
				.await()
				.indefinitely();
		}
		catch (Exception e) {
			failure = e;
		}

		Assertions.assertInstanceOf(K9Error.class, failure);

		then(dataIndexService).shouldHaveNoInteractions();

	}

	@Test
	void should_fail_with_validation_exception_when_ambiguousDto() {

		var response = datasourceService
			.createDatasourceConnection(DatasourceConnectionObjects.AMBIGUOUS_DTO)
			.await().indefinitely();

		Assertions.assertFalse(response.getFieldValidators().isEmpty());

		then(dataIndexService).shouldHaveNoInteractions();

	}

	@Test
	void should_fail_with_validation_exception_when_no_plugin_driver_dto() {

		var response = datasourceService.createDatasourceConnection(
				DatasourceConnectionObjects
					.DATASOURCE_CONNECTION_DTO_BUILDER()
					.build())
			.await().indefinitely();

		Assertions.assertFalse(response.getFieldValidators().isEmpty());

		then(dataIndexService).shouldHaveNoInteractions();

	}

	@Test
	void should_not_associate_any_pipeline() {

		var response = pluginDriverService.findByName("public", Initializer.INIT_DATASOURCE_PLUGIN)
			.flatMap(pluginDriver -> datasourceService
				.createDatasourceConnection(DatasourceConnectionObjects
					.DATASOURCE_CONNECTION_DTO_BUILDER()
					.name(DATASOURCE_NAME)
					.pluginDriverId(pluginDriver.getId())
					.dataIndex(DataIndexDTO.builder()
						.name(DATA_INDEX_NAME)
						.build())
					.build()
				)
			).await().indefinitely();

		var datasourceId = response.getEntity().getId();
		Assertions.assertNull(response.getEntity().getEnrichPipeline());

		then(dataIndexService).should(times(1)).create(
			anySession(),
			eq(datasourceId),
			argThat((DataIndexDTO dto) ->
				dto.getName().equals(DATA_INDEX_NAME)
			)
		);
	}

	@Test
	void should_record_the_shape_of_the_first_data_index_on_the_datasource() {
		// 1. The shape is given on the datasource
		var docTypeField = sampleTextDocTypeField();
		var docTypeId = docTypeField.getDocType().getId();

		var response = datasourceService.createDatasourceConnection(
			DatasourceConnectionObjects.DATASOURCE_CONNECTION_DTO_BUILDER()
				.name(DATASOURCE_NAME)
				.pluginDriverId(pluginDriverId)
				.chunkWindowSize(3)
				.embeddingJsonConfig("{\"batch\": 8}")
				.embeddingDocTypeFieldId(docTypeField.getId())
				.docTypeIds(Set.of(docTypeId))
				.dataIndex(DataIndexDTO.builder()
					.name(DATA_INDEX_NAME)
					.build())
				.build()
		).await().indefinitely();

		var datasourceId = response.getEntity().getId();

		// 2. The datasource records it
		var datasource = sessionFactory.withSession(session -> datasourceService
			.findById(session, datasourceId)
			.call(found -> session.fetch(found.getDocTypes()))
			.call(found -> session.fetch(found.getEmbeddingDocTypeField()))
		).await().indefinitely();

		Assertions.assertFalse(datasource.getKnnIndex());
		Assertions.assertEquals(3, datasource.getChunkWindowSize());
		Assertions.assertEquals("{\"batch\": 8}", datasource.getEmbeddingJsonConfig());
		Assertions.assertEquals(
			docTypeField.getId(), datasource.getEmbeddingDocTypeField().getId());
		Assertions.assertEquals(
			Set.of(docTypeId),
			datasource.getDocTypes().stream()
				.map(K9Entity::getId)
				.collect(Collectors.toSet())
		);

		// 3. And the first dataIndex is created with it
		then(dataIndexService).should().create(
			anySession(),
			eq(datasourceId),
			argThat((DataIndexDTO dto) ->
				dto.getName().equals(DATA_INDEX_NAME)
					&& dto.getChunkWindowSize() == 3
					&& dto.getEmbeddingDocTypeFieldId().equals(docTypeField.getId())
					&& dto.getDocTypeIds().equals(Set.of(docTypeId)))
		);
	}

	@Test
	void should_take_the_shape_from_the_nested_data_index_when_not_given() {
		// a client that only describes the dataIndex
		var response = datasourceService.createDatasourceConnection(
			DatasourceConnectionObjects.DATASOURCE_CONNECTION_DTO_BUILDER()
				.name(DATASOURCE_NAME)
				.pluginDriverId(pluginDriverId)
				.dataIndex(DataIndexDTO.builder()
					.name(DATA_INDEX_NAME)
					.chunkWindowSize(5)
					.build())
				.build()
		).await().indefinitely();

		var datasourceId = response.getEntity().getId();

		// the datasource records what the nested dataIndex asked for
		var datasource = datasourceService.findById(datasourceId)
			.await().indefinitely();

		Assertions.assertEquals(5, datasource.getChunkWindowSize());

		then(dataIndexService).should().create(
			anySession(),
			eq(datasourceId),
			argThat((DataIndexDTO dto) -> dto.getChunkWindowSize() == 5)
		);
	}

	@Test
	void should_prefer_the_shape_of_the_datasource_over_the_nested_data_index() {
		var response = datasourceService.createDatasourceConnection(
			DatasourceConnectionObjects.DATASOURCE_CONNECTION_DTO_BUILDER()
				.name(DATASOURCE_NAME)
				.pluginDriverId(pluginDriverId)
				.chunkWindowSize(2)
				.dataIndex(DataIndexDTO.builder()
					.name(DATA_INDEX_NAME)
					.chunkWindowSize(5)
					.build())
				.build()
		).await().indefinitely();

		var datasource = datasourceService
			.findById(response.getEntity().getId())
			.await().indefinitely();

		Assertions.assertEquals(2, datasource.getChunkWindowSize());
	}

	@AfterEach
	void tearDown() {
		this.pluginDriverId = null;
		this.enrichPipelineId = null;

		try {
			datasourceService.findByName("public", DATASOURCE_NAME)
				.flatMap(datasource -> datasourceService
					.deleteById(datasource.getId()))
				.await().indefinitely();
		}
		catch (NoResultException e) {
			// datasource already does not exist
		}

		try {
			enrichPipelineService.findByName("public", ENRICH_PIPELINE_NAME)
				.flatMap(enrichPipeline -> enrichPipelineService
					.deleteById("public", enrichPipeline.getId()))
				.await().indefinitely();
		}
		catch (NoResultException e) {
			// enrichPipeline already does not exist
		}
	}

	private DocTypeField sampleTextDocTypeField() {
		return sessionFactory.withSession(session -> docTypeFieldService
			.findAll(session)
			.map(docTypeFields -> docTypeFields.stream()
				.filter(field -> "sample".equalsIgnoreCase(field.getDocType().getName()))
				.filter(field -> FieldType.TEXT.equals(field.getFieldType()))
				.findFirst()
				.orElseThrow())
		).await().indefinitely();
	}

	private static Mutiny.Session anySession() {
		return any(Mutiny.Session.class);
	}
}