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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.openk9.datasource.model.dto.request.HybridSearchPipelineDTO;
import io.openk9.datasource.model.dto.response.SearchPipelineResponseDTO;
import io.openk9.datasource.service.SearchConfigService;
import io.openk9.datasource.web.dto.openapi.SearchConfigDtoExamples;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@ApplicationScoped
@Path("/v1/search-config")
public class SearchConfigResource {

	@Inject
	SearchConfigService service;

	@APIResponses(value = {
		@APIResponse(
			responseCode = "200",
			description = "Hybrid search configure successfully",
			content = {
				@Content(
					mediaType = MediaType.APPLICATION_JSON,
					schema = @Schema(implementation = SearchPipelineResponseDTO.class),
					example = SearchConfigDtoExamples.CONFIGURE_HYBRID_SEARCH_RESPONSE
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
				schema = @Schema(implementation = HybridSearchPipelineDTO.class),
				examples = {
					@ExampleObject(
						name = "Configure Hybrid Search DTO",
						value = SearchConfigDtoExamples.CONFIGURE_HYBRID_SEARCH_REQUEST
					)
				}
			)
		}
	)
	@Path("/{id}/configure-hybrid-search")
	@POST
	public Uni<SearchPipelineResponseDTO> configureHybridSearch(
		@PathParam("id") long id,
		HybridSearchPipelineDTO pipelineDTO) {

		return service.configureHybridSearch(
			id,
			pipelineDTO == null ? HybridSearchPipelineDTO.DEFAULT : pipelineDTO
		);

	}

}
