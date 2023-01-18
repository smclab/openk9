package io.openk9.tenantmanager.pipe.liquibase.validate;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import io.openk9.tenantmanager.pipe.liquibase.validate.util.Params;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.Deque;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class LiquibaseValidatorActorSystem {

	@PostConstruct
	void init() {
		_actorSystem = ActorSystem.apply(
			Supervisor.create(), "liquibase-validator-supervisor");
	}

	public Uni<Void> validateSchemas(Deque<Params> paramsList) {
		CompletionStage<Supervisor.Response> completionStage =
			AskPattern.ask(
				_actorSystem,
				(ActorRef<Supervisor.Response> replyTo) ->
					new Supervisor.Start(paramsList, replyTo),
				Duration.ofMinutes(10),
				_actorSystem.scheduler());

		return Uni.createFrom()
			.completionStage(completionStage)
			.invoke(response -> logger.info("response: " + response))
			.replaceWithVoid();

	}

	@Inject
	Logger logger;

	private ActorSystem<Supervisor.Command> _actorSystem;

}
