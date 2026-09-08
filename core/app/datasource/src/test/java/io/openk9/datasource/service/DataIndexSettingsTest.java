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

import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.IndexTemplateUtils;
import io.openk9.datasource.Initializer;
import io.openk9.datasource.model.dto.base.DataIndexDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensearch.client.RestHighLevelClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The index settings requested when a dataIndex is created are persisted
 * state: the service reads them off the entity, whoever the caller is.
 */
@QuarkusTest
class DataIndexSettingsTest {

	private static final String CUSTOM_SETTINGS =
		"{\"index\": {\"number_of_replicas\": 2}}";
	private static final String DATA_INDEX = "dist.custom-settings-data-index";
	private static final String REPLICAS_SETTING = "index.number_of_replicas";
	private static final String TENANT_ID = "public";

	@Inject
	DataIndexService dataIndexService;

	@Inject
	DatasourceService datasourceService;

	@Inject
	RestHighLevelClient restHighLevelClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@AfterEach
	void tearDown() {
		try {
			var dataIndex =
				EntitiesUtils.getEntity(DATA_INDEX, dataIndexService, sessionFactory);

			dataIndexService.deleteById(dataIndex.getId()).await().indefinitely();
		}
		catch (Exception ignored) {
		}
	}

	@Test
	@DisplayName("Should store the requested settings and apply them to the template")
	void should_persist_the_requested_index_settings() {
		// a dataIndex is created with custom settings
		var datasource = EntitiesUtils.getEntity(
			Initializer.INIT_DATASOURCE_CONNECTION, datasourceService, sessionFactory);

		sessionFactory.withTransaction((s, t) -> dataIndexService.create(
				s,
				datasource.getId(),
				DataIndexDTO.builder()
					.name(DATA_INDEX)
					.knnIndex(false)
					.settings(CUSTOM_SETTINGS)
					.build()
			))
			.await()
			.indefinitely();

		// the settings are readable back from the entity
		var reloaded =
			EntitiesUtils.getEntity(DATA_INDEX, dataIndexService, sessionFactory);

		assertEquals(CUSTOM_SETTINGS, reloaded.getSettings());

		// and they are the settings the index template declares
		var indexTemplate = IndexTemplateUtils.getIndexTemplate(
			restHighLevelClient, TENANT_ID, DATA_INDEX);

		assertNotNull(indexTemplate);
		assertEquals(
			"2", indexTemplate.template().settings().get(REPLICAS_SETTING));
	}

}
