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

package io.openk9.datasource.processor;


import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.pipeline.SchedulationKeyUtils;
import io.openk9.datasource.pipeline.actor.Schedulation;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.processor.payload.IngestionIndexWriterPayload;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.openk9.datasource.util.MessageUtil;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class DatasourceProcessor {

	@Incoming("ingestion")
	public Uni<Void> process(Message<IngestionIndexWriterPayload> message) {

		IngestionIndexWriterPayload payload = MessageUtil.toObj(
			message, IngestionIndexWriterPayload.class);

		IngestionPayload ingestionPayload = payload.getIngestionPayload();

		DataPayload dataPayload =
			ingestionPayloadMapper.map(ingestionPayload);

		ActorSystem<?> actorSystem = actorSystemProvider.getActorSystem();
		ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);

		String tenantId = dataPayload.getTenantId();
		String scheduleId = dataPayload.getScheduleId();

		EntityRef<Schedulation.Command> schedulationEntityRef = clusterSharding.entityRefFor(
			Schedulation.ENTITY_TYPE_KEY, SchedulationKeyUtils.getValue(tenantId, scheduleId));

		CompletionStage<Schedulation.Response> completionStage = schedulationEntityRef.ask(
				replyTo -> new Schedulation.Ingest(dataPayload, replyTo), Duration.ofMinutes(10));

		return Uni
			.createFrom()
			.completionStage(completionStage)
			.onItemOrFailure()
			.transform((response, throwable) -> {
				if (throwable != null) {
					return message.nack(throwable);
				}
				if (response instanceof Schedulation.Failure) {
					return message.nack(new RuntimeException(((Schedulation.Failure) response).error()));
				}
				else {
					return message.ack();
				}
			})
			.flatMap(Uni.createFrom()::completionStage);

	}

	@Inject
	ActorSystemProvider actorSystemProvider;

	@Inject
	IngestionPayloadMapper ingestionPayloadMapper;

}
