package io.openk9.datasource.client.api;

import io.openk9.model.Datasource;
import io.openk9.model.Tenant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DatasourceClient {

	Mono<Datasource> findDatasource(long datasourceId);

	Flux<Datasource> findByTenantIdAndIsActive(long tenantId);

	Flux<Datasource> findByTenantId(long tenantId);

	Flux<Tenant> findByVirtualHost(String virtualHost);

}
