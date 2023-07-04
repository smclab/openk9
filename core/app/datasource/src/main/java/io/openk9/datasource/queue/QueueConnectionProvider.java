package io.openk9.datasource.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.smallrye.mutiny.Uni;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@ApplicationScoped
public class QueueConnectionProvider {

	private Connection connect;

	@PostConstruct
	public void init() {
		this.connect = rabbitMQClient.connect();
		executor = Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r, "rabbitmq-channel-factory");
			t.setDaemon(true);
			return t;
		});
	}

	@PreDestroy
	public void destroy() throws IOException {
		connect.close();
	}

	public ChannelFactory getConnectFactory() {
		return () -> Uni
			.createFrom()
			.emitter(sink ->
				executor.execute(() -> {
					try {
						Channel channel = connect.createChannel();
						channel.basicQos(1);
						sink.complete(channel);
					}
					catch (Exception e) {
						sink.fail(e);
					}
				})
			);
	}

	@Inject
	RabbitMQClient rabbitMQClient;

	Executor executor;

}
