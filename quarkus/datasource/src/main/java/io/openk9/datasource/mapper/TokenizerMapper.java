package io.openk9.datasource.mapper;

import io.openk9.datasource.model.Tokenizer;
import io.openk9.datasource.model.dto.TokenizerDTO;
import org.mapstruct.Mapper;

@Mapper(
	config = K9EntityMapper.class
)
public interface TokenizerMapper extends K9EntityMapper<Tokenizer, TokenizerDTO> {
}
