package io.openk9.datasource.web;

import io.openk9.datasource.dto.SuggestionCategoryDto;
import io.openk9.datasource.mapper.SuggestionCategoryIgnoreNullMapper;
import io.openk9.datasource.mapper.SuggestionCategoryNullAwareMapper;
import io.openk9.datasource.model.SuggestionCategory;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/v2/suggestion-category")
public class SuggestionCategoryResource {

	@GET
	@Path("/{id}")
	@Produces()
	public Uni<SuggestionCategory> findById(@PathParam("id") long id){
		return SuggestionCategory.findById(id);
	}

	@POST
	@Path("/filter")
	@Produces()
	public Uni<List<SuggestionCategory>> filter(SuggestionCategoryDto dto){

		Map<String, Object> map = JsonObject.mapFrom(dto).getMap();

		Tuple2<String, Map<String, Object>> query = ResourceUtil.getFilterQuery(map);

		return SuggestionCategory.list(query.getItem1(), query.getItem2());
	}

	@GET
	@Produces()
	public Uni<List<SuggestionCategory>> findAll(
		@QueryParam("sort") List<String> sortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("20") int pageSize
	){
		Page page = Page.of(pageIndex, pageSize);

		if (sortQuery.isEmpty()) {
			return SuggestionCategory.findAll().page(page).list();
		}
		else {
			return SuggestionCategory
				.findAll(Sort.by(sortQuery.toArray(String[]::new)))
				.page(page)
				.list();
		}

	}

	@POST
	@Consumes("application/json")
	public Uni<SuggestionCategory> create(@Valid SuggestionCategoryDto dto) {

		SuggestionCategory datasource = _suggestionCategoryNullAwareMapper.toSuggestionCategory(dto);

		return Panache.withTransaction(datasource::persistAndFlush);

	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<SuggestionCategory> update(
		@PathParam("id") long id, @Valid SuggestionCategoryDto dto) {

		return SuggestionCategory
			.findById(id)
			.onItem()
			.ifNull()
			.failWith(() -> new WebApplicationException(
				"SuggestionCategory with id of " + id + " does not exist.", 404))
			.flatMap(datasource -> {
				SuggestionCategory newSuggestionCategory =
					_suggestionCategoryNullAwareMapper.update((SuggestionCategory)datasource, dto);
				return Panache.withTransaction(newSuggestionCategory::persist);
			});

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<SuggestionCategory> patch(
		@PathParam("id") long id, @Valid SuggestionCategoryDto dto) {

		return SuggestionCategory
			.findById(id)
			.onItem()
			.ifNull()
			.failWith(() -> new WebApplicationException(
				"SuggestionCategory with id of " + id + " does not exist.", 404))
			.flatMap(datasource -> {
				SuggestionCategory newSuggestionCategory =
					_suggestionCategoryIgnoreNullMapper.update((SuggestionCategory)datasource, dto);
				return Panache.withTransaction(newSuggestionCategory::persist);
			});

	}

	@DELETE
	@Path("/{id}")
	public Uni<Response> deleteById(@PathParam("id") long id){

		return Panache.withTransaction(() ->
			SuggestionCategory
				.findById(id)
				.onItem()
				.ifNull()
				.failWith(() -> new WebApplicationException(
					"SuggestionCategory with id of " + id + " does not exist.", 404))
				.flatMap(PanacheEntityBase::delete)
				.map(unused -> Response.status(204).build())
		);

	}

	@Inject
	SuggestionCategoryNullAwareMapper _suggestionCategoryNullAwareMapper;

	@Inject
	SuggestionCategoryIgnoreNullMapper _suggestionCategoryIgnoreNullMapper;
}