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

import io.openk9.datasource.events.DatasourceEventBus;
import io.openk9.datasource.events.DatasourceMessage;
import io.openk9.datasource.index.util.OpenSearchUtils;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Writer;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.jboss.logging.Logger;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
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
	private final long datasourceId;

	public VectorIndexWriter(
		ActorContext<Writer.Command> context,
		SchedulerDTO scheduler,
		ActorRef<Writer.Response> replyTo) {

		super(context);
		this.asyncClient = CDI.current().select(OpenSearchAsyncClient.class).get();
		this.indexName = scheduler.getIndexName();
		this.datasourceId = scheduler.getDatasourceId();
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
			.onMessage(DeleteDocumentResponse.class, this::onDeleteDocumentResponse)
			.onMessage(WriteDocuments.class, this::onWriteDocuments)
			.onMessage(IndexDocumentResponse.class, this::onIndexDocumentResponse)
			.build();
	}

	protected static List<Map<String, Object>> parseChunks(byte[] json)
		throws IllegalArgumentException {

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

	private CompletableFuture<DeleteByQueryResponse> deleteChunksByContentId(HeldMessage heldMessage)
		throws IOException {

		return asyncClient.deleteByQuery(delete -> delete
			.index(indexName)
			.ignoreUnavailable(true)
			.query(query -> query
				.match(match -> match
					.field("contentId.keyword")
					.query(fieldValue -> fieldValue
						.stringValue(heldMessage.contentId()))
				))
		);
	}

	private Behavior<Writer.Command> onDeleteDocumentResponse(
		DeleteDocumentResponse deleteDocumentResponse) {

		var heldMessage = deleteDocumentResponse.heldMessage();
		var throwable = deleteDocumentResponse.throwable();

		if (throwable != null) {

			log.warnf("%s: Documents deletion failed.");

			sendDatasourceEventError(heldMessage, throwable.getMessage());

			replyTo.tell(
				new Writer.Failure(new WriterException(throwable), heldMessage));
		}
		else {

			log.infof("%s: Documents deleted.");

			sendDatasourceEventDelete(heldMessage);

			replyTo.tell(new Writer.Success(heldMessage));
		}

		return this;
	}

	private Behavior<Writer.Command> onIndexDocumentResponse(
		IndexDocumentResponse indexDocumentResponse) {

		var bulkResponse = indexDocumentResponse.bulkResponse();
		var heldMessage = indexDocumentResponse.heldMessage();
		var throwable = indexDocumentResponse.throwable();

		if (throwable != null) {

			if (log.isDebugEnabled()) {
				log.debugf(throwable, "%s: Error on bulk request", heldMessage);
			}

			replyTo.tell(new Writer.Failure(new WriterException(throwable), heldMessage));

		}
		else if (bulkResponse != null) {

			if (bulkResponse.errors()) {

				// Aggregate all errors
				String errors = bulkResponse.items()
					.stream()
					.map(BulkResponseItem::error)
					.filter(Objects::nonNull)
					.map(OpenSearchUtils::getPrimaryAndFirstCauseReason)
					.collect(Collectors.joining("\n------------------------------------\n"));

				if (log.isDebugEnabled()) {
					log.debugf("%s: Bulk request error: %s", heldMessage, errors);
				}

				sendDatasourceEventError(heldMessage, errors);

				replyTo.tell(new Writer.Failure(
					new WriterException(errors), heldMessage));
			}
			else {

				log.infof("%s: Document stored successfully", heldMessage);

				sendDatasourceEventCreate(heldMessage);

				replyTo.tell(new Writer.Success(heldMessage));
			}

		}
		else {
			log.errorf("%s: Response is null.");

			replyTo.tell(new Writer.Failure(
				new WriterException("No response"), heldMessage));
		}

		return this;
	}

	private Behavior<Writer.Command> onStart(Writer.Start start) {

		var dataPayload = start.dataPayload();
		var heldMessage = start.heldMessage();

		// If dataPayload is null, we only delete the chunks with this contentId.

		if (dataPayload == null) {

			try {
				getContext().pipeToSelf(
					deleteChunksByContentId(heldMessage),
					(deleteChunksResponse, throwable) ->
						new DeleteDocumentResponse(
							heldMessage,
							deleteChunksResponse,
							throwable
						)
				);
			}
			catch (IOException e) {
				log.errorf("%s: I/O failed to search engine.", heldMessage);

				replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));
			}

		}

		// Else we try to delete and then to write chunks

		try {

			var chunks = parseChunks(dataPayload);

			// If there are no chunks to write, send a failure to the caller
			if (chunks.isEmpty()) {
				if (log.isDebugEnabled()) {
					log.debugf(
						"%s: There isn't any chunk to write.",
						heldMessage
					);
				}

				replyTo.tell(new Writer.Failure(
						new WriterException("The list of chunks to write is empty."),
						heldMessage
					)
				);

				return this;
			}

			getContext().pipeToSelf(
				deleteChunksByContentId(heldMessage),
				(deleteChunksResponse, throwable) ->
					new WriteDocuments(
						heldMessage,
						deleteChunksResponse,
						throwable,
						chunks
					)
			);
		}
		catch (IllegalArgumentException e) {
			log.warnf("%s: Failed to parse chunks from jsonPayload.", heldMessage);

			replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));
		}
		catch (IOException e) {
			log.errorf("%s: I/O failed to search engine.", heldMessage);

			replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));
		}

		return this;

	}

	private Behavior<Writer.Command> onWriteDocuments(WriteDocuments writeDocuments) {

		var heldMessage = writeDocuments.heldMessage();
		var chunks = writeDocuments.chunks();
		var deletionException = writeDocuments.throwable();

		if (deletionException != null) {
			log.warnf("%s: Deletion failed.", heldMessage);

			replyTo.tell(new Writer.Failure(
				new WriterException(deletionException), heldMessage));

			return this;
		}

		List<BulkOperation> bulkOperations = new ArrayList<>();

		for (Map<String, Object> chunk : chunks) {

			// Handle ACL mapping, fallback if not defined.
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
				(bulkResponse, throwable) -> new IndexDocumentResponse(
					heldMessage, bulkResponse, throwable)
			);

		}
		catch (IOException e) {
			log.errorf("%s: I/O failed to search engine.", heldMessage);

			replyTo.tell(new Writer.Failure(new WriterException(e), heldMessage));
		}

		return this;
	}

	private void sendDatasourceEventCreate(HeldMessage heldMessage) {

		DatasourceEventBus.sendMessage(DatasourceMessage.New.builder()
			.datasourceId(datasourceId)
			.contentId(heldMessage.contentId())
			.tenantId(heldMessage.processKey().tenantId())
			.indexName(indexName)
			.build()
		);

	}

	private void sendDatasourceEventDelete(HeldMessage heldMessage) {

		DatasourceEventBus.sendMessage(DatasourceMessage.Delete
			.builder()
			.datasourceId(datasourceId)
			.contentId(heldMessage.contentId())
			.tenantId(heldMessage.processKey().tenantId())
			.indexName(indexName)
			.build()
		);
	}

	private void sendDatasourceEventError(HeldMessage heldMessage, String reasons) {

		DatasourceEventBus.sendMessage(DatasourceMessage.Failure
			.builder()
			.datasourceId(datasourceId)
			.contentId(heldMessage.contentId())
			.tenantId(heldMessage.processKey().tenantId())
			.indexName(indexName)
			.error(reasons)
			.build()
		);
	}

	private record DeleteDocumentResponse(
		HeldMessage heldMessage,
		DeleteByQueryResponse deleteByQueryResponse,
		Throwable throwable
	) implements Writer.Command {}

	private record IndexDocumentResponse(
		HeldMessage heldMessage,
		BulkResponse bulkResponse,
		Throwable throwable
	) implements Writer.Command {}

	private record WriteDocuments(
		HeldMessage heldMessage,
		DeleteByQueryResponse deleteChunksResponse,
		Throwable throwable,
		List<Map<String, Object>> chunks
	) implements Writer.Command {}

}