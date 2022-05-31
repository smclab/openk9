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
import io.openk9.datasource.event.repo.EventRepository;
import io.openk9.datasource.event.sender.EventSender;
import io.smallrye.reactive.messaging.rabbitmq.IncomingRabbitMQMessage;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class EventProcessor {

	@Incoming("events")
	public CompletionStage<Void> process(Message<?> message) {

		Object obj = message.getPayload();

		JsonObject jsonObject =
			obj instanceof JsonObject
				? (JsonObject) obj
				: new JsonObject(new String((byte[]) obj));

		JsonObject ingestionPayload =
			jsonObject.getJsonObject("ingestionPayload");

		if (ingestionPayload != null) {
			ingestionPayload = jsonObject.getJsonObject("payload");
		}

		_logFirstLevel(ingestionPayload);

		if (ingestionPayload != null) {

			String ingestionId =
				ingestionPayload.getString("ingestionId");

			if (ingestionId != null){

				IncomingRabbitMQMessage incomingRabbitMQMessage =
					(IncomingRabbitMQMessage) message;

				Envelope envelope =
					incomingRabbitMQMessage.getRabbitMQMessage().envelope();

				/*
					TODO data is empty momentarily. find the previous event and use it to populate the data.
				 */

				eventSender.sendEventAsJson(
					"PIPELINE", ingestionId, envelope.getRoutingKey(), jsonObject);

			}

		}

		return message.ack();

	}

	/*
	 * this method log the first level of json object
	 */
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

		logger.info(stringBuilder.toString());

	}

	@Inject
	EventSender eventSender;

	@Inject
	EventRepository eventRepository;

	@Inject
	Logger logger;

}
