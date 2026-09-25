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

import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.pipeline.actor.EnrichPipeline;
import io.openk9.datasource.pipeline.actor.enrichitem.CallbackToken;
import io.openk9.datasource.pipeline.stages.working.Processor;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding;
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityRef;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("/pipeline")
public class PipelineResource {

	private static final Duration ASK_TIMEOUT = Duration.ofSeconds(10);

	@Inject
	ActorSystemProvider actorSystemProvider;

	/**
	 * Receives the result of an asynchronous enricher and hands it to the
	 * pipeline entity awaiting it.
	 *
	 * @param tokenId the token the enricher received as {@code replyTo}
	 * @param body the enricher result
	 * @return 202 when the callback was awaited, 404 when no pipeline awaits
	 * it, 400 when the token is malformed, 503 when the pipeline did not
	 * answer in time
	 */
	@POST
	@Path("/callback/{token-id}")
	@Operation(
		summary = "Callback",
		description = "Delivers the result of an asynchronous enricher to the "
			+ "pipeline that is awaiting it."
	)
	@APIResponse(responseCode = "202", description = "Callback delivered")
	@APIResponse(responseCode = "400", description = "Malformed token")
	@APIResponse(responseCode = "404", description = "No pipeline awaits this token")
	@APIResponse(
		responseCode = "503",
		description = "The pipeline did not answer in time, the callback can be retried"
	)
	public Uni<Response> callback(
		@PathParam("token-id") String tokenId, JsonObject body) {

		CallbackToken token;

		try {
			token = CallbackToken.decode(tokenId);
		}
		catch (IllegalArgumentException e) {
			return Uni.createFrom().item(
				Response.status(Response.Status.BAD_REQUEST).build());
		}

		var actorSystem = actorSystemProvider.getActorSystem();

		EntityRef<Processor.Command> pipeline = ClusterSharding
			.get(actorSystem)
			.entityRefFor(
				EnrichPipeline.ENTITY_TYPE_KEY,
				token.processKey().asString());

		byte[] payload = body == null
			? new byte[0]
			: body.toBuffer().getBytes();

		return Uni.createFrom()
			.completionStage(pipeline.<EnrichPipeline.CallbackResponse>ask(
				replyTo -> new EnrichPipeline.Callback(token.nonce(), payload, replyTo),
				ASK_TIMEOUT
			))
			.map(response -> switch (response) {
				case ACCEPTED -> Response.accepted().build();
				case UNKNOWN -> Response.status(Response.Status.NOT_FOUND).build();
			})
			.onFailure(PipelineResource::isTimeout)
			.recoverWithItem(() -> Response
				.status(Response.Status.SERVICE_UNAVAILABLE)
				.build());
	}

	// no answer within the ask timeout, e.g. a node unreachable or the
	// shards rebalancing: the entity may still exist, so not a 404
	static boolean isTimeout(Throwable throwable) {
		var cause = throwable instanceof CompletionException
					&& throwable.getCause() != null
			? throwable.getCause()
			: throwable;

		return cause instanceof TimeoutException;
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

}
