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

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import io.openk9.common.util.ShardingKey;
import io.openk9.common.util.ingestion.PayloadType;
import io.openk9.datasource.pipeline.actor.Scheduling;
import io.openk9.datasource.pipeline.actor.common.AggregateBehavior;
import io.openk9.datasource.pipeline.actor.common.AggregateBehaviorException;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.processor.payload.DataPayload;
import io.quarkus.runtime.util.ExceptionUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.function.BiFunction;

public class WorkStage extends AbstractBehavior<WorkStage.Command> {

	private static final Logger log = Logger.getLogger(WorkStage.class);
	private final ShardingKey shardingKey;
	private final ActorRef<Response> replyTo;
	private final ActorRef<Writer.Response> indexWriterAdapter;
	private final BiFunction<SchedulerDTO, ActorRef<Writer.Response>,
		Behavior<Writer.Command>> writerFactory;
	private final ActorRef<AggregateBehavior.Response> endProcessAdapter;
	private ActorRef<Writer.Command> writer;
	private final ActorRef<Processor.Response> dataProcessAdapter;
	private final ClusterSharding sharding;
	private final EntityTypeKey<Processor.Command> processorType;
	private long counter = 0;

	public WorkStage(
		ActorContext<Command> context,
		ShardingKey shardingKey,
		ActorRef<Response> replyTo,
		EntityTypeKey<Processor.Command> processorType,
		BiFunction<SchedulerDTO, ActorRef<Writer.Response>,
			Behavior<Writer.Command>> writerFactory) {

		super(context);
		this.shardingKey = shardingKey;
		this.replyTo = replyTo;
		this.processorType = processorType;

		ActorSystem<Void> system = getContext().getSystem();
		this.sharding = ClusterSharding.get(system);

		this.indexWriterAdapter = getContext().messageAdapter(
			Writer.Response.class,
			PostWrite::new
		);

		this.dataProcessAdapter = getContext().messageAdapter(
			Processor.Response.class,
			PostProcess::new
		);

		this.writerFactory = writerFactory;

		this.endProcessAdapter = getContext().messageAdapter(
			AggregateBehavior.Response.class,
			EndProcessResponse::new
		);

	}

	public static Behavior<Command> create(
		ShardingKey shardingKey,
		ActorRef<Response> replyTo,
		EntityTypeKey<Processor.Command> processorType,
		BiFunction<SchedulerDTO, ActorRef<Writer.Response>, Behavior<Writer.Command>> writerFactory) {

		return Behaviors.setup(ctx -> new WorkStage(
			ctx, shardingKey, replyTo, processorType, writerFactory));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(StartWorker.class, this::onStartWorker)
			.onMessage(PostProcess.class, this::onPostProcess)
			.onMessage(Write.class, this::onWrite)
			.onMessage(PostWrite.class, this::onPostWrite)
			.onMessage(EndProcessResponse.class, this::onEndProcessResponse)
			.build();
	}

	private Behavior<Command> onStartWorker(StartWorker startWorker) {

		var payloadArray = startWorker.payload();
		var requester = startWorker.requester();
		var scheduler = startWorker.scheduler();

		DataPayload dataPayload =
			Json.decodeValue(Buffer.buffer(payloadArray), DataPayload.class);

		if (dataPayload.getType() != null && dataPayload.getType() == PayloadType.HALT) {

			log.warnf(
				"The publisher has sent an HALT message. So %s will be cancelled.",
				shardingKey
			);

			this.replyTo.tell(new Halt(requester));

		}
		else if (dataPayload.getContentId() != null) {

			if (this.writer == null) {
				this.writer = getContext()
					.spawnAnonymous(this.writerFactory.apply(
							scheduler,
							indexWriterAdapter
						)
					);
			}

			counter++;
			var parsingDateTimeStamp = dataPayload.getParsingDate();

			var processKey = ShardingKey.concat(shardingKey, String.valueOf(counter));

			EntityRef<Processor.Command> dataProcess = sharding.entityRefFor(
				processorType,
				processKey.asString()
			);

			var heldMessage = new HeldMessage(
				processKey,
				counter,
				parsingDateTimeStamp
			);

			dataProcess.tell(new Processor.Start(
				Json.encodeToBuffer(dataPayload).getBytes(),
				scheduler,
				heldMessage,
				this.dataProcessAdapter
			));

			this.replyTo.tell(new Working(heldMessage, requester));

		}
		else if (!dataPayload.isLast()) {

			this.replyTo.tell(new Invalid("content-id is null", requester));

		}
		else {

			this.replyTo.tell(new Last(requester));

		}

		return Behaviors.same();
	}

	private Behavior<Command> onPostProcess(PostProcess postProcess) {

		var response = postProcess.response();
		var heldMessage = response.heldMessage();

		if (response instanceof Processor.Success success) {
			log.infof("data process success for %s", heldMessage);

			getContext().getSelf().tell(new Write(
				success.payload(),
				heldMessage
			));

		}
		else if (response instanceof Processor.Failure failure) {

			Exception exception = failure.exception();
			log.error("data process failure for %s", heldMessage, exception);

			this.replyTo.tell(new Failed(
				ExceptionUtil.generateStackTrace(exception), heldMessage));

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

	private Behavior<Command> onPostWrite(PostWrite postWrite) {

		var response = postWrite.response();

		if (response instanceof Writer.Success success) {

			var payload = success.dataPayload();
			var heldMessage = success.heldMessage();

			var endProcess = getContext().spawnAnonymous(EndProcess.create(
				List.of(),
				endProcessAdapter
			));

			endProcess.tell(new EndProcess.Start(payload, heldMessage));

		}
		else if (response instanceof Writer.Failure failure) {

			var heldMessage = failure.heldMessage();

			this.replyTo.tell(new Failed(
				ExceptionUtil.generateStackTrace(failure.exception()),
				heldMessage
			));

		}

		return Behaviors.same();
	}

	private Behavior<Command> onEndProcessResponse(EndProcessResponse endProcessResponse) {

		if (endProcessResponse.response() instanceof EndProcess.EndProcessDone endProcessDone) {

			this.replyTo.tell(new Done(endProcessDone.heldMessage()));

			return Behaviors.same();
		}

		throw new AggregateBehaviorException();

	}

	public sealed interface Command {}

	public sealed interface Response {}

	public sealed interface Callback extends Response {}

	public record StartWorker(
		SchedulerDTO scheduler,
		byte[] payload,
		ActorRef<Scheduling.Response> requester
	) implements Command {}

	private record PostProcess(Processor.Response response) implements Command {}

	public record Halt(ActorRef<Scheduling.Response> requester) implements Response {}

	public record Last(ActorRef<Scheduling.Response> requester) implements Response {}

	public record Working(HeldMessage heldMessage, ActorRef<Scheduling.Response> requester)
		implements Response {}

	public record Invalid(String errorMessage, ActorRef<Scheduling.Response> requester)
		implements Response {}

	private record Write(
		byte[] payload,
		HeldMessage heldMessage
	) implements Command {}

	private record PostWrite(
		Writer.Response response
	) implements Command {}

	private record EndProcessResponse(AggregateBehavior.Response response) implements Command {}

	public record Done(HeldMessage heldMessage) implements Callback {}

	public record Failed(String errorMessage, HeldMessage heldMessage) implements Callback {}

}
