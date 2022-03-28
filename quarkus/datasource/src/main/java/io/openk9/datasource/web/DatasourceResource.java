package io.openk9.datasource.web;

import io.openk9.datasource.dto.DatasourceDto;
import io.openk9.datasource.mapper.DatasourceIgnoreNullMapper;
import io.openk9.datasource.mapper.DatasourceNullAwareMapper;
import io.openk9.datasource.model.Datasource;
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
@Path("/v2/datasource")
public class DatasourceResource {

	@GET
	@Path("/count")
	public Uni<Long> count(){
		return Datasource.count();
	}

	@POST
	@Path("/filter/count")
	public Uni<Long> filterCount(DatasourceDto dto){

		Map<String, Object> map = JsonObject.mapFrom(dto).getMap();

		Tuple2<String, Map<String, Object>> query =
			ResourceUtil.getFilterQuery(map);

		return Datasource.count(query.getItem1(), query.getItem2());
	}

	@GET
	@Path("/{id}")
	@Produces()
	public Uni<Datasource> findById(@PathParam("id") long id){
		return Datasource.findById(id);
	}

	@POST
	@Path("/filter")
	@Produces()
	public Uni<List<Datasource>> filter(DatasourceDto dto){

		Map<String, Object> map = JsonObject.mapFrom(dto).getMap();

		Tuple2<String, Map<String, Object>> query = ResourceUtil.getFilterQuery(map);

		return Datasource.list(query.getItem1(), query.getItem2());
	}

	@GET
	@Produces()
	public Uni<List<Datasource>> findAll(
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
	public Uni<Datasource> create(@Valid DatasourceDto dto) {

		Datasource datasource = _datasourceMapper.toDatasource(dto);

		return Panache.withTransaction(datasource::persistAndFlush);

	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Datasource> update(
		@PathParam("id") long id, @Valid DatasourceDto dto) {

		return Datasource
			.findById(id)
			.onItem()
			.ifNull()
			.failWith(() -> new WebApplicationException(
				"Datasource with id of " + id + " does not exist.", 404))
			.flatMap(datasource -> {
				Datasource newDatasource =
					_datasourceMapper.update((Datasource)datasource, dto);
				return Panache.withTransaction(newDatasource::persist);
			});

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Datasource> patch(
		@PathParam("id") long id, @Valid DatasourceDto dto) {

		return Datasource
			.findById(id)
			.onItem()
			.ifNull()
			.failWith(() -> new WebApplicationException(
				"Datasource with id of " + id + " does not exist.", 404))
			.flatMap(datasource -> {
				Datasource newDatasource =
					_datasourceIgnoreNullMapper.update((Datasource)datasource, dto);
				return Panache.withTransaction(newDatasource::persist);
			});

	}

	@DELETE
	@Path("/{id}")
	public Uni<Response> deleteById(@PathParam("id") long id){

		return Panache.withTransaction(() ->
			Datasource
				.findById(id)
				.onItem()
				.ifNull()
				.failWith(() -> new WebApplicationException(
					"Datasource with id of " + id + " does not exist.", 404))
				.flatMap(PanacheEntityBase::delete)
				.map(unused -> Response.status(204).build())
		);

	}

	@Inject
	DatasourceNullAwareMapper _datasourceMapper;

	@Inject
	DatasourceIgnoreNullMapper _datasourceIgnoreNullMapper;
}