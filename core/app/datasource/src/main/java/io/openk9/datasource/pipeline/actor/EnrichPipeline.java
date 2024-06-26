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

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
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

		log.infof("start pipeline for datasource with id %s", scheduler.getDatasourceId());

		ActorRef<HttpSupervisor.Command> supervisorActorRef =
			ctx.spawnAnonymous(HttpSupervisor.create(processKey.baseKey()));

		return initPipeline(
			ctx,
			supervisorActorRef,
			scheduling,
			heldMessage,
			dataPayload,
			scheduler.getEnrichItems()
		);

	}

	private static Behavior<Processor.Command> initPipeline(
		ActorContext<Processor.Command> ctx,
		ActorRef<HttpSupervisor.Command> httpSupervisor,
		ActorRef<Processor.Response> replyTo,
		HeldMessage heldMessage,
		DataPayload dataPayload,
		Set<EnrichItemDTO> enrichPipelineItems
	) {

		if (enrichPipelineItems.isEmpty()) {

			log.info("pipeline is empty, ready for the next step");

			var buffer = Json.encodeToBuffer(dataPayload);

			replyTo.tell(new Processor.Success(buffer.getBytes(), heldMessage));

			return Behaviors.stopped();
		}

		EnrichItemDTO enrichItem = Collections.head(enrichPipelineItems);
		Set<EnrichItemDTO> tail = Collections.tail(enrichPipelineItems);

		log.infof("start enrich for enrichItem with id %s", enrichItem.getId());

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
					return new EnrichItemError(enrichItem, t);
				}
				else if (r instanceof EnrichItemSupervisor.Error error) {
					return new EnrichItemError(enrichItem, new RuntimeException(error.error()));
				}
				else {
					return new EnrichItemSupervisorResponseWrapper(r);
				}
			}
		);

		return Behaviors.receive(Processor.Command.class)
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
							ctx, httpSupervisor, replyTo,
							heldMessage, dataPayload, tail
						);

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

						var buffer = Json.encodeToBuffer(dataPayload);

						replyTo.tell(new Processor.Success(buffer.getBytes(), heldMessage));

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
					EnrichItemSupervisor.Error error = (EnrichItemSupervisor.Error) response;
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

				if (newJsonPayload.getBoolean("_openk9SkipDocument", false)) {

					log.infof(
						"Document with contentId %s can be skipped.",
						dataPayload.getContentId()
					);

					var buffer = Json.encodeToBuffer(dataPayload);

					replyTo.tell(new Processor.Success(buffer.getBytes(), heldMessage));

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
					tail
				);

			})
			.onMessage(InternalError.class, srw -> {

				String error = srw.error();

				log.errorf("enrichItem: %s occurred error: %s", enrichItem.getId(), error);
				log.error("terminating pipeline");

				replyTo.tell(new Processor.Failure(
					new DataProcessException(error),
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

	private record EnrichItemError(
		EnrichItemDTO enrichItem,
		Throwable exception
	) implements Processor.Command {}

	private record InternalResponseWrapper(byte[] jsonObject) implements Processor.Command {}

	private record InternalError(String error) implements Processor.Command {}

}
