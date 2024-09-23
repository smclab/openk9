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

import io.openk9.datasource.model.dto.HybridSearchPipelineDTO;
import io.openk9.datasource.model.dto.SearchPipelineResponseDTO;
import io.openk9.datasource.service.SearchConfigService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@ApplicationScoped
@Path("/v1/search-config")
public class SearchConfigResource {

	@Inject
	SearchConfigService service;

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
