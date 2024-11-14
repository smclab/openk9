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

package io.openk9.datasource.events;

import io.openk9.datasource.actor.EventBusInstanceHolder;
import io.quarkus.runtime.Startup;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

@ApplicationScoped
@Startup
public class DatasourceEventBus {

	private static final String SEND_EVENT = "DatasourceEventBus#sendEvent";

	public static void sendDeleteEvent(
		String tenantId, long datasourceId, String dataIndexName, String deletedContentId) {

		DatasourceMessage.Delete deleteEvent = DatasourceMessage
			.Delete
			.builder()
			.indexName(dataIndexName)
			.datasourceId(datasourceId)
			.tenantId(tenantId)
			.contentId(deletedContentId)
			.build();

		EventBusInstanceHolder.getEventBus().send(SEND_EVENT, deleteEvent);
	}

	@Inject
	@Channel("datasource-events-requests")
	Emitter<DatasourceMessage> emitter;

	@ConsumeEvent(SEND_EVENT)
	public void sendEvent(DatasourceMessage datasourceMessage) {
		emitter.send(
			Message.of(
				datasourceMessage,
				Metadata.of(OutgoingRabbitMQMetadata
					.builder()
					.withDeliveryMode(2)
					.build()
				)
			)
		);
	}

}
