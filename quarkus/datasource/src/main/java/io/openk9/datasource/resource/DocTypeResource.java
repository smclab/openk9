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

import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.DocTypeDTO;
import io.openk9.datasource.model.dto.DocTypeFieldDTO;
import io.openk9.datasource.resource.util.BaseK9EntityResource;
import io.openk9.datasource.service.DocTypeService;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Collection;

@Path("/doc-types")
public class DocTypeResource extends
	BaseK9EntityResource<DocTypeService, DocType, DocTypeDTO> {

	protected DocTypeResource(DocTypeService service) {
		super(service);
	}

	@GET
	@Path("/{id}/doc-type-fields")
	public Uni<Collection<DocTypeField>> getDocTypeFields(
		@PathParam("id") long id) {
		return service.getDocTypeFields(id);
	}

	@PUT
	@Path("/{id}/doc-type-fields")
	public Uni<DocTypeField> addDocTypeField(
		@PathParam("id")long id, DocTypeFieldDTO docTypeFieldDTO) {
		return service.addDocTypeField(id, docTypeFieldDTO);
	}

	@DELETE
	@Path("/{id}/doc-type-fields/{docTypeFieldId}")
	public Uni<Void> removeDocTypeField(
		@PathParam("id")long id, @PathParam("docTypeFieldId")long docTypeFieldId) {
		return service.removeDocTypeField(id, docTypeFieldId);
	}

}
