package io.openk9.datasource.actor;

import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.typed.Cluster;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.javadsl.AkkaManagement;
import io.openk9.datasource.model.ScheduleId;
import io.openk9.datasource.pipeline.actor.Schedulation;
import io.openk9.datasource.pipeline.actor.enrichitem.Token;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.sql.TransactionInvoker;
import io.quarkus.arc.Priority;
import io.quarkus.arc.properties.IfBuildProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.UUID;

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

	@Produces
	@ApplicationScoped
	public ActorSystemInitializer clusterSharding() {
		logger.info("init cluster sharding");
		return actorSystem -> {
			ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);

			clusterSharding.init(Entity.of(Schedulation.ENTITY_TYPE_KEY, entityCtx -> {
				String entityId = entityCtx.getEntityId();
				String[] strings = entityId.split("#");
				Schedulation.SchedulationKey key =
					new Schedulation.SchedulationKey(
						strings[0],
						new ScheduleId(UUID.fromString(strings[1])));
				return Schedulation.create(key, transactionInvoker, datasourceService);
			}));

			clusterSharding.init(Entity.of(Token.ENTITY_TYPE_KEY, entityCtx -> {
				String entityId = entityCtx.getEntityId();
				String[] strings = entityId.split("#");
				Schedulation.SchedulationKey key =
					new Schedulation.SchedulationKey(
						strings[0],
						new ScheduleId(UUID.fromString(strings[1])));
				return Token.create(key);
			}));

		};
	}

	@Inject
	Logger logger;
	@Inject
	TransactionInvoker transactionInvoker;
	@Inject
	DatasourceService datasourceService;

}
