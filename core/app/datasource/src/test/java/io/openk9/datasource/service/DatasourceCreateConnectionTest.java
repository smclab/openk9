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
import io.openk9.datasource.mapper.DatasourceMapper;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.service.exception.K9Error;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@QuarkusTest
class DatasourceCreateConnectionTest {

	@InjectMock
	PluginDriverService pluginDriverService;

	@InjectMock
	EnrichPipelineService enrichPipelineService;

	@InjectMock
	DataIndexService dataIndexService;

	@Inject
	MockDatasourceService datasourceService;

	@Test
	@RunOnVertxContext
	void should_create_everything(UniAsserter asserter) {

		asserter.assertThat(
			() -> datasourceService.createDatasourceConnection(CreateConnection.NEW_ENTITIES_DTO),
			response -> {
				then(pluginDriverService)
					.should(times(1))
					.create(
						any(Mutiny.Session.class),
						eq(CreateConnection.NEW_ENTITIES_DTO.getPluginDriver())
					);

				then(enrichPipelineService)
					.should(times(1))
					.create(
						any(Mutiny.Session.class),
						eq(CreateConnection.NEW_ENTITIES_DTO.getPipeline())
					);

				then(dataIndexService)
					.should(times(1))
					.createByDatasource(any(Mutiny.Session.class), any(Datasource.class));
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_create_plugin_and_dataIndex(UniAsserter asserter) {

		asserter.assertThat(
			() -> datasourceService.createDatasourceConnection(
				CreateConnection.NEW_PLUGIN_PRE_EXIST_PIPELINE_DTO),
			response -> {
				then(pluginDriverService)
					.should(times(1))
					.create(
						any(Mutiny.Session.class),
						eq(CreateConnection.NEW_PLUGIN_PRE_EXIST_PIPELINE_DTO.getPluginDriver())
					);

				then(enrichPipelineService)
					.should(times(1))
					.findById(
						any(Mutiny.Session.class),
						eq(CreateConnection.PIPELINE_ID)
					);

				then(enrichPipelineService).shouldHaveNoMoreInteractions();

				then(dataIndexService)
					.should(times(1))
					.createByDatasource(any(Mutiny.Session.class), any(Datasource.class));
			}
		);

	}


	@Test
	@RunOnVertxContext
	void should_create_pipeline_dataIndex(UniAsserter asserter) {

		asserter.assertThat(
			() -> datasourceService.createDatasourceConnection(
				CreateConnection.PRE_EXIST_PLUGIN_NEW_PIPELINE_DTO),
			response -> {

				then(pluginDriverService).should(times(1)).findById(
					any(Mutiny.Session.class),
					eq(CreateConnection.PRE_EXIST_PLUGIN_NEW_PIPELINE_DTO.getPluginDriverId())
				);

				then(pluginDriverService).shouldHaveNoMoreInteractions();

				then(enrichPipelineService)
					.should(times(1))
					.create(any(Mutiny.Session.class), any(PipelineWithItemsDTO.class));

				then(dataIndexService)
					.should(times(1))
					.createByDatasource(any(Mutiny.Session.class), any(Datasource.class));
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_not_associate_any_pipeline(UniAsserter asserter) {

		asserter.assertThat(
			() -> datasourceService.createDatasourceConnection(CreateConnection.NEW_PLUGIN_NO_PIPELINE_DTO),
			response -> {
				then(pluginDriverService)
					.should(times(1))
					.create(
						any(Mutiny.Session.class),
						eq(CreateConnection.NEW_PLUGIN_NO_PIPELINE_DTO.getPluginDriver())
					);

				then(enrichPipelineService).shouldHaveNoInteractions();

				then(dataIndexService)
					.should(times(1))
					.createByDatasource(any(Mutiny.Session.class), any(Datasource.class));
			}
		);

	}

	@Test
	@RunOnVertxContext
	void should_fail_with_validation_exception_when_ambiguousDto(UniAsserter asserter) {

		asserter.assertThat(
			() -> datasourceService.createDatasourceConnection(CreateConnection.AMBIGUOUS_DTO),
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
			() -> datasourceService.createDatasourceConnection(CreateConnection.NO_PLUGIN_NO_PIPELINE_DTO),
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
	void should_fail_with_K9Error_when_transaction_exception(UniAsserter asserter) {

		given(enrichPipelineService
			.create(any(Mutiny.Session.class), any(PipelineWithItemsDTO.class)))
			.willReturn(Uni.createFrom().failure(RuntimeException::new));

		asserter.assertFailedWith(
			() -> datasourceService.createDatasourceConnection(CreateConnection.NEW_ENTITIES_DTO),
			failure -> {

				Assertions.assertInstanceOf(K9Error.class, failure);

				then(pluginDriverService).should(times(1)).create(
					any(Mutiny.Session.class),
					eq(CreateConnection.NEW_ENTITIES_DTO.getPluginDriver())
				);

				then(enrichPipelineService).should(times(1)).create(
					any(Mutiny.Session.class),
					eq(CreateConnection.NEW_ENTITIES_DTO.getPipeline())
				);

				then(dataIndexService).shouldHaveNoInteractions();
			}
		);

	}

	@Mock
	public static final class MockDatasourceService extends DatasourceService {

		public MockDatasourceService() {
			super(null);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends K9Entity> Uni<T> persist(Mutiny.Session session, T entity) {
			return Uni.createFrom().item((T) CreateConnection.DATASOURCE);
		}

		@Inject
		void setDatasourceMapper(DatasourceMapper datasourceMapper) {
			this.mapper = datasourceMapper;
		}

	}
}