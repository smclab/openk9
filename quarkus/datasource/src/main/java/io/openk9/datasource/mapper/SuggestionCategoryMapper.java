package io.openk9.datasource.mapper;

import io.openk9.datasource.dto.SuggestionCategoryDto;
import io.openk9.datasource.model.SuggestionCategory;
import org.mapstruct.MappingTarget;

public interface SuggestionCategoryMapper {
	SuggestionCategory toSuggestionCategory(SuggestionCategoryDto dto);
	SuggestionCategoryDto toDto(SuggestionCategory suggestionCategory);
	SuggestionCategory update(@MappingTarget SuggestionCategory suggestionCategory, SuggestionCategoryDto dto);
}
