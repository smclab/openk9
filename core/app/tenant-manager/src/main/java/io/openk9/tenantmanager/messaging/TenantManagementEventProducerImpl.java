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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.event.tenant.TenantManagementEvent;
import io.openk9.event.tenant.TenantManagementEventProducer;
import io.openk9.tenantmanager.model.OutboxEvent;
import io.openk9.tenantmanager.service.OutboxEventService;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.vertx.VertxContextSupport;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;
import org.jboss.logging.Logger;
import org.reactivestreams.Publisher;

@ApplicationScoped
public class TenantManagementEventProducerImpl
	implements TenantManagementEventProducer {

	@Override
	public void send(TenantManagementEvent event) throws Throwable {
		VertxContextSupport.subscribeAndAwait(() -> outbox.persist(event));
	}

	@Override
	public Publisher<Void> sendAsync(TenantManagementEvent event) {

		return outbox.persist(event)
			.convert()
			.with(UniReactorConverters.toMono());
	}

	@PostConstruct
	public void setup() {
		if (conn == null) {
			conn = rabbit.connect();
		}

		log.info("Setup RabbitMQ connection");

		try {
			Channel channel = getChannel();

			channel.queueDeclare(
				TenantManagementEvent.TOPIC,
				true,
				false, false,
				Collections.singletonMap(X_QUEUE_TYPE, "stream"));

			channel.queueBind(
				TenantManagementEvent.TOPIC,
				EXCHANGE,
				TenantManagementEvent.TOPIC);
		}
		catch (Exception e) {
			log.error(
				"An error occurred while setting up RabbitMQ connection:", e);
		}

	}

	@PreDestroy
	public void teardown() {

		log.info("Teardown RabbitMQ connection");
		try {
			Objects.requireNonNull(conn);

			conn.close();
			conn = null;
		}
		catch (Exception e) {
			log.warn("An error occurred while tearing down RabbitMQ connection:", e);
		}

	}

	@Scheduled(every = "5s")
	public Uni<Void> publish() {

		return outbox.unsentEvents()
			.chain(outboxEvents -> {
				if (log.isDebugEnabled()) {
					log.infof("Sending %d events...", outboxEvents.size());
				}

				List<Long> ids = new ArrayList<>();

				for(OutboxEvent event : outboxEvents) {
					if (log.isTraceEnabled()) {
						log.tracef(
							"Sending to %s queue a %s event: %s",
							TenantManagementEvent.TOPIC,
							event.getEventType(),
							event.getPayload());
					}

					this.amqpSend(
						event.getEventType(),
						event.getPayload().getBytes(StandardCharsets.UTF_8)
					);

					ids.add(event.getId());
				}

				return outbox.flagAsSent(ids);
			}).replaceWithVoid();
	}

	private void amqpSend(String eventType, byte[] payload) {
		try {
			Channel channel = getChannel();
			AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
				.deliveryMode(2)
				.headers(Map.of(X_EVENT_TYPE, eventType))
				.build();

			channel.basicPublish(
				EXCHANGE, TenantManagementEvent.TOPIC, properties, payload);
		}
		catch (Exception e) {
			log.errorf(
				e, "An error occurred while sending a %s event: ", eventType);
		}
	}

	private Channel getChannel() throws IOException {
		Channel channel = channelThreadLocal.get();

		if (channel == null) {
			channel = conn.createChannel();
			channelThreadLocal.set(channel);
		}

		return channel;
	}

	@Inject
	RabbitMQClient rabbit;

	@Inject
	OutboxEventService outbox;

	private Connection conn = null;

	private static final ThreadLocal<Channel> channelThreadLocal = new ThreadLocal<>();

	private static final String EXCHANGE = "amq.topic";
	private static final String X_EVENT_TYPE = "x-event-type";
	private static final String X_QUEUE_TYPE = "x-queue-type";

	private final static Logger log = Logger.getLogger(
		TenantManagementEventProducerImpl.class);
}
