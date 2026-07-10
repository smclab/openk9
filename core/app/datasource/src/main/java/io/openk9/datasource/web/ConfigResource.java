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

package io.openk9.datasource.web;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import io.openk9.common.util.web.InternalHeaders;
import io.openk9.datasource.config.ConfigExporter;
import io.openk9.datasource.config.ConfigImporter;
import io.openk9.datasource.config.model.ConfigPackage;
import io.openk9.datasource.config.model.ImportMode;
import io.openk9.datasource.config.model.ImportResult;

import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import org.eclipse.microprofile.openapi.annotations.Operation;

@ApplicationScoped
@Path("/v1/config")
@RolesAllowed("k9-admin")
public class ConfigResource {

	@Inject
	RoutingContext routingContext;

	@Inject
	ConfigExporter configExporter;

	@Inject
	ConfigImporter configImporter;

	/**
	 * Exports the calling tenant's whole configuration as a portable package,
	 * with database ids replaced by local handles and secrets redacted.
	 *
	 * @return the configuration of the tenant resolved from the request
	 */
	@GET
	@Path("/export")
	@Operation(summary = "Export the current tenant's configuration")
	public Uni<ConfigPackage> export() {
		return configExporter.export(requireTenantId());
	}

	/**
	 * Imports a configuration package into the calling tenant in a single
	 * transaction, creating or overwriting entities according to {@code mode}.
	 *
	 * @param mode {@code SKIP} (default) leaves matched entities untouched,
	 *             {@code OVERWRITE} replaces them
	 * @param pkg  the configuration package to apply
	 * @return the outcome of the import (created, overwritten and skipped counts)
	 */
	@POST
	@Path("/import")
	@Operation(summary = "Import a configuration package into the current tenant")
	public Uni<ImportResult> importConfig(
		@QueryParam("mode") @DefaultValue("SKIP") ImportMode mode,
		ConfigPackage pkg) {

		String tenantId = requireTenantId();
		requireValidPackage(pkg);
		return configImporter.apply(tenantId, pkg, mode);
	}

	private void requireValidPackage(ConfigPackage pkg) {
		if (pkg == null
			|| !ConfigPackage.CURRENT_SCHEMA_VERSION.equals(pkg.getSchemaVersion())) {

			throw new BadRequestException(
				"Unsupported schema version '"
				+ (pkg == null ? null : pkg.getSchemaVersion())
				+ "', expected '" + ConfigPackage.CURRENT_SCHEMA_VERSION + "'");
		}
		if (pkg.getEntities() == null || pkg.getEntities().isEmpty()) {
			throw new BadRequestException(
				"Invalid configuration package: it must contain at least one entity");
		}
	}

	private String requireTenantId() {
		String tenantId = routingContext.get("_tenantId");
		if (tenantId == null || tenantId.isBlank()) {
			throw new BadRequestException(
				"Missing tenant: the '" + InternalHeaders.TENANT_ID
				+ "' header is required");
		}
		return tenantId;
	}

}
