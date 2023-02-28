package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.datasource.processor.payload.DataPayload;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Message;

public class IngestionActor {
	public sealed interface Command {}
	public record IngestionMessage(DataPayload dataPayload, Message<?> message) implements Command {}
	public record Callback(String tokenId, JsonObject body) implements Command { }
	private record DatasourceResponseWrapper(Message<?> message, DatasourceActor.Response response) implements Command {}


	public static Behavior<Command> create() {
		return Behaviors
			.supervise(Behaviors.setup(IngestionActor::initial))
			.onFailure(SupervisorStrategy.resume());
	}

	private static Behavior<Command> initial(ActorContext<Command> ctx) {


		ActorRef<Supervisor.Command> supervisorActorRef =
			ctx.spawn(
				Supervisor.create(),
				"enrich-pipeline-supervisor");

		return Behaviors.receive(Command.class)
			.onMessage(IngestionMessage.class, ingestionMessage -> {

				DataPayload dataPayload = ingestionMessage.dataPayload;
				String ingestionId = dataPayload.getIngestionId();

				ctx.getLog().info(
					"read message ingestionId: {}, contentId: {}",
					ingestionId, dataPayload.getContentId());

				ActorRef<DatasourceActor.Response> responseActorRef =
					ctx.messageAdapter(
						DatasourceActor.Response.class,
						param -> new DatasourceResponseWrapper(ingestionMessage.message(), param)
					);

				ActorRef<DatasourceActor.Command> datasourceActorRef =
					ctx.spawn(
						DatasourceActor.create(dataPayload, supervisorActorRef, responseActorRef),
						"datasource-" + ingestionId);

				datasourceActorRef.tell(DatasourceActor.Start.INSTANCE);

				return Behaviors.same();
			})
			.onMessage(DatasourceResponseWrapper.class, drw -> {

				DatasourceActor.Response response = drw.response;

				if (response instanceof DatasourceActor.Success) {
					drw.message.ack();
				}
				else if (response instanceof DatasourceActor.Failure) {
					drw.message.nack(((DatasourceActor.Failure) response).exception());
				}

				return Behaviors.same();

			})
			.onMessage(Callback.class, callback -> {

				supervisorActorRef.tell(
					new Supervisor.Callback(
						callback.tokenId(), callback.body()));

				return Behaviors.same();

			})
			.build();
	}

}
