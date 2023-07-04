package io.openk9.datasource.pipeline.service;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.pipeline.SchedulationKeyUtils;
import io.openk9.datasource.pipeline.actor.ChannelManager;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;

@ApplicationScoped
public class ChannelManagerService {
	private static final Logger log = LoggerFactory.getLogger(ChannelManagerService.class);
	@Inject
	RabbitMQClient rabbitMQClient;
	@Inject
	ActorSystemProvider actorSystemProvider;

	private ActorSystem<?> actorSystem;
	private Channel channel;

	@PostConstruct
	public void init() throws IOException {
		this.actorSystem = actorSystemProvider.getActorSystem();

		Connection connect = rabbitMQClient.connect();
		this.channel = connect.createChannel();
	}

	@PreDestroy
	public void destroy() throws Exception {
		Connection connection = this.channel.getConnection();
		connection.close();
		this.channel.close();
		this.channel = null;
	}


	public void queueSpawn(String tenantId, String scheduleId) {
		ActorRef<ChannelManager.Command> channelManager = getChannelManager();
		channelManager.tell(
			new ChannelManager.QueueSpawn(SchedulationKeyUtils.getValue(tenantId, scheduleId)));
	}

	public void queueDestroy(String tenantId, String scheduleId) {
		ActorRef<ChannelManager.Command> channelManager = getChannelManager();
		channelManager.tell(
			new ChannelManager.QueueDestroy(SchedulationKeyUtils.getValue(tenantId, scheduleId)));
	}

	public ActorRef<ChannelManager.Command> getChannelManager() {
		ClusterSingleton clusterSingleton = ClusterSingleton.get(actorSystem);
		return clusterSingleton.init(
			SingletonActor.of(ChannelManager.create(rabbitMQClient), "channel-manager")
		);
	}

}
