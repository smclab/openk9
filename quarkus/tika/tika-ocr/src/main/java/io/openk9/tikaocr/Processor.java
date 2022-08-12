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

package io.openk9.tikaocr;

import io.openk9.tika.TikaProcessor;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class Processor {

	@Incoming("tika-ocr")
	public Uni<Void> process(Message<?> message) {

		return Uni.createFrom().deferred(() -> {

			JsonObject json = _messagePayloadToJson(message);

			Tuple2<String, JsonObject> response =
				tikaProcessor.process(
					json, false, 0, null);

			emitter.send(
				Message.of(
					response.getItem2(),
					Metadata.of(
						OutgoingRabbitMQMetadata
							.builder()
							.withRoutingKey(response.getItem1())
							.withDeliveryMode(2)
							.build()
					)
			));

			return Uni.createFrom().completionStage(message.ack());

		});


	}

	private JsonObject _messagePayloadToJson(Message<?> message) {
		Object obj = message.getPayload();

		return obj instanceof JsonObject
			? (JsonObject) obj
			: new JsonObject(new String((byte[]) obj));

	}

	@Channel("tika-sender")
	Emitter<JsonObject> emitter;

	@Inject
	TikaProcessor tikaProcessor;

}
