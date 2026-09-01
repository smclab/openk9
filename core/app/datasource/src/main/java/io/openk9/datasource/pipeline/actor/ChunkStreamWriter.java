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
import java.time.Duration;
import java.util.ArrayList;
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
import org.apache.pekko.actor.typed.javadsl.StashBuffer;
import org.apache.pekko.actor.typed.javadsl.TimerScheduler;
import org.jboss.logging.Logger;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;

/**
 * Writes the chunks of ONE document to the vector index, accumulating them into
 * bulks. It is spawned as a local child by {@link EmbeddingProcessor}, which is
 * itself per-document: that is what lets it hold plain per-document state
 * ({@code firstBatch}, {@code wroteAny}, the pending batch) the schedule-scoped
 * {@link DataIndexWriter} could not hold without smearing it across the
 * documents processed concurrently.
 * <p>
 * <b>Batching lives here, and not in the stream, on purpose.</b> A batch is
 * written when it reaches {@link #BATCH_SIZE} docs <em>or</em> when
 * {@link #BATCH_MAX_DELAY} has passed since its first doc, so a slow stream
 * (one embedding per image) stays progressively visible. Expressing that in the
 * reactive chain would take an operator that emits on a timer, and such an
 * operator must emit even when the consumer holds no demand — which Reactive
 * Streams forbids, so it kills the stream instead. An actor answers to no
 * demand contract: here the timer is just another message.
 * <p>
 * Each incoming chunk is answered with an {@link Ack} or a {@link Failure}, and
 * the answer is what gates the caller's next chunk. A chunk that is only
 * buffered is acknowledged at once; the chunk that fills a batch is
 * acknowledged when its bulk completes, which is where backpressure bites. The
 * first non-empty batch drops the chunks previously indexed for the content
 * before indexing, so a failure midway through the stream leaves the prior
 * version intact.
 * <p>
 * <b>The protocol is strictly sequential</b>: the delete of the prior version
 * must complete before any insert, and the writer must not be closed while a
 * bulk is in flight. Callers are not expected to know that ordering, so the
 * actor enforces it itself: while a write is in flight every incoming
 * {@link WriteChunk} / {@link EndStream} is stashed and replayed, in arrival
 * order, as soon as the pending write is answered.
 */
class ChunkStreamWriter extends AbstractBehavior<ChunkStreamWriter.Command> {

	// Docs per bulk. Small on purpose so the bulk stays bounded and the write
	// is incremental; the exact value is not load-bearing (the batching policy
	// is deliberately free).
	static final int BATCH_SIZE = 32;

	// A partial batch is written anyway this long after its first doc, so a
	// slow stream stays visible without waiting for the batch to fill.
	static final Duration BATCH_MAX_DELAY = Duration.ofSeconds(1);

	private static final Object FLUSH_TIMER_KEY = "flush";

	// The caller keeps a single chunk in flight (ask-per-chunk), so the stash
	// only ever holds the message that raced with a pending write; the capacity
	// is a safety margin, not a working size.
	private static final int STASH_CAPACITY = 32;

	private static final Logger log = Logger.getLogger(ChunkStreamWriter.class);

	private final OpenSearchAsyncClient asyncClient;
	private final String indexName;
	private final long datasourceId;
	private final HeldMessage heldMessage;
	private final StashBuffer<Command> stash;
	private final TimerScheduler<Command> timers;

	/** Docs accepted but not yet written; drained by every flush. */
	private final List<Map<String, Object>> pending = new ArrayList<>();

	private boolean firstBatch = true;
	private boolean wroteAny = false;
	private boolean writing = false;

	/** Who to answer when the write in flight completes; null for a timer flush. */
	private ActorRef<Response> pendingAck;

	/** A failure nobody was waiting for; handed to the next caller. */
	private WriterException deferredFailure;

	ChunkStreamWriter(
		ActorContext<Command> context,
		OpenSearchAsyncClient asyncClient,
		String indexName,
		long datasourceId,
		HeldMessage heldMessage,
		StashBuffer<Command> stash,
		TimerScheduler<Command> timers) {

		super(context);
		this.asyncClient = asyncClient;
		this.indexName = indexName;
		this.datasourceId = datasourceId;
		this.heldMessage = heldMessage;
		this.stash = stash;
		this.timers = timers;
	}

	/**
	 * Creates the writer of the document held by {@code heldMessage}, on the
	 * index of the given scheduler.
	 */
	static Behavior<Command> create(
		SchedulerDTO scheduler, HeldMessage heldMessage) {

		return create(
			CDI.current().select(OpenSearchAsyncClient.class).get(),
			scheduler.getIndexName(),
			scheduler.getDatasourceId(),
			heldMessage
		);
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

		return Behaviors.withStash(STASH_CAPACITY, stash ->
			Behaviors.withTimers(timers ->
				Behaviors.setup(ctx -> new ChunkStreamWriter(
					ctx, asyncClient, indexName, datasourceId, heldMessage,
					stash, timers))));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(WriteChunk.class, this::onWriteChunk)
			.onMessage(EndStream.class, this::onEndStream)
			.onMessage(FlushTick.class, this::onFlushTick)
			.onMessage(DeleteResponse.class, this::onDeleteResponse)
			.onMessage(IndexResponse.class, this::onIndexResponse)
			.build();
	}

	private Behavior<Command> onWriteChunk(WriteChunk writeChunk) {

		// A write is already in flight: hold the chunk until it is answered,
		// so the delete-then-index ordering cannot be broken from outside.
		if (writing) {
			stash.stash(writeChunk);

			return this;
		}

		var ackTo = writeChunk.ackTo();

		// A batch written on the timer had nobody waiting on it; if it failed,
		// this is the first caller that can be told.
		if (deferredFailure != null) {
			return answer(ackTo, new Failure(deferredFailure));
		}

		List<Map<String, Object>> documents;

		try {
			documents = DataIndexOps.parseChunks(writeChunk.payload());
		}
		catch (IllegalArgumentException e) {
			log.warnf("%s: Failed to parse chunks from batch payload.", heldMessage);

			return answer(ackTo, new Failure(new WriterException(e)));
		}

		// An empty payload carries no work: acknowledge without touching the
		// index and without consuming the first batch, so a content that
		// produced no chunk at all keeps its previously indexed version.
		if (documents.isEmpty()) {
			return answer(ackTo, new Ack());
		}

		var wasEmpty = pending.isEmpty();

		pending.addAll(documents);

		if (pending.size() >= BATCH_SIZE) {

			// the batch is full: this chunk is answered once its bulk is done,
			// which is what makes the caller wait.
			return flush(ackTo);
		}

		if (wasEmpty) {
			timers.startSingleTimer(FLUSH_TIMER_KEY, new FlushTick(), BATCH_MAX_DELAY);
		}

		// buffered, not written yet: let the stream carry on.
		return answer(ackTo, new Ack());
	}

	private Behavior<Command> onFlushTick(FlushTick ignored) {

		// A tick that lands during a write is dropped: that write is already
		// draining the batch, and the next buffered doc re-arms the timer.
		if (writing || pending.isEmpty() || deferredFailure != null) {
			return this;
		}

		return flush(null);
	}

	/**
	 * Writes the pending docs. {@code ackTo} is the caller waiting on this
	 * write, or {@code null} when the flush was started by the timer and
	 * nobody is waiting.
	 */
	private Behavior<Command> flush(ActorRef<Response> ackTo) {

		timers.cancel(FLUSH_TIMER_KEY);

		var documents = List.copyOf(pending);
		pending.clear();
		pendingAck = ackTo;
		writing = true;

		if (firstBatch) {

			// First batch of the content: drop the previously indexed version,
			// then index. The delete MUST complete before any insert (invariant).
			try {
				getContext().pipeToSelf(
					DataIndexOps.deleteByContentId(
						asyncClient, indexName, heldMessage),
					(deleteResponse, throwable) ->
						new DeleteResponse(documents, throwable)
				);

				firstBatch = false;
			}
			catch (IOException e) {
				log.errorf("%s: I/O failed to search engine.", heldMessage);

				return completeWrite(new Failure(new WriterException(e)));
			}

			return this;
		}

		// Subsequent batches only add documents (distinct docs, no delete).
		return indexBatch(documents);
	}

	private Behavior<Command> onDeleteResponse(DeleteResponse deleteResponse) {

		var throwable = deleteResponse.throwable();

		if (throwable != null) {
			log.warnf("%s: Deletion failed.", heldMessage);

			return completeWrite(new Failure(new WriterException(throwable)));
		}

		return indexBatch(deleteResponse.documents());
	}

	private Behavior<Command> indexBatch(List<Map<String, Object>> documents) {

		BulkRequest bulkRequest;

		try {
			bulkRequest = DataIndexOps.buildBulkRequest(indexName, documents);
		}
		catch (Exception e) {
			return completeWrite(new Failure(new WriterException(e)));
		}

		try {
			getContext().pipeToSelf(
				asyncClient.bulk(bulkRequest),
				IndexResponse::new
			);
		}
		catch (IOException e) {
			log.errorf("%s: I/O failed to search engine.", heldMessage);

			return completeWrite(new Failure(new WriterException(e)));
		}

		return this;
	}

	private Behavior<Command> onIndexResponse(IndexResponse indexResponse) {

		var bulkResponse = indexResponse.bulkResponse();
		var throwable = indexResponse.throwable();

		if (throwable != null) {

			if (log.isDebugEnabled()) {
				log.debugf(throwable, "%s: Error on batch bulk request", heldMessage);
			}

			return completeWrite(new Failure(new WriterException(throwable)));
		}

		if (bulkResponse == null) {
			log.errorf("%s: Response is null.", heldMessage);

			return completeWrite(new Failure(new WriterException("No response")));
		}

		if (bulkResponse.errors()) {

			String errors = DataIndexOps.aggregateErrors(bulkResponse);

			if (log.isDebugEnabled()) {
				log.debugf("%s: Batch bulk request error: %s", heldMessage, errors);
			}

			DataIndexOps.sendDatasourceEventError(
				datasourceId, indexName, heldMessage, errors);

			return completeWrite(new Failure(new WriterException(errors)));
		}

		wroteAny = true;

		return completeWrite(new Ack());
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

		// A close racing an in-flight bulk would land in dead letters and hang
		// the caller: hold it until the pending write is answered.
		if (writing) {
			stash.stash(endStream);

			return this;
		}

		if (deferredFailure != null) {
			endStream.ackTo().tell(new Failure(deferredFailure));

			return Behaviors.stopped();
		}

		if (!pending.isEmpty()) {

			// Write the tail first; the re-delivered close then finds an empty
			// batch and completes the document.
			stash.stash(endStream);

			return flush(null);
		}

		timers.cancel(FLUSH_TIMER_KEY);

		if (wroteAny) {
			log.infof("%s: Document stored successfully", heldMessage);

			DataIndexOps.sendDatasourceEventCreate(
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

	/**
	 * Ends the write in flight and hands the actor back to whatever was stashed
	 * while it ran: this is the single point where the write slot is released,
	 * so the sequential protocol cannot be bypassed by adding a path.
	 * <p>
	 * A timer flush has no caller waiting on it, so a failure there is kept and
	 * given to the next one: the stream still fails, one chunk later.
	 */
	private Behavior<Command> completeWrite(Response response) {

		writing = false;

		var ackTo = pendingAck;
		pendingAck = null;

		if (ackTo != null) {
			ackTo.tell(response);
		}
		else if (response instanceof Failure failure) {
			deferredFailure = failure.exception();
		}

		return stash.unstashAll(this);
	}

	/** Answers a chunk that started no write. */
	private Behavior<Command> answer(ActorRef<Response> ackTo, Response response) {

		ackTo.tell(response);

		return stash.unstashAll(this);
	}

	sealed interface Command {}

	sealed interface Response {}

	/** One finalized chunk-doc, answered once it is accepted or written. */
	record WriteChunk(byte[] payload, ActorRef<Response> ackTo)
		implements Command {}

	/** The chunk stream is exhausted; the writer answers and then stops. */
	record EndStream(ActorRef<Response> ackTo) implements Command {}

	record Ack() implements Response {}

	record Failure(WriterException exception) implements Response {}

	private record FlushTick() implements Command {}

	private record DeleteResponse(
		List<Map<String, Object>> documents,
		Throwable throwable
	) implements Command {}

	private record IndexResponse(
		BulkResponse bulkResponse,
		Throwable throwable
	) implements Command {}

}
