/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.actor;

import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.typed.Cluster;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.javadsl.AkkaManagement;
import io.openk9.datasource.cache.P2PCache;
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.pipeline.actor.EnrichPipeline;
import io.openk9.datasource.pipeline.actor.MessageGateway;
import io.openk9.datasource.pipeline.actor.Scheduling;
import io.openk9.datasource.pipeline.actor.enrichitem.Token;
import io.openk9.datasource.pipeline.actor.mapper.SchedulerMapper;
import io.openk9.datasource.pipeline.util.SchedulingKeyUtils;
import io.openk9.datasource.queue.QueueConnectionProvider;
import io.quarkus.arc.Priority;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import java.util.Set;
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

	@Produces
	@ApplicationScoped
	public ActorSystemInitializer schedulingSharding() {
		logger.info("init scheduling sharding");
		return actorSystem -> {
			ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);

			clusterSharding.init(Entity.of(Scheduling.ENTITY_TYPE_KEY, entityCtx -> {
				String entityId = entityCtx.getEntityId();
				var schedulingKey = SchedulingKeyUtils.fromString(entityId);
				return Scheduling.create(
					schedulingKey,
					sessionFactory,
					schedulerMapper
				);
			}));

			clusterSharding.init(Entity.of(Token.ENTITY_TYPE_KEY, entityCtx -> {
				String entityId = entityCtx.getEntityId();
				var schedulingKey = SchedulingKeyUtils.fromString(entityId);
				return Token.create(schedulingKey);
			}));

			clusterSharding.init(Entity.of(EnrichPipeline.ENTITY_TYPE_KEY, entityCtx -> {
				String entityId = entityCtx.getEntityId();
				String[] strings = entityId.split("#");
				var schedulingKey = SchedulingKeyUtils.fromString(entityId);
				String contentId = strings[2];

				return EnrichPipeline.create(schedulingKey, contentId);
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
	QueueConnectionProvider queueConnectionProvider;
	@Inject
	IngestionPayloadMapper ingestionPayloadMapper;
	@Inject
	SchedulerMapper schedulerMapper;

}
