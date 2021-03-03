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

package com.openk9.datasource.repository;

import com.openk9.datasource.model.Tenant;
import com.openk9.sql.api.client.Page;
import com.openk9.sql.api.entity.ReactiveRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TenantRepository extends
	ReactiveRepository<Tenant, Long> {

	public Mono<Tenant> removeTenant(Long tenantId);
	public Mono<Tenant> findByVirtualHost(String virtualHost);
	public Mono<Tenant> addTenant(Tenant tenant);
	public Mono<Tenant> addTenant(String name, String virtualHost);
	public Mono<Tenant> updateTenant(Tenant tenant);
	public Mono<Tenant> updateTenant(String name, Long tenantId, String virtualHost);
	public Mono<Tenant> removeTenant(Tenant tenant);
	public Mono<Tenant> findByPrimaryKey(Long tenantId);
	public Flux<Tenant> findAll();
	public Flux<Tenant> findAll(Page page);

}