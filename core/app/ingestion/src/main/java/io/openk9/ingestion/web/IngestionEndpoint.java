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

import java.util.ArrayList;
import java.util.List;

import io.openk9.common.util.ingestion.PayloadType;
import io.openk9.ingestion.dto.IngestionDTO;
import io.openk9.ingestion.dto.IngestionDtoExamples;
import io.openk9.ingestion.exception.MissingDatasourcePayloadException;
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



import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@Path("/v1/ingestion/")
public class IngestionEndpoint {

	private static final String DETAILS_FIELD = "details";
	private static final String EMPTY_JSON = "{}";
	@Inject
	BinaryStorageEmitter _binaryStorageEmitter;
	@Inject
	Logger _logger;

	@Operation(operationId = "ingestion")
	@Tag(name = "Ingestion API", description = "Permits to ingest data and associated resources inside Openk9 and perform elaboration and indexing.")
	@APIResponses(value = {
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
											name = "Simple ingestion payload",
											value = IngestionDtoExamples.INGESTION_SIMPLE_DTO
									),
									@ExampleObject(
											name = "Ingestion payload passing resources and binaries",
											value = IngestionDtoExamples.INGESTION_WITH_RESOURCES_DTO
									),
									@ExampleObject(
											name = "Ingestion payload passing access control list on data",
											value = IngestionDtoExamples.INGESTION_WITH_ACL_DTO
									),
									@ExampleObject(
											name = "Deletion of an indexed content",
											value = IngestionDtoExamples.INGESTION_DELETED_DOCUMENT_DTO
									)
							}
					)
			}
	)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<String> ingestion(IngestionDTO dto) {

		if (dto.getDatasourcePayload() == null && _carriesContent(dto)) {
			return Uni.createFrom().failure(new MissingDatasourcePayloadException(
				"datasourcePayload is required for DOCUMENT and "
				+ "PARTIAL_DOCUMENT messages."));
		}

		if (dto.getType() == PayloadType.DELETED_DOCUMENT) {
			_dropUnusedFields(dto);
		}

		return _binaryStorageEmitter.emit(dto)
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

	@ServerExceptionMapper
	public RestResponse<JsonObject> mapException(
		MissingDatasourcePayloadException exception) {

		return RestResponse.status(Response.Status.BAD_REQUEST,
			JsonObject.of(
				DETAILS_FIELD,
				exception.getMessage()
			));
	}

	/**
	 * Only a DOCUMENT and a PARTIAL_DOCUMENT carry a content, so only they
	 * need a datasourcePayload; a message without type is a DOCUMENT.
	 */
	private static boolean _carriesContent(IngestionDTO dto) {
		return dto.getType() == null
			|| dto.getType() == PayloadType.DOCUMENT
			|| dto.getType() == PayloadType.PARTIAL_DOCUMENT;
	}

	/**
	 * A DELETED_DOCUMENT only names the content to delete: the fields that
	 * describe a content are dropped, so its binaries are not stored and
	 * the queue gets only what the deletion needs.
	 */
	private void _dropUnusedFields(IngestionDTO dto) {

		List<String> unusedFields = new ArrayList<>();

		if (dto.getDatasourcePayload() != null) {
			unusedFields.add("datasourcePayload");
			dto.setDatasourcePayload(null);
		}

		if (dto.getRawContent() != null) {
			unusedFields.add("rawContent");
			dto.setRawContent(null);
		}

		if (dto.getResources() != null) {
			unusedFields.add("resources");
			dto.setResources(null);
		}

		if (dto.getAcl() != null) {
			unusedFields.add("acl");
			dto.setAcl(null);
		}

		if (!unusedFields.isEmpty()) {
			_logger.warnf(
				"DELETED_DOCUMENT %s of datasource %d ignores the fields %s: "
				+ "a deletion needs none of them.",
				dto.getContentId(), dto.getDatasourceId(), unusedFields);
		}
	}

}