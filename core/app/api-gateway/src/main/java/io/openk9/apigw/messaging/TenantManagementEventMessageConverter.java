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
import com.fasterxml.jackson.databind.node.ObjectNode;
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
		this.mapper = mapper.copy()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public Message toMessage(Object object, MessageProperties messageProperties)
		throws MessageConversionException {

		try {
			String eventType = switch (object) {
				case TenantEvent.ApiKeyCreated e -> "ApiKeyCreated";
				case TenantEvent.ApiKeyRevoked e -> "ApiKeyRevoked";
				case TenantEvent.TenantCreated e -> "TenantCreated";
				case TenantEvent.TenantDeleted e -> "TenantDeleted";
				case TenantEvent.TenantUpdated e -> "TenantUpdated";
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

			// Pattern Event Upcaster: intercept and transform the JSON before deserialization
			JsonNode root = mapper.readTree(body);

			if ("TenantCreated".equals(eventType) || "TenantUpdated".equals(eventType)) {
				root = upcastTenantEvent(root);
			}
			else if ("ApiKeyCreated".equals(eventType)) {
				if (shouldIgnoreApiKeyCreated(root)) {
					log.warn("Ignoring legacy ApiKeyCreated event: {}", root);
					return null;
				}
			}

			Object event = switch (eventType) {
				case "ApiKeyCreated" -> mapper.treeToValue(root, TenantEvent.ApiKeyCreated.class);
				case "ApiKeyRevoked" -> mapper.treeToValue(root, TenantEvent.ApiKeyRevoked.class);
				case "TenantCreated" -> mapper.treeToValue(root, TenantEvent.TenantCreated.class);
				case "TenantDeleted" -> mapper.treeToValue(root, TenantEvent.TenantDeleted.class);
				case "TenantUpdated" -> mapper.treeToValue(root, TenantEvent.TenantUpdated.class);
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

	private JsonNode upcastTenantEvent(JsonNode root) {
		if (!(root instanceof ObjectNode objectNode)) {
			return root;
		}

		// Upcast removed SecurityConfiguration values
		if (objectNode.has("securityConfiguration")) {
			String secConfig = objectNode.get(
				"securityConfiguration").asText();
			if ("API_KEY_ONLY".equals(secConfig)) {
				objectNode.put("securityConfiguration", "LEGACY");
			}
		}

		// Handle routeAuthorizationMap adaptation
		if (objectNode.has("routeAuthorizationMap")) {
			JsonNode authMapNode = objectNode.get("routeAuthorizationMap");
			if (authMapNode instanceof ObjectNode authMap) {
				// Detect legacy by presence of DATASOURCE or missing modern keys
				if (authMap.has("DATASOURCE") || !authMap.has("ADMINISTRATION")) {
					JsonNode searchScheme = authMap.get("SEARCH");
					String searchSchemeStr = (searchScheme != null) ? searchScheme.asText() : "OAUTH2";

					authMap.put("ADMINISTRATION", "OAUTH2");
					authMap.put("PUBLIC", searchSchemeStr);
					authMap.put("INGESTION", searchSchemeStr);

					authMap.remove("DATASOURCE");
				}
			}
		}

		return objectNode;
	}

	private boolean shouldIgnoreApiKeyCreated(JsonNode root) {
		// Ignore if missing apiGroup or expirationDate
		return !root.has("apiGroup") || !root.has("expirationDate");
	}

}
