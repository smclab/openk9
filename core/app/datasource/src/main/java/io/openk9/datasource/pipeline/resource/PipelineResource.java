package io.openk9.datasource.pipeline.resource;

import io.openk9.datasource.pipeline.actor.EnrichPipelineActorSystem;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/pipeline")
public class PipelineResource {

	@POST
	@Path("/callback/{token-id}")
	public void callback(
		@PathParam("token-id")String tokenId, JsonObject body) {

		enrichPipelineActorSystem.callback(tokenId, body);

	}

	@Inject
	private EnrichPipelineActorSystem enrichPipelineActorSystem;

}
