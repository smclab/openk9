package io.openk9.datasource.pipeline.actor.mapper;

import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.EnrichPipelineItem;
import io.openk9.datasource.pipeline.actor.dto.GetDatasourceDTO;
import io.openk9.datasource.pipeline.actor.dto.GetEnrichItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "cdi")
public interface DatasourceMapper {

	@Mapping(source = "dataIndex.name", target = "dataIndexName")
	@Mapping(source = "enrichPipeline", target = "enrichItems")
	GetDatasourceDTO map(Datasource datasource);

	default Set<GetEnrichItemDTO> map(EnrichPipeline enrichPipeline) {
		if (enrichPipeline == null) {
			return Set.of();
		}
		else {
			return enrichPipeline
				.getEnrichPipelineItems()
				.stream()
				.map(this::map)
				.collect(Collectors.toSet());
		}
	}

	@Mapping(source = "enrichItem", target = ".")
	GetEnrichItemDTO map(EnrichPipelineItem enrichPipelineItem);

	GetEnrichItemDTO map(EnrichItem enrichItem);

}
