package io.openk9.ingestion.web;

import io.openk9.ingestion.dto.IngestionDTO;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/v1/ingestion/")
public class IngestionEndpoint {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<String> ingestion(IngestionDTO dto) {

		return Uni
			.createFrom()
			.completionStage(() -> _emitter.emit(dto))
			.replaceWith(() -> "{}");

	}

	@Inject
	IngestionEmitter _emitter;

}