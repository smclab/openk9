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

package io.openk9.api.aggregator.api;

import io.openk9.api.aggregator.client.dto.ReindexRequestDTO;
import io.openk9.api.aggregator.client.dto.ReindexResponseDTO;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.List;

public interface ReindexHttp {

	@RolesAllowed({"datasource-write", "datasource-reindex", "admin"})
	@SecurityRequirement(name = "SecurityScheme")
	@POST
	@Path("/v1/index/reindex")
	Uni<List<ReindexResponseDTO>> reindex(ReindexRequestDTO dto);
}