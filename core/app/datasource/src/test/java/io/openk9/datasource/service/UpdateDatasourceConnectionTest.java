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

import io.openk9.datasource.graphql.dto.PipelineWithItemsDTO;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.dto.PluginDriverDTO;
import io.openk9.datasource.model.dto.UpdateDatasourceConnectionDTO;
import io.openk9.datasource.model.dto.VectorIndexDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@QuarkusTest
class UpdateDatasourceConnectionTest extends BaseDatasourceServiceTest {

	// PluginDriver Tests

	@Test
	@RunOnVertxContext
	void should_unbind_pluginDriver_when_pluginId_is_null_and_pluginDto_is_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder().build()
			),
			datasource -> then(mockSession).should(times(1)).merge(argThat(
				UpdateDatasourceConnectionTest::hasNotPluginDriver))
		);

	}

	@Test
	@RunOnVertxContext
	void should_create_and_bind_pluginDriver_when_pluginId_is_null_and_pluginDto_is_not_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		given(pluginDriverService.create(
				anySession(),
				any(PluginDriverDTO.class)
			)
		).willReturn(Uni.createFrom().item(CreateConnection.PLUGIN_DRIVER));

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder()
					.datasourceId(CreateConnection.DATASOURCE_ID)
					.pluginDriver(CreateConnection.PLUGIN_DRIVER_DTO)
					.build()
			),
			datasource -> {
				then(pluginDriverService).should(times(1))
					.create(anySession(), any(PluginDriverDTO.class));

				then(mockSession).should(times(1)).merge(argThat(
					UpdateDatasourceConnectionTest::hasPluginDriver));
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_bind_existing_pluginDriver_when_pluginId_is_not_null_and_pluginDto_is_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		given(pluginDriverService.findById(anySession(), anyLong()))
			.willReturn(Uni.createFrom().item(CreateConnection.PLUGIN_DRIVER));

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder()
					.datasourceId(CreateConnection.DATASOURCE_ID)
					.pluginDriverId(CreateConnection.PLUGIN_DRIVER_ID)
					.build()
			),
			datasource -> then(mockSession).should(times(1)).merge(argThat(
				UpdateDatasourceConnectionTest::hasPluginDriver))
		);

	}

	@Test
	@RunOnVertxContext
	void should_update_existing_pluginDriver_when_pluginId_is_not_null_and_pluginDto_is_not_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		given(pluginDriverService.update(
			anySession(), anyLong(), any(PluginDriverDTO.class))
		).willReturn(Uni.createFrom().item(CreateConnection.PLUGIN_DRIVER));

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder()
					.pluginDriverId(CreateConnection.PLUGIN_DRIVER_ID)
					.pluginDriver(CreateConnection.PLUGIN_DRIVER_DTO)
					.build()
			),
			datasource -> {

				then(pluginDriverService).should(times(1)).update(
					anySession(), anyLong(), any(PluginDriverDTO.class));

				then(mockSession).should(times(1)).merge(argThat(
					UpdateDatasourceConnectionTest::hasPluginDriver));

			}
		);

	}

	// EnrichPipeline tests

	@Test
	@RunOnVertxContext
	void should_unbind_enrichPipeline_when_enrichPipelineId_is_null_and_enrichPipelineDto_is_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder().build()
			),
			datasource -> then(mockSession).should(times(1)).merge(argThat(
				UpdateDatasourceConnectionTest::hasNotEnrichPipeline))
		);

	}

	@Test
	@RunOnVertxContext
	void should_create_and_bind_enrichPipeline_when_enrichPipelineId_is_null_and_enrichPipelineDto_is_not_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		given(enrichPipelineService.createWithItems(anySession(), any(PipelineWithItemsDTO.class)))
			.willReturn(Uni.createFrom().item(CreateConnection.PIPELINE));

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder()
					.datasourceId(CreateConnection.DATASOURCE_ID)
					.pipeline(CreateConnection.PIPELINE_WITH_ITEMS_DTO)
					.build()
			),
			datasource -> {
				then(enrichPipelineService).should(times(1))
					.createWithItems(anySession(), any(PipelineWithItemsDTO.class));

				then(mockSession).should(atLeastOnce())
					.merge(
						argThat(UpdateDatasourceConnectionTest::hasEnrichPipeline)
					);

			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_bind_existing_enrichPipeline_when_enrichPipelineId_is_not_null_and_enrichPipelineDto_is_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		given(enrichPipelineService.findById(anySession(), anyLong()))
			.willReturn(Uni.createFrom().item(CreateConnection.PIPELINE));

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder()
					.datasourceId(CreateConnection.DATASOURCE_ID)
					.pipelineId(CreateConnection.PIPELINE_ID)
					.build()
			),
			datasource -> then(mockSession).should(times(1)).merge(argThat(
				UpdateDatasourceConnectionTest::hasEnrichPipeline))
		);


	}

	@Test
	@RunOnVertxContext
	void should_update_existing_enrichPipeline_when_enrichPipelineId_is_not_null_and_enrichPipelineDto_is_not_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		given(enrichPipelineService.patchOrUpdateWithItems(
			anySession(), anyLong(), any(PipelineWithItemsDTO.class), eq(false))
		).willReturn(Uni.createFrom().item(CreateConnection.PIPELINE));

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder()
					.pipelineId(CreateConnection.PIPELINE_ID)
					.pipeline(CreateConnection.PIPELINE_WITH_ITEMS_DTO)
					.build()
			),
			datasource -> {

				then(enrichPipelineService).should(times(1))
					.patchOrUpdateWithItems(
						anySession(), anyLong(),
						any(PipelineWithItemsDTO.class), eq(false)
					);

				then(mockSession).should(times(1))
					.merge(
						argThat(UpdateDatasourceConnectionTest::hasEnrichPipeline)
					);
			}
		);

	}

	// DataIndex tests

	@Test
	@RunOnVertxContext
	void should_create_a_new_dataIndex_when_dataIndexId_is_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder().build()
			),
			datasource -> {
				then(dataIndexService).should(times(1))
					.createByDatasource(anySession(), any(Datasource.class));
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_create_a_new_dataIndex_with_vectorIndex_when_dataIndexId_is_null_and_vectorIndexDto_is_not_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder().build()
			),
			datasource -> {
				then(dataIndexService).should(times(1))
					.createByDatasource(anySession(), any(Datasource.class));
				then(vectorIndexService).should(times(1))
					.create(any(VectorIndexDTO.class));
				then(dataIndexService).should(times(1))
					.bindVectorDataIndex(anyLong(), anyLong());
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_bind_an_existing_dataIndex_and_update_vectorIndex_when_dataIndexId_is_not_null_and_vectorIndex_exist_and_vectorIndexDto_is_not_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder().build()
			),
			datasource -> {
				then(datasourceService).should(times(1))
					.setDataIndex(anySession(), anyLong(), anyLong());
				then(vectorIndexService).should(times(1))
					.findById(anySession(), anyLong());
				then(vectorIndexService).should(times(1))
					.update(anySession(), anyLong(), any(VectorIndexDTO.class));
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_bind_an_existing_dataIndex_and_create_vectorIndex_when_dataIndexId_is_not_null_and_vectorIndex_does_not_exist_and_vectorIndexDto_is_not_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder().build()
			),
			datasource -> {
				then(datasourceService).should(times(1))
					.setDataIndex(anySession(), anyLong(), anyLong());
				then(vectorIndexService).should(times(1))
					.findById(anySession(), anyLong());
				then(vectorIndexService).should(times(1))
					.create(anySession(), any(VectorIndexDTO.class));
				then(dataIndexService).should(times(1))
					.bindVectorDataIndex(anySession(), anyLong(), anyLong());
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_bind_an_existing_dataIndex_and_do_not_modify_vectorIndex_when_dataIndexId_is_not_null_and_vectorIndex_exist_and_vectorIndexDto_is_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder().build()
			),
			datasource -> {
				then(datasourceService).should(times(1))
					.setDataIndex(anySession(), anyLong(), anyLong());
			}
		);
	}

	@Test
	@RunOnVertxContext
	void should_bind_an_existing_dataIndex_and_do_not_create_vectorIndex_when_dataIndexId_is_not_null_and_vectorIndex_does_not_exist_and_vectorIndexDto_is_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder().build()
			),
			datasource -> {
				then(datasourceService).should(times(1))
					.setDataIndex(anySession(), anyLong(), anyLong());
			}
		);
	}

	// Utils

	private static Mutiny.Session anySession() {
		return any(Mutiny.Session.class);
	}

	private static boolean hasPluginDriver(Datasource entity) {
		return entity != null && entity.getPluginDriver() != null;
	}

	private static boolean hasNotPluginDriver(Datasource entity) {
		return entity != null && entity.getPluginDriver() == null;
	}

	private static boolean hasEnrichPipeline(Datasource entity) {
		return entity != null && entity.getEnrichPipeline() != null;
	}

	private static boolean hasNotEnrichPipeline(Datasource entity) {
		return entity != null && entity.getEnrichPipeline() == null;
	}

}
