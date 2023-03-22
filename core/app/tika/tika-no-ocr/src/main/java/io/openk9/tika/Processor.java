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

package io.openk9.tika;


import io.openk9.tika.config.TikaConfiguration;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Objects;

@ApplicationScoped
public class Processor {

	public void process(JsonObject tikaPayload) {

		Tuple2<String, JsonObject> response =
			tikaProcessor.process(
				tikaPayload, true, tikaConfiguration.getCharacterLength(),
				tikaConfiguration.getOcrRoutingKey());

		if (Objects.equals(
			response.getItem1(),
			tikaConfiguration.getOcrRoutingKey())) {

			Message<JsonObject> message = Message.of(
				response.getItem2(),
				Metadata.of(
					OutgoingRabbitMQMetadata
						.builder()
						.withRoutingKey(response.getItem1())
						.withDeliveryMode(2)
						.build()
				)
			);

			emitter.send(message);
		}

	}

	@Channel("tika-sender")
	Emitter<JsonObject> emitter;

	@Inject
	TikaProcessor tikaProcessor;


	@Inject
	TikaConfiguration tikaConfiguration;

}
