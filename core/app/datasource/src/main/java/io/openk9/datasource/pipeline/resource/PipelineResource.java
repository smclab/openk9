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

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.pipeline.actor.enrichitem.Token;
import io.openk9.datasource.pipeline.actor.enrichitem.TokenUtils;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding;
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityRef;

@Path("/pipeline")
public class PipelineResource {

	@POST
	@Path("/callback/{token-id}")
	public void callback(
		@PathParam("token-id")String tokenId, JsonObject body) {

		ActorSystem<?> actorSystem = actorSystemProvider.getActorSystem();

		ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);
		Token.SchedulingToken schedulingToken = TokenUtils.decode(tokenId);

		EntityRef<Token.Command> tokenEntityRef = clusterSharding.entityRefFor(
			Token.ENTITY_TYPE_KEY,
			ShardingKey.asString(
				schedulingToken.tenantId(), schedulingToken.scheduleId())
		);

		tokenEntityRef.tell(
			new Token.Callback(schedulingToken.token(), body.toBuffer().getBytes()));

	}

	@POST
	@RolesAllowed("k9-admin")
	@Path("/enrich-item/{enrich-item-id}")
	@Deprecated
	public Uni<JsonObject> callEnrichItem(
		@PathParam("enrich-item-id") long enrichItemId,
		JsonObject datasourcePayload) {

		return Uni.createFrom().nothing();

	}

	@Inject
	ActorSystemProvider actorSystemProvider;

}
