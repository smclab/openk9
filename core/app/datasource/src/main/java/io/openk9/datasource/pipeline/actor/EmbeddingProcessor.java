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
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import jakarta.enterprise.inject.spi.CDI;

import io.openk9.common.util.ingestion.ShardingKey;
import io.openk9.datasource.pipeline.service.EmbeddingService;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Processor;
import io.openk9.datasource.pipeline.stages.working.Writer;

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
 * #embedContentStream} and writes each matured batch to the {@code writer} it
 * receives with {@link Processor.Start}. Backpressure is
 * {@code transformToUniAndConcatenate} + a Pekko ask per batch: exactly one
 * batch is in flight and the next is not pulled until the current bulk
 * completes. On stream completion it emits a single
 * {@link Processor.Complete}; on error a {@link Processor.Failure}.
 */
public class EmbeddingProcessor extends AbstractBehavior<Processor.Command> {

	public static final EntityTypeKey<Processor.Command> ENTITY_TYPE_KEY =
		EntityTypeKey.create(Processor.Command.class, "embedding-processor");

	// Upper bound for a single batch write (delete + bulk); generous on purpose,
	// the write is normally sub-second.
	// TODO: promote to a configuration property.
	private static final Duration WRITE_ASK_TIMEOUT = Duration.ofMinutes(5);

	private static final Logger log = Logger.getLogger(EmbeddingProcessor.class);

	private final ShardingKey processKey;
	private final EmbeddingService embeddingService;
	private ActorRef<Processor.Response> replyTo;
	private ActorRef<Writer.Command> writer;
	private HeldMessage heldMessage;
	private SchedulerDTO scheduler;

	public EmbeddingProcessor(
		ActorContext<Processor.Command> context,
		ShardingKey processKey) {

		super(context);
		this.processKey = processKey;
		this.embeddingService =
			CDI.current().select(EmbeddingService.class).get();

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
			.onMessage(StreamFailed.class, this::onStreamFailed)
			.build();
	}

	private Behavior<Processor.Command> onStart(Processor.Start start) {
		var payload = start.ingestPayload();
		this.heldMessage = start.heldMessage();
		this.replyTo = start.replyTo();
		this.scheduler = start.scheduler();
		this.writer = start.writerRef();

		var self = getContext().getSelf();
		var pekkoScheduler = getContext().getSystem().scheduler();
		var firstBatch = new AtomicBoolean(true);

		embeddingService
			.embedContentStream(
				processKey.tenantId(), processKey.scheduleId(), payload)
			.onItem().transformToUniAndConcatenate(batch ->
				askWriter(pekkoScheduler, batch, firstBatch.getAndSet(false)))
			.subscribe().with(
				ignored -> {},
				throwable -> self.tell(new StreamFailed(throwable)),
				// firstBatch is still set only if no batch was ever written.
				() -> self.tell(new StreamCompleted(!firstBatch.get()))
			);

		return this;
	}

	private Uni<Writer.Response> askWriter(
		Scheduler pekkoScheduler, byte[] batch, boolean firstBatch) {

		CompletionStage<Writer.Response> ask = AskPattern.ask(
			writer,
			(ActorRef<Writer.Response> ackTo) ->
				new Writer.WriteBatch(batch, firstBatch, heldMessage, ackTo),
			WRITE_ASK_TIMEOUT,
			pekkoScheduler
		);

		return Uni.createFrom().completionStage(ask)
			.onItem().transformToUni(response -> switch (response) {
				case Writer.BatchAck ignored -> Uni.createFrom().item(response);
				case Writer.Success ignored -> Uni.createFrom().item(response);
				case Writer.Failure failure ->
					Uni.createFrom().failure(failure.exception());
			});
	}

	private Behavior<Processor.Command> onStreamCompleted(StreamCompleted completed) {

		replyTo.tell(new Processor.Complete(
			scheduler, heldMessage, completed.wroteAnyBatch()));

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

	private record StreamCompleted(boolean wroteAnyBatch)
		implements Processor.Command {}

	private record StreamFailed(Throwable throwable) implements Processor.Command {}

}
