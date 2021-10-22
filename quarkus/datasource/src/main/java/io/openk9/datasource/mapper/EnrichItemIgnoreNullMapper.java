package io.openk9.datasource.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
	unmappedTargetPolicy = ReportingPolicy.IGNORE,
	componentModel = "cdi",
	nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EnrichItemIgnoreNullMapper extends EnrichItemMapper {
}
