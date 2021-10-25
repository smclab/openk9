package io.openk9.datasource.web;

import io.openk9.datasource.dto.EnrichPipelineDto;
import io.openk9.datasource.mapper.EnrichPipelineIgnoreNullMapper;
import io.openk9.datasource.mapper.EnrichPipelineNullAwareMapper;
import io.openk9.datasource.model.EnrichPipeline;
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

@Path("/v2/enrichPipeline")
public class EnrichPipelineResource {

	@GET
	@Path("/{id}")
	@Produces()
	public EnrichPipeline findById(@PathParam("id") long id){
		return EnrichPipeline.findById(id);
	}

	@POST
	@Path("/filter")
	@Produces()
	public List<EnrichPipeline> filter(EnrichPipeline dto){

		Map<String, Object> map = JsonObject.mapFrom(dto).getMap();

		Tuple2<String, Map<String, Object>> query = ResourceUtil.getFilterQuery(map);

		return EnrichPipeline.list(query.getItem1(), query.getItem2());
	}

	@GET
	@Produces()
	public List<EnrichPipeline> findAll(
		@QueryParam("sort") List<String> sortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("20") int pageSize
	){
		Page page = Page.of(pageIndex, pageSize);
		Sort sort = Sort.by(sortQuery.toArray(String[]::new));

		return EnrichPipeline.findAll(sort).page(page).list();
	}

	@POST
	@Consumes("application/json")
	@Transactional
	public EnrichPipeline create(@Valid EnrichPipelineDto dto) {

		EnrichPipeline enrichPipeline = _enrichPipelineMapper.toEnrichPipeline(dto);

		enrichPipeline.persistAndFlush();

		return enrichPipeline;

	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	@Transactional
	public EnrichPipeline update(
		@PathParam("id") long id, @Valid EnrichPipelineDto dto) {

		EnrichPipeline entity = EnrichPipeline.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"EnrichPipeline with id of " + id + " does not exist.", 404);
		}

		entity = _enrichPipelineMapper.update(entity, dto);

		entity.persistAndFlush();

		return entity;

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	@Transactional
	public EnrichPipeline patch(
		@PathParam("id") long id, @Valid EnrichPipelineDto dto) {

		EnrichPipeline entity = EnrichPipeline.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"EnrichPipeline with id of " + id + " does not exist.", 404);
		}

		entity = _enrichPipelineIgnoreNullMapper.update(entity, dto);

		entity.persistAndFlush();

		return entity;

	}

	@DELETE
	@Path("/{id}")
	@Transactional
	public Response deleteById(@PathParam("id") long id){

		EnrichPipeline entity = EnrichPipeline.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"EnrichPipeline with id of " + id + " does not exist.", 404);
		}

		entity.delete();

		return Response.status(204).build();
	}

	@Inject
	EnrichPipelineNullAwareMapper _enrichPipelineMapper;

	@Inject
	EnrichPipelineIgnoreNullMapper _enrichPipelineIgnoreNullMapper;

}