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

package io.openk9.datasource.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
