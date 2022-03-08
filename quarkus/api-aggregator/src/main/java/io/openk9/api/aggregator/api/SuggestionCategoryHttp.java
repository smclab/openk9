package io.openk9.api.aggregator.api;

import io.openk9.api.aggregator.client.dto.SuggestionCategoryDTO;
import io.openk9.api.aggregator.client.dto.SuggestionCategoryRequestDTO;
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

public interface SuggestionCategoryHttp {

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v2/suggestion-category/count")
	public Uni<Long> suggestionCategoryCount();

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/suggestion-category/filter/count")
	public Uni<Long> suggestionCategoryCountFilter(
		SuggestionCategoryRequestDTO dto);

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v2/suggestion-category/{id}")
	public Uni<SuggestionCategoryDTO> suggestionCategoryFindById(@PathParam("id") long id);

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/suggestion-category/filter")
	public Uni<List<SuggestionCategoryDTO>> suggestionCategoryFilter(SuggestionCategoryRequestDTO dto);

	@RolesAllowed({"datasource-read", "datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@GET
	@Path("/v2/suggestion-category")
	public Uni<List<SuggestionCategoryDTO>> suggestionCategoryFindAll(
		@QueryParam("sort") List<String> suggestionCategorySortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("20") int pageSize
	);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/suggestion-category")
	public Uni<SuggestionCategoryDTO> suggestionCategoryCreate(SuggestionCategoryRequestDTO dto);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v2/suggestion-category/{id}")
	public Uni<SuggestionCategoryDTO> suggestionCategoryUpdate(
		@PathParam("id") long id, SuggestionCategoryRequestDTO dto);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@PATCH
	@Path("/v2/suggestion-category/{id}")
	public Uni<SuggestionCategoryDTO> suggestionCategoryPatch(
		@PathParam("id") long id, SuggestionCategoryRequestDTO dto);

	@RolesAllowed({"datasource-write", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@DELETE
	@Path("/v2/suggestion-category/{id}")
	public Uni<Response> suggestionCategoryDeleteById(@PathParam("id") long id);
	
}
