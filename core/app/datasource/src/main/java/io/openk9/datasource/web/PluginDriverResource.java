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

import io.openk9.datasource.model.dto.PluginDriverDTO;
import io.openk9.datasource.service.PluginDriverService;
import io.openk9.datasource.web.dto.PluginDriverHealthDTO;
import io.openk9.datasource.web.dto.form.FormField;
import io.openk9.datasource.web.dto.form.FormFieldValue;
import io.openk9.datasource.web.dto.form.PluginDriverFormDTO;
import io.openk9.datasource.web.dto.form.Type;
import io.smallrye.mutiny.Uni;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@ApplicationScoped
@Path("/pluginDrivers")
@RolesAllowed("k9-admin")
public class PluginDriverResource {

	@Inject
	PluginDriverService service;

	@GET
	@Path("/health/{id}")
	public Uni<PluginDriverHealthDTO> getHealth(@PathParam("id") long id) {
		// return service.getHealth(id);
		return Uni.createFrom().item(PluginDriverHealthDTO.builder()
			.status(PluginDriverHealthDTO.Status.UNKOWN)
			.build()
		);
	}

	@POST
	@Path("/health")
	public Uni<PluginDriverHealthDTO> getHealth(PluginDriverDTO pluginDriverDTO) {
		// return service.getHealth(pluginDriverDTO);
		return Uni.createFrom().item(PluginDriverHealthDTO.builder()
			.status(PluginDriverHealthDTO.Status.UNKOWN)
			.build()
		);
	}

	@GET
	@Path("/form/{id}")
	public Uni<PluginDriverFormDTO> getForm(@PathParam("id") long id) {
		// return service.getHealth(id);
		return Uni.createFrom().item(PluginDriverFormDTO.builder()
			.field(FormField.builder()
				.name("key1")
				.type(Type.SELECT)
				.size(2)
				.required(false)
				.value(FormFieldValue.builder()
					.value("value1")
					.isDefault(true)
					.build()
				)
				.value(FormFieldValue.builder()
					.value("value2")
					.isDefault(false)
					.build()
				)
				.build()
			)
			.build());
	}

	@POST
	@Path("/form")
	public Uni<PluginDriverFormDTO> getForm(PluginDriverDTO pluginDriverDTO) {
		// return service.getHealth(pluginDriverDTO);
		return Uni.createFrom().item(PluginDriverFormDTO.builder()
			.field(FormField.builder()
				.name("key1")
				.type(Type.SELECT)
				.size(2)
				.required(false)
				.value(FormFieldValue.builder()
					.value("value1")
					.isDefault(true)
					.build()
				)
				.value(FormFieldValue.builder()
					.value("value2")
					.isDefault(false)
					.build()
				)
				.build()
			)
			.build());
	}

}
