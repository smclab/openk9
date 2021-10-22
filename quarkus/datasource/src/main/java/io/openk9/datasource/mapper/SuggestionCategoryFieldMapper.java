package io.openk9.datasource.mapper;

import io.openk9.datasource.dto.SuggestionCategoryFieldDto;
import io.openk9.datasource.model.SuggestionCategoryField;
import org.mapstruct.MappingTarget;

public interface SuggestionCategoryFieldMapper {
	SuggestionCategoryField toSuggestionCategoryField(SuggestionCategoryFieldDto dto);
	SuggestionCategoryFieldDto toDto(SuggestionCategoryField suggestionCategoryField);
	SuggestionCategoryField update(@MappingTarget SuggestionCategoryField suggestionCategoryField, SuggestionCategoryFieldDto dto);
}
