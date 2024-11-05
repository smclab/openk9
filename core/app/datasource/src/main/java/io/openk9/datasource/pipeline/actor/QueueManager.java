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

package io.openk9.datasource.pipeline.actor;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import io.openk9.datasource.queue.QueueConnectionProvider;
import io.openk9.datasource.util.CborSerializable;
import jakarta.enterprise.inject.spi.CDI;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.SupervisorStrategy;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.Map;

public class QueueManager extends AbstractBehavior<QueueManager.Command> {
	public static final String INSTANCE_NAME = "schedulingKey-manager";
	public static final String AMQ_TOPIC_EXCHANGE = "amq.topic";
	private static final String DLX_EXCHANGE = "dlx";
	private static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
	private static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
	private static final Logger log = Logger.getLogger(QueueManager.class);
	public sealed interface Command extends CborSerializable {}
	private enum Start implements Command {INSTANCE}
	private record ChannelInit(Channel c) implements Command {}

	private Behavior<Command> onDestroyQueue(DestroyQueue destroyQueue) {
		QueueBind queueBind = new QueueBind(destroyQueue.schedulingKey());

		try {
			channel.queueDelete(queueBind.getMainQueue());
			channel.queueDelete(queueBind.getRetryQueue());
			channel.queueDelete(queueBind.getErrorQueue());

			destroyQueue.replyTo().tell(ResponseStatus.SUCCESS);
		}
		catch (Exception e) {
			log.errorf("Failed to delete %s", queueBind);

			destroyQueue.replyTo().tell(ResponseStatus.ERROR);
		}

		return Behaviors.same();
	}

	private Behavior<Command> onGetQueue(GetQueue getQueue) {
		QueueBind queueBind = new QueueBind(getQueue.schedulingKey());

		try {
			log.infof("register: %s", queueBind);

			_declareDeadLetterExchange();

			_bindMainQueue(queueBind);

			_bindRetryQueue(queueBind);

			_bindErrorQueue(queueBind);

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		getQueue.replyTo.tell(queueBind);

		return Behaviors.same();
	}
	public sealed interface Response extends CborSerializable {}

	public record GetQueue(String schedulingKey, ActorRef<Response> replyTo) implements Command {}

	private Channel channel;
	private final QueueConnectionProvider connectionProvider;

	public QueueManager(ActorContext<Command> context) {
		super(context);
		this.connectionProvider = CDI.current().select(QueueConnectionProvider.class).get();
		getContext().getSelf().tell(Start.INSTANCE);
	}

	public static Behavior<Command> create() {
		return Behaviors
			.supervise(Behaviors.setup(QueueManager::new))
			.onFailure(SupervisorStrategy.resume());
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessageEquals(Start.INSTANCE, this::onStart)
			.onMessage(ChannelInit.class, this::onChannelInit)
			.onMessage(GetQueue.class, this::onGetQueue)
			.onMessage(DestroyQueue.class, this::onDestroyQueue)
			.build();
	}

	private Behavior<Command> onChannelInit(ChannelInit channelInit) {
		this.channel = channelInit.c;

		return Behaviors.same();
	}

	private Behavior<Command> onStart() {
		connectionProvider
			.getConnectFactory()
			.createChannel()
			.onItemOrFailure()
			.invoke((c, t) -> {

				if (t != null) {
					log.error(t.getMessage(), t);
				}
				else {
					getContext().getSelf().tell(new ChannelInit(c));
				}

			})
			.subscribe()
			.with((c) -> {});

		return Behaviors.same();
	}

	public enum ResponseStatus implements Response {
		SUCCESS,
		ERROR
	}

	public record DestroyQueue(String schedulingKey, ActorRef<Response> replyTo)
		implements Command {}

	public record QueueBind(String schedulingKey) implements Response {
		public String getMainQueue() {
			return schedulingKey;
		}

		public String getMainKey() {
			return getMainQueue();
		}

		public String getRetryQueue() {
			return schedulingKey + ".dlq.retry";
		}

		public String getRetryKey() {
			return getRetryQueue();
		}

		public String getErrorQueue() {
			return schedulingKey + ".dlq.error";
		}

		public String getErrorKey() {
			return getErrorQueue();
		}

	}


	private void _declareDeadLetterExchange() throws IOException {
		channel.exchangeDeclare(DLX_EXCHANGE, BuiltinExchangeType.DIRECT, true);
	}

	private void _bindMainQueue(QueueBind queueBind) throws IOException {
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
	}

	private void _bindRetryQueue(QueueBind queueBind) throws IOException {
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
	}

	private void _bindErrorQueue(QueueBind queueBind) throws IOException {
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

}
