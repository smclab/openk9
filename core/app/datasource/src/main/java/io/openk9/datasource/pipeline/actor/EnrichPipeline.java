package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;
import io.openk9.common.util.collection.Collections;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.pipeline.actor.dto.GetDatasourceDTO;
import io.openk9.datasource.pipeline.actor.dto.GetEnrichItemDTO;
import io.openk9.datasource.pipeline.actor.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.actor.enrichitem.EnrichItemSupervisor;
import io.openk9.datasource.pipeline.actor.enrichitem.HttpSupervisor;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.util.JsonMerge;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Set;

public class EnrichPipeline {

	public sealed interface Command {}
	public enum Start implements Command {INSTANCE}
	private record IndexWriterResponseWrapper(
		IndexWriterActor.Response response) implements Command {}
	private record EnrichItemSupervisorResponseWrapper(
		EnrichItemSupervisor.Response response) implements Command {}
	private record EnrichItemError(GetEnrichItemDTO enrichItem, Throwable exception) implements Command {}
	private record InternalResponseWrapper(byte[] jsonObject) implements Command {}
	private record InternalError(String error) implements Command {}
	public sealed interface Response {}
	public sealed interface Success extends Response {}
	public enum AnyMessage implements Success {INSTANCE}
	public record LastMessage(String scheduleId, String tenantId) implements Success {}

	public record Failure(Throwable exception) implements Response {}

	public static Behavior<Command> create(
		ActorRef<HttpSupervisor.Command> supervisorActorRef,
		ActorRef<Response> replyTo, DataPayload dataPayload,
		GetDatasourceDTO datasource, SchedulerDTO scheduler) {

		return Behaviors.setup(ctx -> {

			Logger log = ctx.getLog();

			ActorRef<IndexWriterActor.Response> responseActorRef =
				ctx.messageAdapter(
					IndexWriterActor.Response.class,
					IndexWriterResponseWrapper::new);

			log.info("start pipeline for datasource with id {}", datasource.getId());

			return Behaviors.receive(Command.class)
				.onMessageEquals(
					Start.INSTANCE, () ->
					initPipeline(
						ctx, supervisorActorRef, responseActorRef, replyTo, dataPayload,
						scheduler, datasource.getEnrichItems())
				)
				.build();
		});
	}

	private static Behavior<Command> initPipeline(
		ActorContext<Command> ctx,
		ActorRef<HttpSupervisor.Command> supervisorActorRef,
		ActorRef<IndexWriterActor.Response> responseActorRef,
		ActorRef<Response> replyTo, DataPayload dataPayload,
		SchedulerDTO scheduler,
		Set<GetEnrichItemDTO> enrichPipelineItems) {

		Logger logger = ctx.getLog();

		if (enrichPipelineItems.isEmpty()) {

			logger.info("pipeline is empty, start index writer");

			ClusterSingleton clusterSingleton =
				ClusterSingleton.get(ctx.getSystem());

			ActorRef<IndexWriterActor.Command> indexWriterActorRef =
				clusterSingleton.init(
					SingletonActor.of(
						IndexWriterActor.create(), "index-writer")
				);

			Buffer buffer = Json.encodeToBuffer(dataPayload);

			indexWriterActorRef.tell(
				new IndexWriterActor.Start(
					scheduler, buffer.getBytes(), responseActorRef)
			);

			return Behaviors.receive(Command.class)
					.onMessage(
						IndexWriterResponseWrapper.class,
						indexWriterResponseWrapper -> {

							IndexWriterActor.Response response =
									indexWriterResponseWrapper.response();

							if (response instanceof IndexWriterActor.Success) {
								if (dataPayload.isLast()) {
									replyTo.tell(new LastMessage(scheduler.getScheduleId(), dataPayload.getTenantId()));
								}
								else {
									replyTo.tell(AnyMessage.INSTANCE);
								}
							}
							else if (response instanceof IndexWriterActor.Failure) {
								replyTo.tell(new Failure(((IndexWriterActor.Failure) response).exception()));
							}

							return Behaviors.stopped();
						})
				.build();

		}

		GetEnrichItemDTO enrichItem = Collections.head(enrichPipelineItems);
		Set<GetEnrichItemDTO> tail = Collections.tail(enrichPipelineItems);


		logger.info("start enrich for enrichItem with id {}", enrichItem.getId());

		String jsonPath = enrichItem.getJsonPath();
		EnrichItem.BehaviorMergeType behaviorMergeType =
			EnrichItem.BehaviorMergeType.valueOf(enrichItem.getBehaviorMergeType());

		ActorRef<EnrichItemSupervisor.Command> enrichItemSupervisorRef =
			ctx.spawnAnonymous(EnrichItemSupervisor.create(supervisorActorRef));

		Long requestTimeout = enrichItem.getRequestTimeout();

		LocalDateTime expiredDate =
			LocalDateTime
				.now()
				.plus(requestTimeout, ChronoUnit.MILLIS);

		ctx.ask(
			EnrichItemSupervisor.Response.class,
			enrichItemSupervisorRef,
			Duration.ofMillis(requestTimeout),
			enrichItemReplyTo ->
				new EnrichItemSupervisor.Execute(
					enrichItem, dataPayload, expiredDate, enrichItemReplyTo),
			(r, t) -> {
				if (t != null) {
					return new EnrichItemError(enrichItem, t);
				}
				else if (r instanceof EnrichItemSupervisor.Error) {
					EnrichItemSupervisor.Error error =(EnrichItemSupervisor.Error)r;
					return new EnrichItemError(enrichItem, new RuntimeException(error.error()));
				}
				else {
					return new EnrichItemSupervisorResponseWrapper(r);
				}
			}
		);

		return Behaviors.receive(Command.class)
				.onMessage(EnrichItemError.class, param -> {

					GetEnrichItemDTO enrichItemError = param.enrichItem();

					EnrichItem.BehaviorOnError behaviorOnError =
						EnrichItem.BehaviorOnError.valueOf(enrichItem.getBehaviorOnError());

					switch (behaviorOnError) {
						case SKIP -> {

							logger.error(
									"behaviorOnError is SKIP, call next enrichItem: " + enrichItemError.getId(), param.exception);

							if (!tail.isEmpty()) {
								ctx.getLog().info("call next enrichItem");
							}

							return initPipeline(
									ctx, supervisorActorRef, responseActorRef, replyTo,
									dataPayload, scheduler, tail);

						}
						case FAIL -> {

							logger.info(
									"behaviorOnError is FAIL, stop pipeline: " + enrichItemError.getId(), param.exception);

							Throwable throwable = param.exception;

							ctx.getSelf().tell(
									new InternalError(throwable.getMessage()));

							return Behaviors.same();
						}
						case REJECT -> {

							logger.error(
									"behaviorOnError is REJECT, stop pipeline: " + enrichItemError.getId(), param.exception);

							replyTo.tell(AnyMessage.INSTANCE);

							return Behaviors.stopped();
						}
						default -> {

							ctx.getSelf().tell(
									new InternalError(
											"behaviorOnError is not valid: " + behaviorOnError));

							return Behaviors.same();

						}
					}

				})
				.onMessage(EnrichItemSupervisorResponseWrapper.class, garw -> {
					EnrichItemSupervisor.Response response = garw.response;

					if (response instanceof EnrichItemSupervisor.Body) {
						EnrichItemSupervisor.Body body =(EnrichItemSupervisor.Body)response;
						ctx.getSelf().tell(new InternalResponseWrapper(body.body()));
					}
					else {
						EnrichItemSupervisor.Error error =(EnrichItemSupervisor.Error)response;
						ctx.getSelf().tell(new InternalError(error.error()));
					}

					return Behaviors.same();

				})
				.onMessage(InternalResponseWrapper.class, srw -> {

					JsonObject result = new JsonObject(new String(srw.jsonObject()));

					logger.info("enrichItem: " + enrichItem.getId() + " OK ");

					if (!tail.isEmpty()) {
						logger.info("call next enrichItem");
					}

					JsonObject newJsonPayload = result.getJsonObject("payload");

					if (newJsonPayload == null) {
						newJsonPayload = result;
					}

					DataPayload newDataPayload =
						mergeResponse(
							jsonPath, behaviorMergeType, dataPayload,
							newJsonPayload.mapTo(DataPayload.class));

					return initPipeline(
						ctx, supervisorActorRef,
						responseActorRef, replyTo, newDataPayload,
						scheduler, tail);

				})
				.onMessage(InternalError.class, srw -> {

					String error = srw.error();

					logger.error("enrichItem: " + enrichItem.getId() + " occurred error: " + error);
					logger.error("terminating pipeline");
					replyTo.tell(new Failure(new RuntimeException(error)));

					return Behaviors.stopped();

				})
				.build();

	}

	private static DataPayload mergeResponse(
		String jsonPath, EnrichItem.BehaviorMergeType behaviorMergeType,
		DataPayload prevDataPayload, DataPayload newDataPayload) {

		JsonObject prevJsonObject = new JsonObject(new LinkedHashMap<>(prevDataPayload.getRest()));
		JsonObject newJsonObject = new JsonObject(new LinkedHashMap<>(newDataPayload.getRest()));

		if (jsonPath == null || jsonPath.isBlank()) {
			jsonPath = "$";
		}

		if (behaviorMergeType == null) {
			behaviorMergeType = EnrichItem.BehaviorMergeType.REPLACE;
		}

		JsonMerge jsonMerge = JsonMerge.of(
			behaviorMergeType == EnrichItem.BehaviorMergeType.REPLACE,
			prevJsonObject, newJsonObject);

		return prevDataPayload.rest(jsonMerge.merge(jsonPath).getMap());

	}

}
