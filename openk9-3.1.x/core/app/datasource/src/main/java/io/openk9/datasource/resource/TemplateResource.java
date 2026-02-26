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

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.dto.base.DocTypeTemplateDTO;
import io.openk9.datasource.resource.util.BaseK9EntityResource;
import io.openk9.datasource.service.DocTypeTemplateService;

import io.smallrye.mutiny.Uni;

@Path("/templates")
public class TemplateResource extends
	BaseK9EntityResource<DocTypeTemplateService, DocTypeTemplate, DocTypeTemplateDTO> {

	protected TemplateResource(DocTypeTemplateService service) {
		super(service);
	}

	@GET
	@Produces("text/javascript")
	@Path("/{id}/compiled")
	@PermitAll
	public Uni<String> getTemplateCompiled(@PathParam("id") long docTypeTemplateId) {

		return service.findById(docTypeTemplateId).map(
			DocTypeTemplate::getCompiled);
	}

}
