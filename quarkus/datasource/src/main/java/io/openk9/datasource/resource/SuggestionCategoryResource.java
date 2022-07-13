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

import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.dto.SuggestionCategoryDTO;
import io.openk9.datasource.resource.util.BaseK9EntityResource;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.SuggestionCategoryService;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/suggestion-categories")
public class SuggestionCategoryResource extends
	BaseK9EntityResource<SuggestionCategoryService, SuggestionCategory, SuggestionCategoryDTO> {

	protected SuggestionCategoryResource(SuggestionCategoryService service) {
		super(service);
	}

	@GET
	@Path("/{id}/doc-type-fields")
	public Uni<Page<DocTypeField>> getDocTypeFields(
		@PathParam("id") long suggestionCategoryId, @BeanParam Pageable pageable) {
		return service.getDocTypeFields(suggestionCategoryId, pageable);
	}

	@PUT
	@Path("/{id}/doc-type-fields/{docTypeFieldId}")
	public Uni<Void> addDocTypeField(
		@PathParam("id")long suggestionCategoryId,
		@PathParam("docTypeFieldId")long docTypeFieldId) {
		return service.addDocTypeField(suggestionCategoryId, docTypeFieldId);
	}

	@DELETE
	@Path("/{id}/doc-type-fields/{docTypeFieldId}")
	public Uni<Void> removeDocTypeField(
		@PathParam("id")long suggestionCategoryId,
		@PathParam("docTypeFieldId")long docTypeFieldId) {
		return service.removeDocTypeField(suggestionCategoryId, docTypeFieldId);
	}

}
