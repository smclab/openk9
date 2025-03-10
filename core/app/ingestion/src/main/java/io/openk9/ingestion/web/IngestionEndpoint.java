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
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@Path("/v1/ingestion/")
public class IngestionEndpoint {

	private static final String DETAILS_FIELD = "details";
	private static final String EMPTY_JSON = "{}";
	@Inject
	FileManagerEmitter _fileManagerEmitter;

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