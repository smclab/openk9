package io.openk9.datasource.pipeline.resource;

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import io.openk9.auth.tenant.TenantResolver;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.pipeline.SchedulationKeyUtils;
import io.openk9.datasource.pipeline.actor.enrichitem.Token;
import io.openk9.datasource.pipeline.actor.enrichitem.TokenUtils;
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

		ActorSystem<?> actorSystem = actorSystemProvider.getActorSystem();

		ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);
		Token.SchedulationToken schedulationToken = TokenUtils.decode(tokenId);

		EntityRef<Token.Command> tokenEntityRef = clusterSharding.entityRefFor(
			Token.ENTITY_TYPE_KEY,
			SchedulationKeyUtils.getValue(
				schedulationToken.tenantId(), schedulationToken.scheduleId()));

		tokenEntityRef.tell(
			new Token.Callback(schedulationToken.token(), body.toBuffer().getBytes()));

	}

	@POST
	@RolesAllowed("k9-admin")
	@Path("/enrich-item/{enrich-item-id}")
	public Uni<JsonObject> callEnrichItem(
		@PathParam("enrich-item-id") long enrichItemId,
		JsonObject datasourcePayload) {

		return Uni.createFrom().nothing();

	}

	@Inject
	ActorSystemProvider actorSystemProvider;

	@Inject
	TenantResolver tenantResolver;

}
