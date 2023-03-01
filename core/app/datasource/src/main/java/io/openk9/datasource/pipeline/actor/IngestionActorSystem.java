package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import io.openk9.datasource.processor.payload.DataPayload;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class IngestionActorSystem {

	@PostConstruct
	public void init() {
		this.actorSystem = ActorSystem.apply(
			IngestionActor.create(), "enrich-pipeline");
	}

	public void startEnrichPipeline(DataPayload dataPayload, Message<?> message) {
		this.actorSystem.tell(new IngestionActor.IngestionMessage(dataPayload, message));
	}

	public void callback(String tokenId, JsonObject body) {
		actorSystem.tell(new IngestionActor.Callback(tokenId, body));
	}

	public Uni<JsonObject> callEnrichItem(
		long datasourceId, long enrichItemId, String tenantId,
		Map<String, Object> datasourcePayload) {

		CompletionStage<IngestionActor.Response> future =
			AskPattern.ask(
				actorSystem,
				(ActorRef<IngestionActor.Response> replyTo) ->
					new IngestionActor.EnrichItemCallback(datasourceId, enrichItemId, tenantId, datasourcePayload, replyTo),
				Duration.ofSeconds(30),
				actorSystem.scheduler());

		return Uni.createFrom()
			.completionStage(future)
			.map(response -> {
				if (response instanceof IngestionActor.EnrichItemCallbackResponse) {
					return ((IngestionActor.EnrichItemCallbackResponse) response).jsonObject();
				}
				else {
					return JsonObject.of("error", ((IngestionActor.EnrichItemCallbackError) response).message());
				}
			});

	}

	private ActorSystem<IngestionActor.Command> actorSystem;

}
