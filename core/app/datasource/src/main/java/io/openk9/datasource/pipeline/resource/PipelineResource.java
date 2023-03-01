package io.openk9.datasource.pipeline.resource;

import io.openk9.auth.tenant.TenantResolver;
import io.openk9.datasource.pipeline.actor.IngestionActorSystem;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

import javax.annotation.security.RolesAllowed;
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

		actorSystem.callback(tokenId, body);

	}

	@POST
	@RolesAllowed("k9-admin")
	@Path("/enrich-item/{enrich-item-id}")
	public Uni<JsonObject> callEnrichItem(
		@PathParam("enrich-item-id") long enrichItemId,
		JsonObject datasourcePayload) {

		return actorSystem.callEnrichItem(
			enrichItemId, tenantResolver.getTenantName(),
			datasourcePayload.getMap());

	}

	@Inject
	IngestionActorSystem actorSystem;

	@Inject
	TenantResolver tenantResolver;

}
