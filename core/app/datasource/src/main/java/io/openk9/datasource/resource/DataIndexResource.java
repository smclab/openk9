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

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.dto.base.DataIndexDTO;
import io.openk9.datasource.resource.util.BaseK9EntityResource;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.DataIndexService;
import io.openk9.datasource.service.util.Tuple2;

import io.smallrye.mutiny.Uni;

@Path("/data-indexes")
@RolesAllowed("k9-admin")
@Deprecated
public class DataIndexResource extends
	BaseK9EntityResource<DataIndexService, DataIndex, DataIndexDTO> {

	protected DataIndexResource(DataIndexService service) {
		super(service);
	}

	@GET
	@Path("/{id}/doc-types")
	public Uni<Page<DocType>> getDocTypes(
		@PathParam("id") long id, @BeanParam Pageable pageable,
		@QueryParam("searchText") String searchText) {
		return service.getDocTypes(id, pageable, searchText);
	}

	@PUT
	@Path("/{id}/doc-types/{docTypeId}")
	public Uni<Tuple2<DataIndex, DocType>> addDocType(
		@PathParam("id")long dataIndexId,
		@PathParam("docTypeId")long docTypeId) {
		return service.addDocType(dataIndexId, docTypeId);
	}

	@DELETE
	@Path("/{id}/doc-types/{docTypeId}")
	public Uni<Tuple2<DataIndex, DocType>> removeDocType(
		@PathParam("id")long dataIndexId,
		@PathParam("docTypeId")long docTypeId) {
		return service.removeDocType(dataIndexId, docTypeId);
	}

}
