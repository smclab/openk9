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

import java.time.Duration;

import io.openk9.datasource.actor.PekkoUtils;
import io.openk9.datasource.pipeline.actor.QueueManager;
import io.openk9.datasource.pipeline.actor.Scheduling;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.typesafe.config.Config;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding;
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityRef;

public abstract class BaseConsumer extends DefaultConsumer {

	private static final String CONSUMER_TIMEOUT = "io.openk9.scheduling.consumer.timeout";

	protected final ActorContext<?> context;
	protected final Config config;
	protected final QueueManager.QueueBind queueBind;
	protected final Duration timeout;

	public BaseConsumer(Channel channel, ActorContext<?> context, QueueManager.QueueBind queueBind) {
		super(channel);
		this.context = context;
		this.config = context.getSystem().settings().config();
		this.queueBind = queueBind;
		this.timeout = getTimeout(config);
	}

	protected EntityRef<Scheduling.Command> getScheduling() {

		ActorSystem<?> actorSystem = context.getSystem();

		ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);

		var schedulingKey = queueBind.schedulingKey();

		return clusterSharding.entityRefFor(
			Scheduling.ENTITY_TYPE_KEY,
			schedulingKey
		);

	}

	private static Duration getTimeout(Config config) {
		return PekkoUtils.getProperty(
			config, CONSUMER_TIMEOUT, config::getDuration,
			Duration.ofMinutes(10)
		);
	}
}
