package io.openk9.datasource.web;

import io.openk9.datasource.dto.EnrichItemDto;
import io.openk9.datasource.mapper.EnrichItemIgnoreNullMapper;
import io.openk9.datasource.mapper.EnrichItemNullAwareMapper;
import io.openk9.datasource.model.EnrichItem;
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
@Path("/v2/enrichItem")
public class EnrichItemResource {

	@GET
	@Path("/count")
	public Uni<Long> count(){
		return EnrichItem.count();
	}

	@POST
	@Path("/filter/count")
	public Uni<Long> filterCount(EnrichItemDto dto){

		Map<String, Object> map = JsonObject.mapFrom(dto).getMap();

		Tuple2<String, Map<String, Object>> query =
			ResourceUtil.getFilterQuery(map);

		return EnrichItem.count(query.getItem1(), query.getItem2());
	}

	@GET
	@Path("/{id}")
	@Produces()
	public Uni<EnrichItem> findById(@PathParam("id") long id){
		return EnrichItem.findById(id);
	}

	@POST
	@Path("/filter")
	@Produces()
	public Uni<List<EnrichItem>> filter(EnrichItemDto dto){

		Map<String, Object> map = JsonObject.mapFrom(dto).getMap();

		Tuple2<String, Map<String, Object>> query = ResourceUtil.getFilterQuery(map);

		return EnrichItem.list(query.getItem1(), query.getItem2());
	}

	@GET
	@Produces()
	public Uni<List<EnrichItem>> findAll(
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
	public Uni<EnrichItem> create(@Valid EnrichItemDto dto) {

		EnrichItem datasource = _enrichItemNullAwareMapper.toEnrichItem(dto);

		return Panache.withTransaction(datasource::persistAndFlush);

	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<EnrichItem> update(
		@PathParam("id") long id, @Valid EnrichItemDto dto) {

		return EnrichItem
			.findById(id)
			.onItem()
			.ifNull()
			.failWith(() -> new WebApplicationException(
				"EnrichItem with id of " + id + " does not exist.", 404))
			.flatMap(datasource -> {
				EnrichItem newEnrichItem =
					_enrichItemNullAwareMapper.update((EnrichItem)datasource, dto);
				return Panache.withTransaction(newEnrichItem::persist);
			});

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<EnrichItem> patch(
		@PathParam("id") long id, @Valid EnrichItemDto dto) {

		return EnrichItem
			.findById(id)
			.onItem()
			.ifNull()
			.failWith(() -> new WebApplicationException(
				"EnrichItem with id of " + id + " does not exist.", 404))
			.flatMap(datasource -> {
				EnrichItem newEnrichItem =
					_enrichItemIgnoreNullMapper.update((EnrichItem)datasource, dto);
				return Panache.withTransaction(newEnrichItem::persist);
			});

	}

	@DELETE
	@Path("/{id}")
	public Uni<Response> deleteById(@PathParam("id") long id){

		return Panache.withTransaction(() ->
			EnrichItem
				.findById(id)
				.onItem()
				.ifNull()
				.failWith(() -> new WebApplicationException(
					"EnrichItem with id of " + id + " does not exist.", 404))
				.flatMap(PanacheEntityBase::delete)
				.map(unused -> Response.status(204).build())
		);

	}

	@Inject
	EnrichItemNullAwareMapper _enrichItemNullAwareMapper;

	@Inject
	EnrichItemIgnoreNullMapper _enrichItemIgnoreNullMapper;
}