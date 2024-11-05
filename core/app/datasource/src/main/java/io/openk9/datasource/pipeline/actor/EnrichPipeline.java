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

import io.openk9.common.util.ShardingKey;
import io.openk9.common.util.collection.Collections;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.pipeline.actor.enrichitem.EnrichItemSupervisor;
import io.openk9.datasource.pipeline.actor.enrichitem.HttpSupervisor;
import io.openk9.datasource.pipeline.service.dto.EnrichItemDTO;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.Processor;
import io.openk9.datasource.processor.payload.DataPayload;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Set;

public class EnrichPipeline {

	public static final EntityTypeKey<Processor.Command> ENTITY_TYPE_KEY =
		EntityTypeKey.create(Processor.Command.class, "enrich-pipeline");
	private static final Logger log = Logger.getLogger(EnrichPipeline.class);

	public static Behavior<Processor.Command> create(ShardingKey processKey) {
		return Behaviors.setup(ctx -> Behaviors
			.receive(Processor.Command.class)
			.onMessage(Processor.Start.class, setup -> onSetup(
				ctx,
				processKey,
				setup
			))
			.build()
		);
	}

	public static Behavior<Processor.Command> onSetup(
		ActorContext<Processor.Command> ctx,
		ShardingKey processKey,
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
			ctx.spawnAnonymous(HttpSupervisor.create(processKey.baseKey()));

		return initPipeline(
			ctx,
			supervisorActorRef,
			scheduling,
			heldMessage,
			dataPayload,
			scheduler.getId(),
			scheduler.getEnrichItems()
		);

	}

	private static Behavior<Processor.Command> initPipeline(
		ActorContext<Processor.Command> ctx,
		ActorRef<HttpSupervisor.Command> httpSupervisor,
		ActorRef<Processor.Response> replyTo,
		HeldMessage heldMessage,
		DataPayload dataPayload,
		long schedulerId,
		Set<EnrichItemDTO> enrichPipelineItems
	) {

		if (enrichPipelineItems.isEmpty()) {

			if (log.isDebugEnabled()) {
				log.debugf(
					"[schedulerId: %s, messageNumber: %s] pipeline is empty, " +
					"ready for the next step.",
					schedulerId,
					heldMessage
				);
			}

			var buffer = Json.encodeToBuffer(dataPayload);

			replyTo.tell(new Processor.Success(buffer.getBytes(), heldMessage));

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

		LocalDateTime expiredDate =
			LocalDateTime.now().plus(requestTimeout, ChronoUnit.MILLIS);

		ctx.ask(
			EnrichItemSupervisor.Response.class,
			enrichItemSupervisorRef,
			Duration.ofMillis(requestTimeout),
			enrichItemReplyTo ->
				new EnrichItemSupervisor.Execute(
					enrichItem, dataPayload, expiredDate, enrichItemReplyTo),
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
							heldMessage, dataPayload, schedulerId, tail
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

						var buffer = Json.encodeToBuffer(dataPayload);

						replyTo.tell(new Processor.Success(buffer.getBytes(), heldMessage));

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

				JsonObject result = new JsonObject(new String(srw.jsonObject()));

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

				JsonObject newJsonPayload = result.getJsonObject("payload");

				if (newJsonPayload == null) {
					newJsonPayload = result;
				}

				if (newJsonPayload.getBoolean("_openk9SkipDocument", false)) {

					log.infof(
						"[schedulerId: %s, messageNumber: %s] document with contentId %s " +
						"can be skipped.",
						schedulerId,
						heldMessage.messageNumber(),
						dataPayload.getContentId()
					);

					replyTo.tell(new Skip(heldMessage));

					return Behaviors.stopped();
				}

				DataPayload newDataPayload =
					mergeResponse(
						jsonPath, behaviorMergeType, dataPayload,
						newJsonPayload.mapTo(DataPayload.class)
					);

				return initPipeline(
					ctx,
					httpSupervisor,
					replyTo,
					heldMessage,
					newDataPayload,
					schedulerId,
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

		String oldDataIndexName = scheduler.getOldDataIndexName();
		if (oldDataIndexName != null) {
			dataPayload.setOldIndexName(oldDataIndexName);
		}
		return dataPayload;
	}

	private record EnrichItemSupervisorResponseWrapper(
		EnrichItemSupervisor.Response response
	) implements Processor.Command {}

	private record EnrichItemError(DataProcessException exception) implements Processor.Command {}

	private record InternalResponseWrapper(byte[] jsonObject) implements Processor.Command {}

	private record InternalError(DataProcessException exception) implements Processor.Command {}

	public record Skip(HeldMessage heldMessage) implements Processor.Response {}

}
