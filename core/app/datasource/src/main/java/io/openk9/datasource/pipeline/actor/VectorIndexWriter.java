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

package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.datasource.pipeline.service.EmbeddingService;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Writer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch._types.ErrorCause;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.enterprise.inject.spi.CDI;

public class VectorIndexWriter extends AbstractBehavior<Writer.Command> {

	private final static org.jboss.logging.Logger logger =
		Logger.getLogger(VectorIndexWriter.class);

	private final OpenSearchAsyncClient asyncClient;
	private final String vectorIndexName;
	private final ActorRef<Writer.Response> replyTo;

	public VectorIndexWriter(
		ActorContext<Writer.Command> context,
		SchedulerDTO scheduler,
		ActorRef<Writer.Response> replyTo) {

		super(context);
		this.asyncClient = CDI.current().select(OpenSearchAsyncClient.class).get();
		this.vectorIndexName = scheduler.getVectorIndexName();
		this.replyTo = replyTo;
	}

	public static Behavior<Writer.Command> create(
		SchedulerDTO scheduler, ActorRef<Writer.Response> replyTo) {

		return Behaviors.setup(ctx -> new VectorIndexWriter(ctx, scheduler, replyTo));
	}

	@Override
	public Receive<Writer.Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Writer.Start.class, this::onStart)
			.onMessage(AsyncResponse.class, this::onAsyncResponse)
			.build();
	}

	private Behavior<Writer.Command> onStart(Writer.Start start) {

		var data = start.dataPayload();
		var heldMessage = start.heldMessage();

		EmbeddingService.EmbeddedChunks dataPayload = Json.decodeValue(
			Buffer.buffer(data), EmbeddingService.EmbeddedChunks.class);

		var bulkOperations = new ArrayList<BulkOperation>();

		for (EmbeddingService.EmbeddedChunk chunk : dataPayload.list()) {
			var document = JsonObject.mapFrom(chunk);

			Map<?, ?> acl = (Map<?, ?>) document.getValue("acl");
			if (acl == null || acl.isEmpty()) {
				document.put("acl", Map.of("public", true));
			}

			var bulkOperation = new BulkOperation.Builder()
				.index(new IndexOperation.Builder<>()
					.index(vectorIndexName)
					.document(document)
					.build())
				.build();

			bulkOperations.add(bulkOperation);

		}

		var bulkRequest = new BulkRequest.Builder()
			.index(vectorIndexName)
			.operations(bulkOperations)
			.build();

		try {
			getContext().pipeToSelf(
				asyncClient.bulk(bulkRequest),
				(bulkResponse, exception) -> new AsyncResponse(
					dataPayload, heldMessage, bulkResponse, (Exception) exception)
			);
		}
		catch (IOException e) {
			replyTo.tell(new Writer.Failure(e, heldMessage));
		}

		return this;
	}

	private Behavior<Writer.Command> onAsyncResponse(
		AsyncResponse brc) {

		var bulkResponse = brc.bulkResponse;
		var heldMessage = brc.heldMessage;
		var embeddedChunks = brc.embeddedChunks;
		var throwable = brc.exception;

		if (bulkResponse != null) {

			if (bulkResponse.errors()) {
				String reasons = bulkResponse.items()
					.stream()
					.map(BulkResponseItem::error)
					.filter(Objects::nonNull)
					.map(ErrorCause::reason)
					.collect(Collectors.joining());

				logger.error("Bulk request error: " + reasons);
				replyTo.tell(new Writer.Failure(
					new RuntimeException(reasons),
					heldMessage
				));
			}

		}

		if (throwable != null) {
			logger.error("Error on bulk request", throwable);
			replyTo.tell(new Writer.Failure(throwable, heldMessage));
		}
		else {
			var dataPayload = Json.encodeToBuffer(embeddedChunks).getBytes();
			replyTo.tell(new Writer.Success(dataPayload, heldMessage));
		}

		return this;
	}


	private record AsyncResponse(
		EmbeddingService.EmbeddedChunks embeddedChunks,
		HeldMessage heldMessage,
		BulkResponse bulkResponse,
		Exception exception
	) implements Writer.Command {}

}
