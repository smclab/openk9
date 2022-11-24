package io.openk9.datasource.mapper;

import io.openk9.datasource.model.CharFilter;
import io.openk9.datasource.model.dto.CharFilterDTO;
import org.mapstruct.Mapper;

@Mapper(
	config = K9EntityMapper.class
)
public interface CharFilterMapper extends K9EntityMapper<CharFilter, CharFilterDTO> {
}
