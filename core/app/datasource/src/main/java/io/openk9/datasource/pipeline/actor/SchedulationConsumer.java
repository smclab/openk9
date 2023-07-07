package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.typesafe.config.Config;
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.processor.payload.IngestionIndexWriterPayload;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.Duration;

public class SchedulationConsumer extends DefaultConsumer {
	private final QueueManager.QueueBind queueBind;
	private final ActorContext<?> context;
	private final Logger log;
	private final IngestionPayloadMapper payloadMapper;
	private final Duration timeout;

	public SchedulationConsumer(
		Channel channel,
		ActorContext<?> context,
		QueueManager.QueueBind queueBind,
		IngestionPayloadMapper payloadMapper) {

		super(channel);
		this.context = context;
		this.queueBind = queueBind;
		this.log = context.getLog();
		this.payloadMapper = payloadMapper;
		this.timeout = getTimeout(context);
	}

	@Override
	public void handleDelivery(
			String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
		throws IOException {

		log.info(
			"consuming deliveryTag {} on actor {}",
			envelope.getDeliveryTag(), context.getSelf());

		EntityRef<Schedulation.Command> entityRef =
			getSchedulation(queueBind.routingKey());

		IngestionIndexWriterPayload ingestionIndexWriterPayload =
			Json.decodeValue(
				Buffer.buffer(body),
				IngestionIndexWriterPayload.class
			);

		DataPayload payload = payloadMapper.map(ingestionIndexWriterPayload.getIngestionPayload());

		AskPattern.ask(
			entityRef,
			(ActorRef<Schedulation.Response> replyTo) ->
				new Schedulation.Ingest(Json.encodeToBuffer(payload).getBytes(), replyTo),
			timeout,
			context.getSystem().scheduler()
		).whenComplete((r, t) -> {
			try {
				if (t != null) {
					log.info("nack message with deliveryTag {} on actor {}",
						envelope.getDeliveryTag(), context.getSelf());
					getChannel().basicNack(envelope.getDeliveryTag(), false, false);
				} else if (r instanceof Schedulation.Failure) {
					log.info("nack message with deliveryTag {} on actor {}",
						envelope.getDeliveryTag(), context.getSelf());
					getChannel().basicNack(envelope.getDeliveryTag(), false, false);
				} else {
					log.info("ack message with deliveryTag {} on actor {}",
						envelope.getDeliveryTag(), context.getSelf());
					getChannel().basicAck(envelope.getDeliveryTag(), false);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}


	private EntityRef<Schedulation.Command> getSchedulation(String schedulationId) {

		ActorSystem<?> actorSystem = context.getSystem();

		ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);

		return clusterSharding.entityRefFor(Schedulation.ENTITY_TYPE_KEY, schedulationId);
	}

	private static Duration getTimeout(ActorContext<?> context) {
		Config config = context.getSystem().settings().config();

		String configPath = "io.openk9.schedulation.consumer.timeout";

		if (config.hasPathOrNull(configPath)) {
			if (config.getIsNull(configPath)) {
				return Duration.ofMinutes(10);
			} else {
				return config.getDuration(configPath);
			}
		} else {
			return Duration.ofMinutes(10);
		}

	}

}
