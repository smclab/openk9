package io.openk9.datasource.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
	unmappedTargetPolicy = ReportingPolicy.IGNORE,
	componentModel = "cdi"
)
public interface EnrichPipelineNullAwareMapper extends EnrichPipelineMapper{
}
