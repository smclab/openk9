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

import io.openk9.datasource.actor.EventBusInstanceHolder;
import io.openk9.datasource.index.IndexMappingService;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;
import io.openk9.datasource.plugindriver.WireMockPluginDriver;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import io.vertx.mutiny.core.eventbus.Message;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@QuarkusTest
@QuarkusTestResource(WireMockPluginDriver.class)
class DynamicMappingDataIndexTest {

	public static final String TENANT_ID = "public";
	private static final Logger log = Logger.getLogger(DynamicMappingDataIndexTest.class);

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
				indexMappingService.generateDocTypeFieldsFromPluginDriverSampleUser(
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

	@Test
	void should_create_docTypes_from_event_bus_message() {
		Message<Object> responseMessage = EventBusInstanceHolder.getEventBus()
			.request(
				IndexMappingService.GENERATE_DOC_TYPE,
				new IndexMappingService.GenerateDocTypeFromPluginSampleMessage(
					TENANT_ID,
					HttpPluginDriverInfo.builder()
						.baseUri(WireMockPluginDriver.HOST + ":" + WireMockPluginDriver.PORT)
						.secure(false)
						.build()
				)
			)
			.await()
			.indefinitely();

		var docTypes = (Set<DocType>) responseMessage.body();

		assertNotNull(docTypes);
		assertFalse(docTypes.isEmpty());
		assertTrue(
			docTypes.stream().anyMatch(docType ->
				docType.getName().equalsIgnoreCase("sample")
			)
		);
	}

	private static boolean isAnIndexMapping(Map<String, Object> map) {
		return map != null && !map.isEmpty() && map.containsKey("properties");
	}

	private static boolean isADocumentTypeList(List<String> list) {
		return list != null && !list.isEmpty() && list.contains("sample");
	}

}