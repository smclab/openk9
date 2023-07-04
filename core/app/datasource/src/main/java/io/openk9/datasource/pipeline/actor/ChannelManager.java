package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.Signal;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.internal.receptionist.ReceptionistMessages;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.ServiceKey;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.processor.payload.IngestionIndexWriterPayload;
import io.openk9.datasource.queue.QueueConnectionProvider;
import io.openk9.datasource.util.CborSerializable;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import scala.Option;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ChannelManager extends AbstractBehavior<ChannelManager.Command> {

	public static final ServiceKey<Command> SERVICE_KEY = ServiceKey.create(Command.class, "channel-manager");

	private static final String EXCHANGE = "amq.topic";
	private final QueueConnectionProvider rabbitMQClient;
	private final Logger log;
	private final Map<QueueBind, List<String>> queues = new HashMap<>();
	private Channel channel;
	private final IngestionPayloadMapper ingestionPayloadMapper;
	private final Deque<Command> lag = new ArrayDeque<>();

	public ChannelManager(
		ActorContext<Command> context, QueueConnectionProvider rabbitMQClient,
		IngestionPayloadMapper ingestionPayloadMapper) {
		super(context);
		this.rabbitMQClient = rabbitMQClient;
		this.log = context.getLog();
		this.ingestionPayloadMapper = ingestionPayloadMapper;

		context.getSelf().tell(Start.INSTANCE);

		context
			.getSystem()
			.receptionist()
            .tell(
				new ReceptionistMessages.Register<>(SERVICE_KEY, context.getSelf(), Option.empty()));

	}

	public static Behavior<Command> create(
		QueueConnectionProvider rabbitMQClient,
		IngestionPayloadMapper ingestionPayloadMapper) {

		return Behaviors
            .<Command>supervise(
                Behaviors.setup(ctx -> new ChannelManager(ctx, rabbitMQClient, ingestionPayloadMapper))
            )
            .onFailure(
                SupervisorStrategy.restartWithBackoff(
                    Duration.ofMillis(200), Duration.ofMinutes(2), 0.1)
            );
	}

	@Override
	public Receive<Command> createReceive() {
		return init();
	}

	private Receive<Command> init() {
		return newReceiveBuilder()
			.onMessageEquals(Start.INSTANCE, this::onStart)
			.onAnyMessage(this::onStartup)
			.build();
	}

	private Receive<Command> ready() {
		return newReceiveBuilder()
			.onMessage(QueueSpawn.class, this::onQueueSpawn)
			.onMessage(QueueDestroy.class, this::onQueueDestroy)
			.onMessage(QueueMessage.class, this::onQueueMessage)
			.onMessage(SchedulationResponseWrapper.class, this::onSchedulationResponse)
			.onSignal(PreRestart.class, this::destroyChannel)
			.onSignal(PostStop.class, this::destroyChannel)
			.build();
	}

	private Behavior<Command> onStartup(Command command) {
		this.lag.add(command);
		this.log.info("there are {} commands waiting", lag.size());
		return Behaviors.same();
	}

	private Behavior<Command> onQueueMessage(QueueMessage queueMessage) {

		EntityRef<Schedulation.Command> entityRef =
			getSchedulation(queueMessage.queueBind.routingKey);

		IngestionIndexWriterPayload ingestionIndexWriterPayload =
			Json.decodeValue(
				Buffer.buffer(queueMessage.body),
				IngestionIndexWriterPayload.class
			);

		DataPayload payload =
			ingestionPayloadMapper.map(
				ingestionIndexWriterPayload.getIngestionPayload());

		ActorRef<Schedulation.Response> replyTo =
			getContext()
				.messageAdapter(
						Schedulation.Response.class,
						response ->
							new SchedulationResponseWrapper(response, queueMessage.deliveryTag)
				);

		entityRef.tell(new Schedulation.Ingest(payload, replyTo));

		return Behaviors.same();
	}

	private Behavior<Command> onSchedulationResponse(SchedulationResponseWrapper cartResponse) {
		try {
			if (cartResponse.response == Schedulation.Success.INSTANCE) {
				channel.basicAck(cartResponse.deliveryTag, false);
			} else {
				channel.basicNack(cartResponse.deliveryTag, false, false);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return Behaviors.same();
	}

	private Behavior<Command> destroyChannel(Signal ignore) {
		if (channel != null) {
			log.info("Destroying RabbitMQ channel");
			try {
				this.channel.close();
				this.channel = null;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return Behaviors.same();
	}

	private Behavior<Command> onStart() {

		log.info("Connect to RabbitMQ");

		rabbitMQClient
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

		return Behaviors
			.receive(Command.class)
			.onMessage(ChannelInit.class, this::onChannelInit)
			.build();
	}


	private Behavior<Command> onChannelInit(ChannelInit ci) {

		this.channel = ci.channel;

		log.info("Rabbitmq channel created.. channel number: {}", channel.getChannelNumber());

		while (!lag.isEmpty()) {
			getContext().getSelf().tell(lag.pop());
		}

		return ready();

	}

	private Behavior<Command> onQueueSpawn(QueueSpawn queueSpawn) {
		registerQueue(queueSpawn.entityId, queueSpawn.entityId, (queueBind) -> {
			try {
				// register a consumer for messages
				return channel.basicConsume(queueBind.queue, false, new DefaultConsumer(channel) {
					@Override
					public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
						log.info("consuming {}", Buffer.buffer(body));
						getContext().getSelf().tell(new QueueMessage(queueBind, envelope.getDeliveryTag(), body));
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});

		return Behaviors.same();
	}

	private Behavior<Command> onQueueDestroy(QueueDestroy queueDestroy) {
		unregisterQueue(new QueueBind(queueDestroy.entityId, EXCHANGE, queueDestroy.entityId));
		return Behaviors.same();
	}

	private void unregisterQueue(QueueBind queueBind) {
		try {
			List<String> consumerTags = queues.get(queueBind);
			if (consumerTags != null) {
				log.info("unregister: {}", queueBind);
				channel.queueDelete(queueBind.queue);
				queues.remove(queueBind);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void registerQueue(String queue, String routingKey, Function<QueueBind, String> createConsumer) {
		QueueBind queueBind = new QueueBind(queue, EXCHANGE, routingKey);
		if (!queues.containsKey(queueBind)) {
			try {
				log.info("register: {}", queueBind);
				channel.queueDeclare(queueBind.queue, true, false, true, null);
				channel.queueBind(queueBind.queue, queueBind.exchange, queueBind.routingKey);
				ArrayList<String> consumerTags = new ArrayList<>();
				consumerTags.add(createConsumer.apply(queueBind));
				queues.put(queueBind, consumerTags);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private EntityRef<Schedulation.Command> getSchedulation(String schedulationId) {
		ActorSystem<?> actorSystem = getContext().getSystem();
		ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);
		return clusterSharding.entityRefFor(Schedulation.ENTITY_TYPE_KEY, schedulationId);
	}


	public sealed interface Command extends CborSerializable {}
	public enum Start implements Command {INSTANCE}
	public record QueueSpawn(String entityId) implements Command {}
	public record QueueDestroy(String entityId) implements Command {}

	private record QueueBind(String queue, String exchange, String routingKey) {}

	private record SchedulationResponseWrapper(Schedulation.Response response, long deliveryTag) implements Command { }

	private record QueueMessage(QueueBind queueBind, long deliveryTag, byte[] body) implements Command { }
	private record ChannelInit(Channel channel) implements Command {}
}
