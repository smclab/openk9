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
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Supplier;

import io.openk9.common.util.Collections;
import io.openk9.common.util.ingestion.ShardingKey;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.pipeline.actor.common.Http;
import io.openk9.datasource.pipeline.actor.enrichitem.EnrichItemSupervisor;
import io.openk9.datasource.pipeline.actor.enrichitem.HttpProcessor;
import io.openk9.datasource.pipeline.actor.enrichitem.HttpSupervisor;
import io.openk9.datasource.pipeline.service.dto.EnrichItemDTO;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Processor;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.util.CborSerializable;
import io.openk9.datasource.util.JsonMerge;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityTypeKey;
import org.jboss.logging.Logger;

public class EnrichPipeline {

	public static final EntityTypeKey<Processor.Command> ENTITY_TYPE_KEY =
		EntityTypeKey.create(Processor.Command.class, "enrich-pipeline");
	private static final Logger log = Logger.getLogger(EnrichPipeline.class);

	/**
	 * Creates the entity behavior, calling enrichers through the CDI-managed
	 * HTTP actor.
	 *
	 * @param processKey the sharding key of this entity
	 * @return the behavior
	 */
	public static Behavior<Processor.Command> create(ShardingKey processKey) {
		return create(processKey, Http::create);
	}

	/**
	 * Creates the entity behavior with a custom factory for the actor that
	 * performs the HTTP requests to the enrichers.
	 *
	 * @param processKey the sharding key of this entity
	 * @param httpFactory factory of the HTTP actor
	 * @return the behavior
	 */
	public static Behavior<Processor.Command> create(
		ShardingKey processKey,
		Supplier<Behavior<Http.Command>> httpFactory) {

		return Behaviors.setup(ctx -> Behaviors
			.receive(Processor.Command.class)
			.onMessage(Processor.Start.class, setup -> onSetup(
				ctx,
				processKey,
				httpFactory,
				setup
			))
			.onMessage(Callback.class, callback -> {

				// the sharding recreates the entity on any message addressed
				// to it: a callback with no pipeline running is stale, tell so
				// and leave right away not to linger as an empty entity
				log.warnf(
					"[processKey: %s] unknown callback %s: no pipeline is running, " +
					"stopping.",
					processKey.asString(),
					callback.nonce()
				);

				callback.replyTo().tell(CallbackResponse.UNKNOWN);

				return Behaviors.stopped();
			})
			.build()
		);
	}

	public static Behavior<Processor.Command> onSetup(
		ActorContext<Processor.Command> ctx,
		ShardingKey processKey,
		Supplier<Behavior<Http.Command>> httpFactory,
		Processor.Start setup
	) {

		SchedulerDTO scheduler = setup.scheduler();
		byte[] payloadArray = setup.ingestPayload();

		ActorRef<Processor.Response> scheduling = setup.replyTo();
		HeldMessage heldMessage = setup.heldMessage();

		var dataPayload = prepareDataPayload(payloadArray, scheduler);

		log.infof(
			"[schedulerId: %s, messageNumber: %s] start enrichPipeline for %s.",
			scheduler.getId(),
			heldMessage.messageNumber(),
			heldMessage
		);

		ActorRef<HttpSupervisor.Command> supervisorActorRef =
			ctx.spawnAnonymous(HttpSupervisor.create(
				processKey, ctx.getSelf(), httpFactory));

		return initPipeline(
			ctx,
			supervisorActorRef,
			scheduling,
			heldMessage,
			dataPayload,
			scheduler,
			scheduler.getEnrichItems()
		);

	}

	private static Behavior<Processor.Command> initPipeline(
		ActorContext<Processor.Command> ctx,
		ActorRef<HttpSupervisor.Command> httpSupervisor,
		ActorRef<Processor.Response> replyTo,
		HeldMessage heldMessage,
		DataPayload dataPayload,
		SchedulerDTO scheduler,
		Set<EnrichItemDTO> enrichPipelineItems
	) {

		long schedulerId = scheduler.getId();

		if (enrichPipelineItems.isEmpty()) {

			if (log.isDebugEnabled()) {
				log.debugf(
					"[schedulerId: %s, messageNumber: %s] pipeline is empty, " +
					"ready for the next step.",
					schedulerId,
					heldMessage
				);
			}

			stripBinaryUrls(dataPayload);

			var buffer = Json.encodeToBuffer(dataPayload);

			replyTo.tell(new Processor.Success(buffer.getBytes(), scheduler, heldMessage));

			return Behaviors.stopped();
		}

		EnrichItemDTO enrichItem = Collections.head(enrichPipelineItems);
		Set<EnrichItemDTO> tail = Collections.tail(enrichPipelineItems);


		if (log.isDebugEnabled()) {
			log.debugf(
				"[schedulerId %s, messageNumber: %s] start enrichItem with id %s.",
				schedulerId,
				heldMessage.messageNumber(),
				enrichItem.getId()
			);
		}

		String jsonPath = enrichItem.getJsonPath();
		EnrichItem.BehaviorMergeType behaviorMergeType = enrichItem.getBehaviorMergeType();

		ActorRef<EnrichItemSupervisor.Command> enrichItemSupervisorRef =
			ctx.spawnAnonymous(EnrichItemSupervisor.create(httpSupervisor));

		Long requestTimeout = enrichItem.getRequestTimeout();

		var pending = new PendingCallback();

		ctx.ask(
			EnrichItemSupervisor.Response.class,
			enrichItemSupervisorRef,
			Duration.ofMillis(requestTimeout),
			enrichItemReplyTo ->
				new EnrichItemSupervisor.Execute(
					enrichItem, dataPayload, enrichItemReplyTo),
			(r, t) -> {
				if (t != null) {
					return new EnrichItemError(new DataProcessException(t));
				}
				else if (r instanceof EnrichItemSupervisor.Error supervisorError) {
					return new EnrichItemError(new DataProcessException(supervisorError.error()));
				}
				else {
					return new EnrichItemSupervisorResponseWrapper(r);
				}
			}
		);

		return Behaviors.receive(Processor.Command.class)
			.onMessage(EnrichItemError.class, enrichItemError -> {

				var exception = enrichItemError.exception();

				EnrichItem.BehaviorOnError behaviorOnError;

				if (enrichItem.getBehaviorOnError() == null) {
					if (log.isDebugEnabled()) {
						log.debugf(
							"[schedulerId: %s, messageNumber: %s] enrichItem %s " +
							"behavior on error fallback to FAIL");
					}

					behaviorOnError = EnrichItem.BehaviorOnError.FAIL;
				}
				else {
					behaviorOnError = enrichItem.getBehaviorOnError();
				}

				return switch (behaviorOnError) {
					case SKIP -> {

						log.warnf(
							exception,
							"[schedulerId: %s, messageNumber: %s] enrichItem %s error detected, " +
							"behavior is SKIP, pipeline is going on. Caught",
							schedulerId,
							heldMessage.messageNumber(),
							enrichItem.getId()
						);

						if (!tail.isEmpty() && log.isDebugEnabled()) {
							log.debugf(
								"[schedulerId: %s, messageNumber: %s] call next enrichItem.",
								schedulerId,
								heldMessage.messageNumber()
							);
						}

						yield initPipeline(
							ctx, httpSupervisor, replyTo,
							heldMessage, dataPayload, scheduler, tail
						);

					}
					case REJECT -> {

						log.warnf(
							exception,
							"[schedulerId: %s, messageNumber: %s] enrichItem %s error detected " +
							"behavior is REJECT, " +
							"pipeline is stopped and processor is succeeded. Caught",
							schedulerId,
							heldMessage.messageNumber(),
							enrichItem.getId()
						);

						stripBinaryUrls(dataPayload);

						var buffer = Json.encodeToBuffer(dataPayload);

						replyTo.tell(new Processor.Success(
							buffer.getBytes(),
							scheduler,
							heldMessage
						));

						yield Behaviors.stopped();

					}
					case FAIL -> {

						log.warnf(
							"[schedulerId: %s, messageNumber: %s] enrichItem %s error detected, " +
							"behavior is FAIL (default), " +
							"raising error to the pipeline: %s",
							schedulerId,
							heldMessage.messageNumber(),
							enrichItem.getId(),
							exception
						);

						ctx.getSelf().tell(new InternalError(exception));

						yield Behaviors.same();

					}
				};

			})
			.onMessage(EnrichItemSupervisorResponseWrapper.class, garw -> {
				EnrichItemSupervisor.Response response = garw.response;

				if (response instanceof EnrichItemSupervisor.Body body) {
					ctx.getSelf().tell(new InternalResponseWrapper(body.body()));
				}
				else {
					EnrichItemSupervisor.Error error = (EnrichItemSupervisor.Error) response;

					log.warnf(
						"[schedulerId: %s, messageNumber: %s] enrichItem %s error detected, " +
						"raising error to the pipeline.",
						schedulerId,
						heldMessage.messageNumber(),
						enrichItem.getId()
					);

					ctx.getSelf().tell(new InternalError(new DataProcessException(error.error())));
				}

				return Behaviors.same();

			})
			.onMessage(InternalResponseWrapper.class, srw -> {

				if (log.isDebugEnabled()) {
					log.debugf(
						"[schedulerId: %s, messageNumber: %s] enrichItem %s response is OK.",
						schedulerId,
						heldMessage.messageNumber(),
						enrichItem.getId()
					);

					if (!tail.isEmpty()) {
						log.debugf(
							"[schedulerId: %s, messageNumber: %s] call next enrichItem.",
							schedulerId,
							heldMessage.messageNumber()
						);
					}
				}

				JsonObject newJsonPayload = null;

				try {

					JsonObject result = new JsonObject(new String(srw.jsonObject()));
					newJsonPayload = result.getJsonObject("payload", result);

				}
				catch (Exception e) {

					ctx.getSelf().tell(
						new EnrichItemError(new DataProcessException(e)));

					return Behaviors.same();

				}

				if (newJsonPayload.getBoolean("_openk9SkipDocument", false)) {

					log.infof(
						"[schedulerId: %s, messageNumber: %s] document with contentId %s " +
						"can be skipped.",
						schedulerId,
						heldMessage.messageNumber(),
						dataPayload.getContentId()
					);

					replyTo.tell(new Processor.Skip(heldMessage));

					return Behaviors.stopped();
				}

				DataPayload newDataPayload = null;

				try {

					newDataPayload =
						mergeResponse(
							jsonPath, behaviorMergeType, dataPayload,
							newJsonPayload.mapTo(DataPayload.class)
						);

				}
				catch (Exception e) {

					ctx.getSelf().tell(
						new EnrichItemError(new DataProcessException(e)));

					return Behaviors.same();

				}

				return initPipeline(
					ctx,
					httpSupervisor,
					replyTo,
					heldMessage,
					newDataPayload,
					scheduler,
					tail
				);

			})
			.onMessage(InternalError.class, internalError -> {

				replyTo.tell(new Processor.Failure(
					internalError.exception(),
					heldMessage
				));

				return Behaviors.stopped();

			})
			.onMessage(HttpProcessor.RegisterCallback.class, register -> {

				pending.nonce = register.nonce();
				pending.waiter = register.waiter();

				return Behaviors.same();

			})
			.onMessage(Callback.class, callback -> {

				if (pending.nonce == null || !pending.nonce.equals(callback.nonce())) {

					log.warnf(
						"[schedulerId: %s, messageNumber: %s] unknown callback %s " +
						"for enrichItem %s, discarding.",
						schedulerId,
						heldMessage.messageNumber(),
						callback.nonce(),
						enrichItem.getId()
					);

					callback.replyTo().tell(CallbackResponse.UNKNOWN);

					return Behaviors.same();
				}

				pending.waiter.tell(new HttpProcessor.Callback(callback.body()));
				pending.nonce = null;
				pending.waiter = null;

				callback.replyTo().tell(CallbackResponse.ACCEPTED);

				return Behaviors.same();

			})
			.onMessage(Processor.Start.class, start -> {

				// a redelivery landed on the entity still working on the
				// previous delivery: fail it fast instead of letting the
				// consumer wait for an answer that will never come
				log.warnf(
					"[schedulerId: %s, messageNumber: %s] start received while " +
					"enrichItem %s is in progress, rejecting the redelivery.",
					schedulerId,
					heldMessage.messageNumber(),
					enrichItem.getId()
				);

				start.replyTo().tell(new Processor.Failure(
					new DataProcessException(
						"processor " + heldMessage.processKey().asString() +
						" is still working on a previous delivery"),
					start.heldMessage()
				));

				return Behaviors.same();

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
			prevJsonObject, newJsonObject
		);

		return prevDataPayload.rest(jsonMerge.merge(jsonPath).getMap());

	}

	private static DataPayload prepareDataPayload(byte[] payloadArray, SchedulerDTO scheduler) {
		DataPayload dataPayload =
			Json.decodeValue(Buffer.buffer(payloadArray), DataPayload.class);

		dataPayload.setIndexName(scheduler.getIndexName());

		String oldDataIndexName = scheduler.getOldIndexName();
		if (oldDataIndexName != null) {
			dataPayload.setOldIndexName(oldDataIndexName);
		}
		return dataPayload;
	}

	/**
	 * Drops the pre-signed URL injected into the binary references before the
	 * payload leaves the pipeline: it is a short-lived bearer credential that
	 * enrichers consume during processing and must never be persisted to the
	 * index.
	 *
	 * @param dataPayload the payload about to be handed to the writer
	 */
	static void stripBinaryUrls(DataPayload dataPayload) {

		var resources = dataPayload.getResources();

		if (resources == null || resources.getBinaries() == null) {
			return;
		}

		for (var binary : resources.getBinaries()) {
			binary.setUrl(null);
		}
	}

	private record EnrichItemSupervisorResponseWrapper(
		EnrichItemSupervisor.Response response
	) implements Processor.Command {}

	private record EnrichItemError(DataProcessException exception) implements Processor.Command {}

	private record InternalResponseWrapper(byte[] jsonObject) implements Processor.Command {}

	private record InternalError(DataProcessException exception) implements Processor.Command {}

	/**
	 * The body posted by an asynchronous enricher to the callback endpoint.
	 *
	 * @param nonce the nonce carried by the token the enricher was given
	 * @param body the posted body
	 * @param replyTo where to tell whether the callback was awaited
	 */
	public record Callback(
		String nonce,
		byte[] body,
		ActorRef<CallbackResponse> replyTo
	) implements Processor.Command {}

	public enum CallbackResponse implements CborSerializable {
		ACCEPTED,
		UNKNOWN
	}

	/**
	 * The callback awaited by the enrich item in progress, if any.
	 */
	private static final class PendingCallback {
		private String nonce;
		private ActorRef<HttpProcessor.Command> waiter;
	}

}
