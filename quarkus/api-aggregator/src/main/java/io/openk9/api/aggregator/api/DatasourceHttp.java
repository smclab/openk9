package io.openk9.api.aggregator.api;

import io.openk9.api.aggregator.client.dto.DatasourceDTO;
import io.openk9.api.aggregator.client.dto.DatasourceRequestDTO;
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

public interface DatasourceHttp {

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v2/datasource/count")
	public Uni<Long> datasourceCount();

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/datasource/filter/count")
	public Uni<Long> datasourceCountFilter(DatasourceRequestDTO dto);

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v2/datasource/{id}")
	public Uni<DatasourceDTO> datasourceFindById(@PathParam("id") long id);

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/datasource/filter")
	public Uni<List<DatasourceDTO>> datasourceFilter(DatasourceRequestDTO dto);

	@Path("/v2/datasource")
	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	public Uni<List<DatasourceDTO>> datasourceFindAll(
		@QueryParam("sort") List<String> datasourceSortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("20") int pageSize
	);

	@Path("/v2/datasource")
	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	public Uni<DatasourceDTO> datasourceCreate(DatasourceRequestDTO dto);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/datasource/{id}")
	public Uni<DatasourceDTO> datasourceUpdate(
		@PathParam("id") long id, DatasourceRequestDTO dto);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@PATCH
	@Path("/v2/datasource/{id}")
	public Uni<DatasourceDTO> datasourcePatch(
		@PathParam("id") long id, DatasourceRequestDTO dto);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@DELETE
	@Path("/v2/datasource/{id}")
	public Uni<Response> datasourceDeleteById(@PathParam("id") long id);

}
