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

package io.openk9.datasource.pipeline.service.mapper;

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.service.dto.SchedulingType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The kind of scheduling follows the dataIndex the documents are written to.
 */
class SchedulerMapperTest {

	private final SchedulerMapper mapper = new SchedulerMapperImpl();

	@Test
	@DisplayName("Should type a first indexing from the dataIndex being created")
	void should_type_a_first_indexing_from_the_new_data_index() {
		// a datasource that has never been indexed does not point at a dataIndex
		var datasource = new Datasource();

		var scheduler = new Scheduler();
		scheduler.setDatasource(datasource);
		scheduler.setNewDataIndex(dataIndex(true));

		// the knn dataIndex that is being created decides
		assertEquals(SchedulingType.EMBEDDING, mapper.map(scheduler));

		// and it still enriches when the datasource has a pipeline
		datasource.setEnrichPipeline(new EnrichPipeline());

		assertEquals(SchedulingType.ENRICH_EMBEDDING, mapper.map(scheduler));
	}

	@Test
	@DisplayName("Should not embed when the dataIndex being created is not knn")
	void should_not_embed_when_the_new_data_index_is_not_knn() {
		var datasource = new Datasource();

		var scheduler = new Scheduler();
		scheduler.setDatasource(datasource);
		scheduler.setNewDataIndex(dataIndex(false));

		assertEquals(SchedulingType.ENRICH, mapper.map(scheduler));
	}

	@Test
	@DisplayName("Should type an ordinary scheduling from the current dataIndex")
	void should_type_an_ordinary_scheduling_from_the_current_data_index() {
		// no dataIndex is being created: the one the datasource points at is used
		var datasource = new Datasource();
		datasource.setDataIndex(dataIndex(true));

		var scheduler = new Scheduler();
		scheduler.setDatasource(datasource);

		assertEquals(SchedulingType.EMBEDDING, mapper.map(scheduler));
	}

	private static DataIndex dataIndex(boolean knnIndex) {
		var dataIndex = new DataIndex();
		dataIndex.setKnnIndex(knnIndex);

		return dataIndex;
	}

}
