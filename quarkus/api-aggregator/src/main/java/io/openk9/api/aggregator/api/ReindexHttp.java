package io.openk9.api.aggregator.api;

import io.openk9.api.aggregator.client.dto.ReindexRequestDTO;
import io.openk9.api.aggregator.client.dto.ReindexResponseDTO;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.List;

public interface ReindexHttp {

	@RolesAllowed({"datasource-write", "datasource-reindex", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v1/index/reindex")
	Uni<List<ReindexResponseDTO>> reindex(ReindexRequestDTO dto);
}