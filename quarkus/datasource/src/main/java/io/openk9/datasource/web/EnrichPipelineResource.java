package io.openk9.datasource.web;

import io.openk9.datasource.dto.EnrichPipelineDto;
import io.openk9.datasource.mapper.EnrichPipelineIgnoreNullMapper;
import io.openk9.datasource.mapper.EnrichPipelineNullAwareMapper;
import io.openk9.datasource.model.EnrichPipeline;
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
import java.util.Map;

@Path("/v2/enrichPipeline")
public class EnrichPipelineResource {

	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Uni<EnrichPipeline> findById(@PathParam("id") long id){
		return EnrichPipeline.findById(id);
	}

	@POST
	@Path("/filter")
	@Produces("application/json")
	public Uni<List<EnrichPipeline>> filter(Map<String, Object> maps){

		String query = ResourceUtil.getFilterQuery(maps);

		return EnrichPipeline.list(query, maps);
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

		return EnrichPipeline
			.findAll(sort)
			.page(page)
			.list()
			.onItem()
			.transform(list -> Response.ok(list).build());
	}

	@POST
	@Consumes("application/json")
	public Uni<Response> create(@Valid EnrichPipelineDto dto) {

		EnrichPipeline enrichPipeline = _enrichPipelineMapper.toEnrichPipeline(dto);

		return Panache
			.<EnrichPipeline>withTransaction(enrichPipeline::persist)
			.onItem()
			.transform(inserted -> Response.created(
					URI.create(
						"/v2/enrichPipeline/" + inserted.getEnrichPipelineId()
					)
				).build()
			);
	}

	@POST
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Response> update(
		@PathParam("id") long id, @Valid EnrichPipelineDto dto) {

		return EnrichPipeline
			.<EnrichPipeline>findById(id)
			.onItem()
			.ifNull()
			.failWith(NotFoundException::new)
			.onItem()
			.transformToUni(enrichPipeline -> {
				EnrichPipeline update = _enrichPipelineMapper.update(enrichPipeline, dto);
				return Panache.<EnrichPipeline>withTransaction(update::persist);
			})
			.map(o -> Response.ok(o).build());

	}

	@PATCH
	@Path("/{id}")
	@Consumes("application/json")
	public Uni<Response> patch(
		@PathParam("id") long id, @Valid EnrichPipelineDto dto) {

		return EnrichPipeline
			.<EnrichPipeline>findById(id)
			.onItem()
			.ifNull()
			.failWith(NotFoundException::new)
			.onItem()
			.transformToUni(enrichPipeline -> {
				EnrichPipeline update = _enrichPipelineIgnoreNullMapper.update(enrichPipeline, dto);
				return Panache.<EnrichPipeline>withTransaction(update::persist);
			})
			.map(o -> Response.ok(o).build());

	}

	@DELETE
	@Path("/{id}")
	public Uni<Void> deleteById(@PathParam("id") long id){
		return EnrichPipeline.deleteById(id).replaceWithVoid();
	}

	@Inject
	EnrichPipelineNullAwareMapper _enrichPipelineMapper;

	@Inject
	EnrichPipelineIgnoreNullMapper _enrichPipelineIgnoreNullMapper;

}