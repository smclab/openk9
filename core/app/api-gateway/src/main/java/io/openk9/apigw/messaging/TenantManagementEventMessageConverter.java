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

import java.io.IOException;

import io.openk9.event.tenant.TenantManagementEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

@Slf4j
@RequiredArgsConstructor
public class TenantManagementEventMessageConverter implements MessageConverter {

	private static final String X_EVENT_TYPE = "x-event-type";
	private final ObjectMapper mapper;

	@Override
	public Message toMessage(Object object, MessageProperties messageProperties)
		throws MessageConversionException {

		try {
			String eventType = switch (object) {
				case TenantManagementEvent.ApiKeyCreated e -> "ApiKeyCreated";
				case TenantManagementEvent.TenantCreated e -> "TenantCreated";
				case TenantManagementEvent.TenantDeleted e -> "TenantDeleted";
				case TenantManagementEvent.TenantUpdated e -> "TenantUpdated";
				default -> throw new MessageConversionException("invalid type");
			};

			messageProperties.setHeader(X_EVENT_TYPE, eventType);

			byte[] body = mapper.writeValueAsBytes(object);
			Message message = new Message(body, messageProperties);

			if (log.isDebugEnabled()) {
				log.debug("Serialized message: {}", message);
			}

			return message;
		}
		catch (JsonProcessingException e) {
			throw new MessageConversionException("error during message creation", e);
		}

	}

	@Override
	public Object fromMessage(Message message)
		throws MessageConversionException {

		try {
			MessageProperties messageProperties = message.getMessageProperties();
			byte[] body = message.getBody();

			String eventType = messageProperties.getHeader(X_EVENT_TYPE);
			Object event = switch (eventType) {
				case "ApiKeyCreated" -> mapper.readValue(body, TenantManagementEvent.ApiKeyCreated.class);
				case "TenantCreated" -> mapper.readValue(body, TenantManagementEvent.TenantCreated.class);
				case "TenantDeleted" -> mapper.readValue(body, TenantManagementEvent.TenantDeleted.class);
				case "TenantUpdated" -> mapper.readValue(body, TenantManagementEvent.TenantUpdated.class);
				default -> throw new MessageConversionException("unknown x-event-type value");
			};

			if (log.isDebugEnabled()) {
				log.debug("deserialized props: {} event: {}", messageProperties, event);
			}

			return event;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
