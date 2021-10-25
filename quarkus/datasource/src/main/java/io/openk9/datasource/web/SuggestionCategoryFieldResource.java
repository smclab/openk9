package io.openk9.datasource.web;

import io.openk9.datasource.dto.SuggestionCategoryFieldDto;
import io.openk9.datasource.mapper.SuggestionCategoryFieldIgnoreNullMapper;
import io.openk9.datasource.mapper.SuggestionCategoryFieldNullAwareMapper;
import io.openk9.datasource.model.SuggestionCategoryField;
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

@Path("/v2/suggestion-category-field")
public class SuggestionCategoryFieldResource {

	@GET
	@Path("/{id}")
	@Produces()
	public SuggestionCategoryField findById(@PathParam("id") long id){
		return SuggestionCategoryField.findById(id);
	}

	@POST
	@Path("/filter")
	@Produces()
	public List<SuggestionCategoryField> filter(SuggestionCategoryFieldDto dto){

		Map<String, Object> map = JsonObject.mapFrom(dto).getMap();

		Tuple2<String, Map<String, Object>> query = ResourceUtil.getFilterQuery(map);

		return SuggestionCategoryField.list(query.getItem1(), query.getItem2());
	}

	@GET
	@Produces()
	public List<SuggestionCategoryField> findAll(
		@QueryParam("sort") List<String> sortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("20") int pageSize
	){
		Page page = Page.of(pageIndex, pageSize);
		Sort sort = Sort.by(sortQuery.toArray(String[]::new));

		return SuggestionCategoryField.findAll(sort).page(page).list();
	}

	@POST
	@Consumes("application/json")
	@Transactional
	public SuggestionCategoryField create(@Valid SuggestionCategoryFieldDto dto) {

		SuggestionCategoryField suggestionCategoryField = _suggestionCategoryFieldMapper.toSuggestionCategoryField(dto);

		suggestionCategoryField.persistAndFlush();

		return suggestionCategoryField;

	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	@Transactional
	public SuggestionCategoryField update(
		@PathParam("id") long id, @Valid SuggestionCategoryFieldDto dto) {

		SuggestionCategoryField entity = SuggestionCategoryField.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"SuggestionCategoryField with id of " + id + " does not exist.", 404);
		}

		entity = _suggestionCategoryFieldMapper.update(entity, dto);

		entity.persistAndFlush();

		return entity;

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	@Transactional
	public SuggestionCategoryField patch(
		@PathParam("id") long id, @Valid SuggestionCategoryFieldDto dto) {

		SuggestionCategoryField entity = SuggestionCategoryField.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"SuggestionCategoryField with id of " + id + " does not exist.", 404);
		}

		entity = _suggestionCategoryFieldIgnoreNullMapper.update(entity, dto);

		entity.persistAndFlush();

		return entity;

	}

	@DELETE
	@Path("/{id}")
	@Transactional
	public Response deleteById(@PathParam("id") long id){

		SuggestionCategoryField entity = SuggestionCategoryField.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"SuggestionCategoryField with id of " + id + " does not exist.", 404);
		}

		entity.delete();

		return Response.status(204).build();
	}

	@Inject
	SuggestionCategoryFieldNullAwareMapper _suggestionCategoryFieldMapper;

	@Inject
	SuggestionCategoryFieldIgnoreNullMapper
		_suggestionCategoryFieldIgnoreNullMapper;

}