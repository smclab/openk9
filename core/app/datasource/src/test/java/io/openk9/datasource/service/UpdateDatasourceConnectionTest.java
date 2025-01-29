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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import jakarta.inject.Inject;

import io.openk9.datasource.graphql.dto.EmbeddingVectorDTO;
import io.openk9.datasource.graphql.dto.PipelineWithItemsDTO;
import io.openk9.datasource.mapper.DatasourceMapper;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.dto.UpdateDatasourceConnectionDTO;

import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@QuarkusTest
class UpdateDatasourceConnectionTest {

	@Inject
	DatasourceService datasourceService;
	@InjectMock
	PluginDriverService pluginDriverService;
	@InjectMock
	EnrichPipelineService enrichPipelineService;
	@InjectMock
	DataIndexService dataIndexService;

	// PluginDriver Tests

	@Test
	@RunOnVertxContext
	void should_bind_existing_enrichPipeline_when_enrichPipelineId_is_not_null_and_enrichPipelineDto_is_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		given(enrichPipelineService.findById(anySession(), anyLong()))
			.willReturn(Uni.createFrom().item(new EnrichPipeline()));

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder()
					.datasourceId(Long.MAX_VALUE)
					.pipelineId(Long.MAX_VALUE)
					.build()
			),
			datasource -> then(mockSession).should(times(1)).merge(argThat(
				UpdateDatasourceConnectionTest::hasEnrichPipeline))
		);


	}

	@Test
	@RunOnVertxContext
	void should_create_a_new_dataIndex_when_dataIndexId_is_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		given(dataIndexService.createByDatasource(
			anySession(), nullable(EmbeddingVectorDTO.class), any(Datasource.class)))
			.willReturn(Uni.createFrom().item(new DataIndex()));

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder()
					.datasourceId(Long.MAX_VALUE)
					.build()
			),
			datasource -> {
				then(dataIndexService).should(times(1))
					.createByDatasource(
						anySession(), nullable(EmbeddingVectorDTO.class), any(Datasource.class));

				then(mockSession).should(atLeastOnce())
					.merge(argThat(UpdateDatasourceConnectionTest::hasDataIndex));
			}
		);

	}

	// EnrichPipeline tests

	@Test
	@RunOnVertxContext
	void should_create_and_bind_enrichPipeline_when_enrichPipelineId_is_null_and_enrichPipelineDto_is_not_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		given(enrichPipelineService.createWithItems(anySession(), any(PipelineWithItemsDTO.class)))
			.willReturn(Uni.createFrom().item(new EnrichPipeline()));

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder()
					.datasourceId(Long.MAX_VALUE)
					.pipeline(PipelineWithItemsDTO.builder()
						.name("mockpipeline")
						.build()
					)
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
	void should_have_no_interaction_with_pluginDriver_when_pluginDriverDto_is_not_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder()
					.name("mockdatasourceconnection")
					.scheduling(CreateConnection.SCHEDULING)
					.datasourceId(Long.MAX_VALUE)
					.pluginDriver(CreateConnection.PLUGIN_DRIVER_DTO_BUILDER()
						.name("mockplugindatasourceconnection")
						.build()
					)
					.build()
			),
			datasource -> then(pluginDriverService).shouldHaveNoInteractions()
		);
	}

	@Test
	@RunOnVertxContext
	void should_have_no_interaction_with_pluginDriver_when_pluginId_is_not_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder()
					.name("mockdatasourceconnection")
					.datasourceId(Long.MAX_VALUE)
					.pluginDriverId(Long.MAX_VALUE)
					.scheduling(CreateConnection.SCHEDULING)
					.build()
			),
			datasource -> then(pluginDriverService).shouldHaveNoInteractions()
		);
	}

	@Test
	@RunOnVertxContext
	void should_unbind_enrichPipeline_when_enrichPipelineId_is_null_and_enrichPipelineDto_is_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder()
					.datasourceId(Long.MAX_VALUE)
					.build()
			),
			datasource -> then(mockSession).should(times(1)).merge(argThat(
				UpdateDatasourceConnectionTest::hasNotEnrichPipeline))
		);

	}

	// DataIndex tests

	@Test
	@RunOnVertxContext
	void should_update_existing_enrichPipeline_when_enrichPipelineId_is_not_null_and_enrichPipelineDto_is_not_null(
		UniAsserter uniAsserter) {

		var mockSession = mock(Mutiny.Session.class);

		given(enrichPipelineService.patchOrUpdateWithItems(
			anySession(), anyLong(), any(PipelineWithItemsDTO.class), eq(false))
		).willReturn(Uni.createFrom().item(new EnrichPipeline()));

		uniAsserter.assertThat(
			() -> datasourceService.updateDatasourceConnection(
				mockSession,
				UpdateDatasourceConnectionDTO.builder()
					.datasourceId(Long.MAX_VALUE)
					.pipelineId(Long.MAX_VALUE)
					.pipeline(PipelineWithItemsDTO.builder()
						.name("mockpipeline")
						.build()
					)
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

	@Test
	@RunOnVertxContext
	void should_create_a_new_dataIndex_with_vectorIndex_when_dataIndexId_is_null_and_vectorIndexDto_is_not_null(
		UniAsserter uniAsserter) {
	}

	@Test
	@RunOnVertxContext
	void should_bind_an_existing_dataIndex_and_update_vectorIndex_when_dataIndexId_is_not_null_and_vectorIndex_exist_and_vectorIndexDto_is_not_null(
		UniAsserter uniAsserter) {
	}

	@Test
	@RunOnVertxContext
	void should_bind_an_existing_dataIndex_and_create_vectorIndex_when_dataIndexId_is_not_null_and_vectorIndex_does_not_exist_and_vectorIndexDto_is_not_null(
		UniAsserter uniAsserter) {
	}

	@Test
	@RunOnVertxContext
	void should_bind_an_existing_dataIndex_and_do_not_modify_vectorIndex_when_dataIndexId_is_not_null_and_vectorIndex_exist_and_vectorIndexDto_is_null(
		UniAsserter uniAsserter) {
	}

	@Test
	@RunOnVertxContext
	void should_bind_an_existing_dataIndex_and_do_not_create_vectorIndex_when_dataIndexId_is_not_null_and_vectorIndex_does_not_exist_and_vectorIndexDto_is_null(
		UniAsserter uniAsserter) {
	}

	// Utils

	private static Mutiny.Session anySession() {
		return any(Mutiny.Session.class);
	}

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

	@Mock
	public static class MockDatasourceService extends DatasourceService {

		MockDatasourceService() {
			super(Mappers.getMapper(DatasourceMapper.class));
		}

		@Override
		public Uni<Datasource> findByIdWithPluginDriver(Mutiny.Session session, long datasourceId) {
			if (datasourceId == Long.MAX_VALUE) {
				return Uni.createFrom().item(new Datasource());
			}
			else {
				return super.findByIdWithPluginDriver(session, datasourceId);
			}
		}

	}
}
