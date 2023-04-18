package io.openk9.datasource.actor;

import akka.actor.typed.ActorSystem;
import akka.cluster.typed.Cluster;
import akka.management.javadsl.AkkaManagement;
import io.quarkus.arc.Priority;
import io.quarkus.arc.profile.IfBuildProfile;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Priority(Integer.MAX_VALUE)
@IfBuildProfile("dev")
public class LocalClusterInitializer implements ActorSystemInitializer {

	@Override
	public void init(ActorSystem<?> actorSystem) {
		logger.info("Local Cluster Initializer");
		Cluster.get(actorSystem);
		AkkaManagement.get(actorSystem).start();
	}

	@Inject
	Logger logger;

}
