package io.openk9.datasource.mapper;

import io.openk9.datasource.dto.EnrichItemDto;
import io.openk9.datasource.model.EnrichItem;
import org.mapstruct.MappingTarget;

public interface EnrichItemMapper {
	EnrichItem toEnrichItem(EnrichItemDto dto);
	EnrichItemDto toDto(EnrichItem enrichItem);
	EnrichItem update(@MappingTarget EnrichItem enrichItem, EnrichItemDto dto);
}
