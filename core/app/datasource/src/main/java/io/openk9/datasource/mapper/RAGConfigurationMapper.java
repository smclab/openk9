package io.openk9.datasource.mapper;

import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.dto.RAGConfigurationDTO;
import org.mapstruct.Mapper;

@Mapper(
	config = K9EntityMapper.class
)
public interface RAGConfigurationMapper
	extends K9EntityMapper<RAGConfiguration, RAGConfigurationDTO> {
}
