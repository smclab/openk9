package io.openk9.datasource.actor;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.ClusterSingleton;
import io.quarkus.runtime.Startup;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
@Startup
public class ActorSystemProvider {

	@PostConstruct
	void init() {

		actorSystem = ActorSystem.create(Behaviors.empty(), "datasource");

		for (ActorSystemInitializer actorSystemInitializer : actorSystemInitializerInstance) {
			actorSystemInitializer.init(actorSystem);
		}

	}

	@PreDestroy
	void destroy() {
		actorSystem.terminate();
	}

	public ActorSystem<?> getActorSystem() {
		return actorSystem;
	}

	public ClusterSingleton getClusterSingleton() {
		return ClusterSingleton.get(actorSystem);
	}

	private ActorSystem<?> actorSystem;

	@Inject
	Instance<ActorSystemInitializer> actorSystemInitializerInstance;

}
