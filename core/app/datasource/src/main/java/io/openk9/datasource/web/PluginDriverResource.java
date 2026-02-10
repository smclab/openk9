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

import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.dto.base.PluginDriverDTO;
import io.openk9.datasource.model.form.FormTemplate;
import io.openk9.datasource.service.PluginDriverService;
import io.openk9.datasource.web.dto.HealthDTO;
import io.openk9.datasource.web.dto.PluginDriverDocTypesDTO;
import io.openk9.datasource.web.dto.openapi.BucketDtoExamples;
import io.openk9.datasource.web.dto.openapi.PluginDriverDtoExamples;
import io.openk9.datasource.web.dto.openapi.SchedulerDtoExamples;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;


@ApplicationScoped
@Path("/pluginDrivers")
public class PluginDriverResource {

	@Inject
	PluginDriverService service;

	@POST
	@Path("/documentTypes/{id}")
	public Uni<Set<DocType>> createDocTypes(
		@Parameter(description = "Plugin Driver's id")
		@PathParam("id") long id) {

		return service.createPluginDriverDocTypes(id);
	}

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "The list of the Document Types created from this Plugin Driver",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = PluginDriverDocTypesDTO.class),
					example = BucketDtoExamples.TEMPLATES_RESPONSE
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@GET
	@Path("/documentTypes/{id}")
	public Uni<PluginDriverDocTypesDTO> getDocTypes(
		@Parameter(description = "Plugin Driver's id")
		@PathParam("id") long id) {

		return service.getDocTypes(id);
	}

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "Form returned",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = FormTemplate.class)
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@GET
	@Path("/form/{id}")
	public Uni<FormTemplate> getForm(
		@Parameter(description = "Plugin Driver's id")
		@PathParam("id") long id) {

		return service.getForm(id);
	}

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "Plugin Driver Health Status",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = HealthDTO.class),
					example = PluginDriverDtoExamples.HEALTH_STATUS
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@GET
	@Path("/health/{id}")
	public Uni<HealthDTO> getHealth(
		@Parameter(description = "Plugin Driver's id")
		@PathParam("id") long id) {

		return service.getHealth(id);
	}

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "Plugin Driver Health Status",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = HealthDTO.class),
					example = PluginDriverDtoExamples.HEALTH_STATUS
				)
			}
		),
		@APIResponse(ref = "#/components/responses/bad-request"),
		@APIResponse(ref = "#/components/responses/not-found"),
		@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@RequestBody(
		content = {
			@Content(
				mediaType = MediaType.APPLICATION_JSON,
				schema = @Schema(implementation = PluginDriverDTO.class),
				examples = {
					@ExampleObject(
						name = "PluginDriver DTO",
						value = PluginDriverDtoExamples.PLUGIN_DRIVER_DTO
					)
				}
			)
		}
	)
	@POST
	@Path("/health")
	public Uni<HealthDTO> getHealth(PluginDriverDTO pluginDriverDTO) {
		return service.getHealth(pluginDriverDTO);
	}

}
