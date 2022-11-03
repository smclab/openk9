package io.openk9.datasource.mapper;

import io.openk9.datasource.model.TokenFilter;
import io.openk9.datasource.model.dto.TokenFilterDTO;
import org.mapstruct.Mapper;

@Mapper(
	config = K9EntityMapper.class
)
public interface TokenFilterMapper extends K9EntityMapper<TokenFilter, TokenFilterDTO> {
}
