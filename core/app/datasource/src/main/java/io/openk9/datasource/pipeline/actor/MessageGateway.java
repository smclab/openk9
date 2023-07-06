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
import akka.actor.typed.javadsl.ReceiveBuilder;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;
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
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

public class MessageGateway extends AbstractBehavior<MessageGateway.Command> {

	public static final ServiceKey<Command> SERVICE_KEY =
		ServiceKey.create(Command.class, "message-gateway");

	public sealed interface Command {}
	public enum Start implements Command {INSTANCE}
	public record Register(String schedulationKey) implements Command {}
	public record Deregister(String schedulationKey) implements Command {}
	private record SpawnConsumer(QueueManager.QueueBind queueBind) implements Command, CborSerializable {}
	private record SchedulationResponseWrapper(Schedulation.Response response, long deliveryTag) implements Command {}
	private record QueueManagerResponseWrapper(QueueManager.Response response) implements Command {}
	private record Forward(QueueManager.QueueBind queueBind, long deliveryTag, byte[] body) implements Command {}
	private record ChannelInit(Channel channel) implements Command {}
	private record ReceptionistSubscribeWrapper(Receptionist.Listing listing) implements Command {}

	private final QueueConnectionProvider connectionProvider;
	private final Logger log;
	private final IngestionPayloadMapper ingestionPayloadMapper;
	private final Deque<Command> lag = new ArrayDeque<>();
	private final ActorRef<QueueManager.Response> queueManagerAdapter;

	private Channel channel;
	private Set<ActorRef<Command>> messageGateways;

	private ActorRef<QueueManager.Command> queueManager;

	public MessageGateway(
		ActorContext<Command> context, QueueConnectionProvider connectionProvider,
		IngestionPayloadMapper ingestionPayloadMapper) {
		super(context);
		this.connectionProvider = connectionProvider;
		this.log = context.getLog();
		this.ingestionPayloadMapper = ingestionPayloadMapper;

		context.getSelf().tell(Start.INSTANCE);

		context
			.getSystem()
			.receptionist()
            .tell(
				new ReceptionistMessages.Register<>(SERVICE_KEY, context.getSelf(), Option.empty()));

		ActorRef<Receptionist.Listing> receptionistAdapter = context.messageAdapter(Receptionist.Listing.class, ReceptionistSubscribeWrapper::new);

		context.getSystem().receptionist().tell(new ReceptionistMessages.Subscribe<>(SERVICE_KEY, receptionistAdapter));

		this.queueManagerAdapter =
			context.messageAdapter(QueueManager.Response.class, QueueManagerResponseWrapper::new);
	}

	public static Behavior<Command> create(
		QueueConnectionProvider rabbitMQClient,
		IngestionPayloadMapper ingestionPayloadMapper) {

		return Behaviors
            .<Command>supervise(
                Behaviors.setup(ctx -> new MessageGateway(ctx, rabbitMQClient, ingestionPayloadMapper))
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

	@Override
	public ReceiveBuilder<Command> newReceiveBuilder() {
		return super.newReceiveBuilder()
			.onMessage(ReceptionistSubscribeWrapper.class, this::onReceptionistSubscribe);

	}

	private Receive<Command> init() {
		return newReceiveBuilder()
			.onMessageEquals(Start.INSTANCE, this::onStart)
			.onAnyMessage(this::onStartup)
			.build();
	}

	private Receive<Command> ready() {
		return newReceiveBuilder()
			.onMessage(Register.class, this::onRegister)
			.onMessage(QueueManagerResponseWrapper.class, this::onQueueManagerResponse)
			.onMessage(SpawnConsumer.class, this::onSpawnConsumer)
			.onMessage(Forward.class, this::onForward)
			.onMessage(SchedulationResponseWrapper.class, this::onSchedulationResponse)
			.onMessage(Deregister.class, this::onDeregister)
			.onSignal(PreRestart.class, this::destroyChannel)
			.onSignal(PostStop.class, this::destroyChannel)
			.build();
	}

	private Behavior<Command> onSpawnConsumer(SpawnConsumer spawnConsumer) {
		QueueManager.QueueBind queueBind = spawnConsumer.queueBind;

		try {
			channel.basicConsume((queueBind).queue(), false, new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(
						String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body)
					throws IOException {

					log.info(
						"consuming deliveryTag {} on actor {}",
						envelope.getDeliveryTag(), getContext().getSelf());
					getContext().getSelf().tell(
						new Forward(queueBind, envelope.getDeliveryTag(), body));
				}
			});
		} catch (IOException e) {
			log.error("consumer cannot be registered", e);
			throw new RuntimeException(e);
		}
		return Behaviors.same();
	}

	private Behavior<Command> onReceptionistSubscribe(
		ReceptionistSubscribeWrapper receptionistSubscribeWrapper) {

		this.messageGateways = receptionistSubscribeWrapper
			.listing()
			.getServiceInstances(MessageGateway.SERVICE_KEY);

		return Behaviors.same();
	}

	private Behavior<Command> onDeregister(Deregister deregister) {
		queueManager.tell(new QueueManager.DestroyQueue(deregister.schedulationKey()));
		return Behaviors.same();
	}

	private Behavior<Command> onQueueManagerResponse(
		QueueManagerResponseWrapper queueManagerResponseWrapper) {

		QueueManager.Response response = queueManagerResponseWrapper.response();

		if (response instanceof QueueManager.QueueBind) {
			for (ActorRef<Command> messageGateway : messageGateways) {
				messageGateway.tell(new SpawnConsumer((QueueManager.QueueBind) response));
			}
		}

		return Behaviors.same();
	}

	private Behavior<Command> onStartup(Command command) {
		this.lag.add(command);
		this.log.info("there are {} commands waiting", lag.size());
		return Behaviors.same();
	}

	private Behavior<Command> onForward(Forward forward) {

		EntityRef<Schedulation.Command> entityRef =
			getSchedulation(forward.queueBind.routingKey());

		IngestionIndexWriterPayload ingestionIndexWriterPayload =
			Json.decodeValue(
				Buffer.buffer(forward.body),
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
							new SchedulationResponseWrapper(response, forward.deliveryTag)
				);

		entityRef.tell(new Schedulation.Ingest(payload, replyTo));

		return Behaviors.same();
	}

	private Behavior<Command> onSchedulationResponse(
		SchedulationResponseWrapper schedulationResponseWrapper) {
		long deliveryTag = schedulationResponseWrapper.deliveryTag;

		try {
			if (schedulationResponseWrapper.response == Schedulation.Success.INSTANCE) {
				log.info("ack message with deliveryTag {} on actor {}", deliveryTag, getContext().getSelf());
				channel.basicAck(deliveryTag, false);
			} else {
				log.info("nack message with deliveryTag {} on actor {}", deliveryTag, getContext().getSelf());
				channel.basicNack(deliveryTag, false, false);
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

		return newReceiveBuilder()
			.onMessage(ChannelInit.class, this::onChannelInit)
			.build();
	}

	private Behavior<Command> onChannelInit(ChannelInit ci) {

		this.channel = ci.channel;

		log.info("Rabbitmq channel created.. channel number: {}", channel.getChannelNumber());

		ClusterSingleton clusterSingleton = ClusterSingleton.get(getContext().getSystem());

		this.queueManager = clusterSingleton.init(
			SingletonActor.of(QueueManager.create(), QueueManager.INSTANCE_NAME));

		while (!lag.isEmpty()) {
			getContext().getSelf().tell(lag.pop());
		}

		return ready();

	}

	private Behavior<Command> onRegister(Register register) {

		queueManager.tell(new QueueManager.GetQueue(register.schedulationKey, queueManagerAdapter));

		return Behaviors.same();
	}

	private EntityRef<Schedulation.Command> getSchedulation(String schedulationId) {

		ActorSystem<?> actorSystem = getContext().getSystem();

		ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);

		return clusterSharding.entityRefFor(Schedulation.ENTITY_TYPE_KEY, schedulationId);
	}

}
