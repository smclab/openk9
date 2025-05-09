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

import java.util.List;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.dto.base.DatasourceDTO;
import io.openk9.datasource.resource.util.BaseK9EntityResource;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.util.Tuple2;

import io.smallrye.mutiny.Uni;

@Path("/datasources")
@RolesAllowed("k9-admin")
@Deprecated
public class DatasourceResource extends
	BaseK9EntityResource<DatasourceService, Datasource, DatasourceDTO> {

	protected DatasourceResource(DatasourceService service) {
		super(service);
	}

	@GET
	@Path("/{id}/enrich-pipeline")
	public Uni<EnrichPipeline> getEnrichPipeline(@PathParam("id") long id) {
		return service.getEnrichPipeline(id);
	}

	@PUT
	@Path("/{id}/enrich-pipeline/{enrichPipelineId}")
	public Uni<Tuple2<Datasource, EnrichPipeline>> setEnrichPipeline(
		@PathParam("id")long datasourceId,
		@PathParam("enrichPipelineId")long enrichPipelineId) {

		return service.setEnrichPipeline(datasourceId, enrichPipelineId);
	}

	@DELETE
	@Path("/{id}/enrich-pipeline")
	public Uni<Datasource> removeEnrichPipeline(
		@PathParam("id")long datasourceId) {

		return service.unsetEnrichPipeline(datasourceId);
	}

	@GET
	@Path("/{id}/data-index/{dataIndexId}")
	public Uni<DataIndex> getDataIndex(
		@PathParam("id")long datasourceId,
		@PathParam("dataIndexId")long dataIndexId) {

		return service.getDataIndex(datasourceId);
	}

	@PUT
	@Path("/{id}/data-index/{dataIndexId}")
	public Uni<Tuple2<Datasource, DataIndex>> setDataIndex(
		@PathParam("id")long datasourceId,
		@PathParam("dataIndexId")long dataIndexId) {
		return service.setDataIndex(datasourceId, dataIndexId);
	}

	@DELETE
	@Path("/{id}/data-index")
	public Uni<Datasource> unsetDataIndex(
		@PathParam("id")long datasourceId) {
		return service.unsetDataIndex(datasourceId);
	}

	@GET
	@Path("/{id}/plugin-driver")
	public Uni<PluginDriver> getPluginDriver(@PathParam("id") long id) {
		return service.getPluginDriver(id);
	}

	@PUT
	@Path("/{id}/plugin-driver/{pluginDriverId}")
	public Uni<Tuple2<Datasource, PluginDriver>> setPluginDriver(
		@PathParam("id")long datasourceId,
		@PathParam("pluginDriverId")long pluginDriverId) {
		return service.setPluginDriver(datasourceId, pluginDriverId);
	}

	@DELETE
	@Path("/{id}/plugin-driver")
	public Uni<Datasource> unsetPluginDriver(
		@PathParam("id")long datasourceId) {
		return service.unsetPluginDriver(datasourceId);
	}

	@GET
	@Path("/{id}/data-index-orphans")
	public Uni<List<DataIndex>> getDataIndexOrphans(@PathParam("id") long datasourceId) {
		return service.getDataIndexOrphans(datasourceId);
	}
}
