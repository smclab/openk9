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

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.pipeline.actor.QueueManager;
import io.openk9.datasource.pipeline.actor.Schedulation;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.processor.payload.IngestionIndexWriterPayload;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.jboss.logging.Logger;

import java.io.IOException;

public class MainConsumer extends BaseConsumer {
	private static final Logger log = Logger.getLogger(MainConsumer.class);
	private final IngestionPayloadMapper payloadMapper;

	public MainConsumer(
		Channel channel,
		ActorContext<?> context,
		QueueManager.QueueBind queueBind,
		IngestionPayloadMapper payloadMapper) {

		super(channel, context, queueBind);
		this.payloadMapper = payloadMapper;
	}

	@Override
	public void handleDelivery(
			String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
		throws IOException {

		log.infof(
			"consuming deliveryTag %s on actor %s",
			envelope.getDeliveryTag(),
			context.getSelf()
		);

		IngestionIndexWriterPayload ingestionIndexWriterPayload =
			Json.decodeValue(
				Buffer.buffer(body),
				IngestionIndexWriterPayload.class
			);

		DataPayload payload = payloadMapper.map(ingestionIndexWriterPayload.getIngestionPayload());

		AskPattern.ask(
			getSchedulation(),
			(ActorRef<Schedulation.Response> replyTo) ->
				new Schedulation.Ingest(
					Json.encodeToBuffer(payload).getBytes(),
					replyTo,
					String.format("%s-%d", getConsumerTag(), envelope.getDeliveryTag())
				),
			timeout,
			context.getSystem().scheduler()
		).whenComplete((r, t) -> {
			try {
				if (t != null) {
					log.infof(
						"nack message with deliveryTag %s on actor %s",
						envelope.getDeliveryTag(),
						context.getSelf()
					);
					getChannel().basicNack(envelope.getDeliveryTag(), false, false);
				} else if (r instanceof Schedulation.Failure) {
					log.infof(
						"nack message with deliveryTag %s on actor %s",
						envelope.getDeliveryTag(),
						context.getSelf()
					);
					getChannel().basicNack(envelope.getDeliveryTag(), false, false);
				} else {
					log.infof(
						"ack message with deliveryTag %s on actor %s",
						envelope.getDeliveryTag(),
						context.getSelf()
					);
					getChannel().basicAck(envelope.getDeliveryTag(), false);
				}
			} catch (Exception e) {
				log.errorf(
					e,
					"Error on message acknowledgement for deliveryTag %s",
					envelope.getDeliveryTag()
				);
			}
		});
	}

}
