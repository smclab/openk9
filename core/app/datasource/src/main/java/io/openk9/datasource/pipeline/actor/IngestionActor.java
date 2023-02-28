package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.datasource.processor.payload.DataPayload;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class IngestionActor {
	public sealed interface Command {}
	public record IngestionMessage(DataPayload dataPayload, Message<?> message) implements Command {}
	public record Callback(String tokenId, JsonObject body) implements Command { }
	private record DatasourceResponseWrapper(Message<?> message, DatasourceActor.Response response) implements Command {}


	public static Behavior<Command> create() {
		return Behaviors
			.supervise(Behaviors.<Command>setup(ctx -> {

				ActorRef<Supervisor.Command> supervisorActorRef =
					ctx.spawn(
						Supervisor.create(),
						"enrich-pipeline-supervisor");

				return initial(ctx, supervisorActorRef, new ArrayList<>());

			}))
			.onFailure(SupervisorStrategy.restartWithBackoff(
				Duration.ofMillis(500), Duration.ofSeconds(5), 0.1));
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx,
		ActorRef<Supervisor.Command> supervisorActorRef,
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

				return initial(ctx, supervisorActorRef, newMessages);

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

				return initial(ctx, supervisorActorRef, newMessages);

			})
			.onMessage(Callback.class, callback -> {

				ctx.getLog().info("callback with tokenId: {}", callback.tokenId());

				supervisorActorRef.tell(
					new Supervisor.Callback(
						callback.tokenId(), callback.body()));

				return Behaviors.same();

			})
			.onSignal(PreRestart.class, signal -> onSignal(ctx, "ingestion actor restarting", messages))
			.onSignal(PostStop.class, signal -> onSignal(ctx, "ingestion actor stopped", messages))
			.build();
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
