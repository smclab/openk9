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
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import jakarta.inject.Inject;

import io.openk9.datasource.Initializer;
import io.openk9.datasource.graphql.dto.PipelineWithItemsDTO;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.dto.DataIndexDTO;
import io.openk9.datasource.model.dto.PluginDriverDTO;
import io.openk9.datasource.service.exception.K9Error;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CreateDatasourceConnectionTest {

	@Inject
	DatasourceService datasourceService;

	@InjectSpy(delegate = true)
	PluginDriverService pluginDriverService;

	@InjectSpy(delegate = true)
	EnrichPipelineService enrichPipelineService;

	@InjectSpy(delegate = true)
	DataIndexService dataIndexService;

	@Test
	@RunOnVertxContext
	void should_create_everything_base(UniAsserter asserter) {

		asserter.assertThat(
			() -> enrichPipelineService.createWithItems(
				PipelineWithItemsDTO.builder()
					.name("NEW_ENTITIES_BASE_PIPELINE")
					.build()
			).flatMap(pipeline -> datasourceService.createDatasourceConnection(
				DatasourceConnectionObjects.NEW_ENTITIES_BASE_DTO_BUILDER
						.pipelineId(pipeline.getEntity().getId())
						.build()
				)
			),
			datasourceEntity ->
				asserter.assertThat(
					() -> datasourceService
						.getDataIndex(datasourceEntity.getEntity().getId())
						.flatMap(dataIndex -> datasourceService
							.getPluginDriver(datasourceEntity.getEntity().getId())
							.flatMap(pluginDriver -> datasourceService
								.getEnrichPipeline(datasourceEntity.getEntity().getId())
								.map(enrichPipeline -> {
									var datasource = new Datasource();
									datasource.setDataIndex(dataIndex);
									datasource.setPluginDriver(pluginDriver);
									datasource.setEnrichPipeline(enrichPipeline);
									return datasource;
								})
							)
						),
					datasource -> {
						Assertions.assertNotNull(datasource.getDataIndex());
						Assertions.assertNotNull(datasource.getEnrichPipeline());
						Assertions.assertNotNull(datasource.getPluginDriver());
					}
				)
		);

	}

	@Test
	@RunOnVertxContext
	void should_create_everything_vector(UniAsserter asserter) {

		asserter.assertThat(
			() -> datasourceService.createDatasourceConnection(
				DatasourceConnectionObjects.NEW_ENTITIES_VECTOR_DTO),
			response -> {
				then(pluginDriverService)
					.should(times(1))
					.create(
						anySession(),
						any(PluginDriverDTO.class)
					);

				then(enrichPipelineService)
					.should(times(1))
					.createWithItems(
						anySession(),
						any(PipelineWithItemsDTO.class)
					);

				then(dataIndexService)
					.should(times(1))
					.createDataIndexByDatasourceConnection(
						anySession(),
						any(Datasource.class),
						nullable(DataIndexDTO.class)
					);

			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_create_pipeline_dataIndex(UniAsserter asserter) {

		asserter.assertThat(
			() -> pluginDriverService.create(DatasourceConnectionObjects.PLUGIN_DRIVER_DTO_BUILDER()
				.name("PRE_EXIST_PLUGIN")
				.build()
			).flatMap(pluginDriver -> datasourceService
				.createDatasourceConnection(
					DatasourceConnectionObjects.PRE_EXIST_PLUGIN_NEW_PIPELINE_DTO_BUILDER
						.pluginDriverId(pluginDriver.getId())
						.build()
				)
			),
			datasourceEntity ->
				asserter.assertThat(
					() -> datasourceService
						.getDataIndex(datasourceEntity.getEntity().getId())
						.flatMap(dataIndex -> datasourceService
							.getPluginDriver(datasourceEntity.getEntity().getId())
							.flatMap(pluginDriver -> datasourceService
								.getEnrichPipeline(datasourceEntity.getEntity().getId())
								.map(enrichPipeline -> {
									var datasource = new Datasource();
									datasource.setDataIndex(dataIndex);
									datasource.setPluginDriver(pluginDriver);
									datasource.setEnrichPipeline(enrichPipeline);
									return datasource;
								})
							)
						),
					datasource -> {
						Assertions.assertNotNull(datasource.getDataIndex());
						Assertions.assertNotNull(datasource.getEnrichPipeline());
						Assertions.assertNotNull(datasource.getPluginDriver());
					}
				)
		);

	}

	@Test
	@RunOnVertxContext
	void should_create_plugin_and_dataIndex(UniAsserter asserter) {

		asserter.assertThat(
			() -> enrichPipelineService.createWithItems(PipelineWithItemsDTO.builder()
				.name("PRE_EXIST_PIPELINE")
				.build()
			).flatMap(enrichPipelineResponse -> datasourceService.createDatasourceConnection(
				DatasourceConnectionObjects.NEW_PLUGIN_PRE_EXIST_PIPELINE_DTO_BUILDER
						.pipelineId(enrichPipelineResponse.getEntity().getId())
						.build()
				)
			),
			response -> {
				then(pluginDriverService)
					.should(times(1))
					.create(anySession(), any(PluginDriverDTO.class));

				then(enrichPipelineService)
					.should(times(1))
					.findById(anySession(), anyLong());

				then(dataIndexService)
					.should(times(1))
					.createDataIndexByDatasourceConnection(
						anySession(),
						any(Datasource.class), nullable(DataIndexDTO.class)
					);
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_fail_with_K9Error_when_transaction_exception(UniAsserter asserter) {

		asserter.assertFailedWith(
			() -> datasourceService.createDatasourceConnection(
				DatasourceConnectionObjects.NEW_ENTITIES_BASE_DTO_BUILDER
					.name("transaction_exception_test")
					.pipeline(PipelineWithItemsDTO.builder()
						.name(Initializer.INIT_DATASOURCE_PIPELINE)
						.build()
					)
					.build()),
			failure -> {
				Assertions.assertInstanceOf(K9Error.class, failure);

				then(dataIndexService).shouldHaveNoInteractions();
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_fail_with_validation_exception_when_ambiguousDto(UniAsserter asserter) {

		asserter.assertThat(
			() -> datasourceService.createDatasourceConnection(DatasourceConnectionObjects.AMBIGUOUS_DTO),
			response -> {

				Assertions.assertFalse(response.getFieldValidators().isEmpty());

				then(pluginDriverService).shouldHaveNoInteractions();
				then(enrichPipelineService).shouldHaveNoInteractions();
				then(dataIndexService).shouldHaveNoInteractions();

			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_fail_with_validation_exception_when_no_plugin_driver_dto(UniAsserter asserter) {

		asserter.assertThat(
			() -> datasourceService.createDatasourceConnection(DatasourceConnectionObjects.NO_PLUGIN_NO_PIPELINE_DTO),
			response -> {
				Assertions.assertFalse(response.getFieldValidators().isEmpty());

				then(pluginDriverService).shouldHaveNoInteractions();
				then(enrichPipelineService).shouldHaveNoInteractions();
				then(dataIndexService).shouldHaveNoInteractions();
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_not_associate_any_pipeline(UniAsserter asserter) {

		asserter.assertThat(
			() -> datasourceService.createDatasourceConnection(DatasourceConnectionObjects.NEW_PLUGIN_NO_PIPELINE_DTO),
			response -> {
				then(pluginDriverService)
					.should(times(1))
					.create(
						anySession(),
						eq(DatasourceConnectionObjects.NEW_PLUGIN_NO_PIPELINE_DTO.getPluginDriver())
					);

				then(enrichPipelineService).shouldHaveNoInteractions();

				then(dataIndexService)
					.should(times(1))
					.createDataIndexByDatasourceConnection(
						anySession(),
						any(Datasource.class), nullable(DataIndexDTO.class)
					);
			}
		);

	}

	private static Mutiny.Session anySession() {
		return any(Mutiny.Session.class);
	}
}