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
import java.util.List;
import java.util.Map;
import jakarta.enterprise.inject.spi.CDI;

import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;

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

/**
 * Writes the chunks of ONE document to the vector index, one bulk per batch of
 * the embedding stream. It is spawned as a local child by
 * {@link EmbeddingProcessor}, which is itself per-document: that is what lets
 * it hold plain per-document state ({@code firstBatch}, {@code wroteAny}) the
 * schedule-scoped {@link VectorIndexWriter} could not hold without smearing it
 * across the documents processed concurrently.
 * <p>
 * Every batch is answered with an {@link Ack} or a {@link Failure}: the answer
 * is what gates the caller's next batch (ask-per-batch backpressure). The first
 * non-empty batch drops the chunks previously indexed for the content before
 * indexing, so a failure midway through the stream leaves the prior version
 * intact.
 */
class ChunkStreamWriter extends AbstractBehavior<ChunkStreamWriter.Command> {

	private static final Logger log = Logger.getLogger(ChunkStreamWriter.class);

	private final OpenSearchAsyncClient asyncClient;
	private final String indexName;
	private final long datasourceId;
	private final HeldMessage heldMessage;

	private boolean firstBatch = true;
	private boolean wroteAny = false;

	ChunkStreamWriter(
		ActorContext<Command> context,
		OpenSearchAsyncClient asyncClient,
		String indexName,
		long datasourceId,
		HeldMessage heldMessage) {

		super(context);
		this.asyncClient = asyncClient;
		this.indexName = indexName;
		this.datasourceId = datasourceId;
		this.heldMessage = heldMessage;
	}

	/**
	 * Creates the writer of the document held by {@code heldMessage}, on the
	 * index of the given scheduler.
	 */
	static Behavior<Command> create(
		SchedulerDTO scheduler, HeldMessage heldMessage) {

		return Behaviors.setup(ctx -> new ChunkStreamWriter(
			ctx,
			CDI.current().select(OpenSearchAsyncClient.class).get(),
			scheduler.getIndexName(),
			scheduler.getDatasourceId(),
			heldMessage
		));
	}

	/**
	 * Creates the writer on an explicit OpenSearch client; the actor is not a
	 * CDI bean, so this is how tests pass a fake one.
	 */
	static Behavior<Command> create(
		OpenSearchAsyncClient asyncClient,
		String indexName,
		long datasourceId,
		HeldMessage heldMessage) {

		return Behaviors.setup(ctx -> new ChunkStreamWriter(
			ctx, asyncClient, indexName, datasourceId, heldMessage));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(WriteBatch.class, this::onWriteBatch)
			.onMessage(EndStream.class, this::onEndStream)
			.onMessage(DeleteResponse.class, this::onDeleteResponse)
			.onMessage(IndexResponse.class, this::onIndexResponse)
			.build();
	}

	private Behavior<Command> onWriteBatch(WriteBatch writeBatch) {

		var ackTo = writeBatch.ackTo();

		List<Map<String, Object>> chunks;

		try {
			chunks = VectorIndexOps.parseChunks(writeBatch.payload());
		}
		catch (IllegalArgumentException e) {
			log.warnf("%s: Failed to parse chunks from batch payload.", heldMessage);

			ackTo.tell(new Failure(new WriterException(e)));

			return this;
		}

		// An empty batch carries no work: ack without touching the index and
		// without consuming the first batch, so a content that produced no chunk
		// at all keeps its previously indexed version.
		if (chunks.isEmpty()) {
			ackTo.tell(new Ack());

			return this;
		}

		if (firstBatch) {

			// First batch of the content: drop the previously indexed version,
			// then index. The delete MUST complete before any insert (invariant).
			try {
				getContext().pipeToSelf(
					VectorIndexOps.deleteChunksByContentId(
						asyncClient, indexName, heldMessage),
					(deleteResponse, throwable) ->
						new DeleteResponse(chunks, ackTo, throwable)
				);

				firstBatch = false;
			}
			catch (IOException e) {
				log.errorf("%s: I/O failed to search engine.", heldMessage);

				ackTo.tell(new Failure(new WriterException(e)));
			}
		}
		else {

			// Subsequent batches only add documents (distinct docs, no delete).
			indexBatch(chunks, ackTo);
		}

		return this;
	}

	private Behavior<Command> onDeleteResponse(DeleteResponse deleteResponse) {

		var ackTo = deleteResponse.ackTo();
		var throwable = deleteResponse.throwable();

		if (throwable != null) {
			log.warnf("%s: Deletion failed.", heldMessage);

			ackTo.tell(new Failure(new WriterException(throwable)));

			return this;
		}

		indexBatch(deleteResponse.chunks(), ackTo);

		return this;
	}

	private void indexBatch(
		List<Map<String, Object>> chunks, ActorRef<Response> ackTo) {

		BulkRequest bulkRequest;

		try {
			bulkRequest = VectorIndexOps.buildBulkRequest(indexName, chunks);
		}
		catch (Exception e) {
			ackTo.tell(new Failure(new WriterException(e)));

			return;
		}

		try {
			getContext().pipeToSelf(
				asyncClient.bulk(bulkRequest),
				(bulkResponse, throwable) ->
					new IndexResponse(bulkResponse, throwable, ackTo)
			);
		}
		catch (IOException e) {
			log.errorf("%s: I/O failed to search engine.", heldMessage);

			ackTo.tell(new Failure(new WriterException(e)));
		}
	}

	private Behavior<Command> onIndexResponse(IndexResponse indexResponse) {

		var bulkResponse = indexResponse.bulkResponse();
		var throwable = indexResponse.throwable();
		var ackTo = indexResponse.ackTo();

		if (throwable != null) {

			if (log.isDebugEnabled()) {
				log.debugf(throwable, "%s: Error on batch bulk request", heldMessage);
			}

			ackTo.tell(new Failure(new WriterException(throwable)));
		}
		else if (bulkResponse != null) {

			if (bulkResponse.errors()) {

				String errors = VectorIndexOps.aggregateErrors(bulkResponse);

				if (log.isDebugEnabled()) {
					log.debugf("%s: Batch bulk request error: %s", heldMessage, errors);
				}

				VectorIndexOps.sendDatasourceEventError(
					datasourceId, indexName, heldMessage, errors);

				ackTo.tell(new Failure(new WriterException(errors)));
			}
			else {
				wroteAny = true;

				ackTo.tell(new Ack());
			}
		}
		else {
			log.errorf("%s: Response is null.", heldMessage);

			ackTo.tell(new Failure(new WriterException("No response")));
		}

		return this;
	}

	/**
	 * Closes the document.
	 * <p>
	 * The New event (the one the single-bulk write emits) is emitted only when
	 * at least one batch was actually written: a zero-chunk stream indexed
	 * nothing, so signalling a creation would be spurious.
	 * <p>
	 * <b>A zero-chunk stream is a success, not a failure.</b> This is a
	 * deliberate difference from the single-response {@code GetMessages} path,
	 * where an empty response failed the document
	 * ("No chunks created from this payload"). Here the document is answered
	 * {@code Done} and the index is left untouched, which means:
	 * <ul>
	 *   <li>on a first indexing the content is simply absent from the index;</li>
	 *   <li>on a reprocessing the <em>previously indexed version survives</em>,
	 *   because the delete is bound to the first non-empty batch.</li>
	 * </ul>
	 * That is what makes an immediate module error harmless (the indexed
	 * version is not dropped before knowing there is a replacement), and it is
	 * the reason the outcome is only logged, at WARN, and not turned into a
	 * scheduling failure.
	 */
	private Behavior<Command> onEndStream(EndStream endStream) {

		if (wroteAny) {
			log.infof("%s: Document stored successfully", heldMessage);

			VectorIndexOps.sendDatasourceEventCreate(
				datasourceId, indexName, heldMessage);
		}
		else {
			log.warnf(
				"%s: The embedding stream produced no chunk, nothing was" +
					" indexed and any previously indexed version is kept.",
				heldMessage);
		}

		endStream.ackTo().tell(new Ack());

		return Behaviors.stopped();
	}

	sealed interface Command {}

	sealed interface Response {}

	/** A batch of chunks to index, answered once the batch is written. */
	record WriteBatch(byte[] payload, ActorRef<Response> ackTo)
		implements Command {}

	/** The batch stream is exhausted; the writer answers and then stops. */
	record EndStream(ActorRef<Response> ackTo) implements Command {}

	record Ack() implements Response {}

	record Failure(WriterException exception) implements Response {}

	private record DeleteResponse(
		List<Map<String, Object>> chunks,
		ActorRef<Response> ackTo,
		Throwable throwable
	) implements Command {}

	private record IndexResponse(
		BulkResponse bulkResponse,
		Throwable throwable,
		ActorRef<Response> ackTo
	) implements Command {}

}
