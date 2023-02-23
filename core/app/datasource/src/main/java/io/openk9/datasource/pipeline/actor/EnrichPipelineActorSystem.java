package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.json.JsonObject;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class EnrichPipelineActorSystem {

	@PostConstruct
	public void init() {
		this.actorSystem = ActorSystem.apply(
			Supervisor.create(), "enrich-pipeline");
	}

	public Uni<JsonObject> call(
		boolean async, String url, JsonObject body) {

		CompletionStage<Supervisor.Response> response =
			AskPattern.ask(
				actorSystem,
				(ActorRef<Supervisor.Response> replyTo) -> new Supervisor.Call(
					async, url, body, replyTo),
				Duration.ofMinutes(5),
				actorSystem.scheduler()
			);

		return Uni
			.createFrom()
			.completionStage(response)
			.map(Unchecked.function(r -> {
				if (r instanceof Supervisor.Body) {
					return ((Supervisor.Body) r).jsonObject();
				}
				else {
					throw new RuntimeException(
						((Supervisor.Error) r).error());
				}
			}));

	}

	public void callback(String tokenId, JsonObject body) {

		actorSystem.tell(new Supervisor.Callback(tokenId, body));

	}

	private ActorSystem<Supervisor.Command> actorSystem;

}
