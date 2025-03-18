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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.util.stream.Collectors;
import jakarta.inject.Inject;

import io.openk9.datasource.graphql.dto.PipelineWithItemsDTO;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.dto.DataIndexDTO;
import io.openk9.datasource.model.dto.PluginDriverDTO;
import io.openk9.datasource.model.dto.UpdateDatasourceConnectionDTO;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.ml.grpc.EmbeddingOuterClass;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

@QuarkusTest
class UpdateDatasourceConnectionTest {

	private static final String TENANT_ID = "public";
	private static final String ENRICH_PIPELINE = "UDCT.enrichPiepeline";
	private static final String PLUGIN_DRIVER = "UDCT.pluginDriver";
	private static final String DATASOURCE = "UDCT.datasource";
	private static final String DATAINDEX = "UDCT.dataindex";

	@Inject
	DatasourceService datasourceService;
	@InjectSpy
	PluginDriverService pluginDriverService;
	@InjectSpy
	EnrichPipelineService enrichPipelineService;
	@InjectSpy
	DataIndexService dataIndexService;
	@Inject
	DocTypeService docTypeService;

	private long datasourceId;
	private long dataIndexId;
	private long pipelineId;
	private long pluginDriverId;
	private long embeddingDocTypeFieldId;

	@Test
	@RunOnVertxContext
	void dataIndex_should_be_created_when_dataIndexId_is_null(
		UniAsserter uniAsserter) {

		var newDataIndexName = "newDataIndex";

		DataIndexDTO newDataIndex = DataIndexDTO.builder()
			.name(newDataIndexName)
			.build();

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				UpdateDatasourceConnectionDTO.builder()
					.name(DATASOURCE)
					.scheduling(DatasourceConnectionObjects.SCHEDULING)
					.reindexing(DatasourceConnectionObjects.REINDEXING)
					.datasourceId(datasourceId)
					.dataIndex(newDataIndex)
					.build()
			),
			response -> {
				then(dataIndexService).should(times(1))
					.createDataIndex(
						any(Mutiny.Session.class),
						any(Datasource.class),
						eq(newDataIndex)
					);

				var datasource = response.getEntity();
				var dataIndex = datasource.getDataIndex();

				Assertions.assertTrue(hasDataIndex(datasource));
				Assertions.assertEquals(newDataIndexName, dataIndex.getName());

			}
		);

	}

	// DataIndex tests

	@Test
	@RunOnVertxContext
	void dataindex_should_not_be_updated_when_dataIndexId_is_not_null_and_dataIndexDto_is_not_null(
		UniAsserter uniAsserter) {

		var name = "no-updatable-name";
		var knnIndex = true;
		var chunkWindowSize = 2;

		DataIndexDTO updateDataIndexDto = DataIndexDTO.builder()
			.name(name)
			.knnIndex(knnIndex)
			.chunkWindowSize(chunkWindowSize)
			.embeddingDocTypeFieldId(embeddingDocTypeFieldId)
			.build();

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				UpdateDatasourceConnectionDTO.builder()
					.name(DATASOURCE)
					.scheduling(DatasourceConnectionObjects.SCHEDULING)
					.reindexing(DatasourceConnectionObjects.REINDEXING)
					.datasourceId(datasourceId)
					.dataIndexId(dataIndexId)
					.dataIndex(updateDataIndexDto)
					.build()),
			response -> {
				then(dataIndexService).should(never())
					.update(
						any(Mutiny.Session.class),
						anyLong(),
						any(DataIndexDTO.class)
					);

				var datasource = response.getEntity();
				var dataIndex = datasource.getDataIndex();

				Assertions.assertTrue(hasDataIndex(datasource));
				Assertions.assertFalse(dataIndex.getKnnIndex());
				Assertions.assertEquals(0, dataIndex.getChunkWindowSize());
				Assertions.assertNull(dataIndex.getEmbeddingDocTypeField());

				// immutable (updatable-false) fields
				Assertions.assertNotEquals(name, dataIndex.getName());
				Assertions.assertNotEquals(knnIndex, dataIndex.getKnnIndex());

			}
		);
	}

	@Test
	@RunOnVertxContext
	void enrichPipeline_should_be_bound_when_enrichPipelineId_is_not_null_and_enrichPipelineDto_is_null(
		UniAsserter uniAsserter) {

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				UpdateDatasourceConnectionDTO.builder()
					.name(DATASOURCE)
					.scheduling(DatasourceConnectionObjects.SCHEDULING)
					.reindexing(DatasourceConnectionObjects.REINDEXING)
					.datasourceId(datasourceId)
					.pipelineId(pipelineId)
					.build()
			),
			datasource -> Assertions.assertTrue(
				hasEnrichPipeline(datasource.getEntity())
			)
		);

	}

	// EnrichPipeline tests

	@Test
	@RunOnVertxContext
	void enrichPipeline_should_be_created_and_bound_when_enrichPipelineId_is_null_and_enrichPipelineDto_is_not_null(
		UniAsserter uniAsserter) {

		PipelineWithItemsDTO newPipeline = PipelineWithItemsDTO.builder()
			.name("enrich-pipeline2")
			.build();

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				UpdateDatasourceConnectionDTO.builder()
					.name(DATASOURCE)
					.scheduling(DatasourceConnectionObjects.SCHEDULING)
					.reindexing(DatasourceConnectionObjects.REINDEXING)
					.datasourceId(datasourceId)
					.pipeline(newPipeline)
					.build()
			),
			datasource -> {
				then(enrichPipelineService).should(times(1)).createWithItems(
					any(Mutiny.Session.class),
					eq(newPipeline)
				);

				Assertions.assertTrue(hasEnrichPipeline(datasource.getEntity()));
			}
		);
	}

	@Test
	@RunOnVertxContext
	void enrichPipeline_should_be_unbound_when_enrichPipelineId_is_null_and_enrichPipelineDto_is_null(
		UniAsserter uniAsserter) {

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				UpdateDatasourceConnectionDTO.builder()
					.name(DATASOURCE)
					.scheduling(DatasourceConnectionObjects.SCHEDULING)
					.reindexing(DatasourceConnectionObjects.REINDEXING)
					.datasourceId(datasourceId)
					.build()
			),
			datasource -> Assertions.assertTrue(
				hasNotEnrichPipeline(datasource.getEntity()))
		);

	}

	@Test
	@RunOnVertxContext
	void enrichPipeline_should_be_updated_when_enrichPipelineId_is_not_null_and_enrichPipelineDto_is_not_null(
		UniAsserter uniAsserter) {

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				UpdateDatasourceConnectionDTO.builder()
					.name(DATASOURCE)
					.scheduling(DatasourceConnectionObjects.SCHEDULING)
					.reindexing(DatasourceConnectionObjects.REINDEXING)
					.datasourceId(datasourceId)
					.pipelineId(pipelineId)
					.pipeline(PipelineWithItemsDTO.builder()
						.name(ENRICH_PIPELINE)
						.description("update")
						.build()
					)
					.build()
			),
			datasource -> {

				then(enrichPipelineService).should(times(1))
					.patchOrUpdateWithItems(
						any(Mutiny.Session.class),
						anyLong(),
						any(PipelineWithItemsDTO.class),
						eq(false)
					);

				Assertions.assertTrue(hasEnrichPipeline(datasource.getEntity()));
			}
		);

	}

	@Test
	@RunOnVertxContext
	void pluginDriverService_should_have_no_interaction_when_pluginDriverDto_is_not_null(
		UniAsserter uniAsserter) {

		final String pluginName = "mockplugindatasourceconnection";

		uniAsserter.assertThat(
			() ->
				datasourceService.updateDatasourceConnection(
					UpdateDatasourceConnectionDTO.builder()
						.name(DATASOURCE)
						.scheduling(DatasourceConnectionObjects.SCHEDULING)
						.reindexing(DatasourceConnectionObjects.REINDEXING)
						.datasourceId(datasourceId)
						.pluginDriver(DatasourceConnectionObjects.PLUGIN_DRIVER_DTO_BUILDER()
							.name(pluginName)
							.build()
						)
						.build()
				),
			datasource -> then(pluginDriverService)
				.should(never())
				.create(
					any(Mutiny.Session.class),
					ArgumentMatchers.<PluginDriverDTO>argThat(p ->
						p.getName().equals(pluginName))
				)
		);
	}

	// PluginDriver Tests

	@Test
	@RunOnVertxContext
	void pluginDriverService_should_have_no_interaction_when_pluginId_is_not_null(
		UniAsserter uniAsserter) {

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				UpdateDatasourceConnectionDTO.builder()
					.name(DATASOURCE)
					.scheduling(DatasourceConnectionObjects.SCHEDULING)
					.reindexing(DatasourceConnectionObjects.REINDEXING)
					.datasourceId(datasourceId)
					.pluginDriverId(pluginDriverId)
					.scheduling(DatasourceConnectionObjects.SCHEDULING)
					.build()
			),
			datasource -> then(pluginDriverService)
				.should(never())
				.findById(eq(pluginDriverId))
		);
	}

	@BeforeEach
	void setup() {

		var pluginDriver =
			pluginDriverService.create(DatasourceConnectionObjects.PLUGIN_DRIVER_DTO_BUILDER()
				.name(PLUGIN_DRIVER)
				.build()
			).await().indefinitely();

		var docTypeIds = docTypeService.findAll()
			.await()
			.indefinitely()
			.stream()
			.map(DocType::getId)
			.collect(Collectors.toSet());

		var datasource = datasourceService.createDatasourceConnection(
				DatasourceConnectionObjects.DATASOURCE_CONNECTION_DTO_BUILDER()
					.name(DATASOURCE)
					.pluginDriverId(pluginDriver.getId())
					.pipeline(PipelineWithItemsDTO.builder()
						.name(ENRICH_PIPELINE)
						.build()
					)
					.dataIndex(DataIndexDTO.builder()
						.name(DATAINDEX)
						.chunkType(EmbeddingOuterClass.ChunkType.CHUNK_TYPE_TEXT_SPLITTER)
						.docTypeIds(docTypeIds)
						.knnIndex(false)
						.build())
					.build()
			)
			.await()
			.indefinitely();

		this.pluginDriverId = pluginDriver.getId();

		this.pipelineId = enrichPipelineService.findByName(TENANT_ID, ENRICH_PIPELINE)
			.await().indefinitely().getId();

		this.datasourceId = datasource.getEntity().getId();

		var dataIndex = datasourceService.getDataIndex(datasourceId)
			.await().indefinitely();

		this.dataIndexId = dataIndex.getId();

		var docTypes = dataIndexService.getDocTypes(
			dataIndex.getId(), Pageable.DEFAULT).await().indefinitely();

		var firstDocType = docTypes.iterator().next();

		var docTypeFields = docTypeService.getDocTypeFields(
			firstDocType.getId(), Pageable.DEFAULT).await().indefinitely();

		var firstDocTypeField = docTypeFields.iterator().next();

		this.embeddingDocTypeFieldId = firstDocTypeField.getId();

	}

	@AfterEach
	void tearDown() {
		var datasource = datasourceService.findByName(TENANT_ID, DATASOURCE)
			.await().indefinitely();

		var pluginDriver = pluginDriverService.findByName(TENANT_ID, PLUGIN_DRIVER)
			.await().indefinitely();

		var dataIndex = datasourceService.getDataIndex(datasource.getId())
			.await().indefinitely();

		var enrichPipeline = enrichPipelineService.findByName(TENANT_ID, ENRICH_PIPELINE)
			.await().indefinitely();

		// unset relations
		datasourceService.unsetPluginDriver(datasource.getId())
			.await().indefinitely();

		datasourceService.unsetDataIndex(datasource.getId())
			.await().indefinitely();

		datasourceService.unsetEnrichPipeline(datasource.getId())
			.await().indefinitely();

		// deletes entities
		enrichPipelineService.deleteById(enrichPipeline.getId())
			.await().indefinitely();
		dataIndexService.deleteById(dataIndex.getId())
			.await().indefinitely();
		pluginDriverService.deleteById(pluginDriver.getId())
			.await().indefinitely();
		datasourceService.deleteById(datasource.getId())
			.await().indefinitely();

		this.datasourceId = 0;

	}

	// Utils

	private static boolean hasEnrichPipeline(Datasource datasource) {
		return datasource != null && datasource.getEnrichPipeline() != null;
	}

	private static boolean hasNotEnrichPipeline(Datasource datasource) {
		return datasource != null && datasource.getEnrichPipeline() == null;
	}

	private static boolean hasDataIndex(Datasource datasource) {
		return datasource != null && datasource.getDataIndex() != null;
	}

	private static boolean hasNotDataIndex(Datasource datasource) {
		return datasource != null && datasource.getDataIndex() == null;
	}

}
