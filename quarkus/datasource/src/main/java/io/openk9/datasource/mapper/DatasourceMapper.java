package io.openk9.datasource.mapper;

import io.openk9.datasource.dto.DatasourceDto;
import io.openk9.datasource.model.Datasource;
import org.mapstruct.MappingTarget;

public interface DatasourceMapper {
	Datasource toDatasource(DatasourceDto dto);
	DatasourceDto toDto(Datasource datasource);
	Datasource update(@MappingTarget Datasource datasource, DatasourceDto dto);
}