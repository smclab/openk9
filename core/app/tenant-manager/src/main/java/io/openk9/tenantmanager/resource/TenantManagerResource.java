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

package io.openk9.tenantmanager.resource;

import io.openk9.common.util.RandomGenerator;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.pipe.tenant.create.TenantManagerActorSystem;
import io.openk9.tenantmanager.pipe.tenant.delete.DeleteTenantActorSystem;
import io.openk9.tenantmanager.service.TenantService;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/tenant-manager/tenant")
@RolesAllowed("admin")
public class TenantManagerResource {

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<Tenant> createTenant(CreateTenantRequest createTenantRequest) {

		String virtualHost = createTenantRequest.virtualHost();

		return tenantService
			.findTenantByVirtualHost(virtualHost)
			.flatMap(tenant -> {
				if (tenant == null) {
					return tenantService
						.findAllSchemaName()
						.flatMap(schemaNames -> {

							String newSchemaName = RandomGenerator.generate(
								schemaNames.toArray(String[]::new));

							return tenantManagerActorSystem
								.startCreateTenant(virtualHost, newSchemaName);

						});
				}
				else {
					return Uni
						.createFrom()
						.failure(
							new WebApplicationException(
								"Tenant exist with virtualHost: " + virtualHost,
								Response.Status.CONFLICT)
						);
				}
			});
	}

	@POST
	@Path("/{id}/tables")
	public Uni<CreateTablesResponse> createTables(@PathParam("id") Long id) {
		return tenantService.findById(id).flatMap(t -> {
			if (t == null) {
				return Uni.createFrom().failure(
					new WebApplicationException(
						"Tenant not found with id: " + id,
						Response.Status.NOT_FOUND)
				);
			}
			else {
				return tenantManagerActorSystem
					.populateSchema(t.getSchemaName(), t.getVirtualHost())
					.onItemOrFailure()
					.transformToUni((ignore, err) -> {
						if (err != null) {
							return Uni.createFrom().failure(new WebApplicationException(err));
						}
						else {
							return Uni.createFrom().item(
								new CreateTablesResponse(
									"Tables for schema " + t.getSchemaName() + " created"));
						}
					});
			}
		});
	}

	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<DeleteTenantResponse> deleteTenant(DeleteTenantRequest deleteTenantRequest) {

		String virtualHost = deleteTenantRequest.virtualHost();

		return tenantService
			.findTenantByVirtualHost(virtualHost)
			.flatMap(tenant -> {
				if (tenant == null) {
					return Uni
						.createFrom()
						.failure(
							new WebApplicationException(
								"Tenant not exist with virtualHost: " + virtualHost,
								Response.Status.NOT_FOUND)
						);
				}
				else {
					deleteTenantActorSystem.startDeleteTenant(virtualHost);
					return Uni
						.createFrom()
						.item(new DeleteTenantResponse("delete tenant started...read logs"));
				}
			});

	}

	@DELETE
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<DeleteTenantResponse> deleteTenant(
		EffectiveDeleteTenantRequest effectiveDeleteTenantRequest) {

		deleteTenantActorSystem.runDelete(
			effectiveDeleteTenantRequest.virtualHost(),
			effectiveDeleteTenantRequest.token()
		);

		return Uni
			.createFrom()
			.item(new DeleteTenantResponse("delete tenant started"));

	}

	@Inject
	TenantService tenantService;

	@Inject
	TenantManagerActorSystem tenantManagerActorSystem;

	@Inject
	DeleteTenantActorSystem deleteTenantActorSystem;

	@RegisterForReflection
	public record RequestId(UUID requestId) {}

	@RegisterForReflection
	public record CreateTenantRequest(String virtualHost) {}

	@RegisterForReflection
	public record DeleteTenantRequest(String virtualHost) {}

	@RegisterForReflection
	public record EffectiveDeleteTenantRequest(String virtualHost, String token) {}

	@RegisterForReflection
	public record DeleteTenantResponse(String message) {}

	@RegisterForReflection
	public record CreateTablesResponse(String message) {}

}
