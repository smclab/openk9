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

import java.util.stream.Collectors;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;

import io.openk9.datasource.Initializer;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.dto.DataIndexDTO;
import io.openk9.datasource.model.init.Bucket;
import io.openk9.datasource.web.dto.PluginDriverDocTypesDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DeleteDatasourceTest {

	public static final String DDT_DATASOURCE_CONNECTION = "ddt.DatasourceConnection";
	public static final String DDT_DATA_INDEX = "ddt.DataIndex";

	@Inject
	BucketService bucketService;
	@Inject
	DatasourceService datasourceService;
	@Inject
	EnrichPipelineService enrichPipelineService;
	@Inject
	PluginDriverService pluginDriverService;
	@Inject
	SchedulerService schedulerService;

	@BeforeEach
	void setup() {

		var enrichPipeline = enrichPipelineService.findByName(
				"public",
				Initializer.INIT_DATASOURCE_PIPELINE
			)
			.await().indefinitely();

		var pluginDriver = pluginDriverService.findByName(
				"public",
				Initializer.INIT_DATASOURCE_PLUGIN
			)
			.await().indefinitely();

		var docTypesByPluginDriver = pluginDriverService.getDocTypes(pluginDriver.getId())
			.await().indefinitely()
			.docTypes()
			.stream()
			.map(PluginDriverDocTypesDTO.PluginDriverDocType::docTypeId)
			.collect(Collectors.toSet());

		var datasource = datasourceService.createDatasourceConnection(
			DatasourceConnectionObjects.DATASOURCE_CONNECTION_DTO_BUILDER()
				.name(DDT_DATASOURCE_CONNECTION)
				.pluginDriverId(pluginDriver.getId())
				.pipelineId(enrichPipeline.getId())
				.dataIndex(DataIndexDTO.builder()
					.knnIndex(false)
					.name(DDT_DATA_INDEX)
					.docTypeIds(docTypesByPluginDriver)
					.build()
				)
				.build()
		).await().indefinitely().getEntity();

		var defaultBucket = bucketService.findByName("public", Bucket.INSTANCE.getName())
			.await().indefinitely();

		bucketService.addDatasource(defaultBucket.getId(), datasource.getId())
			.await().indefinitely();

		var dataIndex = datasourceService.getDataIndex(datasource.getId())
			.await().indefinitely();

		Scheduler scheduler = new Scheduler();
		scheduler.setScheduleId("a-random-schedule-id");
		scheduler.setDatasource(datasource);
		scheduler.setOldDataIndex(dataIndex);
		scheduler.setStatus(Scheduler.SchedulerStatus.RUNNING);

		schedulerService.create(scheduler)
			.await().indefinitely();

	}

	@Test
	void should_thrown_validation_exception() {
		var datasource = datasourceService.findByName("public", DDT_DATASOURCE_CONNECTION)
			.await().indefinitely();

		Assertions.assertThrows(
			ValidationException.class,
			() ->
				datasourceService.deleteById(
					datasource.getId(),
					"not-the-same-name.datasourceConnection"
				).await().indefinitely()
		);

	}

}
