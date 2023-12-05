package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.ChildFailed;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import io.openk9.common.util.collection.Collections;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.pipeline.actor.dto.EnrichItemDTO;
import io.openk9.datasource.pipeline.actor.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.actor.enrichitem.EnrichItemSupervisor;
import io.openk9.datasource.pipeline.actor.enrichitem.HttpSupervisor;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.util.CborSerializable;
import io.openk9.datasource.util.JsonMerge;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Set;

public class EnrichPipeline {

	public static final EntityTypeKey<EnrichPipeline.Command> ENTITY_TYPE_KEY =
		EntityTypeKey.create(EnrichPipeline.Command.class, "enrich-pipeline");
	private static final Logger log = Logger.getLogger(EnrichPipeline.class);

	public sealed interface Command {}
	public record Setup(
		ActorRef<Response> schedulation,
		ActorRef<Schedulation.Response> consumer,
		DataPayload dataPayload,
		io.openk9.datasource.pipeline.actor.dto.SchedulerDTO scheduler

	) implements Command, CborSerializable {}
	private record IndexWriterResponseWrapper(
		IndexWriterActor.Response response
	) implements Command {}
	private record EnrichItemSupervisorResponseWrapper(
		EnrichItemSupervisor.Response response
	) implements Command {}
	private record EnrichItemError(
		EnrichItemDTO enrichItem,
		Throwable exception
	) implements Command {}
	private record InternalResponseWrapper(byte[] jsonObject) implements Command {}
	private record InternalError(String error) implements Command {}
	public sealed interface Response {
		ActorRef<Schedulation.Response> replyTo();

		String scheduleId();
		String tenantId();
	}
	public record Success(
		DataPayload dataPayload,
		ActorRef<Schedulation.Response> replyTo,
		String scheduleId,
		String tenantId
	) implements Response {}

	public record Failure(
		Throwable exception,
		ActorRef<Schedulation.Response> replyTo,
		String scheduleId,
		String tenantId
	) implements Response {}

	public static Behavior<Command> create(
		Schedulation.SchedulationKey schedulationKey,
		String contentId
	) {
		return Behaviors.setup(ctx -> Behaviors
			.receive(Command.class)
			.onMessage(Setup.class, setup -> onSetup(ctx, schedulationKey, contentId, setup))
			.build()
		);
	}

	public static Behavior<Command> onSetup(
		ActorContext<EnrichPipeline.Command> ctx,
		Schedulation.SchedulationKey key,
		String contentId,
		Setup setup
	) {

		SchedulerDTO scheduler = setup.scheduler();
		DataPayload dataPayload = setup.dataPayload();
		ActorRef<Response> schedulation = setup.schedulation();
		ActorRef<Schedulation.Response> consumer = setup.consumer();

		ActorRef<IndexWriterActor.Response> responseActorRef =
			ctx.messageAdapter(
				IndexWriterActor.Response.class,
				IndexWriterResponseWrapper::new);

		String oldDataIndexName = scheduler.getOldDataIndexName();
		if (oldDataIndexName != null) {
			dataPayload.setOldIndexName(oldDataIndexName);
		}


		log.infof("start pipeline for datasource with id %s", scheduler.getDatasourceId());

		ActorRef<HttpSupervisor.Command> supervisorActorRef =
			ctx.spawnAnonymous(HttpSupervisor.create(key));

		return initPipeline(
			ctx,
			supervisorActorRef,
			responseActorRef,
			schedulation,
			consumer,
			dataPayload,
			scheduler,
			scheduler.getEnrichItems()
		);

	}

	private static Behavior<Command> initPipeline(
		ActorContext<Command> ctx,
		ActorRef<HttpSupervisor.Command> supervisorActorRef,
		ActorRef<IndexWriterActor.Response> responseActorRef,
		ActorRef<Response> replyTo,
		ActorRef<Schedulation.Response> consumer,
		DataPayload dataPayload,
		SchedulerDTO scheduler,
		Set<EnrichItemDTO> enrichPipelineItems
	) {


		String scheduleId = scheduler.getScheduleId();

		if (enrichPipelineItems.isEmpty()) {

			log.info("pipeline is empty, start index writer");

			ActorRef<IndexWriterActor.Command> indexWriterActorRef =
				ctx.spawnAnonymous(IndexWriterActor.create());

			ctx.watch(indexWriterActorRef);

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
								replyTo.tell(new Success(
									dataPayload,
									consumer,
									scheduleId,
									dataPayload.getTenantId()
									)
								);
							}
							else if (response instanceof IndexWriterActor.Failure failure) {
								replyTo.tell(new Failure(
									failure.exception(),
									consumer,
									scheduleId,
									dataPayload.getTenantId()
									)
								);
							}

							return Behaviors.stopped();
						})
				.onSignal(ChildFailed.class, childFailed -> {
					replyTo.tell(new Failure(
						childFailed.cause(),
						consumer,
						dataPayload.getTenantId(),
						dataPayload.getScheduleId()
						)
					);

					return Behaviors.stopped();
				})
				.build();

		}

		EnrichItemDTO enrichItem = Collections.head(enrichPipelineItems);
		Set<EnrichItemDTO> tail = Collections.tail(enrichPipelineItems);


		log.infof("start enrich for enrichItem with id %s", enrichItem.getId());

		String jsonPath = enrichItem.getJsonPath();
		EnrichItem.BehaviorMergeType behaviorMergeType = enrichItem.getBehaviorMergeType();

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

				EnrichItemDTO enrichItemError = param.enrichItem();

				EnrichItem.BehaviorOnError behaviorOnError = enrichItemError.getBehaviorOnError();

				switch (behaviorOnError) {
					case SKIP -> {

						log.errorf(
							param.exception,
							"behaviorOnError is SKIP, call next enrichItem: %s",
							enrichItemError.getId()
						);

						if (!tail.isEmpty()) {
							log.info("call next enrichItem");
						}

						return initPipeline(
							ctx, supervisorActorRef, responseActorRef, replyTo,
							consumer, dataPayload, scheduler, tail);

					}
					case FAIL -> {

						log.infof(
							param.exception,
							"behaviorOnError is FAIL, stop pipeline: %s",
							enrichItemError.getId()
						);

						Throwable throwable = param.exception;

						ctx.getSelf().tell(
							new InternalError(throwable.getMessage())
						);

						return Behaviors.same();
					}
					case REJECT -> {

						log.errorf(
							param.exception,
							"behaviorOnError is REJECT, stop pipeline: %s",
							enrichItemError.getId()
						);

						replyTo.tell(new Success(
							dataPayload,
							consumer,
							dataPayload.getScheduleId(),
							dataPayload.getTenantId()
						));

						return Behaviors.stopped();
					}
					default -> {

						ctx.getSelf().tell(
							new InternalError("behaviorOnError is not valid: " + behaviorOnError)
						);

						return Behaviors.same();

					}
				}

			})
			.onMessage(EnrichItemSupervisorResponseWrapper.class, garw -> {
				EnrichItemSupervisor.Response response = garw.response;

				if (response instanceof EnrichItemSupervisor.Body body) {
					ctx.getSelf().tell(new InternalResponseWrapper(body.body()));
				}
				else {
					EnrichItemSupervisor.Error error = (EnrichItemSupervisor.Error)response;
					ctx.getSelf().tell(new InternalError(error.error()));
				}

				return Behaviors.same();

			})
			.onMessage(InternalResponseWrapper.class, srw -> {

				JsonObject result = new JsonObject(new String(srw.jsonObject()));

				log.infof("enrichItem: %s OK", enrichItem.getId());

				if (!tail.isEmpty()) {
					log.info("call next enrichItem");
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
					ctx,
					supervisorActorRef,
					responseActorRef,
					replyTo,
					consumer,
					newDataPayload,
					scheduler,
					tail);

			})
			.onMessage(InternalError.class, srw -> {

				String error = srw.error();

				log.errorf("enrichItem: %s occurred error: %s", enrichItem.getId(), error);
				log.error("terminating pipeline");

				replyTo.tell(new Failure(
					new RuntimeException(error),
					consumer,
					dataPayload.getScheduleId(),
					dataPayload.getTenantId()
				));

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
