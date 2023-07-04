package io.openk9.datasource.queue;

import com.rabbitmq.client.Channel;
import io.smallrye.mutiny.Uni;

public interface ChannelFactory {
	Uni<Channel> createChannel();
}
