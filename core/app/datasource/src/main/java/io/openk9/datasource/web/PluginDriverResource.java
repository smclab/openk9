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

import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.dto.base.PluginDriverDTO;
import io.openk9.datasource.model.form.FormTemplate;
import io.openk9.datasource.service.PluginDriverService;
import io.openk9.datasource.web.dto.PluginDriverDocTypesDTO;
import io.openk9.datasource.web.dto.HealthDTO;
import io.openk9.datasource.web.dto.openapi.BucketDtoExamples;
import io.openk9.datasource.web.dto.openapi.PluginDriverDtoExamples;
import io.openk9.datasource.web.dto.openapi.SchedulerDtoExamples;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Set;

@ApplicationScoped
@Path("/pluginDrivers")
@RolesAllowed("k9-admin")
public class PluginDriverResource {

	@Inject
	PluginDriverService service;

	@POST
	@Path("/documentTypes/{id}")
	public Uni<Set<DocType>> createDocTypes(@PathParam("id") long id) {
		return service.createPluginDriverDocTypes(id);
	}

	@Operation(operationId = "document-types-plugin-driver")
	@Tag(description = "Return document types for a specific plugin driver")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "200",
					description = "List of templates returned",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = Response.class),
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
			@Parameter(description = "Id of Plugin Driver")
			@PathParam("id") long id) {
		return service.getDocTypes(id);
	}

	@Operation(operationId = "form")
	@Tag(description = "Return form template for specific plugin driver")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "200",
					description = "Form returned",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = Response.class),
									example = BucketDtoExamples.TEMPLATES_RESPONSE
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
			@Parameter(description = "Id of Plugin Driver")
			@PathParam("id") long id) {
		return service.getForm(id);
	}

	@Operation(operationId = "health")
	@Tag(description = "Perform health check for specific plugin driver by id")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "200",
					description = "Health Check Ok",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = Response.class),
									example = BucketDtoExamples.TEMPLATES_RESPONSE
							)
					}
			),
			@APIResponse(ref = "#/components/responses/bad-request"),
			@APIResponse(ref = "#/components/responses/not-found"),
			@APIResponse(ref = "#/components/responses/internal-server-error"),
	})
	@GET
	@Path("/health/{id}")
	public Uni<HealthDTO> getHealth(@PathParam("id") long id) {
		return service.getHealth(id);
	}

	@Operation(operationId = "health-dto")
	@Tag(description = "Perform health check for specific plugin driver using DTO")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "200",
					description = "Health Check Ok",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = Response.class),
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
											name = "Health DTO",
											value = SchedulerDtoExamples.TRIGGER_REQUEST
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
