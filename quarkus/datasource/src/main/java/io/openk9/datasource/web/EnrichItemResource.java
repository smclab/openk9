package io.openk9.enrichItem.web;

import io.openk9.datasource.dto.EnrichItemDto;
import io.openk9.datasource.mapper.EnrichItemIgnoreNullMapper;
import io.openk9.datasource.mapper.EnrichItemNullAwareMapper;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.web.ResourceUtil;
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

@Path("/v2/enrichItem")
public class EnrichItemResource {

	@GET
	@Path("/{id}")
	@Produces("application/json")
	@Transactional
	public EnrichItem findById(@PathParam("id") long id){
		return EnrichItem.findById(id);
	}

	@POST
	@Path("/filter")
	@Produces("application/json")
	@Transactional
	public List<EnrichItem> filter(EnrichItemDto dto){

		Map<String, Object> map = JsonObject.mapFrom(dto).getMap();

		Tuple2<String, Map<String, Object>> query = ResourceUtil.getFilterQuery(map);

		return EnrichItem.list(query.getItem1(), query.getItem2());
	}

	@GET
	@Produces("application/json")
	@Transactional
	public List<EnrichItem> findAll(
		@QueryParam("sort") List<String> sortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("20") int pageSize
	){
		Page page = Page.of(pageIndex, pageSize);
		Sort sort = Sort.by(sortQuery.toArray(String[]::new));

		return EnrichItem.findAll(sort).page(page).list();
	}

	@POST
	@Consumes("application/json")
	@Transactional
	public EnrichItem create(@Valid EnrichItemDto dto) {

		EnrichItem enrichItem = _enrichItemMapper.toEnrichItem(dto);

		enrichItem.persistAndFlush();

		return enrichItem;

	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	@Transactional
	public EnrichItem update(
		@PathParam("id") long id, @Valid EnrichItemDto dto) {

		EnrichItem entity = EnrichItem.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"EnrichItem with id of " + id + " does not exist.", 404);
		}

		entity = _enrichItemMapper.update(entity, dto);

		entity.persistAndFlush();

		return entity;

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	@Transactional
	public EnrichItem patch(
		@PathParam("id") long id, @Valid EnrichItemDto dto) {

		EnrichItem entity = EnrichItem.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"EnrichItem with id of " + id + " does not exist.", 404);
		}

		entity = _enrichItemIgnoreNullMapper.update(entity, dto);

		entity.persistAndFlush();

		return entity;

	}

	@DELETE
	@Path("/{id}")
	@Transactional
	public Response deleteById(@PathParam("id") long id){

		EnrichItem entity = EnrichItem.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"EnrichItem with id of " + id + " does not exist.", 404);
		}

		entity.delete();

		return Response.status(204).build();
	}

	@Inject
	EnrichItemNullAwareMapper _enrichItemMapper;

	@Inject
	EnrichItemIgnoreNullMapper _enrichItemIgnoreNullMapper;

}