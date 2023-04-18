package io.openk9.datasource.actor;

import akka.actor.typed.ActorSystem;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.javadsl.AkkaManagement;
import io.quarkus.arc.Priority;
import io.quarkus.arc.profile.IfBuildProfile;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Priority(Integer.MAX_VALUE)
@IfBuildProfile("prod")
public class ClusterBootstrapInitializer implements ActorSystemInitializer {

	@Override
	public void init(ActorSystem<?> actorSystem) {
		logger.info("Remote Cluster Initializer");
		AkkaManagement.get(actorSystem).start();
		ClusterBootstrap.get(actorSystem).start();
	}

	@Inject
	Logger logger;

}
