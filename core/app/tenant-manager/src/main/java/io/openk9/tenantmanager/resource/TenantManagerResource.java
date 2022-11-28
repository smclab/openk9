package io.openk9.tenantmanager.resource;

import io.openk9.common.util.RandomGenerator;
import io.openk9.tenantmanager.pipe.TenantManagerActorSystem;
import io.openk9.tenantmanager.service.TenantService;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/tenant-manager/tenant")
@SecurityRequirement(name = "SecurityScheme")
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

	@Inject
	TenantService tenantService;

	@Inject
	TenantManagerActorSystem tenantManagerActorSystem;

	@RegisterForReflection
	record RequestId(UUID requestId) { }

	@RegisterForReflection
	record CreateTenantRequest(String virtualHost) { }

}
