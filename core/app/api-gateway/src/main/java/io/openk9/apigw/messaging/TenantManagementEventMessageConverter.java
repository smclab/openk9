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

import io.openk9.event.tenant.TenantEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

@Slf4j
public class TenantManagementEventMessageConverter implements MessageConverter {

	private static final String X_EVENT_TYPE = "x-event-type";
	private final ObjectMapper mapper;

	public TenantManagementEventMessageConverter(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Message toMessage(Object object, MessageProperties messageProperties)
		throws MessageConversionException {

		try {
			String eventType = switch (object) {
				case TenantEvent.ApiKeyCreated e -> TenantEvent.API_KEY_CREATED;
				case TenantEvent.ApiKeyRevoked e -> TenantEvent.API_KEY_REVOKED;
				case TenantEvent.TenantCreated e -> TenantEvent.TENANT_CREATED;
				case TenantEvent.TenantDeleted e -> TenantEvent.TENANT_DELETED;
				case TenantEvent.TenantUpdated e -> TenantEvent.TENANT_UPDATED;
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

			if (eventType == null) {
				log.warn(
					"Message missing x-event-type header, skipping: {}",
					messageProperties.getMessageId());
				return null;
			}

			JsonNode root = TenantEventUpcaster.upcast(
				eventType, mapper.readTree(body));

			if (root == null) {
				return null;
			}

			Object event = switch (eventType) {
				case TenantEvent.API_KEY_CREATED -> mapper.treeToValue(root, TenantEvent.ApiKeyCreated.class);
				case TenantEvent.API_KEY_REVOKED -> mapper.treeToValue(root, TenantEvent.ApiKeyRevoked.class);
				case TenantEvent.TENANT_CREATED -> mapper.treeToValue(root, TenantEvent.TenantCreated.class);
				case TenantEvent.TENANT_DELETED -> mapper.treeToValue(root, TenantEvent.TenantDeleted.class);
				case TenantEvent.TENANT_UPDATED -> mapper.treeToValue(root, TenantEvent.TenantUpdated.class);
				default -> throw new MessageConversionException("unknown x-event-type value");
			};

			if (log.isDebugEnabled()) {
				log.debug("deserialized props: {} event: {}", messageProperties, event);
			}

			return event;
		}
		catch (IOException e) {
			throw new MessageConversionException("Error during message deserialization", e);
		}
	}

}
