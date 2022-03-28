package io.openk9.datasource.web;

import io.openk9.datasource.dto.TenantDto;
import io.openk9.datasource.mapper.TenantIgnoreNullMapper;
import io.openk9.datasource.mapper.TenantNullAwareMapper;
import io.openk9.datasource.model.Tenant;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.opentracing.Traced;

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

@Traced
@Path("/v2/tenant")
public class TenantResource {

	@GET
	@Path("/count")
	public Uni<Long> count(){
		return Tenant.count();
	}

	@POST
	@Path("/filter/count")
	public Uni<Long> filterCount(TenantDto dto){

		Map<String, Object> map = JsonObject.mapFrom(dto).getMap();

		Tuple2<String, Map<String, Object>> query =
			ResourceUtil.getFilterQuery(map);

		return Tenant.count(
			query.getItem1(), query.getItem2());
	}

	@GET
	@Path("/{id}")
	@Produces()
	public Uni<Tenant> findById(@PathParam("id") long id){
		return Tenant.findById(id);
	}

	@POST
	@Path("/filter")
	@Produces()
	public Uni<List<Tenant>> filter(TenantDto dto){

		Map<String, Object> map = JsonObject.mapFrom(dto).getMap();

		Tuple2<String, Map<String, Object>> query = ResourceUtil.getFilterQuery(map);

		return Tenant.list(query.getItem1(), query.getItem2());
	}

	@GET
	@Produces()
	public Uni<List<Tenant>> findAll(
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
	public Uni<Tenant> create(@Valid TenantDto dto) {

		Tenant datasource = _tenantNullAwareMapper.toTenant(dto);

		return Panache.withTransaction(datasource::persistAndFlush);

	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Tenant> update(
		@PathParam("id") long id, @Valid TenantDto dto) {

		return Tenant
			.findById(id)
			.onItem()
			.ifNull()
			.failWith(() -> new WebApplicationException(
				"Tenant with id of " + id + " does not exist.", 404))
			.flatMap(datasource -> {
				Tenant newTenant =
					_tenantNullAwareMapper.update((Tenant)datasource, dto);
				return Panache.withTransaction(newTenant::persist);
			});

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Tenant> patch(
		@PathParam("id") long id, @Valid TenantDto dto) {

		return Tenant
			.findById(id)
			.onItem()
			.ifNull()
			.failWith(() -> new WebApplicationException(
				"Tenant with id of " + id + " does not exist.", 404))
			.flatMap(datasource -> {
				Tenant newTenant =
					_tenantIgnoreNullMapper.update((Tenant)datasource, dto);
				return Panache.withTransaction(newTenant::persist);
			});

	}

	@DELETE
	@Path("/{id}")
	public Uni<Response> deleteById(@PathParam("id") long id){

		return Panache.withTransaction(() ->
			Tenant
				.findById(id)
				.onItem()
				.ifNull()
				.failWith(() -> new WebApplicationException(
					"Tenant with id of " + id + " does not exist.", 404))
				.flatMap(PanacheEntityBase::delete)
				.map(unused -> Response.status(204).build())
		);

	}

	@Inject
	TenantNullAwareMapper _tenantNullAwareMapper;

	@Inject
	TenantIgnoreNullMapper _tenantIgnoreNullMapper;
}