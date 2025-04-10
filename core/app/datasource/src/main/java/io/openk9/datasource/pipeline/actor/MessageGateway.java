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

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.actor.PekkoUtils;
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.pipeline.consumer.ErrorConsumer;
import io.openk9.datasource.pipeline.consumer.MainConsumer;
import io.openk9.datasource.pipeline.consumer.RetryConsumer;
import io.openk9.datasource.queue.QueueConnectionProvider;
import io.openk9.datasource.util.CborSerializable;

import com.rabbitmq.client.Channel;
import com.typesafe.config.Config;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.PostStop;
import org.apache.pekko.actor.typed.PreRestart;
import org.apache.pekko.actor.typed.Signal;
import org.apache.pekko.actor.typed.SupervisorStrategy;
import org.apache.pekko.actor.typed.internal.receptionist.ReceptionistMessages;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.AskPattern;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.apache.pekko.actor.typed.javadsl.ReceiveBuilder;
import org.apache.pekko.actor.typed.receptionist.Receptionist;
import org.apache.pekko.actor.typed.receptionist.ServiceKey;
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding;
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityRef;
import org.apache.pekko.cluster.typed.ClusterSingleton;
import org.apache.pekko.cluster.typed.SingletonActor;
import org.jboss.logging.Logger;
import scala.Option;

public class MessageGateway
	extends AbstractBehavior<MessageGateway.Command> {

	private static final Logger log = Logger.getLogger(MessageGateway.class);

	public static final ServiceKey<Command> SERVICE_KEY =
		ServiceKey.create(Command.class, "message-gateway");

	public static void askReroute(ActorSystem<?> actorSystem, ShardingKey shardingKey) {
		Receptionist receptionist = Receptionist.get(actorSystem);

		AskPattern.ask(
			receptionist.ref(),
			(ActorRef<Receptionist.Listing> replyTo) ->
				Receptionist.find(MessageGateway.SERVICE_KEY, replyTo),
			Duration.ofSeconds(10),
			actorSystem.scheduler()
		).whenComplete(
			(listing, throwable) -> {
				if (throwable == null) {
					listing
						.getServiceInstances(MessageGateway.SERVICE_KEY)
						.stream()
						.filter(ref -> ref.path().address().port().isEmpty())
						.forEach(ref -> ref.tell(
							new Reroute(new QueueManager.QueueBind(shardingKey.asString()))));
				}
				else {
					log.warnf("Cannot reroute scheduling %s", shardingKey);
				}
			}
		);
	}

	public static void askRegister(ActorSystem<?> actorSystem, ShardingKey shardingKey) {
		Receptionist receptionist = Receptionist.get(actorSystem);

		AskPattern.ask(
			receptionist.ref(),
			(ActorRef<Receptionist.Listing> replyTo) ->
				Receptionist.find(MessageGateway.SERVICE_KEY, replyTo),
			Duration.ofSeconds(10),
			actorSystem.scheduler()
		).whenComplete(
			(listing, throwable) -> {
				if (throwable == null) {
					listing
						.getServiceInstances(MessageGateway.SERVICE_KEY)
						.stream()
						.filter(ref -> ref.path().address().port().isEmpty())
						.forEach(ref -> ref.tell(new Register(shardingKey.asString())));
				}
				else {
					log.warnf("Cannot register scheduling %s", shardingKey);
				}
			}
		);
	}

	public sealed interface Command extends CborSerializable {}
	public enum Start implements Command {INSTANCE}

	private Behavior<Command> onReroute(Reroute reroute) {

		QueueManager.QueueBind queueBind = reroute.queueBind;
		var schedulingKey = queueBind.schedulingKey();

		ActorSystem<Void> system = getContext().getSystem();

		ClusterSharding clusterSharding = ClusterSharding.get(system);

		EntityRef<Scheduling.Command> entityRef = clusterSharding.entityRefFor(
			Scheduling.ENTITY_TYPE_KEY,
			schedulingKey
		);

		AskPattern.ask(
			entityRef,
			Scheduling.Restart::new,
			Duration.ofSeconds(10),
			system.scheduler()
		).whenComplete((r, t) -> {
			try {
				if (t != null || r instanceof Scheduling.Failure) {
					log.warnf(
						"error when restart scheduling %s",
						schedulingKey
					);
				}
				else {
					log.infof(
						"restart scheduling with key %s",
						schedulingKey
					);

					channel.basicQos(1);
					channel.basicConsume(
						queueBind.getErrorQueue(),
						false,
						new ErrorConsumer(channel, getContext(), queueBind)
					);
				}
			} catch (Exception e) {
				log.error(
					"cannot consume from errorQueue", e
				);
			}
		});

		return Behaviors.same();
	}

	private record SpawnConsumer(QueueManager.QueueBind queueBind) implements Command {}
	private record Reroute(QueueManager.QueueBind queueBind) implements Command {}
	private record QueueManagerResponseWrapper(QueueManager.Response response) implements Command {}
	private record ChannelInit(Channel channel) implements Command {}
	private record ReceptionistSubscribeWrapper(Receptionist.Listing listing) implements Command {}

	private final QueueConnectionProvider connectionProvider;
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
			.onMessage(Reroute.class, this::onReroute)
			.onSignal(PreRestart.class, this::destroyChannel)
			.onSignal(PostStop.class, this::destroyChannel)
			.build();
	}

	private Behavior<Command> onSpawnConsumer(SpawnConsumer spawnConsumer) {
		QueueManager.QueueBind queueBind = spawnConsumer.queueBind;

		try {
			channel.basicQos(getWorkersPerNode(getContext()));
			channel.basicConsume(
				queueBind.getMainQueue(),
				false,
				new MainConsumer(
					channel, getContext(), queueBind, ingestionPayloadMapper)
			);
			channel.basicQos(1);
			channel.basicConsume(
				queueBind.getRetryQueue(),
				false,
				new RetryConsumer(channel, getContext(), queueBind)
			);
		} catch (IOException e) {
			log.error("consumers cannot be registered", e);
			throw new RuntimeException(e);
		}
		return Behaviors.same();
	}

	private Behavior<Command> onRegister(Register register) {

		queueManager.tell(new QueueManager.GetQueue(register.schedulingKey, queueManagerAdapter));

		return Behaviors.same();
	}

	private Behavior<Command> onReceptionistSubscribe(
		ReceptionistSubscribeWrapper receptionistSubscribeWrapper) {

		this.messageGateways = receptionistSubscribeWrapper
			.listing()
			.getServiceInstances(MessageGateway.SERVICE_KEY);

		return Behaviors.same();
	}

	public record Register(String schedulingKey) implements Command {}

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
		log.infof("there are %s commands waiting", lag.size());
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

		log.infof("Rabbitmq channel created... channel number: %s", channel.getChannelNumber());

		ClusterSingleton clusterSingleton = ClusterSingleton.get(getContext().getSystem());

		this.queueManager = clusterSingleton.init(
			SingletonActor.of(QueueManager.create(), QueueManager.INSTANCE_NAME));

		while (!lag.isEmpty()) {
			getContext().getSelf().tell(lag.removeLast());
		}

		return ready();

	}

	private int getWorkersPerNode(ActorContext<Command> context) {
		Config config = context.getSystem().settings().config();

		return PekkoUtils.getInteger(
			config,
			Scheduling.WORKERS_PER_NODE,
			Scheduling.WORKERS_PER_NODE_DEFAULT
		);
	}
}
