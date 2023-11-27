package io.openk9.datasource.pipeline.consumer;

import akka.actor.typed.javadsl.ActorContext;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import io.openk9.datasource.pipeline.actor.QueueManager;

import java.io.IOException;

public class ErrorConsumer extends BaseConsumer {

	public ErrorConsumer(
		Channel channel, ActorContext<?> context, QueueManager.QueueBind queueBind) {

		super(channel, context, queueBind);
	}

	@Override
	public void handleDelivery(
			String consumerTag,
			Envelope envelope,
			AMQP.BasicProperties properties,
			byte[] body)
		throws IOException {

		getChannel().basicPublish(
			QueueManager.AMQ_TOPIC_EXCHANGE,
			queueBind.getMainKey(),
			properties,
			body
		);
		getChannel().basicAck(envelope.getDeliveryTag(), false);
	}
}
