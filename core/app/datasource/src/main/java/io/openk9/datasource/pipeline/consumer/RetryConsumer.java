package io.openk9.datasource.pipeline.consumer;

import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.typesafe.config.Config;
import io.openk9.datasource.pipeline.actor.QueueManager;
import io.openk9.datasource.pipeline.actor.Schedulation;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RetryConsumer extends BaseConsumer {

	private static final Logger log = Logger.getLogger(RetryConsumer.class);
	private static final String CONSUMER_MAX_RETRIES =
		"io.openk9.schedulation.consumer.max-retries";
	private final int maxRetries;

	public RetryConsumer(
		Channel channel, ActorContext<?> context, QueueManager.QueueBind queueBind) {

		super(channel, context, queueBind);
		this.maxRetries = getMaxRetries(config);
	}

	@Override
	public void handleDelivery(
			String consumerTag,
			Envelope envelope,
			AMQP.BasicProperties properties,
			byte[] body)
		throws IOException {

		Map<String, Object> headers = properties.getHeaders();

		Map<String, Object> xDeath = getXDeath(headers);

		long count = (long) xDeath.getOrDefault("count", 0L);

		if (count < maxRetries) {
			getChannel().basicPublish(
				QueueManager.AMQ_TOPIC_EXCHANGE,
				queueBind.getMainKey(),
				properties,
				body
			);
			getChannel().basicAck(envelope.getDeliveryTag(), false);
		}
		else {
			getChannel().basicNack(envelope.getDeliveryTag(), false, false);

			AskPattern.ask(
				getSchedulation(),
				Schedulation.TrackError::new,
				timeout,
				context.getSystem().scheduler()
			).whenComplete((r, t) -> {
				if (t != null) {
					log.warnf(
						t,
						"Error cannot be tracked for schedulation: {}",
						queueBind.schedulationKey()
					);
				}
				else if (r instanceof Schedulation.Failure) {
					log.warnf(
						"Error cannot be tracked for schedulation: {}, cause: {}",
						queueBind.schedulationKey(),
						((Schedulation.Failure) r).error()
					);
				}
				else {
					log.warnf(
						"Error tracked for schedulation: {}",
						queueBind.schedulationKey()
					);
				}
			});

		}

	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> getXDeath(Map<String, Object> headers) {
		List<Map<String, Object>> list = (List<Map<String, Object>>)
			headers.getOrDefault("x-death", List.of(Map.of()));
		return list.iterator().next();
	}

	private static int getMaxRetries(Config config) {
		return getProperty(config, CONSUMER_MAX_RETRIES, config::getInt, 3);
	}

}
