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

package io.openk9.datasource;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import jakarta.inject.Inject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkus.test.junit.QuarkusTest;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class QueueTest {

	private static final String AMQ_TOPIC_EXCHANGE = "amq.topic";
	private static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
	private static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
	private static final String MAIN_QUEUE = "MAIN_QUEUE";
	private static final String RETRY_QUEUE = "RETRY_QUEUE";
	private static final String ERROR_QUEUE = "ERROR_QUEUE";
	private static final byte[] MESSAGE_BODY = "Hello World!".getBytes();
	private final Logger log = Logger.getLogger(QueueTest.class);
	@Inject
	RabbitMQClient rabbitMQClient;

	private Connection connection;

	@BeforeEach
	void setup() {

		try {
			this.connection = rabbitMQClient.connect();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Test
	void should_consume_without_error() throws Exception {

		try (Channel channel = this.connection.createChannel()) {

			bindQueues(channel);

			// publish a message
			channel.basicPublish(
				AMQ_TOPIC_EXCHANGE,
				MAIN_QUEUE,
				MessageProperties.PERSISTENT_BASIC,
				MESSAGE_BODY
			);

			var latch = new CountDownLatch(1);

			// consume message
			channel.basicConsume(
				MAIN_QUEUE,
				(consumerTag, message) -> {
					log.infof("Received: %s", message.getBody());
					latch.countDown();
				},
				(consumerTag, sig) -> {
					log.info("consumer closed");
					latch.countDown();
				}
			);

			latch.await();
		}

	}

	@Test
	void should_move_to_error() throws Exception {

		try (Channel channel = this.connection.createChannel()) {

			bindQueues(channel);

			// publish a message
			channel.basicPublish(
				AMQ_TOPIC_EXCHANGE,
				MAIN_QUEUE,
				MessageProperties.PERSISTENT_BASIC,
				MESSAGE_BODY
			);

			var latch = new CountDownLatch(1);

			// consume message
			channel.basicConsume(
				MAIN_QUEUE,
				(consumerTag, message) -> {
					var deliveryTag = message.getEnvelope().getDeliveryTag();
					log.infof("MainQueue received: %s", message.getBody());

					channel.basicNack(
						deliveryTag,
						false,
						false
					);

					log.infof("nack %s", deliveryTag);
				},
				(consumerTag, sig) -> log.info("consumer closed")
			);

			// retry message
			channel.basicConsume(
				RETRY_QUEUE,
				(consumerTag, message) -> {
					var deliveryTag = message.getEnvelope().getDeliveryTag();
					log.infof("RetryQueue received: %s", message.getBody());
					var properties = message.getProperties();
					var headers = properties.getHeaders();
					int count = (int) headers.getOrDefault("retry", 0);

					if (count < 3) {
						headers.put("retry", ++count);

						channel.basicPublish(
							AMQ_TOPIC_EXCHANGE,
							MAIN_QUEUE,
							properties,
							message.getBody()
						);

						channel.basicAck(
							message.getEnvelope().getDeliveryTag(), false);

						log.infof("re-publish %s", deliveryTag);
					}
					else {

						channel.basicNack(
							message.getEnvelope().getDeliveryTag(), false, false);

						log.infof("dlq error %s", deliveryTag);
					}
				},
				(consumerTag, sig) -> log.info("consumer closed")
			);

			// consume with ack on error
			channel.basicConsume(
				ERROR_QUEUE,
				(consumerTag, message) -> {
					var deliveryTag = message.getEnvelope().getDeliveryTag();
					log.infof("ErrorQueue received: %s", message.getBody());

					latch.countDown();

					channel.basicAck(deliveryTag, false);

					log.infof("ack %s", deliveryTag);
				},
				(consumerTag, sig) -> {
					log.info("consumer closed");
					latch.countDown();
				}
			);

			latch.await();
		}

	}

	@AfterEach
	void tearDown() {
		if (this.connection != null) {
			try {
				connection.close();
			}
			catch (Exception e) {
				log.error("Channel cannot be close");
			}
		}
	}

	private static void bindErrorQueue(Channel channel) throws IOException {
		// bind error queue
		channel.queueDeclare(
			ERROR_QUEUE,
			true,
			false,
			false,
			Map.of()
		);

		channel.queueBind(ERROR_QUEUE, AMQ_TOPIC_EXCHANGE, ERROR_QUEUE);
	}

	private static void bindMainQueue(Channel channel) throws IOException {
		// bind main queue
		channel.queueDeclare(
			MAIN_QUEUE,
			true,
			false,
			false,
			Map.of(
				X_DEAD_LETTER_EXCHANGE, AMQ_TOPIC_EXCHANGE,
				X_DEAD_LETTER_ROUTING_KEY, RETRY_QUEUE
			)
		);

		channel.queueBind(MAIN_QUEUE, AMQ_TOPIC_EXCHANGE, MAIN_QUEUE);
	}

	private static void bindQueues(Channel channel) throws IOException {
		bindMainQueue(channel);

		bindRetryQueue(channel);

		bindErrorQueue(channel);
	}

	private static void bindRetryQueue(Channel channel) throws IOException {
		// bind retry queue
		channel.queueDeclare(
			RETRY_QUEUE,
			true,
			false,
			false,
			Map.of(
				X_DEAD_LETTER_EXCHANGE, AMQ_TOPIC_EXCHANGE,
				X_DEAD_LETTER_ROUTING_KEY, ERROR_QUEUE
			)
		);

		channel.queueBind(RETRY_QUEUE, AMQ_TOPIC_EXCHANGE, RETRY_QUEUE);
	}

}
