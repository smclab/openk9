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

package io.openk9.datasource.web;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import io.openk9.datasource.model.dto.PluginDriverDTO;
import io.openk9.datasource.service.PluginDriverService;
import io.openk9.datasource.web.dto.PluginDriverDocTypesDTO;
import io.openk9.datasource.web.dto.PluginDriverHealthDTO;
import io.openk9.datasource.web.dto.form.PluginDriverFormDTO;

import io.smallrye.mutiny.Uni;

@ApplicationScoped
@Path("/pluginDrivers")
@RolesAllowed("k9-admin")
public class PluginDriverResource {

	@Inject
	PluginDriverService service;

	@GET
	@Path("/documentTypes")
	public Uni<PluginDriverDocTypesDTO> getDocTypes(@PathParam("id") long id) {
		return service.getDocTypes(id);
	}

	@GET
	@Path("/form/{id}")
	public Uni<PluginDriverFormDTO> getForm(@PathParam("id") long id) {
		return service.getForm(id);
	}

	@POST
	@Path("/form")
	public Uni<PluginDriverFormDTO> getForm(PluginDriverDTO pluginDriverDTO) {
		return service.getForm(pluginDriverDTO);
	}

	@GET
	@Path("/health/{id}")
	public Uni<PluginDriverHealthDTO> getHealth(@PathParam("id") long id) {
		return service.getHealth(id);
	}

	@POST
	@Path("/health")
	public Uni<PluginDriverHealthDTO> getHealth(PluginDriverDTO pluginDriverDTO) {
		return service.getHealth(pluginDriverDTO);
	}

}
