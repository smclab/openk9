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

import io.openk9.datasource.mapper.DatasourceMapper;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.util.K9Entity;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.inject.Inject;

@QuarkusTest
abstract class BaseDatasourceServiceTest {

	@Inject
	DatasourceService datasourceService;

	@InjectMock
	PluginDriverService pluginDriverService;

	@InjectMock
	EnrichPipelineService enrichPipelineService;

	@InjectMock
	DataIndexService dataIndexService;

	@InjectMock
	VectorIndexService vectorIndexService;


	@Mock
	public static class MockDatasourceService extends DatasourceService {

		public MockDatasourceService() {
			super(null);
		}

		@Override
		public Uni<Datasource> findById(Mutiny.Session s, long id) {
			return Uni.createFrom().item(() -> {
				if (id == CreateConnection.DATASOURCE_ID) {
					return CreateConnection.DATASOURCE;
				}
				else {
					return null;
				}
			});
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends K9Entity> Uni<T> persist(Mutiny.Session s, T entity) {
			return Uni.createFrom().item((T) CreateConnection.DATASOURCE);
		}

		@Override
		protected Uni<Datasource> getDatasourceConnection(
			Mutiny.Session session, long datasourceId) {

			return Uni.createFrom().item(CreateConnection.DATASOURCE);

		}

		@Inject
		void setDatasourceMapper(DatasourceMapper datasourceMapper) {
			this.mapper = datasourceMapper;
		}

	}


}
