package io.openk9.datasource.mapper;

import io.openk9.datasource.dto.TenantDto;
import io.openk9.datasource.model.Tenant;
import org.mapstruct.MappingTarget;

public interface TenantMapper {
	Tenant toTenant(TenantDto dto);
	TenantDto toDto(Tenant tenant);
	Tenant update(@MappingTarget Tenant tenant, TenantDto dto);
}
