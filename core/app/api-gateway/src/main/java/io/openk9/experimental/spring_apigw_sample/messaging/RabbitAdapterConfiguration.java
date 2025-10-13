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

package io.openk9.experimental.spring_apigw_sample.messaging;

import io.openk9.experimental.spring_apigw_sample.event.TenantManagementEvent;
import io.openk9.experimental.spring_apigw_sample.event.TenantManagementEventConsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitAdapterConfiguration {

	private final TenantManagementEventConsumer consumer;

	@Bean
	public Queue tenantEventQueue() {
		return new Queue(TenantManagementEvent.TOPIC);
	}

	@Bean
	public MessageConverter messageConverter(ObjectMapper objectMapper) {
		return new TenantManagementEventMessageConverter(objectMapper);
	}

	@RabbitListener(queues = TenantManagementEvent.TOPIC)
	public void adapter(TenantManagementEvent payload) {
		if (log.isDebugEnabled()) {
			log.debug("Processing: {}", payload);
		}

		switch (payload) {
			case TenantManagementEvent.ApiKeyCreated event -> consumer.handleApiKeyCreatedEvent(event);
			case TenantManagementEvent.TenantCreated event -> consumer.handleTenantCreatedEvent(event);
			case TenantManagementEvent.TenantDeleted event -> consumer.handleTenantDeletedEvent(event);
			case TenantManagementEvent.TenantUpdated event -> consumer.handleTenantUpdatedEvent(event);
		}
	}
}