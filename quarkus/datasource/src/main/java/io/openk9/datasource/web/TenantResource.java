package io.openk9.datasource.web;

import io.openk9.datasource.dto.TenantDto;
import io.openk9.datasource.mapper.TenantIgnoreNullMapper;
import io.openk9.datasource.mapper.TenantNullAwareMapper;
import io.openk9.datasource.model.Tenant;
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

@Path("/v2/tenant")
public class TenantResource {

	@GET
	@Path("/{id}")
	@Produces("application/json")
	@Transactional
	public Tenant findById(@PathParam("id") long id){
		return Tenant.findById(id);
	}

	@POST
	@Path("/filter")
	@Produces("application/json")
	@Transactional
	public List<Tenant> filter(TenantDto dto){

		Map<String, Object> map = JsonObject.mapFrom(dto).getMap();

		Tuple2<String, Map<String, Object>> query = ResourceUtil.getFilterQuery(map);

		return Tenant.list(query.getItem1(), query.getItem2());
	}

	@GET
	@Produces("application/json")
	@Transactional
	public List<Tenant> findAll(
		@QueryParam("sort") List<String> sortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("20") int pageSize
	){
		Page page = Page.of(pageIndex, pageSize);
		Sort sort = Sort.by(sortQuery.toArray(String[]::new));

		return Tenant.findAll(sort).page(page).list();
	}

	@POST
	@Consumes("application/json")
	@Transactional
	public Tenant create(@Valid TenantDto dto) {

		Tenant tenant = _tenantMapper.toTenant(dto);

		tenant.persistAndFlush();

		return tenant;

	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	@Transactional
	public Tenant update(
		@PathParam("id") long id, @Valid TenantDto dto) {

		Tenant entity = Tenant.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"Tenant with id of " + id + " does not exist.", 404);
		}

		entity = _tenantMapper.update(entity, dto);

		entity.persistAndFlush();

		return entity;

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	@Transactional
	public Tenant patch(
		@PathParam("id") long id, @Valid TenantDto dto) {

		Tenant entity = Tenant.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"Tenant with id of " + id + " does not exist.", 404);
		}

		entity = _tenantIgnoreNullMapper.update(entity, dto);

		entity.persistAndFlush();

		return entity;

	}

	@DELETE
	@Path("/{id}")
	@Transactional
	public Response deleteById(@PathParam("id") long id){

		Tenant entity = Tenant.findById(id);

		if (entity == null) {
			throw new WebApplicationException(
				"Tenant with id of " + id + " does not exist.", 404);
		}

		entity.delete();

		return Response.status(204).build();
	}

	@Inject
	TenantNullAwareMapper _tenantMapper;

	@Inject
	TenantIgnoreNullMapper _tenantIgnoreNullMapper;

}