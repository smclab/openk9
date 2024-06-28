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

import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.EnrichPipelineItem;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.service.dto.EnrichItemDTO;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "cdi")
public interface SchedulerMapper {

	@Mappings({
		@Mapping(source = "datasource.id", target = "datasourceId"),
		@Mapping(source = "datasource.enrichPipeline", target = "enrichItems"),
		@Mapping(source = "oldDataIndex.id", target = "oldDataIndexId"),
		@Mapping(source = "oldDataIndex.name", target = "oldDataIndexName"),
		@Mapping(source = "newDataIndex.id", target = "newDataIndexId"),
		@Mapping(source = "newDataIndex.name", target = "newDataIndexName"),
		@Mapping(
			source = "oldDataIndex.vectorIndex.name",
			target = "vectorIndexName"
		)
	})
	SchedulerDTO map(Scheduler source);

	EnrichItemDTO map(EnrichItem source);

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
