package io.openk9.datasource.pipeline.consumer;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.ActorContext;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.typesafe.config.Config;
import io.openk9.datasource.actor.AkkaUtils;
import io.openk9.datasource.pipeline.actor.QueueManager;
import io.openk9.datasource.pipeline.actor.Schedulation;

import java.time.Duration;

public abstract class BaseConsumer extends DefaultConsumer {

	private static final String CONSUMER_TIMEOUT = "io.openk9.schedulation.consumer.timeout";

	protected final ActorContext<?> context;
	protected final Config config;
	protected final QueueManager.QueueBind queueBind;
	protected final Duration timeout;

	public BaseConsumer(Channel channel, ActorContext<?> context, QueueManager.QueueBind queueBind) {
		super(channel);
		this.context = context;
		this.config = context.getSystem().settings().config();
		this.queueBind = queueBind;
		this.timeout = getTimeout(config);
	}

	protected EntityRef<Schedulation.Command> getSchedulation() {

		ActorSystem<?> actorSystem = context.getSystem();

		ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);

		return clusterSharding.entityRefFor(
			Schedulation.ENTITY_TYPE_KEY, queueBind.schedulationKey());
	}

	private static Duration getTimeout(Config config) {
		return AkkaUtils.getProperty(config, CONSUMER_TIMEOUT, config::getDuration, Duration.ofMinutes(10));
	}
}
