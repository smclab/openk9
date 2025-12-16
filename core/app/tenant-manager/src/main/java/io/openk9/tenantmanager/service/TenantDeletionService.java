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
import jakarta.inject.Inject;

import io.openk9.tenantmanager.dto.TenantResponseDTO;

import io.quarkus.vertx.ConsumeEvent;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;

@ApplicationScoped
public class TenantDeletionService {

	public static final String CREATE_REALM = "CREATE_REALM";
	public static final String CREATE_SCHEMA = "CREATE_SCHEMA";
	public static final String CREATE_TENANT = "CREATE_TENANT";
	public static final String DELETE_REALM = "DELETE_REALM";
	public static final String DELETE_SCHEMA = "DELETE_SCHEMA";
	public static final String DELETE_TENANT = "DELETE_TENANT";
	public static final String FIND_TENANT_BY_VIRTUAL_HOST = "FIND_TENANT_BY_VIRTUAL_HOST";

	private static final Logger log = Logger.getLogger(TenantDeletionService.class);

	@Inject
	Keycloak keycloakAdmin;
	@Inject
	TenantSchemaService tenantSchemaService;
	@Inject
	TenantService tenantService;

	@ConsumeEvent(FIND_TENANT_BY_VIRTUAL_HOST)
	public Uni<TenantResponseDTO> findTenant(String virtualHost) {
		return tenantService.findTenantByVirtualHost(virtualHost);
	}

	@ConsumeEvent(DELETE_REALM)
	public Uni<Void> deleteRealm(String realmName) {

		return VertxContextSupport.executeBlocking(() -> {
			try {
				keycloakAdmin.realm(realmName).remove();
			}
			catch (Exception e) {
				log.errorf(
					e, "An error occurred while deleting realm %s", realmName);
			}

			return null;
		});
	}

	@ConsumeEvent(DELETE_SCHEMA)
	public Uni<Void> deleteSchema(String schemaName) {

		return VertxContextSupport.executeBlocking(() -> {
			try {
				tenantSchemaService.rollbackRunLiquibaseMigration(schemaName);
			}
			catch (Exception e) {
				log.errorf(
					e, "An error occurred while deleting schema %s", schemaName);
			}

			return null;
		});
	}

	@ConsumeEvent(DELETE_TENANT)
	public Uni<Void> deleteTenant(String tenantId) {

		return tenantService.deleteTenant(Long.parseLong(tenantId));

	}

}
