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

import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.typesafe.config.Config;
import io.openk9.datasource.actor.AkkaUtils;
import io.openk9.datasource.pipeline.actor.QueueManager;
import io.openk9.datasource.pipeline.actor.Scheduling;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RetryConsumer extends BaseConsumer {

	private static final Logger log = Logger.getLogger(RetryConsumer.class);
	private static final String CONSUMER_MAX_RETRIES =
		"io.openk9.scheduling.consumer.max-retries";
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
			if (log.isTraceEnabled()) {
				log.tracef(
					"(count < maxRetries): %s < %s for payload with hashCode %s",
					count,
					maxRetries,
					Arrays.hashCode(body)
				);
			}
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

	@SuppressWarnings("unchecked")
	private static Map<String, Object> getXDeath(Map<String, Object> headers) {
		List<Map<String, Object>> list = (List<Map<String, Object>>)
			headers.getOrDefault("x-death", List.of(Map.of()));
		return list.iterator().next();
	}

	private static int getMaxRetries(Config config) {
		return AkkaUtils.getProperty(config, CONSUMER_MAX_RETRIES, config::getInt, 3);
	}

}
