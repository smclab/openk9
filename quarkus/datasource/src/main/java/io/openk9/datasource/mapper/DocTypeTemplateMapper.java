package io.openk9.datasource.mapper;

import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.dto.DocTypeTemplateDTO;
import io.openk9.datasource.model.dto.TabDTO;
import org.mapstruct.Mapper;

@Mapper(
	config = K9EntityMapper.class
)
public interface DocTypeTemplateMapper extends K9EntityMapper<DocTypeTemplate, DocTypeTemplateDTO> {
}

