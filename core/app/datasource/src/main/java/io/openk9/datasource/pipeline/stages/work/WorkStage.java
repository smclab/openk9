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

package io.openk9.datasource.pipeline.stages.work;

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
import io.openk9.datasource.pipeline.actor.EnrichPipelineException;
import io.openk9.datasource.pipeline.actor.EnrichPipelineKey;
import io.openk9.datasource.pipeline.actor.IndexWriter;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.processor.payload.DataPayload;
import io.quarkus.runtime.util.ExceptionUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.jboss.logging.Logger;

public class WorkStage extends AbstractBehavior<WorkStage.Command> {

	private static final Logger log = Logger.getLogger(WorkStage.class);
	private final SchedulingKey schedulingKey;

	public WorkStage(
		ActorContext<Command> context,
		SchedulingKey schedulingKey) {

		super(context);
		this.schedulingKey = schedulingKey;
	}

	public static Behavior<Command> create(
		SchedulingKey schedulingKey) {
		return Behaviors.setup(ctx -> new WorkStage(ctx, schedulingKey));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(StartWorker.class, this::onStartWorker)
			.onMessage(EnrichPipelineResponse.class, this::onEnrichPipelineResponse)
			.onMessage(IndexWriterResponse.class, this::onIndexWriterResponse)
			.onMessage(Write.class, this::onWrite)
			.build();
	}

	private Behavior<Command> onStartWorker(StartWorker startWorker) {

		var payloadArray = startWorker.payload();

		DataPayload dataPayload =
			Json.decodeValue(Buffer.buffer(payloadArray), DataPayload.class);

		if (dataPayload.getType() != null && dataPayload.getType() == PayloadType.HALT) {

			log.warnf(
				"The publisher has sent an HALT message. So %s will be cancelled.",
				schedulingKey
			);

			startWorker.replyTo().tell(HaltMessage.INSTANCE);

		}

		if (dataPayload.getContentId() != null) {

			String contentId = dataPayload.getContentId();
			var parsingDateTimeStamp = dataPayload.getParsingDate();

			ActorSystem<Void> system = getContext().getSystem();

			ClusterSharding clusterSharding = ClusterSharding.get(system);

			EntityRef<EnrichPipeline.Command> enrichPipeline = clusterSharding.entityRefFor(
				EnrichPipeline.ENTITY_TYPE_KEY,
				EnrichPipelineKey.of(schedulingKey, contentId, startWorker.messageKey()).asString()
			);

			var heldMessage = new HeldMessage(
				schedulingKey,
				startWorker.scheduler(),
				contentId,
				startWorker.messageKey(),
				parsingDateTimeStamp
			);

			ActorRef<EnrichPipeline.Response> enrichPipelineAdapter =
				getContext().messageAdapter(
					EnrichPipeline.Response.class,
					res -> new EnrichPipelineResponse(
						res,
						heldMessage,
						startWorker.replyTo().narrow()
					)
				);

			enrichPipeline.tell(new EnrichPipeline.Setup(
				enrichPipelineAdapter,
				heldMessage,
				Json.encodeToBuffer(dataPayload).getBytes(),
				startWorker.scheduler()
			));

			startWorker.replyTo().tell(new WorkingMessage(heldMessage));

		}
		else if (!dataPayload.isLast()) {
			startWorker.replyTo().tell(new InvalidMessage("content-id is null"));

		}
		else {
			startWorker.replyTo().tell(LastMessage.INSTANCE);

		}

		return Behaviors.same();
	}

	private Behavior<Command> onEnrichPipelineResponse(EnrichPipelineResponse enrichPipelineResponse) {

		var response = enrichPipelineResponse.response();
		var heldMessage = enrichPipelineResponse.heldMessage();

		var replyTo = enrichPipelineResponse.replyTo();

		if (response instanceof EnrichPipeline.Success success) {
			log.infof(
				"enrich pipeline success for content-id %s replyTo %s",
				heldMessage.contentId(), replyTo
			);

			getContext().getSelf().tell(new Write(
				success.payload(),
				heldMessage,
				replyTo
			));

		}
		else if (response instanceof EnrichPipeline.Failure failure) {

			EnrichPipelineException epe = failure.exception();
			log.error("enrich pipeline failure", epe);

			replyTo.tell(new Failed(
				ExceptionUtil.generateStackTrace(epe), heldMessage));

		}

		return Behaviors.same();
	}

	private Behavior<Command> onWrite(Write write) {

		var payload = write.payload();
		var heldMessage = write.heldMessage();

		var indexWriter = getContext().spawnAnonymous(IndexWriter.create());
		var indexWriterAdapter = getContext().messageAdapter(
			IndexWriter.Response.class,
			res -> new IndexWriterResponse(res, heldMessage, write.replyTo())
		);

		indexWriter.tell(new IndexWriter.Start(
			heldMessage.scheduler(),
			payload,
			indexWriterAdapter
		));

		return Behaviors.same();
	}

	private Behavior<Command> onIndexWriterResponse(IndexWriterResponse indexWriterResponse) {

		var response = indexWriterResponse.response();
		var heldMessage = indexWriterResponse.heldMessage();

		var replyTo = indexWriterResponse.replyTo();

		if (response instanceof IndexWriter.Success) {

			replyTo.tell(new Done(heldMessage));

		}
		else if (response instanceof IndexWriter.Failure failure) {

			replyTo.tell(new Failed(
				ExceptionUtil.generateStackTrace(failure.exception()),
				heldMessage
			));

		}

		return Behaviors.same();
	}

	public sealed interface Command {}

	public sealed interface Response {}

	public enum HaltMessage implements Response {
		INSTANCE
	}

	public enum LastMessage implements Response {
		INSTANCE
	}

	public sealed interface Callback extends Response {}

	public record StartWorker(
		SchedulerDTO scheduler,
		String messageKey,
		byte[] payload,
		ActorRef<Response> replyTo
	) implements Command {}

	private record EnrichPipelineResponse(
		EnrichPipeline.Response response,
		HeldMessage heldMessage,
		ActorRef<Callback> replyTo
	) implements Command {}

	public record WorkingMessage(HeldMessage heldMessage) implements Response {}

	public record InvalidMessage(String errorMessage) implements Response {}

	private record Write(
		byte[] payload,
		HeldMessage heldMessage,
		ActorRef<Callback> replyTo
	) implements Command {}

	private record IndexWriterResponse(
		IndexWriter.Response response,
		HeldMessage heldMessage,
		ActorRef<Callback> replyTo
	) implements Command {}

	public record Done(HeldMessage heldMessage) implements Callback {}

	public record Failed(String errorMessage, HeldMessage heldMessage) implements Callback {}

}
