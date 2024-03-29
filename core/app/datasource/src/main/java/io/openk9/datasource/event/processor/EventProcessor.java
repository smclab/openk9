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

package io.openk9.datasource.event.processor;

import com.rabbitmq.client.Envelope;
import io.openk9.datasource.event.sender.EventSender;
import io.openk9.datasource.event.util.EventType;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMessage;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

public class EventProcessor {

	public EventProcessor(EventSender eventSender, Logger logger) {
		this.eventSender = eventSender;
		this.logger = logger;
	}

	@Incoming("events")
	@Blocking
	public CompletionStage<Void> process(Message<?> message) {

		Object obj = message.getPayload();

		JsonObject jsonObject =
			obj instanceof JsonObject
				? (JsonObject) obj
				: new JsonObject(new String((byte[]) obj));

		if (logger.isDebugEnabled()) {
			_logFirstLevel(jsonObject);
		}

		JsonObject ingestionPayload =
			jsonObject.getJsonObject("ingestionPayload");

		if (ingestionPayload == null || ingestionPayload.isEmpty()) {
			ingestionPayload = jsonObject.getJsonObject("payload");
		}

		Long parsingDate = Objects.requireNonNullElse(ingestionPayload, jsonObject)
			.getLong("parsingDate", 0L);

		String contentId = Objects.requireNonNullElse(ingestionPayload, jsonObject)
			.getString("contentId", "");

		String ingestionId =
			Objects.requireNonNullElse(ingestionPayload, jsonObject)
				.getString("ingestionId");

		_sendEvent(
			(IncomingRabbitMQMessage)message, jsonObject, ingestionId,
			contentId, parsingDate);

		return message.ack();

	}

	private void _sendEvent(
		IncomingRabbitMQMessage message, JsonObject jsonObject,
		String ingestionId, String classPK, Long parsingDate) {

		if (ingestionId != null) {

			Envelope envelope =
				message.getRabbitMQMessage().envelope();

			eventSender.sendEventAsJson(
				EventType.PIPELINE, ingestionId, envelope.getRoutingKey(),
				classPK, LocalDateTime.ofInstant(
					Instant.ofEpochMilli(parsingDate),
					ZoneId.systemDefault()), jsonObject);

		}
		else {
			logger.warn(
				"No ingestionId found in message: " +
				jsonObject.getMap().keySet());
		}

	}

	private void _logFirstLevel(JsonObject jsonObject) {

		if (jsonObject == null) {
			logger.info("jsonObject is null");
			return;
		}

		StringBuilder stringBuilder = new StringBuilder();

		Iterator<Map.Entry<String, Object>> iterator = jsonObject.iterator();

		stringBuilder.append("{");

		while (iterator.hasNext()) {
			Map.Entry<String, Object> kv = iterator.next();

			String key = kv.getKey();

			stringBuilder
				.append('"')
				.append(key)
				.append("\":");

			Object value = kv.getValue();

			if (!(value instanceof JsonObject)) {
				stringBuilder
					.append('"')
					.append(value)
					.append('"');
			}

			if (iterator.hasNext()) {
				stringBuilder.append(", ");
			}

		}

		stringBuilder.append("}");

		logger.debug(stringBuilder.toString());

	}

	private final EventSender eventSender;

	private final Logger logger;

}
