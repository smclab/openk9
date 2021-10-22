package io.openk9.datasource.web;

import io.openk9.datasource.dto.DatasourceDto;
import io.openk9.datasource.mapper.DatasourceIgnoreNullMapper;
import io.openk9.datasource.mapper.DatasourceNullAwareMapper;
import io.openk9.datasource.model.Datasource;
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

@Path("/v1/datasource")
public class DatasourceResource {

	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Uni<Datasource> findById(@PathParam("id") long id){
		return Datasource.findById(id);
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

		return Datasource
			.findAll(sort)
			.page(page)
			.list()
			.onItem()
			.transform(list -> Response.ok(list).build());
	}

	@POST
	@Consumes("application/json")
	public Uni<Response> create(@Valid DatasourceDto dto) {

		Datasource datasource = _datasourceMapper.toDatasource(dto);

		return Panache
			.<Datasource>withTransaction(datasource::persist)
			.onItem()
			.transform(inserted -> Response.created(
					URI.create(
						"/v1/datasource/" + inserted.getDatasourceId()
					)
				).build()
			);
	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Response> update(
		@PathParam("id") long id, @Valid DatasourceDto dto) {

		return Datasource
			.<Datasource>findById(id)
			.onItem()
			.ifNull()
			.failWith(NotFoundException::new)
			.onItem()
			.transformToUni(datasource -> {
				Datasource update = _datasourceMapper.update(datasource, dto);
				return Panache.<Datasource>withTransaction(update::persist);
			})
			.map(o -> Response.ok(o).build());

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Response> patch(
		@PathParam("id") long id, @Valid DatasourceDto dto) {

		return Datasource
			.<Datasource>findById(id)
			.onItem()
			.ifNull()
			.failWith(NotFoundException::new)
			.onItem()
			.transformToUni(datasource -> {
				Datasource update = _datasourceIgnoreNullMapper.update(datasource, dto);
				return Panache.<Datasource>withTransaction(update::persist);
			})
			.map(o -> Response.ok(o).build());

	}

	@DELETE
	@Path("/{id}")
	public Uni<Void> deleteById(@PathParam("id") long id){
		return Datasource.deleteById(id).replaceWithVoid();
	}

	@Inject
	DatasourceNullAwareMapper _datasourceMapper;

	@Inject
	DatasourceIgnoreNullMapper _datasourceIgnoreNullMapper;
}