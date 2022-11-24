package io.openk9.datasource.mapper;

import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.dto.TabDTO;
import org.mapstruct.Mapper;

@Mapper(
	config = K9EntityMapper.class
)
public interface TabMapper extends K9EntityMapper<Tab, TabDTO> {
}
