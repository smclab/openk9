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
import io.openk9.common.util.SchedulingKey;
import io.openk9.common.util.ingestion.PayloadType;
import io.openk9.datasource.pipeline.actor.EnrichPipeline;
import io.openk9.datasource.pipeline.actor.EnrichPipelineKey;
import io.openk9.datasource.pipeline.actor.IndexWriter;
import io.openk9.datasource.pipeline.actor.Scheduling;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.processor.payload.DataPayload;
import io.quarkus.runtime.util.ExceptionUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.jboss.logging.Logger;

public class WorkStage extends AbstractBehavior<WorkStage.Command> {

	private static final Logger log = Logger.getLogger(WorkStage.class);
	private final SchedulingKey schedulingKey;
	private final ActorRef<Response> replyTo;
	private final ActorRef<WriteProtocol.Command> indexWriter;
	private final ActorRef<WorkProtocol.Response> dataProcessAdapter;
	private final ClusterSharding sharding;
	private long counter = 0;

	public WorkStage(
		ActorContext<Command> context,
		SchedulingKey schedulingKey,
		SchedulerDTO scheduler,
		ActorRef<Response> replyTo) {

		super(context);
		this.schedulingKey = schedulingKey;
		this.replyTo = replyTo;

		ActorSystem<Void> system = getContext().getSystem();
		this.sharding = ClusterSharding.get(system);

		var oldDataIndexName = scheduler.getOldDataIndexName();
		var newDataIndexName = scheduler.getNewDataIndexName();

		var indexWriterAdapter = getContext().messageAdapter(
			WriteProtocol.Response.class,
			PostWrite::new
		);

		this.indexWriter = getContext().spawnAnonymous(IndexWriter.create(
			oldDataIndexName,
			newDataIndexName,
			indexWriterAdapter
		));

		this.dataProcessAdapter = getContext().messageAdapter(
			WorkProtocol.Response.class,
			PostProcess::new
		);

	}

	public static Behavior<Command> create(
		SchedulingKey schedulingKey,
		SchedulerDTO scheduler,
		ActorRef<Response> replyTo) {

		return Behaviors.setup(ctx -> new WorkStage(
			ctx, schedulingKey, scheduler, replyTo));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(StartWorker.class, this::onStartWorker)
			.onMessage(PostProcess.class, this::onPostProcess)
			.onMessage(Write.class, this::onWrite)
			.onMessage(PostWrite.class, this::onPostWrite)
			.build();
	}

	private Behavior<Command> onStartWorker(StartWorker startWorker) {

		var payloadArray = startWorker.payload();
		var requester = startWorker.requester();

		DataPayload dataPayload =
			Json.decodeValue(Buffer.buffer(payloadArray), DataPayload.class);

		if (dataPayload.getType() != null && dataPayload.getType() == PayloadType.HALT) {

			log.warnf(
				"The publisher has sent an HALT message. So %s will be cancelled.",
				schedulingKey
			);

			this.replyTo.tell(new Halt(requester));

		}
		else if (dataPayload.getContentId() != null) {

			counter++;
			String contentId = dataPayload.getContentId();
			var parsingDateTimeStamp = dataPayload.getParsingDate();

			EntityRef<WorkProtocol.Command> dataProcess = sharding.entityRefFor(
				EnrichPipeline.ENTITY_TYPE_KEY,
				EnrichPipelineKey.of(schedulingKey, contentId, String.valueOf(counter)).asString()
			);

			var heldMessage = new HeldMessage(
				schedulingKey,
				contentId,
				counter,
				parsingDateTimeStamp
			);

			dataProcess.tell(new WorkProtocol.Start(
				Json.encodeToBuffer(dataPayload).getBytes(),
				startWorker.scheduler(),
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

		if (response instanceof WorkProtocol.Success success) {
			log.infof("data process success for %s", heldMessage);

			getContext().getSelf().tell(new Write(
				success.payload(),
				heldMessage
			));

		}
		else if (response instanceof WorkProtocol.Failure failure) {

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

		indexWriter.tell(new WriteProtocol.Start(
			payload, heldMessage
		));

		return Behaviors.same();
	}

	private Behavior<Command> onPostWrite(PostWrite postWrite) {

		var response = postWrite.response();

		if (response instanceof WriteProtocol.Success success) {

			var heldMessage = success.heldMessage();

			this.replyTo.tell(new Done(heldMessage));

		}
		else if (response instanceof WriteProtocol.Failure failure) {

			var heldMessage = failure.heldMessage();

			this.replyTo.tell(new Failed(
				ExceptionUtil.generateStackTrace(failure.exception()),
				heldMessage
			));

		}

		return Behaviors.same();
	}

	public sealed interface Command {}

	public sealed interface Response {}

	public sealed interface Callback extends Response {}

	public record StartWorker(
		SchedulerDTO scheduler,
		byte[] payload,
		ActorRef<Scheduling.Response> requester
	) implements Command {}

	private record PostProcess(WorkProtocol.Response response) implements Command {}

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
		WriteProtocol.Response response
	) implements Command {}

	public record Done(HeldMessage heldMessage) implements Callback {}

	public record Failed(String errorMessage, HeldMessage heldMessage) implements Callback {}

}
