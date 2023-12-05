package io.openk9.datasource.pipeline.actor.mapper;

import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.EnrichPipelineItem;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.dto.EnrichItemDTO;
import io.openk9.datasource.pipeline.actor.dto.SchedulerDTO;
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
