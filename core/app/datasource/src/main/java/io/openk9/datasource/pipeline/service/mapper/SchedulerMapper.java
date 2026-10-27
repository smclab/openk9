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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.EnrichPipelineItem;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.service.dto.EnrichItemDTO;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.service.dto.SchedulingType;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "cdi")
public interface SchedulerMapper {

	@Mappings({
		@Mapping(source = "scheduler.datasource.id", target = "datasourceId"),
		@Mapping(source = "scheduler.datasource.enrichPipeline", target = "enrichItems"),
		@Mapping(source = "scheduler.oldDataIndex.id", target = "oldDataIndexId"),
		@Mapping(source = "scheduler.oldDataIndex.name", target = "oldDataIndexName"),
		@Mapping(source = "scheduler.newDataIndex.id", target = "newDataIndexId"),
		@Mapping(source = "scheduler.newDataIndex.name", target = "newDataIndexName"),
		@Mapping(source = "scheduler", target = "schedulingType"),
		@Mapping(source = "tenantId", target = "tenantId")
	})
	SchedulerDTO map(Scheduler scheduler, String tenantId);

	EnrichItemDTO map(EnrichItem source);

	/**
	 * Chooses the kind of scheduling from the dataIndex the documents are
	 * written to: the one being created when there is one, because a
	 * datasource that has never been indexed does not point at any yet.
	 *
	 * @param scheduler the scheduler to choose the scheduling for
	 * @return the kind of scheduling
	 */
	default SchedulingType map(Scheduler scheduler) {

		var dataIndex = scheduler.getNewDataIndex() != null
			? scheduler.getNewDataIndex()
			: scheduler.getDatasource().getDataIndex();

		return schedulingTypeOf(scheduler.getDatasource(), dataIndex);
	}

	/**
	 * Chooses the kind of scheduling from the dataIndex the datasource
	 * currently points at.
	 *
	 * @param datasource the datasource to choose the scheduling for
	 * @return the kind of scheduling
	 */
	default SchedulingType map(Datasource datasource) {
		return schedulingTypeOf(datasource, datasource.getDataIndex());
	}

	private static SchedulingType schedulingTypeOf(
		Datasource datasource, DataIndex dataIndex) {

		boolean isEnrich = datasource.getEnrichPipeline() != null;
		boolean isEmbedding = dataIndex != null
			&& dataIndex.getKnnIndex() != null && dataIndex.getKnnIndex();

		if (isEnrich && isEmbedding) {
			return SchedulingType.ENRICH_EMBEDDING;
		}
		else if (isEmbedding) {
			return SchedulingType.EMBEDDING;
		}
		else {
			return SchedulingType.ENRICH;
		}

	}

	default Set<EnrichItemDTO> map(EnrichPipeline source) {
		return source == null
			? Set.of()
			: source
				.getEnrichPipelineItems()
				.stream()
				.map(EnrichPipelineItem::getEnrichItem)
				.map(this::map)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
