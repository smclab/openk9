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

import io.openk9.datasource.enricher.HttpEnricherInfo;
import io.openk9.datasource.model.form.FormTemplate;
import io.openk9.datasource.service.EnrichItemService;
import io.openk9.datasource.web.dto.EnricherInputDTO;
import io.openk9.datasource.web.dto.PluginDriverHealthDTO;
import io.openk9.datasource.web.dto.openapi.BucketDtoExamples;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.client.HttpResponse;
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
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@ApplicationScoped
@Path("/enrichers")
@RolesAllowed("k9-admin")
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
                                    example = BucketDtoExamples.TEMPLATES_RESPONSE
                            )
                    }
            ),
            @APIResponse(ref = "#/components/responses/bad-request"),
            @APIResponse(ref = "#/components/responses/not-found"),
            @APIResponse(ref = "#/components/responses/internal-server-error"),
    })
    @GET
    @Path("/health/{serviceName}")
    public Uni<PluginDriverHealthDTO> getHealth(
            @Parameter(description = "ServiceName of enricher")
            @PathParam("serviceName") HttpEnricherInfo enricherInfo) {
        return service.getHealth(enricherInfo);
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
                                    example = BucketDtoExamples.TEMPLATES_RESPONSE
                            )
                    }
            ),
            @APIResponse(ref = "#/components/responses/bad-request"),
            @APIResponse(ref = "#/components/responses/not-found"),
            @APIResponse(ref = "#/components/responses/internal-server-error"),
    })
    @GET
    @Path("/form/{serviceName}")
    public Uni<FormTemplate> getForm(
            @Parameter(description = "ServiceName of enricher")
            @PathParam("serviceName") HttpEnricherInfo enricherInfo) {
        return service.getForm(enricherInfo);
    }

    @Operation(operationId = "process")
    @Tag(name = "Process API", description = "Start enrichment data process for specific enricher")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "success"),
            @APIResponse(responseCode = "404", description = "not found"),
            @APIResponse(responseCode = "400", description = "invalid"),
            @APIResponse(ref = "#/components/responses/bad-request"),
            @APIResponse(ref = "#/components/responses/not-found"),
            @APIResponse(ref = "#/components/responses/internal-server-error"),
    })
    @POST
    @Path("/process/{serviceName}")
    public Uni<HttpResponse<?>> process(
            @Parameter(description = "ServiceName of enricher")
            @PathParam("serviceName") HttpEnricherInfo enricherInfo, EnricherInputDTO enricherInputDTO){
        return service.process(enricherInfo, enricherInputDTO);
    }
}
