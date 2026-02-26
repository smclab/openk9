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

import java.io.IOException;

import io.openk9.datasource.actor.PekkoUtils;
import io.openk9.datasource.pipeline.actor.QueueManager;
import io.openk9.datasource.pipeline.actor.Scheduling;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.typesafe.config.Config;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.AskPattern;
import org.jboss.logging.Logger;

public class RetryConsumer extends BaseConsumer {

	private static final Logger log = Logger.getLogger(RetryConsumer.class);
	private static final String CONSUMER_MAX_RETRIES =
		"io.openk9.scheduling.consumer.max-retries";
	private static final String MESSAGE_RETRY_HEADER = "openk9-message-retry";
	private final int maxRetries;

	public RetryConsumer(
		Channel channel, ActorContext<?> context, QueueManager.QueueBind queueBind) {

		super(channel, context, queueBind);
		this.maxRetries = getMaxRetries(config);
	}

	public static Integer incrementDeathCount(AMQP.BasicProperties properties) {
		var headers = properties.getHeaders();
		return (int) headers.compute(
			MESSAGE_RETRY_HEADER,
			(String k, Object v) -> {
				if (v instanceof Integer) {
					int count = (int) v;
					return ++count;
				}
				else {
					return 1;
				}
			}
		);
	}

	private static int getMaxRetries(Config config) {
		return PekkoUtils.getProperty(config, CONSUMER_MAX_RETRIES, config::getInt, 3);
	}

	@Override
	public void handleDelivery(
			String consumerTag,
			Envelope envelope,
			AMQP.BasicProperties properties,
			byte[] body)
		throws IOException {

		int count = incrementDeathCount(properties);

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
				getScheduling(),
				Scheduling.TrackError::new,
				timeout,
				context.getSystem().scheduler()
			).whenComplete((r, t) -> {
				if (t != null) {
					log.warnf(
						t,
						"Error cannot be tracked for scheduling: %s",
						queueBind.schedulingKey()
					);
				}
				else if (r instanceof Scheduling.Failure) {
					log.warnf(
						"Error cannot be tracked for scheduling: %s, cause: %s",
						queueBind.schedulingKey(),
						((Scheduling.Failure) r).error()
					);
				}
				else {
					log.infof(
						"Error tracked for scheduling: %s",
						queueBind.schedulingKey()
					);
				}
			});

		}

	}
}
