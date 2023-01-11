package io.openk9.tenantmanager.mapper;

import io.openk9.tenantmanager.dto.TenantDTO;
import io.openk9.tenantmanager.grpc.TenantResponse;
import io.openk9.tenantmanager.model.Tenant;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(
	componentModel = "cdi"
)
public interface TenantMapper {

	@BeanMapping(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	TenantResponse toTenantResponse(Tenant tenant);

	Tenant map(TenantDTO tenantDTO);
	@BeanMapping(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	Tenant patch(TenantDTO tenantDTO);

	Tenant map(@MappingTarget Tenant tenant, TenantDTO tenantDTO);
	@BeanMapping(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
	Tenant patch(@MappingTarget Tenant tenant, TenantDTO tenantDTO);

}
