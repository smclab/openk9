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

package io.openk9.tenantmanager.messaging;

import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.event.tenant.TenantManagementEvent;
import io.openk9.event.tenant.TenantManagementEventProducer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;

@ApplicationScoped
public class Producer implements TenantManagementEventProducer {

	private final ObjectMapper mapper = new ObjectMapper();

	@Override
	public void send(TenantManagementEvent event) {


		try {
			var propertiesBuilder = new AMQP.BasicProperties.Builder();
			log.info("sending---");
			byte[] payload = mapper.writeValueAsBytes(event);
			com.rabbitmq.client.Channel channel = conn.createChannel();
			channel.basicPublish(
				"amq.topic",
				TenantManagementEvent.TOPIC,
				propertiesBuilder
					.deliveryMode(2)
					.headers(Map.of("x-event-type", event.getClass().getSimpleName()))
					.build(),
				payload
			);
		}
		catch (Exception e) {
			log.error("error: ", e);
		}
	}

	@Override
	public Publisher<Void> send(String eventType, byte[] payload) {
		return FlowAdapters.toPublisher(Uni.createFrom()
			.emitter(sink -> {
				try {
					AMQP.BasicProperties.Builder propertiesBuilder = new AMQP.BasicProperties.Builder();
					com.rabbitmq.client.Channel channel = conn.createChannel();
					channel.basicPublish(
						"amq.topic",
						TenantManagementEvent.TOPIC,
						propertiesBuilder
							.deliveryMode(2)
							.headers(Map.of("x-event-type", eventType))
							.build(),
						payload
					);
					sink.complete(null);
				}
				catch (Exception e) {
					sink.fail(e);
				}
			})
			.replaceWithVoid()
			.toMulti()
		);
	}

	@Inject
	RabbitMQClient rabbit;

	private Connection conn = null;

	@PostConstruct
	public void setup() {
		conn = rabbit.connect();

		log.info("setup rabbit");
		try {
			com.rabbitmq.client.Channel channel = conn.createChannel();
			channel.queueDeclare(
				TenantManagementEvent.TOPIC,
				true,
				false,
				false,
				null);
			channel.queueBind(
				TenantManagementEvent.TOPIC,
				"amq.topic",
				TenantManagementEvent.TOPIC);
		}
		catch (Exception e) {
			log.error("error:", e);
		}

	}

	@PreDestroy
	public void teardown() {

		log.info("teardown rabbit");
		try {
			conn.close();
			conn = null;
		}
		catch (Exception e) {
			log.error("error:", e);
		}

	}

	private final static Logger log = Logger.getLogger(Producer.class);
}
