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

package io.openk9.datasource.pipeline.service;

import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.actor.EventBusInstanceHolder;
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.VectorIndex;
import io.openk9.datasource.pipeline.vector.VectorPipeline;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.processor.payload.IngestionIndexWriterPayload;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class InternalVectorPipelineIngestionService {

	private static final String SEND_REQUEST = "InternalVectorPipelineIngestionService#send";
	private static final Logger log =
		Logger.getLogger(InternalVectorPipelineIngestionService.class);

	@Inject
	@Channel("internal-ingest")
	Emitter<IngestionIndexWriterPayload> emitter;
	@Inject
	Mutiny.SessionFactory sessionFactory;
	@Inject
	IngestionPayloadMapper ingestionPayloadMapper;

	public static CompletionStage<Void> send(ShardingKey shardingKey, byte[] payload) {

		return EventBusInstanceHolder.getEventBus()
			.request(SEND_REQUEST, new SendRequest(shardingKey, payload))
			.replaceWithVoid()
			.subscribeAsCompletionStage();

	}

	@ConsumeEvent(SEND_REQUEST)
	Uni<Void> send(SendRequest sendRequest) {

		var shardingKey = sendRequest.shardingKey();

		var tenantId = shardingKey.tenantId();
		var scheduleId = shardingKey.scheduleId();
		var vScheduleId = scheduleId + VectorPipeline.VECTOR_PIPELINE_SUFFIX;

		return sessionFactory.withStatelessSession(tenantId, (s -> s
			.createNamedQuery(VectorIndex.FETCH_BY_SCHEDULE_ID, VectorIndex.class)
			.setParameter("scheduleId", scheduleId)
			.getSingleResult()
			.flatMap(vectorIndex -> s.createNamedQuery(
						EmbeddingModel.FETCH_CURRENT,
						EmbeddingModel.class
					)
					.getSingleResult()
			)
		)).onItem().invoke(() -> {

			log.infof("VectorIndex is active for scheduleId %s", scheduleId);

			var payload = sendRequest.payload();

			var dataPayload = Json.decodeValue(Buffer.buffer(payload), DataPayload.class);
			var ingestionPayload = ingestionPayloadMapper.map(dataPayload);

			var metadata = Metadata.of(
				OutgoingRabbitMQMetadata
					.builder()
					.withRoutingKey(ShardingKey.asString(tenantId, vScheduleId))
					.withDeliveryMode(2)
					.build()
			);

			var ingestionIndexWriterPayload = IngestionIndexWriterPayload.builder()
				.ingestionPayload(ingestionPayload)
				.build();

			emitter.send(Message.of(ingestionIndexWriterPayload, metadata));

		}).onFailure().invoke((throwable) ->
			log.error("No vector index for scheduleId %s", scheduleId, throwable)
		).replaceWithVoid();

	}

	private record SendRequest(ShardingKey shardingKey, byte[] payload) {}

}
