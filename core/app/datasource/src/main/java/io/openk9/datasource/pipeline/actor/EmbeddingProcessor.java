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

import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import jakarta.enterprise.inject.spi.CDI;

import io.openk9.common.util.ingestion.ShardingKey;
import io.openk9.datasource.actor.PekkoUtils;
import io.openk9.datasource.pipeline.service.EmbeddingService;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Processor;

import io.smallrye.mutiny.Uni;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.Scheduler;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.AskPattern;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityTypeKey;
import org.jboss.logging.Logger;

/**
 * Terminal streaming processor of the embedding path. It drives the
 * server-streaming {@code EmbedContent} through {@link EmbeddingService
 * #embedContentStream} and hands each matured chunk-doc to its own
 * {@link ChunkStreamWriter} child, spawned per document as this processor
 * itself is. The child accumulates the docs and writes them in bulks; grouping
 * them here, in the stream, would take a time-based operator that emits without
 * demand and kills the stream.
 * <p>
 * Backpressure is {@code transformToUniAndConcatenate} + a Pekko ask per chunk:
 * exactly one chunk is in flight, and a chunk that triggers a bulk is not
 * answered until that bulk completes, so the stream waits on the write and on
 * nothing else. When the stream is exhausted it closes the child and, only once
 * the child confirms, emits a single {@link Processor.Complete}; on error a
 * {@link Processor.Failure}.
 */
public class EmbeddingProcessor extends AbstractBehavior<Processor.Command> {

	public static final EntityTypeKey<Processor.Command> ENTITY_TYPE_KEY =
		EntityTypeKey.create(Processor.Command.class, "embedding-processor");

	// Upper bound for a single batch write (delete + bulk); generous on purpose,
	// the write is normally sub-second.
	public static final String WRITE_TIMEOUT =
		"io.openk9.pipeline.embedding.write-timeout";

	private static final Duration WRITE_TIMEOUT_DEFAULT = Duration.ofMinutes(5);

	private static final Logger log = Logger.getLogger(EmbeddingProcessor.class);

	private final ShardingKey processKey;
	private final EmbeddingService embeddingService;
	private final Duration writeTimeout;
	private ActorRef<Processor.Response> replyTo;
	private ActorRef<ChunkStreamWriter.Command> writer;
	private HeldMessage heldMessage;

	public EmbeddingProcessor(
		ActorContext<Processor.Command> context,
		ShardingKey processKey) {

		super(context);
		this.processKey = processKey;
		this.embeddingService =
			CDI.current().select(EmbeddingService.class).get();
		this.writeTimeout = PekkoUtils.getDuration(
			context.getSystem().settings().config(),
			WRITE_TIMEOUT,
			WRITE_TIMEOUT_DEFAULT);

	}

	public static Behavior<Processor.Command> create(ShardingKey shardingKey) {

		return Behaviors.setup(ctx ->
			new EmbeddingProcessor(ctx, shardingKey)
		);

	}

	@Override
	public Receive<Processor.Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Processor.Start.class, this::onStart)
			.onMessage(StreamCompleted.class, this::onStreamCompleted)
			.onMessage(StreamClosed.class, this::onStreamClosed)
			.onMessage(StreamFailed.class, this::onStreamFailed)
			.build();
	}

	private Behavior<Processor.Command> onStart(Processor.Start start) {
		var payload = start.ingestPayload();
		this.heldMessage = start.heldMessage();
		this.replyTo = start.replyTo();

		this.writer = getContext().spawnAnonymous(
			ChunkStreamWriter.create(start.scheduler(), heldMessage));

		var pekkoScheduler = getContext().getSystem().scheduler();

		// Collapse the stream to its terminal outcome and pipe it back as a
		// message: every chunk is already handed over along the way
		// (ask-per-chunk), so completion and failure are the only events left
		// to observe.
		var streamOutcome = embeddingService
			.embedContentStream(
				processKey.tenantId(), processKey.scheduleId(), payload)
			.onItem().transformToUniAndConcatenate(document ->
				askWriter(pekkoScheduler, document))
			.collect().last()
			.subscribeAsCompletionStage();

		getContext().pipeToSelf(
			streamOutcome,
			(ignored, throwable) -> throwable == null
				? new StreamCompleted()
				: new StreamFailed(unwrap(throwable)));

		return this;
	}

	private Uni<ChunkStreamWriter.Response> askWriter(
		Scheduler pekkoScheduler, byte[] document) {

		CompletionStage<ChunkStreamWriter.Response> ask = AskPattern.ask(
			writer,
			(ActorRef<ChunkStreamWriter.Response> ackTo) ->
				new ChunkStreamWriter.WriteChunk(document, ackTo),
			writeTimeout,
			pekkoScheduler
		);

		return Uni.createFrom().completionStage(ask)
			.onItem().transformToUni(response -> switch (response) {
				case ChunkStreamWriter.Ack ignored ->
					Uni.createFrom().item(response);
				case ChunkStreamWriter.Failure failure ->
					Uni.createFrom().failure(failure.exception());
			});
	}

	private Behavior<Processor.Command> onStreamCompleted(StreamCompleted ignored) {

		// The stream is exhausted and every matured chunk is handed over: close
		// the child, which writes the tail still buffered. The document is not
		// done until the child confirms.
		CompletionStage<ChunkStreamWriter.Response> ask = AskPattern.ask(
			writer,
			(ActorRef<ChunkStreamWriter.Response> ackTo) ->
				new ChunkStreamWriter.EndStream(ackTo),
			writeTimeout,
			getContext().getSystem().scheduler()
		);

		getContext().pipeToSelf(ask, StreamClosed::new);

		return this;
	}

	private Behavior<Processor.Command> onStreamClosed(StreamClosed streamClosed) {

		var throwable = streamClosed.throwable();

		if (throwable != null) {
			return onStreamFailed(new StreamFailed(throwable));
		}

		if (streamClosed.response() instanceof ChunkStreamWriter.Failure failure) {
			return onStreamFailed(new StreamFailed(failure.exception()));
		}

		replyTo.tell(new Processor.Complete(heldMessage));

		return Behaviors.stopped();
	}

	private Behavior<Processor.Command> onStreamFailed(StreamFailed streamFailed) {

		if (log.isDebugEnabled()) {
			log.debugf(
				streamFailed.throwable(),
				"%s: embedding stream failed", heldMessage);
		}

		replyTo.tell(new Processor.Failure(
			new DataProcessException(streamFailed.throwable()), heldMessage));

		return Behaviors.stopped();
	}

	// CompletionStage failures may surface wrapped in a CompletionException;
	// report the cause, as the direct subscription used to.
	private static Throwable unwrap(Throwable throwable) {
		return throwable instanceof CompletionException wrapped
			&& wrapped.getCause() != null
				? wrapped.getCause()
				: throwable;
	}

	private record StreamCompleted() implements Processor.Command {}

	private record StreamClosed(
		ChunkStreamWriter.Response response, Throwable throwable
	) implements Processor.Command {}

	private record StreamFailed(Throwable throwable) implements Processor.Command {}

}
