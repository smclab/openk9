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

import io.openk9.datasource.mapper.DataIndexMapper;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.plugindriver.WireMockPluginDriver;
import io.openk9.datasource.processor.indexwriter.IndexerEvents;
import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@QuarkusTest
@QuarkusTestResource(WireMockPluginDriver.class)
class DynamicMappingDataIndexTest {

	@Inject
	DataIndexService dataIndexService;

	@InjectMock
	IndexerEvents indexerEvents;

	@InjectSpy
	HttpPluginDriverClient httpPluginDriverClient;

	@Test
	@RunOnVertxContext
	void should_create_dynamicMapping_and_docTypes(UniAsserter asserter) {

		var useless = Mockito.mock(Mutiny.Session.class);

		given(indexerEvents.generateDocTypeFields(
			any(Mutiny.Session.class),
			any(DataIndex.class),
			any(Map.class),
			any(List.class)
		)).willReturn(Uni.createFrom().voidItem());

		asserter.assertThat(
			() -> dataIndexService.createByDatasource(useless, CreateConnection.DATASOURCE),
			dataIndex -> {

				then(httpPluginDriverClient)
					.should(times(1))
					.getSample(any());

				then(indexerEvents)
					.should(times(1))
					.generateDocTypeFields(
						any(Mutiny.Session.class),
						any(DataIndex.class),
						argThat(DynamicMappingDataIndexTest::isAnIndexMapping),
						argThat(DynamicMappingDataIndexTest::isADocumentTypeList)
					);

			}
		);


	}

	@Mock
	public static class MockDataIndexService extends DataIndexService {

		MockDataIndexService() {
			super(null);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends K9Entity> Uni<T> persist(Mutiny.Session session, T entity) {
			return Uni.createFrom().item((T) CreateConnection.DATAINDEX);
		}

		@Override
		public Uni<DataIndex> findById(Mutiny.Session s, long id) {
			return Uni.createFrom().item(CreateConnection.DATAINDEX);
		}

		@Inject
		void setDataIndexMapper(DataIndexMapper mapper) {
			this.mapper = mapper;
		}

	}

	private static boolean isAnIndexMapping(Map<String, Object> map) {
		return map != null && !map.isEmpty() && map.containsKey("properties");
	}

	private static boolean isADocumentTypeList(List<String> list) {
		return list != null && !list.isEmpty() && list.contains("sample");
	}

}