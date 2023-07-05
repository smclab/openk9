package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.rabbitmq.client.Channel;
import io.openk9.datasource.queue.QueueConnectionProvider;
import io.openk9.datasource.util.CborSerializable;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.CDI;
import java.util.HashSet;
import java.util.Set;

public class QueueManager extends AbstractBehavior<QueueManager.Command> {
	private static final String EXCHANGE = "amq.topic";

	public sealed interface Command extends CborSerializable {}
	private enum Start implements Command {INSTANCE}
	private record ChannelInit(Channel c) implements Command {}

	public record GetQueue(String schedulationKey, ActorRef<Response> replyTo) implements Command {}
	public record DestroyQueue(String schedulationKey) implements Command {}
	public sealed interface Response extends CborSerializable {}
	public record QueueBind(String queue, String exchange, String routingKey) implements Response {}

	private Channel channel;
	private final QueueConnectionProvider connectionProvider;

	private final Logger log;
	private final Set<QueueBind> queueBinds = new HashSet<>();

	public QueueManager(ActorContext<Command> context) {
		super(context);
		this.connectionProvider = CDI.current().select(QueueConnectionProvider.class).get();
		this.log = context.getLog();
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
			QueueBind queueBind =
				new QueueBind(
					destroyQueue.schedulationKey(), EXCHANGE, destroyQueue.schedulationKey());

			if (queueBinds.contains(queueBind)) {
				channel.queueDelete(queueBind.queue);
				queueBinds.remove(queueBind);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return Behaviors.same();
	}

	private Behavior<Command> onGetQueue(GetQueue getQueue) {
		QueueBind queueBind = new QueueBind(getQueue.schedulationKey(), EXCHANGE, getQueue.schedulationKey());

		if (!queueBinds.contains(queueBind)) {
			try {
				log.info("register: {}", queueBind);
				channel.queueDeclare(
					queueBind.queue, true, false, true, null);
				channel.queueBind(queueBind.queue, queueBind.exchange, queueBind.routingKey);
				queueBinds.add(queueBind);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		getQueue.replyTo.tell(queueBind);

		return Behaviors.same();
	}
}
