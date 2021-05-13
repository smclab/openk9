package io.openk9.datasource.client.api;

import io.openk9.model.Datasource;
import io.openk9.model.Tenant;
import io.openk9.model.TenantDatasource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DatasourceClient {

	Mono<TenantDatasource> findTenantAndDatasourceByDatasourceId(
		long datasourceId);

	Mono<Datasource> findDatasource(long datasourceId);

	Flux<Datasource> findDatasourceByTenantIdAndIsActive(long tenantId);

	Flux<Datasource> findDatasourceByTenantId(long tenantId);

	Flux<Tenant> findTenantByVirtualHost(String virtualHost);

	Mono<Tenant> findTenant(long tenantId);

}
