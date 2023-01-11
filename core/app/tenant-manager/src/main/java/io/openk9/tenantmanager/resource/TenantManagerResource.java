package io.openk9.tenantmanager.resource;

import io.openk9.common.util.RandomGenerator;
import io.openk9.tenantmanager.pipe.tenant.create.TenantManagerActorSystem;
import io.openk9.tenantmanager.pipe.tenant.delete.DeleteTenantActorSystem;
import io.openk9.tenantmanager.service.TenantService;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/tenant-manager/tenant")
@RolesAllowed("admin")
public class TenantManagerResource {

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<RequestId> createTenant(CreateTenantRequest createTenantRequest) {

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
								.startCreateTenant(virtualHost, newSchemaName)
								.map(RequestId::new);

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
	record RequestId(UUID requestId) { }

	@RegisterForReflection
	record CreateTenantRequest(String virtualHost) { }

	@RegisterForReflection
	record DeleteTenantRequest(String virtualHost) { }

	@RegisterForReflection
	record EffectiveDeleteTenantRequest(String virtualHost, String token) { }

	@RegisterForReflection
	record DeleteTenantResponse(String message) { }

	@RegisterForReflection
	record CreateTablesResponse(String message) { }

}
