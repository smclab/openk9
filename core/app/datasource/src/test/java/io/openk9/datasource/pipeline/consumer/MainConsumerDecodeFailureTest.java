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

package io.openk9.datasource.pipeline.consumer;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import jakarta.inject.Inject;

import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.pipeline.actor.QueueManager;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.pekko.actor.typed.Props;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class MainConsumerDecodeFailureTest {

	private static final String AMQ_TOPIC_EXCHANGE = "amq.topic";
	private static final String DLX_EXCHANGE = "dlx";
	private static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
	private static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

	private static final int AWAIT_SECONDS = 30;
	private static final int QUIET_SECONDS = 5;

	private static final byte[] UNDECODABLE_BODY =
		"this is not a json payload".getBytes();
	private static final byte[] VALID_JSON_BUT_NOT_A_PAYLOAD =
		"[1, 2, 3]".getBytes();

	private static final Logger LOGGER =
		Logger.getLogger(MainConsumerDecodeFailureTest.class);

	@Inject
	RabbitMQClient rabbitMQClient;

	@Inject
	IngestionPayloadMapper ingestionPayloadMapper;

	@Inject
	ActorSystemProvider actorSystemProvider;

	private Connection connection;
	private Channel channel;
	private QueueManager.QueueBind queueBind;

	private final LinkedBlockingQueue<byte[]> deadLettered =
		new LinkedBlockingQueue<>();
	private final LinkedBlockingQueue<byte[]> reroutedToMain =
		new LinkedBlockingQueue<>();

	@BeforeEach
	void setup() throws Exception {
		this.connection = rabbitMQClient.connect();
		this.channel = connection.createChannel();

		// a fresh scheduling key per test keeps the three queues isolated
		this.queueBind = new QueueManager.QueueBind(
			"test-tenant#" + UUID.randomUUID());

		declareQueues();
	}

	@Test
	void should_dead_letter_undecodable_message_and_keep_channel_open()
		throws Exception {

		// 1. register the real MainConsumer on the main queue. RetryConsumer is
		// left out on purpose: it asks the Scheduling entity, which needs a
		// started shard region that a QuarkusTest does not have. What the retry
		// queue does with a rejected message is covered by
		// RetryConsumerDeathCountTest.
		registerMainConsumer();

		// 2. observe what the main queue dead-letters into the retry queue
		observeRetryQueue();

		// 3. publish a body that cannot be decoded
		publishToMainQueue(UNDECODABLE_BODY);

		// 4. it left the main queue through the dead letter exchange, so
		// MainConsumer rejected it instead of letting the exception escape
		Assertions.assertArrayEquals(
			UNDECODABLE_BODY,
			deadLettered.poll(AWAIT_SECONDS, TimeUnit.SECONDS),
			"the undecodable message was not dead-lettered");

		// 5. the channel must have survived. Before the fix the AMQP client
		// closed it, and every consumer registered on it went down at once.
		Assertions.assertTrue(
			channel.isOpen(),
			"the channel was closed by the undecodable message");

		// 6. and the subscription with it, so a second broken message is still
		// consumed instead of piling up in the main queue
		publishToMainQueue(VALID_JSON_BUT_NOT_A_PAYLOAD);

		Assertions.assertArrayEquals(
			VALID_JSON_BUT_NOT_A_PAYLOAD,
			deadLettered.poll(AWAIT_SECONDS, TimeUnit.SECONDS),
			"the consumer stopped working after the first broken message");

		Assertions.assertTrue(channel.isOpen(), "the channel was closed");

		// 7. each message was rejected once and for all: nothing was requeued
		// into the main queue and delivered again
		Assertions.assertNull(
			deadLettered.poll(QUIET_SECONDS, TimeUnit.SECONDS),
			"a message is still being redelivered");
	}

	@Test
	void should_let_error_consumer_reroute_without_closing_channel()
		throws Exception {

		// 1. only the error consumer is registered, as it happens on reroute
		channel.basicConsume(
			queueBind.getErrorQueue(),
			false,
			new ErrorConsumer(channel, spawnContext(), queueBind)
		);

		// 2. collect whatever is republished on the main queue
		channel.basicConsume(
			queueBind.getMainQueue(),
			false,
			(consumerTag, message) -> {
				reroutedToMain.add(message.getBody());
				channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
			},
			(consumerTag, sig) -> LOGGER.info("main queue consumer closed")
		);

		// 3. publish straight to the error queue
		channel.basicPublish(
			DLX_EXCHANGE,
			queueBind.getErrorKey(),
			MessageProperties.PERSISTENT_BASIC,
			UNDECODABLE_BODY
		);

		// 4. ErrorConsumer republishes to the main queue and acks
		Assertions.assertArrayEquals(
			UNDECODABLE_BODY,
			reroutedToMain.poll(AWAIT_SECONDS, TimeUnit.SECONDS),
			"the error consumer did not reroute the message");

		Assertions.assertTrue(channel.isOpen(), "the channel was closed");
	}

	@AfterEach
	void tearDown() {
		if (connection == null) {
			return;
		}

		try {
			deleteQueues();
		}
		catch (Exception e) {
			LOGGER.warn("cannot delete the test queues", e);
		}

		try {
			connection.close();
		}
		catch (Exception e) {
			LOGGER.warn("cannot close the test connection", e);
		}

		channel = null;
		connection = null;
	}

	/**
	 * Mirrors the topology declared by QueueManager for a scheduling: a main
	 * queue dead-lettering to the retry queue, and a retry queue
	 * dead-lettering to the error queue.
	 */
	private void declareQueues() throws IOException {
		channel.exchangeDeclare(DLX_EXCHANGE, BuiltinExchangeType.DIRECT, true);

		channel.queueDeclare(
			queueBind.getMainQueue(),
			true,
			false,
			false,
			Map.of(
				X_DEAD_LETTER_EXCHANGE, DLX_EXCHANGE,
				X_DEAD_LETTER_ROUTING_KEY, queueBind.getRetryKey()
			)
		);

		channel.queueBind(
			queueBind.getMainQueue(),
			AMQ_TOPIC_EXCHANGE,
			queueBind.getMainKey()
		);

		channel.queueDeclare(
			queueBind.getRetryQueue(),
			true,
			false,
			false,
			Map.of(
				X_DEAD_LETTER_EXCHANGE, DLX_EXCHANGE,
				X_DEAD_LETTER_ROUTING_KEY, queueBind.getErrorKey()
			)
		);

		channel.queueBind(
			queueBind.getRetryQueue(),
			DLX_EXCHANGE,
			queueBind.getRetryKey()
		);

		channel.queueDeclare(
			queueBind.getErrorQueue(),
			true,
			false,
			false,
			Map.of()
		);

		channel.queueBind(
			queueBind.getErrorQueue(),
			DLX_EXCHANGE,
			queueBind.getErrorKey()
		);
	}

	private void deleteQueues() throws IOException {
		channel.queueDelete(queueBind.getMainQueue());
		channel.queueDelete(queueBind.getRetryQueue());
		channel.queueDelete(queueBind.getErrorQueue());
	}

	private void observeRetryQueue() throws IOException {
		channel.basicConsume(
			queueBind.getRetryQueue(),
			false,
			(consumerTag, message) -> {
				deadLettered.add(message.getBody());
				channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
			},
			(consumerTag, sig) -> LOGGER.info("retry queue consumer closed")
		);
	}

	private void publishToMainQueue(byte[] body) throws IOException {
		channel.basicPublish(
			AMQ_TOPIC_EXCHANGE,
			queueBind.getMainKey(),
			MessageProperties.PERSISTENT_BASIC,
			body
		);
	}

	private void registerMainConsumer() throws IOException {
		channel.basicConsume(
			queueBind.getMainQueue(),
			false,
			new MainConsumer(
				channel, spawnContext(), queueBind, ingestionPayloadMapper)
		);
	}

	/**
	 * The consumers need an ActorContext, which only exists inside an actor:
	 * MessageGateway builds them with its own getContext(). A throwaway actor
	 * on the application ActorSystem gives the test the same thing. It stays
	 * alive for the whole test: the consumers keep using its context on every
	 * delivery.
	 */
	private ActorContext<Void> spawnContext() {
		var contextFuture = new CompletableFuture<ActorContext<Void>>();

		actorSystemProvider.getActorSystem().systemActorOf(
			Behaviors.<Void>setup(ctx -> {
				contextFuture.complete(ctx);

				return Behaviors.empty();
			}),
			"decode-failure-test-" + UUID.randomUUID(),
			Props.empty()
		);

		return contextFuture
			.orTimeout(10, TimeUnit.SECONDS)
			.join();
	}

}
