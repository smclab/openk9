package io.openk9.api.aggregator.api;

import io.openk9.api.aggregator.client.dto.EnrichPipelineDTO;
import io.openk9.api.aggregator.client.dto.EnrichPipelineRequestDTO;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

public interface EnrichPipelineHttp {

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v2/enrichPipeline/count")
	public Uni<Long> enrichPipelineCount();

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/enrichPipeline/filter/count")
	public Uni<Long> enrichPipelineCountFilter(EnrichPipelineRequestDTO dto);

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v2/enrichPipeline/{id}")
	public Uni<EnrichPipelineDTO> enrichPipelineFindById(@PathParam("id") long id);

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/enrichPipeline/filter")
	public Uni<List<EnrichPipelineDTO>> enrichPipelineFilter(EnrichPipelineRequestDTO dto);

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v2/enrichPipeline")
	public Uni<List<EnrichPipelineDTO>> enrichPipelineFindAll(
		@QueryParam("sort") List<String> enrichPipelineSortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("20") int pageSize
	);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/enrichPipeline")
	public Uni<EnrichPipelineDTO> enrichPipelineCreate(EnrichPipelineRequestDTO dto);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/enrichPipeline/{id}")
	public Uni<EnrichPipelineDTO> enrichPipelineUpdate(
		@PathParam("id") long id, EnrichPipelineRequestDTO dto);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@PATCH
	@Path("/v2/enrichPipeline/{id}")
	public Uni<EnrichPipelineDTO> enrichPipelinePatch(
		@PathParam("id") long id, EnrichPipelineRequestDTO dto);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@DELETE
	@Path("/v2/enrichPipeline/{id}")
	public Uni<Response> enrichPipelineDeleteById(@PathParam("id") long id);
	
}
