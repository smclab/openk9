package io.openk9.datasource.web;

import io.openk9.datasource.dto.SuggestionCategoryDto;
import io.openk9.datasource.mapper.SuggestionCategoryIgnoreNullMapper;
import io.openk9.datasource.mapper.SuggestionCategoryNullAwareMapper;
import io.openk9.datasource.model.SuggestionCategory;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Path("/v2/suggestion-category")
public class SuggestionCategoryResource {

	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Uni<SuggestionCategory> findById(@PathParam("id") long id){
		return SuggestionCategory.findById(id);
	}

	@GET
	@Produces("application/json")
	public Uni<Response> findAll(
		@QueryParam("sort") List<String> sortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("20") int pageSize
	){
		Page page = Page.of(pageIndex, pageSize);
		Sort sort = Sort.by(sortQuery.toArray(String[]::new));

		return SuggestionCategory
			.findAll(sort)
			.page(page)
			.list()
			.onItem()
			.transform(list -> Response.ok(list).build());
	}

	@POST
	@Consumes("application/json")
	public Uni<Response> create(@Valid SuggestionCategoryDto dto) {

		SuggestionCategory suggestionCategory = _suggestionCategoryMapper.toSuggestionCategory(dto);

		return Panache
			.<SuggestionCategory>withTransaction(suggestionCategory::persist)
			.onItem()
			.transform(inserted -> Response.created(
					URI.create(
						"/v2/suggestionCategory/" + inserted.getSuggestionCategoryId()
					)
				).build()
			);
	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Response> update(
		@PathParam("id") long id, @Valid SuggestionCategoryDto dto) {

		return SuggestionCategory
			.<SuggestionCategory>findById(id)
			.onItem()
			.ifNull()
			.failWith(NotFoundException::new)
			.onItem()
			.transformToUni(suggestionCategory -> {
				SuggestionCategory update = _suggestionCategoryMapper.update(suggestionCategory, dto);
				return Panache.<SuggestionCategory>withTransaction(update::persist);
			})
			.map(o -> Response.ok(o).build());

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Response> patch(
		@PathParam("id") long id, @Valid SuggestionCategoryDto dto) {

		return SuggestionCategory
			.<SuggestionCategory>findById(id)
			.onItem()
			.ifNull()
			.failWith(NotFoundException::new)
			.onItem()
			.transformToUni(suggestionCategory -> {
				SuggestionCategory update = _suggestionCategoryIgnoreNullMapper.update(suggestionCategory, dto);
				return Panache.<SuggestionCategory>withTransaction(update::persist);
			})
			.map(o -> Response.ok(o).build());

	}

	@DELETE
	@Path("/{id}")
	public Uni<Void> deleteById(@PathParam("id") long id){
		return SuggestionCategory.deleteById(id).replaceWithVoid();
	}

	@Inject
	SuggestionCategoryNullAwareMapper _suggestionCategoryMapper;

	@Inject
	SuggestionCategoryIgnoreNullMapper _suggestionCategoryIgnoreNullMapper;

}