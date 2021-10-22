package io.openk9.datasource.mapper;

import io.openk9.datasource.dto.EnrichPipelineDto;
import io.openk9.datasource.model.EnrichPipeline;
import org.mapstruct.MappingTarget;

public interface EnrichPipelineMapper {
	EnrichPipeline toEnrichPipeline(EnrichPipelineDto dto);
	EnrichPipelineDto toDto(EnrichPipeline enrichPipeline);
	EnrichPipeline update(@MappingTarget EnrichPipeline enrichPipeline, EnrichPipelineDto dto);
}
