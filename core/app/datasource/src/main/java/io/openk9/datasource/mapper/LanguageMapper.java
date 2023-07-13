package io.openk9.datasource.mapper;

import io.openk9.datasource.model.Language;
import io.openk9.datasource.model.dto.LanguageDTO;
import org.mapstruct.Mapper;

@Mapper(
	config = K9EntityMapper.class
)
public interface LanguageMapper extends
	K9EntityMapper<Language, LanguageDTO>{
}
