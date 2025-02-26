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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import jakarta.enterprise.inject.spi.CDI;

import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Writer;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import io.vertx.core.json.Json;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.jboss.logging.Logger;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch._types.ErrorCause;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;

public class VectorIndexWriter extends AbstractBehavior<Writer.Command> {

	private final static org.jboss.logging.Logger log =
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
		this.vectorIndexName = scheduler.getIndexName();
		this.replyTo = replyTo;

	}

	public static Behavior<Writer.Command> create(
		SchedulerDTO scheduler, ActorRef<Writer.Response> replyTo) {

		return Behaviors.setup(ctx -> new VectorIndexWriter(ctx, scheduler, replyTo));
	}

	protected static List<Object> parsePayload(byte[] json) {
		try {
			var documentContext = JsonPath
				.using(Configuration.defaultConfiguration())
				.parseUtf8(json);

			var root = documentContext.read("$");

			if (root instanceof List) {
				return documentContext.read("$.*");
			}
			else {
				return List.of(root);
			}
		}
		catch (IllegalArgumentException e) {
			log.warn("Cannot parse the json payload", e);
			return List.of();
		}
	}

	@Override
	public Receive<Writer.Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Writer.Start.class, this::onStart)
			.onMessage(IndexDocumentResponse.class, this::onIndexDocumentResponse)
			.build();
	}

	private Behavior<Writer.Command> onIndexDocumentResponse(
		IndexDocumentResponse brc) {

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

				if (log.isDebugEnabled()) {
					log.debugf(
						"%s: Bulk request error: %s", heldMessage, reasons);
				}

				replyTo.tell(new Writer.Failure(
					new WriterException(reasons),
					heldMessage
				));
			}

		}

		if (throwable != null) {

			if (log.isDebugEnabled()) {
				log.debugf(throwable, "%s: Error on bulk request", heldMessage);
			}

			replyTo.tell(new Writer.Failure(new WriterException(throwable), heldMessage));
		}
		else {
			var dataPayload = Json.encodeToBuffer(embeddedChunks).getBytes();
			replyTo.tell(new Writer.Success(dataPayload, heldMessage));
		}

		return this;
	}

	private Behavior<Writer.Command> onStart(Writer.Start start) {

		var embeddedChunks = start.dataPayload();
		var heldMessage = start.heldMessage();

		var bulkOperations = new ArrayList<BulkOperation>();

		for (Object chunk : parsePayload(embeddedChunks)) {

			var bulkOperation = new BulkOperation.Builder()
				.index(new IndexOperation.Builder<>()
					.index(vectorIndexName)
					.document(chunk)
					.build())
				.build();

			bulkOperations.add(bulkOperation);

			if (log.isTraceEnabled()) {
				log.tracef("%s: Add a new bulk operation", heldMessage);
			}
		}

		if (bulkOperations.isEmpty()) {

			if (log.isDebugEnabled()) {
				log.debugf(
					"%s: There isn't any operation to send to the server.",
					heldMessage
				);
			}

			replyTo.tell(new Writer.Failure(
					new WriterException("Nothing to write"),
					heldMessage
				)
			);

		}

		var bulkRequest = new BulkRequest.Builder()
			.index(vectorIndexName)
			.operations(bulkOperations)
			.build();

		try {

			getContext().pipeToSelf(
				asyncClient.bulk(bulkRequest),
				(bulkResponse, exception) -> new IndexDocumentResponse(
					embeddedChunks, heldMessage, bulkResponse, (Exception) exception)
			);

		}
		catch (IOException e) {
			replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));
		}

		return this;

	}

	private record IndexDocumentResponse(
		byte[] embeddedChunks,
		HeldMessage heldMessage,
		BulkResponse bulkResponse,
		Exception exception
	) implements Writer.Command {}

}
