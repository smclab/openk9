package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorSystem;
import io.openk9.datasource.processor.payload.DataPayload;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

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

	private ActorSystem<IngestionActor.Command> actorSystem;

}
