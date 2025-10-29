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

package io.openk9.tenantmanager.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

import io.openk9.tenantmanager.dto.TenantResponseDTO;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import org.keycloak.admin.client.Keycloak;

@ApplicationScoped
public class DeleteService {

	public static final String DELETE_REALM = "DELETE_REALM";
	public static final String DELETE_SCHEMA = "DELETE_SCHEMA";
	public static final String DELETE_TENANT = "DELETE_TENANT";
	public static final String FIND_TENANT_BY_VIRTUAL_HOST = "FIND_TENANT_BY_VIRTUAL_HOST";

	@Inject
	Keycloak keycloakAdmin;
	@Inject
	DatasourceLiquibaseService datasourceLiquibaseService;
	@Inject
	TenantService tenantService;

	@ConsumeEvent(FIND_TENANT_BY_VIRTUAL_HOST)
	public Uni<TenantResponseDTO> findTenant(String virtualHost) {
		return tenantService.findTenantByVirtualHost(virtualHost);
	}

	@ActivateRequestContext
	@ConsumeEvent(DELETE_REALM)
	@Blocking
	public int deleteRealm(String realmName) {

		keycloakAdmin.realm(realmName).remove();

		return 0;
	}

	@ConsumeEvent(DELETE_SCHEMA)
	@Blocking
	public int deleteSchema(String schemaName) {

		datasourceLiquibaseService.rollbackRunLiquibaseMigration(schemaName);

		return 0;
	}

	@ConsumeEvent(DELETE_TENANT)
	public Uni<Void> deleteTenant(long tenantId) {

		return tenantService.deleteTenant(tenantId);

	}

}
