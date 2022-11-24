/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.resource;

import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.dto.EnrichPipelineDTO;
import io.openk9.datasource.resource.util.BaseK9EntityResource;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.EnrichPipelineService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Path("/enrich-pipelines")
public class EnrichPipelineResource extends
	BaseK9EntityResource<EnrichPipelineService, EnrichPipeline, EnrichPipelineDTO> {

	protected EnrichPipelineResource(EnrichPipelineService service) {
		super(service);
	}

	@GET
	@Path("/{id}/enrich-items")
	public Uni<Page<EnrichItem>> getEnrichItems(
		@PathParam("id") long enrichPipelineId, @BeanParam Pageable pageable,
		@QueryParam("searchText") String searchText) {
		return service.getEnrichItems(enrichPipelineId, pageable, searchText);
	}

	@PUT
	@Path("/{id}/enrich-items/{enrichItemId}")
	public Uni<Tuple2<EnrichPipeline, EnrichItem>> addEnrichItem(
		@PathParam("id") long enrichPipelineId,
		@PathParam("enrichItemId") long enrichItemId,
		@QueryParam("tail") @DefaultValue("true") boolean tail) {
		return service.addEnrichItem(enrichPipelineId, enrichItemId, tail);
	}

	@DELETE
	@Path("/{id}/enrich-items/{enrichItemId}")
	public Uni<Tuple2<EnrichPipeline, EnrichItem>> removeEnrichItem(
		@PathParam("id") long enrichPipelineId,
		@PathParam("enrichItemId") long enrichItemId) {
		return service.removeEnrichItem(enrichPipelineId, enrichItemId);
	}

}
