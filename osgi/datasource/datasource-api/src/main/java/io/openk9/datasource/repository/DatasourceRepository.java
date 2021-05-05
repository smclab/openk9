/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.repository;

import io.openk9.model.Datasource;
import io.openk9.model.DatasourceContext;
import io.openk9.sql.api.client.Page;
import io.openk9.sql.api.entity.ReactiveRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface DatasourceRepository extends
	ReactiveRepository<Datasource, Long> {

	public Mono<Datasource> removeDatasource(Long datasourceId);
	public Mono<Datasource> findByName(String name);
	public Flux<Datasource> findByTenantId(Long tenantId);
	public Flux<Datasource> findByTenantIdAndIsActive(Long tenantId);
	public Flux<Datasource> findByTenantIdAndNotActive(Long tenantId);
	public Flux<Datasource> findByTenantId(Long tenantId, Page page);
	public Mono<Datasource> addDatasource(Datasource datasource);
	public Mono<Datasource> addDatasource(Boolean active, String description, String jsonConfig, Instant lastIngestionDate, String name, Long tenantId, String scheduling, String driverServiceName);
	public Mono<Datasource> updateDatasource(Datasource datasource);
	public Mono<Void> updateLastIngestionDate(
		Long datasourceId, Instant lastIngestionDate);
	public Mono<Datasource> updateDatasource(Boolean active, Long datasourceId, String description, String jsonConfig, Instant lastIngestionDate, String name, Long tenantId, String scheduling, String driverServiceName);
	public Mono<Datasource> removeDatasource(Datasource datasource);
	public Mono<Datasource> findByPrimaryKey(Long datasourceId);
	public Flux<Datasource> findAll();

	public Flux<Datasource> findAll(boolean enabled);

	public Mono<DatasourceContext> findContext(Long datasourceId);

}