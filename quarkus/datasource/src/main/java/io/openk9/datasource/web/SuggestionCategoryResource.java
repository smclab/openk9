package io.openk9.datasource.web;

import io.openk9.datasource.dto.SuggestionCategoryDto;
import io.openk9.datasource.mapper.SuggestionCategoryIgnoreNullMapper;
import io.openk9.datasource.mapper.SuggestionCategoryNullAwareMapper;
import io.openk9.datasource.model.SuggestionCategory;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;
import javax.transaction.Transactional;
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
	public SuggestionCategory findById(@PathParam("id") long id){
		return SuggestionCategory.findById(id);
	}

	@POST
	@Path("/filter")
	@Produces()
	public List<SuggestionCategory> filter(SuggestionCategoryDto dto){

		Map<String, Object> map = JsonObject.mapFrom(dto).getMap();

		Tuple2<String, Map<String, Object>> query = ResourceUtil.getFilterQuery(map);

		return SuggestionCategory.list(query.getItem1(), query.getItem2());
	}

	@GET
	@Produces()
	public List<SuggestionCategory> findAll(
		@QueryParam("sort") List<String> sortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("20") int pageSize
	){
		Page page = Page.of(pageIndex, pageSize);
		Sort sort = Sort.by(sortQuery.toArray(String[]::new));

		return SuggestionCategory.findAll(sort).page(page).list();
	}

	@POST
	@Consumes("application/json")
	@Transactional
	public SuggestionCategory create(@Valid SuggestionCategoryDto dto) {

		SuggestionCategory suggestionCategory = _suggestionCategoryMapper.toSuggestionCategory(dto);

		suggestionCategory.persistAndFlush();

		return suggestionCategory;

	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	@Transactional
	public SuggestionCategory update(
		@PathParam("id") long id, @Valid SuggestionCategoryDto dto) {

		SuggestionCategory entity = SuggestionCategory.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"SuggestionCategory with id of " + id + " does not exist.", 404);
		}

		entity = _suggestionCategoryMapper.update(entity, dto);

		entity.persistAndFlush();

		return entity;

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	@Transactional
	public SuggestionCategory patch(
		@PathParam("id") long id, @Valid SuggestionCategoryDto dto) {

		SuggestionCategory entity = SuggestionCategory.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"SuggestionCategory with id of " + id + " does not exist.", 404);
		}

		entity = _suggestionCategoryIgnoreNullMapper.update(entity, dto);

		entity.persistAndFlush();

		return entity;

	}

	@DELETE
	@Path("/{id}")
	@Transactional
	public Response deleteById(@PathParam("id") long id){

		SuggestionCategory entity = SuggestionCategory.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"SuggestionCategory with id of " + id + " does not exist.", 404);
		}

		entity.delete();

		return Response.status(204).build();
	}

	@Inject
	SuggestionCategoryNullAwareMapper _suggestionCategoryMapper;

	@Inject
	SuggestionCategoryIgnoreNullMapper _suggestionCategoryIgnoreNullMapper;

}