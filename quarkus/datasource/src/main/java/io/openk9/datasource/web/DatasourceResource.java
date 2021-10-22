package io.openk9.datasource.web;

import io.openk9.datasource.dto.DatasourceDto;
import io.openk9.datasource.mapper.DatasourceIgnoreNullMapper;
import io.openk9.datasource.mapper.DatasourceNullAwareMapper;
import io.openk9.datasource.model.Datasource;
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

@Path("/v2/datasource")
public class DatasourceResource {

	@GET
	@Path("/{id}")
	@Produces("application/json")
	@Transactional
	public Datasource findById(@PathParam("id") long id){
		return Datasource.findById(id);
	}

	@POST
	@Path("/filter")
	@Produces("application/json")
	@Transactional
	public List<Datasource> filter(DatasourceDto dto){

		Map<String, Object> map = JsonObject.mapFrom(dto).getMap();

		Tuple2<String, Map<String, Object>> query = ResourceUtil.getFilterQuery(map);

		return Datasource.list(query.getItem1(), query.getItem2());
	}

	@GET
	@Produces("application/json")
	@Transactional
	public List<Datasource> findAll(
		@QueryParam("sort") List<String> sortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("20") int pageSize
	){
		Page page = Page.of(pageIndex, pageSize);
		Sort sort = Sort.by(sortQuery.toArray(String[]::new));

		return Datasource.findAll(sort).page(page).list();
	}

	@POST
	@Consumes("application/json")
	@Transactional
	public Datasource create(@Valid DatasourceDto dto) {

		Datasource datasource = _datasourceMapper.toDatasource(dto);

		datasource.persistAndFlush();

		return datasource;

	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	@Transactional
	public Datasource update(
		@PathParam("id") long id, @Valid DatasourceDto dto) {

		Datasource entity = Datasource.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"Datasource with id of " + id + " does not exist.", 404);
		}

		entity = _datasourceMapper.update(entity, dto);

		entity.persistAndFlush();

		return entity;

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	@Transactional
	public Datasource patch(
		@PathParam("id") long id, @Valid DatasourceDto dto) {

		Datasource entity = Datasource.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"Datasource with id of " + id + " does not exist.", 404);
		}

		entity = _datasourceIgnoreNullMapper.update(entity, dto);

		entity.persistAndFlush();

		return entity;

	}

	@DELETE
	@Path("/{id}")
	@Transactional
	public Response deleteById(@PathParam("id") long id){

		Datasource entity = Datasource.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"Datasource with id of " + id + " does not exist.", 404);
		}

		entity.delete();

		return Response.status(204).build();
	}

	@Inject
	DatasourceNullAwareMapper _datasourceMapper;

	@Inject
	DatasourceIgnoreNullMapper _datasourceIgnoreNullMapper;
}