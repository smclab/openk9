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

package io.openk9.apigw.messaging;

import java.util.Map;

import io.openk9.event.tenant.TenantManagementEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitConfiguration {

	@Bean
	public Queue tenantEventQueue() {
		return QueueBuilder
			.durable(TenantManagementEvent.TOPIC)
			.stream()
			.build();
	}

	@Bean
	public MessageConverter tenantManagementEventMessageConverter(ObjectMapper objectMapper) {
		return new TenantManagementEventMessageConverter(objectMapper);
	}

	@Bean
	public SimpleRabbitListenerContainerFactory replayContainerFactory(
		ConnectionFactory connectionFactory,
		MessageConverter tenantManagementEventMessageConverter) {

		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(tenantManagementEventMessageConverter);
		factory.setContainerCustomizer(container ->
			container.setConsumerArguments(Map.of("x-stream-offset", "first"))
		);

		return factory;
	}
}