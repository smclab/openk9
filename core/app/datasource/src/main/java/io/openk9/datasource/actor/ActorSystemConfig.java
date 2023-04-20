package io.openk9.datasource.actor;

import akka.cluster.typed.Cluster;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.javadsl.AkkaManagement;
import io.quarkus.arc.Priority;
import io.quarkus.arc.properties.IfBuildProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@Dependent
public class ActorSystemConfig {

	private static final String AKKA_CLUSTER_MODE = "akka.cluster.file";

	@Produces
	@ApplicationScoped
	@Priority(Integer.MAX_VALUE)
	@IfBuildProperty(name = AKKA_CLUSTER_MODE, stringValue = "local", enableIfMissing = true)
	public ActorSystemInitializer local() {
		logger.info("create local cluster actor system");
		return actorSystem -> {
			Cluster.get(actorSystem);
			AkkaManagement.get(actorSystem).start();
		};
	}

	@Produces
	@ApplicationScoped
	@Priority(Integer.MAX_VALUE)
	@IfBuildProperty(name = AKKA_CLUSTER_MODE, stringValue = "cluster")
	public ActorSystemInitializer cluster() {
		logger.info("create remote cluster actor system");
		return actorSystem -> {
			AkkaManagement.get(actorSystem).start();
			ClusterBootstrap.get(actorSystem).start();
		};
	}

	@Inject
	Logger logger;

}
