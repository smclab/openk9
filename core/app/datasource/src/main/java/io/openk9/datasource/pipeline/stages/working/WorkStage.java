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

package io.openk9.datasource.pipeline.stages.working;

import java.util.LinkedList;
import java.util.function.BiFunction;

import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.pipeline.actor.DataProcessException;
import io.openk9.datasource.pipeline.actor.Scheduling;
import io.openk9.datasource.pipeline.actor.WorkStageException;
import io.openk9.datasource.pipeline.actor.WriterException;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.processor.payload.DataPayload;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityTypeKey;
import org.jboss.logging.Logger;

public class WorkStage extends AbstractBehavior<WorkStage.Command> {

	private static final Logger log = Logger.getLogger(WorkStage.class);
	private final ShardingKey shardingKey;
	private final ActorRef<Response> replyTo;
	private final ActorRef<Writer.Response> writerAdapter;
	private final BiFunction<SchedulerDTO, ActorRef<Writer.Response>,
		Behavior<Writer.Command>> writerFactory;
	private ActorRef<Writer.Command> writer;
	private final ActorRef<Processor.Response> dataProcessAdapter;
	private final LinkedList<EntityTypeKey<Processor.Command>> processorTypes;
	private long counter = 0;

	public WorkStage(
		ActorContext<Command> context,
		ShardingKey shardingKey,
		ActorRef<Response> replyTo,
		Configurations configurations) {

		super(context);
		this.shardingKey = shardingKey;
		this.replyTo = replyTo;
		this.processorTypes = configurations.processorTypes();

		this.writerAdapter = getContext().messageAdapter(
			Writer.Response.class,
			PostWrite::new
		);

		this.dataProcessAdapter = getContext().messageAdapter(
			Processor.Response.class,
			ProcessorResponse::new
		);

		this.writerFactory = configurations.writerFactory();

	}

	public static Behavior<Command> create(
		ShardingKey shardingKey,
		ActorRef<Response> replyTo,
		Configurations configurations) {

		return Behaviors.setup(ctx -> new WorkStage(
			ctx, shardingKey, replyTo, configurations));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(StartWorker.class, this::onStartWorker)
			.onMessage(ProcessorResponse.class, this::onProcessorResponse)
			.onMessage(Write.class, this::onWrite)
			.onMessage(PostWrite.class, this::onPostWrite)
			.build();
	}

	private Behavior<Command> onPostWrite(PostWrite postWrite) {

		var response = postWrite.response();

		switch (response) {
			case Writer.Success(HeldMessage heldMessage):

				this.replyTo.tell(new Done(heldMessage));
				break;
			case Writer.Failure(WriterException exception, HeldMessage heldMessage):

				this.replyTo.tell(new Failed(heldMessage, exception));
				break;
		}

		return Behaviors.same();
	}

	private Behavior<Command> onProcessorResponse(ProcessorResponse processorResponse) {

		var response = processorResponse.response();
		var heldMessage = response.heldMessage();

		switch (response) {
			case Processor.Success success -> getContext()
				.getSelf()
				.tell(new Write(success.payload(), heldMessage));
			case Processor.Skip ignored -> this.replyTo.tell(new Done(heldMessage));
			case Processor.Failure failure -> {

				var exception = failure.exception();

				this.replyTo.tell(new Failed(heldMessage, exception));
			}
		}

		return Behaviors.same();
	}

	private Behavior<Command> onStartWorker(StartWorker startWorker) {

		var payloadArray = startWorker.payload();
		var requester = startWorker.requester();
		var scheduler = startWorker.scheduler();

		DataPayload dataPayload =
			Json.decodeValue(Buffer.buffer(payloadArray), DataPayload.class);

		switch (dataPayload.getType()) {
			case DOCUMENT -> {
				// Prepares for working on this dataPayload
				var contentId = dataPayload.getContentId();

				if (contentId == null) {
					this.replyTo.tell(new Invalid("content-id is null", requester));
				}

				if (this.writer == null) {
					this.writer = getContext().spawnAnonymous(
						this.writerFactory.apply(
							scheduler,
							writerAdapter
						)
					);
				}

				counter++;
				var parsingDateTimeStamp = dataPayload.getParsingDate();

				var processKey = ShardingKey.concat(shardingKey, String.valueOf(counter));

				var heldMessage = new HeldMessage(
					processKey,
					counter,
					parsingDateTimeStamp,
					contentId
				);

				// If there are no documentTypes defined,
				// then the associated documents has to be deleted.

				var documentTypes = dataPayload.getDocumentTypes();

				if (documentTypes == null || documentTypes.length == 0) {

					log.infof("%s: Document with this contentId has to be deleted.", heldMessage);
					writer.tell(new Writer.Start(null, heldMessage));

					return this;
				}

				var processorChain =
					getContext().spawnAnonymous(ProcessorChain.create(processorTypes));

				processorChain.tell(new Processor.Start(
					Json.encodeToBuffer(dataPayload).getBytes(),
					scheduler,
					heldMessage,
					this.dataProcessAdapter
				));

				this.replyTo.tell(new Working(heldMessage, requester));
			}
			case LAST -> this.replyTo.tell(new Last(requester));
			case HALT -> {
				log.warnf(
					"The publisher has sent an HALT message. So %s will be cancelled.",
					shardingKey
				);

				DataProcessException exception;

				var rawContent = dataPayload.getRawContent();

				if (rawContent != null
					&& !rawContent.isEmpty()) {

					exception = new DataProcessException(rawContent);
				}
				else {
					exception = new DataProcessException(String.format(
						"Halt received from the source for scheduling %s.",
						shardingKey
					)
					);
				}

				this.replyTo.tell(new Halt(exception, requester));

			}
		}

		return Behaviors.same();
	}

	private Behavior<Command> onWrite(Write write) {

		var payload = write.payload();
		var heldMessage = write.heldMessage();

		writer.tell(new Writer.Start(
			payload, heldMessage
		));

		return Behaviors.same();
	}

	public record Configurations(
		LinkedList<EntityTypeKey<Processor.Command>> processorTypes,
		BiFunction<SchedulerDTO, ActorRef<Writer.Response>, Behavior<Writer.Command>> writerFactory
	) {
	}

	public sealed interface Command {}

	public sealed interface Response {}

	public sealed interface Callback extends Response {}

	public record StartWorker(
		SchedulerDTO scheduler,
		byte[] payload,
		ActorRef<Scheduling.Response> requester
	) implements Command {}

	private record ProcessorResponse(Processor.Response response) implements Command {}

	public record Halt(DataProcessException exception, ActorRef<Scheduling.Response> requester)
		implements Response {}

	public record Last(ActorRef<Scheduling.Response> requester) implements Response {}

	public record Working(HeldMessage heldMessage, ActorRef<Scheduling.Response> requester)
		implements Response {}

	public record Invalid(String errorMessage, ActorRef<Scheduling.Response> requester)
		implements Response {}

	private record Write(byte[] payload, HeldMessage heldMessage) implements Command {}

	private record PostWrite(Writer.Response response) implements Command {}

	public record Done(HeldMessage heldMessage) implements Callback {}

	public record Failed(HeldMessage heldMessage, WorkStageException exception)
		implements Callback {}

}
