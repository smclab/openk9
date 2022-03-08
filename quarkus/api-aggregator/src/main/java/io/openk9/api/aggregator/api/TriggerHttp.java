package io.openk9.api.aggregator.api;

import io.openk9.api.aggregator.client.dto.TriggerRequestDTO;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.List;

public interface TriggerHttp {
	@RolesAllowed({"datasource-write", "datasource-trigger", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v1/trigger")
	Uni<List<Long>> trigger(TriggerRequestDTO dto);
}