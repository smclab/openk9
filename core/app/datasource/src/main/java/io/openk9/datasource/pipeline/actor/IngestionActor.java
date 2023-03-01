package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.pipeline.actor.dto.EnrichItemProjection;
import io.openk9.datasource.processor.payload.DataPayload;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IngestionActor {
	public sealed interface Command {}
	public record IngestionMessage(DataPayload dataPayload, Message<?> message) implements Command {}
	public record Callback(String tokenId, JsonObject body) implements Command { }
	private record DatasourceResponseWrapper(Message<?> message, DatasourceActor.Response response) implements Command {}
	private record EnrichItemResponseWrapper(EnrichItemActor.EnrichItemCallbackResponse response, Map<String, Object> datasourcePayload, ActorRef<Response> replyTo) implements Command {}
	private record SupervisorResponseWrapper(Supervisor.Response response, ActorRef<Response> replyTo) implements Command {}
	public record EnrichItemCallback(long datasourceId, long enrichItemId, String tenantId, Map<String, Object> datasourcePayload, ActorRef<Response> replyTo) implements Command { }
	public sealed interface Response {}
	public record EnrichItemCallbackResponse(JsonObject jsonObject) implements Response {}
	public record EnrichItemCallbackError(String message) implements Response {}


	public static Behavior<Command> create() {
		return Behaviors
			.supervise(Behaviors.<Command>setup(ctx -> {

				ActorRef<Supervisor.Command> supervisorActorRef =
					ctx.spawn(
						Supervisor.create(),
						"enrich-pipeline-supervisor");

				ActorRef<EnrichItemActor.Command> enrichItemActorRef =
					ctx.spawn(
						EnrichItemActor.create(),
						"enrich-item-actor");

				return initial(
					ctx, supervisorActorRef, enrichItemActorRef, new ArrayList<>());

			}))
			.onFailure(SupervisorStrategy.restartWithBackoff(
				Duration.ofMillis(500), Duration.ofSeconds(5), 0.1));
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx,
		ActorRef<Supervisor.Command> supervisorActorRef,
		ActorRef<EnrichItemActor.Command> enrichItemActorRef,
		List<Message<?>> messages) {

		return Behaviors.receive(Command.class)
			.onMessage(IngestionMessage.class, ingestionMessage -> {

				DataPayload dataPayload = ingestionMessage.dataPayload;
				Message<?> message = ingestionMessage.message;
				String ingestionId = dataPayload.getIngestionId();

				ctx.getLog().info(
					"read message ingestionId: {}, contentId: {}",
					ingestionId, dataPayload.getContentId());

				ActorRef<DatasourceActor.Response> responseActorRef =
					ctx.messageAdapter(
						DatasourceActor.Response.class,
						param -> new DatasourceResponseWrapper(message, param)
					);

				ActorRef<DatasourceActor.Command> datasourceActorRef =
					ctx.spawn(
						DatasourceActor.create(dataPayload, supervisorActorRef, responseActorRef),
						"datasource-" + ingestionId);

				datasourceActorRef.tell(DatasourceActor.Start.INSTANCE);

				List<Message<?>> newMessages = new ArrayList<>(messages);
				newMessages.add(message);

				return initial(ctx, supervisorActorRef, enrichItemActorRef, newMessages);

			})
			.onMessage(DatasourceResponseWrapper.class, drw -> {

				DatasourceActor.Response response = drw.response;
				Message<?> message = drw.message;

				if (response instanceof DatasourceActor.Success) {
					ctx.getLog().info("enrich pipeline success, ack message");
					message.ack();
				}
				else if (response instanceof DatasourceActor.Failure) {
					Throwable exception =
						((DatasourceActor.Failure) response).exception();
					ctx.getLog().error(
						"enrich pipeline failure, nack message", exception);
					message.nack(exception);
				}

				List<Message<?>> newMessages = new ArrayList<>(messages);
				newMessages.remove(message);

				return initial(ctx, supervisorActorRef, enrichItemActorRef, newMessages);

			})
			.onMessage(Callback.class, callback -> {

				ctx.getLog().info("callback with tokenId: {}", callback.tokenId());

				supervisorActorRef.tell(
					new Supervisor.Callback(
						callback.tokenId(), callback.body()));

				return Behaviors.same();

			})
			.onMessage(EnrichItemResponseWrapper.class, eirw -> onEnrichItemResponseWrapper(ctx, supervisorActorRef, eirw))
			.onMessage(EnrichItemCallback.class, eic -> onEnrichItemCallback(ctx, enrichItemActorRef, eic))
			.onMessage(SupervisorResponseWrapper.class, srw -> onSupervisorResponseWrapper(ctx, srw))
			.onSignal(PreRestart.class, signal -> onSignal(ctx, "ingestion actor restarting", messages))
			.onSignal(PostStop.class, signal -> onSignal(ctx, "ingestion actor stopped", messages))
			.build();
	}

	private static Behavior<Command> onSupervisorResponseWrapper(
		ActorContext<Command> ctx, SupervisorResponseWrapper srw) {

		Supervisor.Response response = srw.response;
		ActorRef<Response> replyTo = srw.replyTo;

		if (response instanceof Supervisor.Body) {
			replyTo.tell(new EnrichItemCallbackResponse(((Supervisor.Body)response).jsonObject()));
		}
		else {
			replyTo.tell(new EnrichItemCallbackError(((Supervisor.Error)response).error()));
		}

		return Behaviors.same();

	}

	private static Behavior<Command> onEnrichItemResponseWrapper(
		ActorContext<Command> ctx,
		ActorRef<Supervisor.Command> supervisorActorRef,
		EnrichItemResponseWrapper eirw) {

		ActorRef<Response> replyTo = eirw.replyTo;
		EnrichItemActor.EnrichItemCallbackResponse response = eirw.response;
		Map<String, Object> datasourcePayload = eirw.datasourcePayload;

		EnrichItemProjection enrichItemProjection =
			response.enrichItemProjection();
		String jsonConfig = enrichItemProjection.jsonConfig();
		EnrichItem.EnrichItemType enrichItemType =
			enrichItemProjection.enrichItemType();
		String serviceName = enrichItemProjection.serviceName();
		long datasourceId = enrichItemProjection.datasourceId();

		boolean async = enrichItemType == EnrichItem.EnrichItemType.ASYNC;

		JsonObject datasourcePayloadJson = new JsonObject(datasourcePayload);

		DataPayload dataPayload =
			DataPayload
				.builder()
				.ingestionId(UUID.randomUUID().toString())
				.contentId(UUID.randomUUID().toString())
				.datasourceId(datasourceId)
				.acl(Map.of())
				.rawContent(datasourcePayloadJson.toString())
				.parsingDate(Instant.now().toEpochMilli())
				.documentTypes(datasourcePayloadJson.fieldNames().toArray(
					new String[0]))
				.rest(datasourcePayload)
				.build();

		ActorRef<Supervisor.Response> responseActorRef =
			ctx.messageAdapter(
				Supervisor.Response.class,
				param -> new SupervisorResponseWrapper(param, replyTo)
			);

		supervisorActorRef.tell(
			new Supervisor.Call(
				async,
				serviceName,
				JsonObject.of(
					"payload", Json.encode(dataPayload),
					"enrichItemConfig", jsonConfig == null || jsonConfig.isBlank()
						? new JsonObject()
						: new JsonObject(jsonConfig)),
				responseActorRef)
		);

		return Behaviors.same();
	}

	private static Behavior<Command> onEnrichItemCallback(
		ActorContext<Command> ctx,
		ActorRef<EnrichItemActor.Command> enrichItemActorRef,
		EnrichItemCallback eic) {

		Map<String, Object> datasourcePayload = eic.datasourcePayload;

		ActorRef<EnrichItemActor.EnrichItemCallbackResponse> enrichItemCallbackResponseActorRef =
			ctx.messageAdapter(
				EnrichItemActor.EnrichItemCallbackResponse.class,
				param -> new EnrichItemResponseWrapper(param, datasourcePayload, eic.replyTo)
			);

		enrichItemActorRef.tell(
			new EnrichItemActor.EnrichItemCallback(
				eic.datasourceId, eic.enrichItemId, eic.tenantId, enrichItemCallbackResponseActorRef));

		return Behaviors.same();

	}

	private static Behavior<Command> onSignal(
		ActorContext<Command> ctx, String signalMessage, List<Message<?>> messages) {

		ctx.getLog().error(signalMessage);

		RuntimeException runtimeException = new RuntimeException(signalMessage);

		for (Message<?> message : messages) {
			message.nack(runtimeException);
		}

		return Behaviors.same();
	}

}
