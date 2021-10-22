package io.openk9.datasource.web;

import io.openk9.datasource.dto.EnrichItemDto;
import io.openk9.datasource.mapper.EnrichItemIgnoreNullMapper;
import io.openk9.datasource.mapper.EnrichItemNullAwareMapper;
import io.openk9.datasource.model.EnrichItem;
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

@Path("/v1/enrichItem")
public class EnrichItemResource {

	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Uni<EnrichItem> findById(@PathParam("id") long id){
		return EnrichItem.findById(id);
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

		return EnrichItem
			.findAll(sort)
			.page(page)
			.list()
			.onItem()
			.transform(list -> Response.ok(list).build());
	}

	@POST
	@Consumes("application/json")
	public Uni<Response> create(@Valid EnrichItemDto dto) {

		EnrichItem enrichItem = _enrichItemMapper.toEnrichItem(dto);

		return Panache
			.<EnrichItem>withTransaction(enrichItem::persist)
			.onItem()
			.transform(inserted -> Response.created(
					URI.create(
						"/v1/enrichItem/" + inserted.getEnrichItemId()
					)
				).build()
			);
	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Response> update(
		@PathParam("id") long id, @Valid EnrichItemDto dto) {

		return EnrichItem
			.<EnrichItem>findById(id)
			.onItem()
			.ifNull()
			.failWith(NotFoundException::new)
			.onItem()
			.transformToUni(enrichItem -> {
				EnrichItem update = _enrichItemMapper.update(enrichItem, dto);
				return Panache.<EnrichItem>withTransaction(update::persist);
			})
			.map(o -> Response.ok(o).build());

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Response> patch(
		@PathParam("id") long id, @Valid EnrichItemDto dto) {

		return EnrichItem
			.<EnrichItem>findById(id)
			.onItem()
			.ifNull()
			.failWith(NotFoundException::new)
			.onItem()
			.transformToUni(enrichItem -> {
				EnrichItem update = _enrichItemIgnoreNullMapper.update(enrichItem, dto);
				return Panache.<EnrichItem>withTransaction(update::persist);
			})
			.map(o -> Response.ok(o).build());

	}

	@DELETE
	@Path("/{id}")
	public Uni<Void> deleteById(@PathParam("id") long id){
		return EnrichItem.deleteById(id).replaceWithVoid();
	}

	@Inject
	EnrichItemNullAwareMapper _enrichItemMapper;

	@Inject
	EnrichItemIgnoreNullMapper _enrichItemIgnoreNullMapper;

}