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
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Map;
import jakarta.inject.Inject;

import io.openk9.datasource.index.IndexMappingService;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;
import io.openk9.datasource.plugindriver.WireMockPluginDriver;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(WireMockPluginDriver.class)
class DynamicMappingDataIndexTest {

	@InjectSpy
	IndexMappingService indexMappingService;

	@InjectSpy
	HttpPluginDriverClient httpPluginDriverClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	@RunOnVertxContext
	void should_create_dynamicMapping_and_docTypes(UniAsserter asserter) {

		asserter.assertThat(
			() -> sessionFactory.withTransaction((s, t) ->
				indexMappingService.generateDocTypeFieldsFromPluginDriverSample(
					s,
					HttpPluginDriverInfo.builder()
						.baseUri(WireMockPluginDriver.HOST + ":" + WireMockPluginDriver.PORT)
						.secure(false)
						.build()
				)
			),
			dataIndex -> {

				then(httpPluginDriverClient)
					.should(times(1))
					.getSample(any());

				then(indexMappingService)
					.should(times(1))
					.generateDocTypeFields(
						any(Mutiny.Session.class),
						argThat(DynamicMappingDataIndexTest::isAnIndexMapping),
						argThat(DynamicMappingDataIndexTest::isADocumentTypeList)
					);

			}
		);

	}

	private static boolean isAnIndexMapping(Map<String, Object> map) {
		return map != null && !map.isEmpty() && map.containsKey("properties");
	}

	private static boolean isADocumentTypeList(List<String> list) {
		return list != null && !list.isEmpty() && list.contains("sample");
	}

}