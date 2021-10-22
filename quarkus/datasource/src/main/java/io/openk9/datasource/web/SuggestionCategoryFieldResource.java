package io.openk9.datasource.web;

import io.openk9.datasource.dto.SuggestionCategoryFieldDto;
import io.openk9.datasource.mapper.SuggestionCategoryFieldIgnoreNullMapper;
import io.openk9.datasource.mapper.SuggestionCategoryFieldNullAwareMapper;
import io.openk9.datasource.model.SuggestionCategoryField;
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

@Path("/v1/suggestion-category-field")
public class SuggestionCategoryFieldResource {

	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Uni<SuggestionCategoryField> findById(@PathParam("id") long id){
		return SuggestionCategoryField.findById(id);
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

		return SuggestionCategoryField
			.findAll(sort)
			.page(page)
			.list()
			.onItem()
			.transform(list -> Response.ok(list).build());
	}

	@POST
	@Consumes("application/json")
	public Uni<Response> create(@Valid SuggestionCategoryFieldDto dto) {

		SuggestionCategoryField suggestionCategoryField = _suggestionCategoryFieldMapper.toSuggestionCategoryField(dto);

		return Panache
			.<SuggestionCategoryField>withTransaction(suggestionCategoryField::persist)
			.onItem()
			.transform(inserted -> Response.created(
					URI.create(
						"/v1/suggestionCategoryField/" + inserted.getSuggestionCategoryFieldId()
					)
				).build()
			);
	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Response> update(
		@PathParam("id") long id, @Valid SuggestionCategoryFieldDto dto) {

		return SuggestionCategoryField
			.<SuggestionCategoryField>findById(id)
			.onItem()
			.ifNull()
			.failWith(NotFoundException::new)
			.onItem()
			.transformToUni(suggestionCategoryField -> {
				SuggestionCategoryField update = _suggestionCategoryFieldMapper.update(suggestionCategoryField, dto);
				return Panache.<SuggestionCategoryField>withTransaction(update::persist);
			})
			.map(o -> Response.ok(o).build());

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Response> patch(
		@PathParam("id") long id, @Valid SuggestionCategoryFieldDto dto) {

		return SuggestionCategoryField
			.<SuggestionCategoryField>findById(id)
			.onItem()
			.ifNull()
			.failWith(NotFoundException::new)
			.onItem()
			.transformToUni(suggestionCategoryField -> {
				SuggestionCategoryField update = _suggestionCategoryFieldIgnoreNullMapper.update(suggestionCategoryField, dto);
				return Panache.<SuggestionCategoryField>withTransaction(update::persist);
			})
			.map(o -> Response.ok(o).build());

	}

	@DELETE
	@Path("/{id}")
	public Uni<Void> deleteById(@PathParam("id") long id){
		return SuggestionCategoryField.deleteById(id).replaceWithVoid();
	}

	@Inject
	SuggestionCategoryFieldNullAwareMapper _suggestionCategoryFieldMapper;

	@Inject
	SuggestionCategoryFieldIgnoreNullMapper
		_suggestionCategoryFieldIgnoreNullMapper;

}