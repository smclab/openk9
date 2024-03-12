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

package io.openk9.datasource.pipeline.resource;

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.pipeline.actor.enrichitem.Token;
import io.openk9.datasource.pipeline.actor.enrichitem.TokenUtils;
import io.openk9.datasource.pipeline.util.SchedulingKeyUtils;
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
			SchedulingKeyUtils.asString(
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

}
