package io.openk9.datasource.actor;

import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.typed.Cluster;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.javadsl.AkkaManagement;
import io.openk9.datasource.cache.P2PCache;
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.pipeline.actor.MessageGateway;
import io.openk9.datasource.pipeline.actor.Schedulation;
import io.openk9.datasource.pipeline.actor.enrichitem.Token;
import io.openk9.datasource.queue.QueueConnectionProvider;
import io.openk9.datasource.service.DatasourceService;
import io.quarkus.arc.Priority;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Set;

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
	public ActorSystemInitializer schedulationSharding() {
		logger.info("init schedulation sharding");
		return actorSystem -> {
			ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);

			clusterSharding.init(Entity.of(Schedulation.ENTITY_TYPE_KEY, entityCtx -> {
				String entityId = entityCtx.getEntityId();
				String[] strings = entityId.split("#");
				Schedulation.SchedulationKey key =
					new Schedulation.SchedulationKey(strings[0], strings[1]);
				return Schedulation.create(
					key,
					sessionFactory
				);
			}));

			clusterSharding.init(Entity.of(Token.ENTITY_TYPE_KEY, entityCtx -> {
				String entityId = entityCtx.getEntityId();
				String[] strings = entityId.split("#");
				Schedulation.SchedulationKey key =
					new Schedulation.SchedulationKey(strings[0], strings[1]);
				return Token.create(key);
			}));

		};
	}

	@Produces
	@ApplicationScoped
	public ActorSystemBehaviorInitializer createChannelManager() {
		return ctx -> ctx.spawnAnonymous(
			MessageGateway.create(queueConnectionProvider, ingestionPayloadMapper));
	}

	@Produces
	@ApplicationScoped
	ActorSystemBehaviorInitializer cacheHandlerBehaviorInit() {
		return ctx -> ctx.spawnAnonymous(
			P2PCache.create(Set.of(bucketResourceCache, searcherServiceCache))
		);
	}


	@CacheName("bucket-resource")
	Cache bucketResourceCache;

	@CacheName("searcher-service")
	Cache searcherServiceCache;

	@Inject
	Logger logger;
	@Inject
	Mutiny.SessionFactory sessionFactory;
	@Inject
	DatasourceService datasourceService;
    @Inject
	QueueConnectionProvider queueConnectionProvider;
	@Inject
	IngestionPayloadMapper ingestionPayloadMapper;

}
