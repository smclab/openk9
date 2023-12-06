package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import io.openk9.datasource.queue.QueueConnectionProvider;
import io.openk9.datasource.util.CborSerializable;
import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import java.io.IOException;
import java.util.Map;

public class QueueManager extends AbstractBehavior<QueueManager.Command> {
	public static final String INSTANCE_NAME = "schedulationKey-manager";
	public static final String AMQ_TOPIC_EXCHANGE = "amq.topic";
	public static final String DLX_EXCHANGE = "dlx";
	public static final String PARKING_QUEUE = "parking_queue";
	private static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
	private static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
	private static final Logger log = Logger.getLogger(QueueManager.class);
	public sealed interface Command extends CborSerializable {}
	private enum Start implements Command {INSTANCE}
	private record ChannelInit(Channel c) implements Command {}

	public record GetQueue(String schedulationKey, ActorRef<Response> replyTo) implements Command {}
	public record DestroyQueue(String schedulationKey) implements Command {}
	public sealed interface Response extends CborSerializable {}
	public record QueueBind(String schedulationKey) implements Response {
		public String getMainQueue() {
			return schedulationKey;
		}

		public String getMainKey() {
			return getMainQueue();
		}

		public String getRetryQueue() {
			return schedulationKey + ".dlq.retry";
		}

		public String getRetryKey() {
			return getRetryQueue();
		}

		public String getErrorQueue() {
			return schedulationKey + ".dlq.error";
		}

		public String getErrorKey() {
			return getErrorQueue();
		}

	}

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

	private Behavior<Command> onDestroyQueue(DestroyQueue destroyQueue) {
		try {
			QueueBind queueBind = new QueueBind(destroyQueue.schedulationKey());

			channel.queueDelete(queueBind.getMainQueue());
			channel.queueDelete(queueBind.getRetryQueue());
			channel.queueDelete(queueBind.getErrorQueue());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return Behaviors.same();
	}

	private Behavior<Command> onGetQueue(GetQueue getQueue) {
		QueueBind queueBind = new QueueBind(getQueue.schedulationKey());

		try {
			log.infof("register: %s", queueBind);

			_declareDeadLetterExchange();

			_bindMainQueue(queueBind);

			_bindRetryQueue(queueBind);

			_bindErrorQueue(queueBind);

			_bindParkingQueue();

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		getQueue.replyTo.tell(queueBind);

		return Behaviors.same();
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

	private void _bindParkingQueue() throws IOException {
		channel.queueDeclare(
			PARKING_QUEUE,
			true,
			false,
			false,
			Map.of()
		);

		channel.queueBind(PARKING_QUEUE, DLX_EXCHANGE, PARKING_QUEUE);
	}
}
