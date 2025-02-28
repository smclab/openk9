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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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
import org.opensearch.client.opensearch.core.DeleteByQueryResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;

public class VectorIndexWriter extends AbstractBehavior<Writer.Command> {

	private final static org.jboss.logging.Logger log =
		Logger.getLogger(VectorIndexWriter.class);

	private final OpenSearchAsyncClient asyncClient;
	private final String indexName;
	private final ActorRef<Writer.Response> replyTo;

	public VectorIndexWriter(
		ActorContext<Writer.Command> context,
		SchedulerDTO scheduler,
		ActorRef<Writer.Response> replyTo) {

		super(context);
		this.asyncClient = CDI.current().select(OpenSearchAsyncClient.class).get();
		this.indexName = scheduler.getIndexName();
		this.replyTo = replyTo;

	}

	public static Behavior<Writer.Command> create(
		SchedulerDTO scheduler, ActorRef<Writer.Response> replyTo) {

		return Behaviors.setup(ctx ->
			new VectorIndexWriter(ctx, scheduler, replyTo));
	}

	@Override
	public Receive<Writer.Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Writer.Start.class, this::onStart)
			.onMessage(WriteDocuments.class, this::onWriteDocuments)
			.onMessage(IndexDocumentResponse.class, this::onIndexDocumentResponse)
			.build();
	}

	protected static List<Map<String, Object>> parseChunks(byte[] json) {
		try {
			var documentContext = JsonPath
				.using(Configuration.defaultConfiguration())
				.parseUtf8(json);

			Object root = documentContext.read("$");

			if (root instanceof List) {
				return (List<Map<String, Object>>) root;
			}
			else {
				return List.of((Map<String, Object>) root);
			}
		}
		catch (IllegalArgumentException e) {
			log.warn("Cannot parse the json payload", e);
			return List.of();
		}
	}

	private CompletableFuture<DeleteByQueryResponse> deleteChunksByContentId(HeldMessage heldMessage)
		throws IOException {

		return asyncClient.deleteByQuery(delete -> delete
			.index(indexName)
			.query(query -> query
				.match(match -> match
					.field("contentId.keyword")
					.query(fieldValue -> fieldValue
						.stringValue(heldMessage.contentId()))
				))
		);
	}

	private Behavior<Writer.Command> onIndexDocumentResponse(
		IndexDocumentResponse indexDocumentResponse) {

		var bulkResponse = indexDocumentResponse.bulkResponse;
		var heldMessage = indexDocumentResponse.heldMessage;
		var embeddedChunks = indexDocumentResponse.dataPayload;
		var throwable = indexDocumentResponse.exception;

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

				return this;
			}

		}

		if (throwable != null) {

			if (log.isDebugEnabled()) {
				log.debugf(throwable, "%s: Error on bulk request", heldMessage);
			}

			replyTo.tell(new Writer.Failure(new WriterException(throwable), heldMessage));

			return this;

		}

		var dataPayload = Json.encodeToBuffer(embeddedChunks).getBytes();
		replyTo.tell(new Writer.Success(dataPayload, heldMessage));

		return this;
	}

	private Behavior<Writer.Command> onStart(Writer.Start start) {

		var dataPayload = start.dataPayload();
		var heldMessage = start.heldMessage();

		var chunks = parseChunks(dataPayload);

		if (chunks.isEmpty()) {
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

			return this;
		}

		try {
			getContext().pipeToSelf(
				deleteChunksByContentId(heldMessage),
				(deleteChunksResponse, throwable) ->
					new WriteDocuments(
						deleteChunksResponse,
						throwable,
						chunks,
						heldMessage,
						dataPayload
					)
			);
		}
		catch (IOException e) {
			replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));
		}

		return this;

	}

	private Behavior<Writer.Command> onWriteDocuments(WriteDocuments writeDocuments) {

		var heldMessage = writeDocuments.heldMessage();
		var chunks = writeDocuments.chunks();
		var dataPayload = writeDocuments.dataPayload();

		List<BulkOperation> bulkOperations = new ArrayList<>();

		for (Map<String, Object> chunk : chunks) {

			// fallback for acl mapping
			try {

				var acl = (Map<String, Object>) chunk.get("acl");

				if (acl == null || acl.isEmpty()) {
					chunk.put("acl", Map.of("public", true));
				}

			}
			catch (Exception e) {

				replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));

				return this;
			}

			var bulkOperation = new BulkOperation.Builder()
				.index(new IndexOperation.Builder<>()
					.index(indexName)
					.document(chunk)
					.build())
				.build();

			bulkOperations.add(bulkOperation);

			if (log.isTraceEnabled()) {
				log.tracef("%s: Add a new bulk operation", heldMessage);
			}

		}

		var bulkRequest = new BulkRequest.Builder()
			.index(indexName)
			.operations(bulkOperations)
			.build();

		try {

			getContext().pipeToSelf(
				asyncClient.bulk(bulkRequest),
				(bulkResponse, exception) -> new IndexDocumentResponse(
					dataPayload, heldMessage, bulkResponse, (Exception) exception)
			);

		}
		catch (IOException e) {
			replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));
		}

		return this;
	}

	private record IndexDocumentResponse(
		byte[] dataPayload,
		HeldMessage heldMessage,
		BulkResponse bulkResponse,
		Exception exception
	) implements Writer.Command {}

	private record WriteDocuments(
		DeleteByQueryResponse deleteChunksResponse,
		Throwable throwable,
		List<Map<String, Object>> chunks,
		HeldMessage heldMessage,
		byte[] dataPayload
	) implements Writer.Command {}

}
