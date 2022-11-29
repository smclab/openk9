package io.openk9.tenantmanager.grpc;

import com.google.protobuf.Empty;
import io.openk9.tenantmanager.mapper.TenantMapper;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.service.TenantService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class TenantManagerGrpcService implements TenantManager {
	@Override
	@ActivateRequestContext
	public Uni<TenantResponse> findTenant(TenantRequest request) {

		Uni<Tenant> tenantUni = tenantService.findTenantByVirtualHost(
			request.getVirtualHost());

		return tenantUni
			.onItem()
			.ifNotNull()
			.transform(tenantMapper::toTenantResponse);

	}

	@Override
	public Uni<TenantListResponse> findTenantList(Empty request) {
		Uni<List<Tenant>> tenantUni = tenantService.findAllTenant();

		return tenantUni
			.onItem()
			.ifNotNull()
			.transform(list ->
					list
						.stream()
						.map(tenantMapper::toTenantResponse)
						.collect(
							Collectors.collectingAndThen(
								Collectors.toList(),
								collect -> TenantListResponse.newBuilder()
									.addAllTenantResponse(collect)
									.build()
							)
						)
			);
	}

	@Inject
	TenantService tenantService;

	@Inject
	TenantMapper tenantMapper;

}
