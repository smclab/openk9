package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.pipeline.actor.enrichitem.EnrichItemSupervisor;
import io.openk9.datasource.pipeline.actor.enrichitem.HttpSupervisor;
import io.openk9.datasource.processor.payload.DataPayload;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IngestionActor {
	public sealed interface Command {}
	public record IngestionMessage(DataPayload dataPayload, Message<?> message) implements Command {}
	public record Callback(String tokenId, JsonObject body) implements Command { }
	private record DatasourceResponseWrapper(Message<?> message, DatasourceActor.Response response) implements Command {}
	private record EnrichItemResponseWrapper(EnrichItemActor.EnrichItemCallbackResponse response, Map<String, Object> datasourcePayload, ActorRef<Response> replyTo) implements Command {}
	private record SupervisorResponseWrapper(EnrichItemSupervisor.Response response, ActorRef<Response> replyTo) implements Command {}
	public record EnrichItemCallback(long enrichItemId, String tenantId, Map<String, Object> datasourcePayload, ActorRef<Response> replyTo) implements Command { }
	public sealed interface Response {}
	public record EnrichItemCallbackResponse(JsonObject jsonObject) implements Response {}
	public record EnrichItemCallbackError(String message) implements Response {}

	public static Behavior<Command> create() {
		return Behaviors
			.supervise(Behaviors.<Command>setup(ctx -> {

				ActorRef<HttpSupervisor.Command> supervisorActorRef =
					ctx.spawn(
						HttpSupervisor.create(),
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
		ActorRef<HttpSupervisor.Command> supervisorActorRef,
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
					new HttpSupervisor.Callback(
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

		EnrichItemSupervisor.Response response = srw.response;
		ActorRef<Response> replyTo = srw.replyTo;

		if (response instanceof EnrichItemSupervisor.Body) {
			replyTo.tell(new EnrichItemCallbackResponse(((EnrichItemSupervisor.Body)response).body()));
		}
		else {
			replyTo.tell(new EnrichItemCallbackError(((EnrichItemSupervisor.Error)response).error()));
		}

		return Behaviors.same();

	}

	private static Behavior<Command> onEnrichItemResponseWrapper(
		ActorContext<Command> ctx,
		ActorRef<HttpSupervisor.Command> supervisorActorRef,
		EnrichItemResponseWrapper eirw) {

		ActorRef<Response> replyTo = eirw.replyTo;
		EnrichItemActor.EnrichItemCallbackResponse response = eirw.response;
		Map<String, Object> datasourcePayload = eirw.datasourcePayload;

		EnrichItem enrichItem = response.enrichItem();

		JsonObject datasourcePayloadJson = new JsonObject(datasourcePayload);

		DataPayload dataPayload =
			DataPayload
				.builder()
				.acl(Map.of())
				.rawContent(datasourcePayloadJson.toString())
				.parsingDate(Instant.now().toEpochMilli())
				.documentTypes(
					datasourcePayloadJson
						.fieldNames()
						.toArray(new String[0])
				)
				.rest(datasourcePayload)
				.build();

		ActorRef<EnrichItemSupervisor.Response> responseActorRef =
			ctx.messageAdapter(
				EnrichItemSupervisor.Response.class,
				param -> new SupervisorResponseWrapper(param, replyTo)
			);

		ActorRef<EnrichItemSupervisor.Command> enrichItemActorRef =
			ctx.spawnAnonymous(EnrichItemSupervisor.create(supervisorActorRef));

		enrichItemActorRef.tell(
			new EnrichItemSupervisor.Execute(
				enrichItem, dataPayload, responseActorRef
			)
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
				eic.enrichItemId, eic.tenantId, enrichItemCallbackResponseActorRef));

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
