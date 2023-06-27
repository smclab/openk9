package io.openk9.datasource.actor;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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

		Config defaultConfig = ConfigFactory.load(clusterFile);
		Config config = defaultConfig.withFallback(ConfigFactory.load());

		actorSystem = ActorSystem.create(Behaviors.empty(), "datasource", config);

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

	private ActorSystem<?> actorSystem;

	@Inject
	Instance<ActorSystemInitializer> actorSystemInitializerInstance;
	@ConfigProperty(name = "akka.cluster.file")
	String clusterFile;

}
