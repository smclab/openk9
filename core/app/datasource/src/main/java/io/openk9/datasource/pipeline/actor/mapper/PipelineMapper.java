package io.openk9.datasource.pipeline.actor.mapper;

import io.openk9.datasource.model.*;
import io.openk9.datasource.pipeline.actor.dto.GetDatasourceDTO;
import io.openk9.datasource.pipeline.actor.dto.GetEnrichItemDTO;
import io.openk9.datasource.pipeline.actor.dto.SchedulerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "cdi")
public interface PipelineMapper {

	@Mapping(source = "enrichPipeline", target = "enrichItems")
	GetDatasourceDTO map(Datasource datasource);

	@Mapping(source = "scheduleId.value", target = "scheduleId")
	@Mapping(source = "datasource.id", target = "datasourceId")
	@Mapping(source = "oldDataIndex.name", target = "oldDataIndexName")
	@Mapping(source = "newDataIndex.name", target = "newDataIndexName")
	SchedulerDTO map(Scheduler scheduler);

	default Collection<GetEnrichItemDTO> map(EnrichPipeline enrichPipeline) {
		if (enrichPipeline == null) {
			return List.of();
		}
		else {
			return enrichPipeline
				.getEnrichPipelineItems()
				.stream()
				.sorted(Comparator.comparing(EnrichPipelineItem::getWeight))
				.map(this::map)
				.toList();
		}
	}

	@Mapping(source = "enrichItem", target = ".")
	GetEnrichItemDTO map(EnrichPipelineItem enrichPipelineItem);

	GetEnrichItemDTO map(EnrichItem enrichItem);

}
