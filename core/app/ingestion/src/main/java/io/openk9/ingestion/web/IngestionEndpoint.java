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

package io.openk9.ingestion.web;

import io.openk9.ingestion.dto.IngestionDTO;
import io.openk9.ingestion.dto.IngestionDtoExamples;
import io.openk9.ingestion.exception.NoSuchQueueException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;



import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;


public class IngestionEndpoint {

	private static final String DETAILS_FIELD = "details";
	private static final String EMPTY_JSON = "{}";
	@Inject
	FileManagerEmitter _fileManagerEmitter;

	@Path("/v1/ingestion/")
	@Operation(operationId = "ingestion")
	@Tag(name = "Ingestion API")
	@APIResponses(value = {
			@APIResponse(responseCode = "200", description = "success"),
			@APIResponse(responseCode = "404", description = "not found"),
			@APIResponse(responseCode = "400", description = "invalid"),
			@APIResponse(
					responseCode = "200",
					description = "Ingestion successful",
					content = {
							@Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(implementation = Response.class),
									example = IngestionDtoExamples.INGESTION_RESPONSE
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
							schema = @Schema(implementation = IngestionDTO.class),
							examples = {
									@ExampleObject(
											name = "simple",
											value = IngestionDtoExamples.INGESTION_SIMPLE_DTO
									),
									@ExampleObject(
											name = "resources",
											value = IngestionDtoExamples.INGESTION_WITH_RESOURCES_DTO
									),
									@ExampleObject(
											name = "acl",
											value = IngestionDtoExamples.INGESTION_WITH_ACL_DTO
									)
							}
					)
			}
	)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<String> ingestion(IngestionDTO dto) {

		return _fileManagerEmitter.emit(dto)
			.replaceWith(() -> EMPTY_JSON);

	}

	@ServerExceptionMapper
	public RestResponse<JsonObject> mapException(NoSuchQueueException exception) {
		return RestResponse.status(Response.Status.NOT_ACCEPTABLE,
			JsonObject.of(
				DETAILS_FIELD,
				"No such queue for this schedule."
			));
	}

}