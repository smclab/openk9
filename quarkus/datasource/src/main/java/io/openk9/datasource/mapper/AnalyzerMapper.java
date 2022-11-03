package io.openk9.datasource.mapper;

import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.dto.AnalyzerDTO;
import org.mapstruct.Mapper;

@Mapper(
	config = K9EntityMapper.class
)
public interface AnalyzerMapper extends K9EntityMapper<Analyzer, AnalyzerDTO> {

}
