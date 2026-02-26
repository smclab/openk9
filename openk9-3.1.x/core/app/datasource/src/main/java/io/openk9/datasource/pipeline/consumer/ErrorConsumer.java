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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import io.openk9.datasource.pipeline.actor.QueueManager;
import org.apache.pekko.actor.typed.javadsl.ActorContext;

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
