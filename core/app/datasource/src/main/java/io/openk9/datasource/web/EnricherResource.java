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

import io.openk9.datasource.model.ResourceUri;
import io.openk9.datasource.web.dto.openapi.SchedulerDtoExamples;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.openk9.datasource.model.form.FormTemplate;
import io.openk9.datasource.service.EnrichItemService;
import io.openk9.datasource.web.dto.HealthDTO;
import io.openk9.datasource.web.dto.openapi.PluginDriverDtoExamples;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;


@ApplicationScoped
@Path("/enrichers")
public class EnricherResource {

	@Inject
	EnrichItemService service;

	@Operation(operationId = "health")
	@Tag(name = "Health API", description = "Perform health check for enricher")
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
				schema = @Schema(implementation = ResourceUri.class),
				examples = {
					@ExampleObject(
						name = "ResourceUri",
						value = SchedulerDtoExamples.RESOURCE_URI_REQUEST
					)
				}
			)
		}
	)
	@POST
	@Path("/health/")
	public Uni<HealthDTO> getHealth(ResourceUri resourceUri) {
		return service.getHealth(resourceUri);
	}

	@Operation(operationId = "form")
	@Tag(name = "Form API", description = "Return form template for specific enricher")
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
					example = PluginDriverDtoExamples.FORM_RESPONSE
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
				schema = @Schema(implementation = ResourceUri.class),
				examples = {
					@ExampleObject(
						name = "ResourceUri",
						value = SchedulerDtoExamples.RESOURCE_URI_REQUEST
					)
				}
			)
		}
	)
	@POST
	@Path("/form")
	public Uni<FormTemplate> getForm(ResourceUri resourceUri) {
		return service.getForm(resourceUri);
	}

}
