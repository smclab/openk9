package io.openk9.datasource.web;

import io.openk9.datasource.dto.TenantDto;
import io.openk9.datasource.mapper.TenantIgnoreNullMapper;
import io.openk9.datasource.mapper.TenantMapper;
import io.openk9.datasource.mapper.TenantNullAwareMapper;
import io.openk9.datasource.model.Tenant;
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

@Path("/v1/tenant")
public class TenantResource {

	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Uni<Tenant> findById(@PathParam("id") long id){
		return Tenant.findById(id);
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

		return Tenant
			.findAll(sort)
			.page(page)
			.list()
			.onItem()
			.transform(list -> Response.ok(list).build());
	}

	@POST
	@Consumes("application/json")
	public Uni<Response> create(@Valid TenantDto dto) {

		Tenant tenant = _tenantMapper.toTenant(dto);

		return Panache
			.<Tenant>withTransaction(tenant::persist)
			.onItem()
			.transform(inserted -> Response.created(
					URI.create(
						"/v1/tenant/" + inserted.getTenantId()
					)
				).build()
			);
	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Response> update(
		@PathParam("id") long id, @Valid TenantDto dto) {

		return Tenant
			.<Tenant>findById(id)
			.onItem()
			.ifNull()
			.failWith(NotFoundException::new)
			.onItem()
			.transformToUni(tenant -> {
				Tenant update = _tenantMapper.update(tenant, dto);
				return Panache.<Tenant>withTransaction(update::persist);
			})
			.map(o -> Response.ok(o).build());

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Response> patch(
		@PathParam("id") long id, @Valid TenantDto dto) {

		return Tenant
			.<Tenant>findById(id)
			.onItem()
			.ifNull()
			.failWith(NotFoundException::new)
			.onItem()
			.transformToUni(tenant -> {
				Tenant update = _tenantIgnoreNullMapper.update(tenant, dto);
				return Panache.<Tenant>withTransaction(update::persist);
			})
			.map(o -> Response.ok(o).build());

	}

	@DELETE
	@Path("/{id}")
	public Uni<Void> deleteById(@PathParam("id") long id){
		return Tenant.deleteById(id).replaceWithVoid();
	}

	@Inject
	TenantNullAwareMapper _tenantMapper;

	@Inject
	TenantIgnoreNullMapper _tenantIgnoreNullMapper;

}