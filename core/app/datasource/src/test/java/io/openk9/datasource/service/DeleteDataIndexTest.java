/*
 * Copyright (C) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.validation.ValidationException;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.Initializer;
import io.openk9.datasource.model.dto.base.DataIndexDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DeleteDataIndexTest {

	private static final String DIST_DATA_INDEX = "dist.dataIndex";
	private static final String DIST_DATASOURCE = "dist.datasource";

	@Inject
	DataIndexService dataIndexService;

	@Inject
	DatasourceService datasourceService;

	@Inject
	PluginDriverService pluginDriverService;

	@Inject
	Mutiny.SessionFactory sf;

	@BeforeEach
	void setup() {
		var pluginDriver = pluginDriverService.findByName(
			"public",
			Initializer.INIT_DATASOURCE_PLUGIN
		).await().indefinitely();

		var datasource = datasourceService.createDatasourceConnection(
				DatasourceConnectionObjects.DATASOURCE_CONNECTION_DTO_BUILDER()
					.pluginDriverId(pluginDriver.getId())
					.name(DIST_DATASOURCE)
					.dataIndex(DataIndexDTO.builder()
						.knnIndex(false)
						.name(DIST_DATA_INDEX)
						.build()
					)
					.build()
			)
			.await()
			.indefinitely()
			.getEntity();

		datasourceService.unsetDataIndex(datasource.getId())
			.await()
			.indefinitely();
	}

	@Test
	@DisplayName("Should throw Validation exception deleting DataIndex with a different name")
	void should_fail_deleting_with_different_name() {
		var dataIndex = EntitiesUtils.getEntity(DIST_DATA_INDEX, dataIndexService, sf);

		assertThrows(
			ValidationException.class,
			() -> dataIndexService.deleteById(
				dataIndex.getId(),
				"not-the-same-name.dataIndex")
				.await()
				.indefinitely()
		);
	}

	@Test
	@DisplayName("Should delete DataIndex with the same name")
	void should_delete_with_correct_name() {
		var dataIndex = EntitiesUtils.getEntity(DIST_DATA_INDEX, dataIndexService, sf);

		dataIndexService.deleteById(dataIndex.getId(), DIST_DATA_INDEX)
			.await()
			.indefinitely();

		assertThrows(
			NoResultException.class,
			() -> EntitiesUtils.getEntity(DIST_DATA_INDEX, dataIndexService, sf)
		);
	}

	@AfterEach
	void tearDown() {
		try {
			EntitiesUtils.removeEntity(DIST_DATA_INDEX, dataIndexService, sf);
		}
		catch (Exception ignored) {
		}
		finally {
			EntitiesUtils.removeEntity(DIST_DATASOURCE, datasourceService, sf);
		}
	}
}
