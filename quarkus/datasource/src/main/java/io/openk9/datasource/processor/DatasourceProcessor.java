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


import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.processor.payload.IngestionDatasourcePayload;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.openk9.datasource.service.DatasourceService;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@ApplicationScoped
public class DatasourceProcessor {

	/*@Incoming("ingestion")
	public Uni<Void> process2(Message<IngestionPayload> message) {

		IngestionPayload ingestionPayload = message.getPayload();

		long datasourceId = ingestionPayload.getDatasourceId();

		datasourceService
			.findById(datasourceId)
			.onItem()
			.transformToUni(datasource -> {

				if (datasource == null) {
					throw new RuntimeException(
						"Datasource not found: " + datasourceId);
				}

				return datasourceService
					.getEnrichPipeline(datasource)
					.flatMap(ep -> {

						if (ep == null) {
							return List.of();
						}

						Set<EnrichPipelineItem> enrichPipelineItems =
							ep.getEnrichPipelineItems();

						if (enrichPipelineItems == null || enrichPipelineItems.isEmpty()) {
							return List.of();
						}




					});

			});

	}*/


	@Incoming("ingestion")
	public Uni<Void> process(Message<?> message) {

		return session
			.withTransaction(t -> Uni.createFrom().item(message)
			.onItem().call(m -> _consumeIngestionMessage(_messagePayloadToJson(m)))
			.onItem().transformToUni(x -> Uni.createFrom().completionStage(message.ack())))
			.onTermination().call(() -> session.close());

	}

	private Uni<Void> _consumeIngestionMessage(JsonObject jsonObject) {

		return Uni.createFrom().deferred(() -> {

			JsonObject ingestionPayloadJson =
				jsonObject.getJsonObject("ingestionPayload");

			long datasourceId = ingestionPayloadJson.getLong("datasourceId");

			Uni<Datasource> datasourceContextUni =
				datasourceService.findById(datasourceId);

			return datasourceContextUni.flatMap(datasource -> {

				IngestionPayload ingestionPayload =
					ingestionPayloadJson.mapTo(IngestionPayload.class);

				long parsingDate = ingestionPayload.getParsingDate();

				OffsetDateTime lastIngestionDate = datasource.getLastIngestionDate();

				OffsetDateTime instantParsingDate = OffsetDateTime.ofInstant(
					Instant.ofEpochMilli(parsingDate), ZoneOffset.UTC);

				if (lastIngestionDate != null) {

					if (lastIngestionDate.equals(instantParsingDate)) {
						return Uni
							.createFrom()
							.item(
								IngestionDatasourcePayload.of(
									ingestionPayload, datasource));
					}

				}

				datasource.setLastIngestionDate(instantParsingDate);

				return datasource.persist()
					.replaceWith(
						IngestionDatasourcePayload.of(
							ingestionPayload, datasource));

			})
				.onItem()
				.invoke(ingestionDatasourceEmitter::send)
				.onFailure()
				.invoke((t) -> logger.error(
				"Error while processing ingestion message", t))
				.replaceWithVoid();
		});

	}

	private JsonObject _messagePayloadToJson(Message<?> message) {
		Object obj = message.getPayload();

		return obj instanceof JsonObject
			? (JsonObject) obj
			: new JsonObject(new String((byte[]) obj));

	}


	@Inject
	DatasourceService datasourceService;

	@Inject
	Logger logger;

	@Inject
	Mutiny.Session session;

	@Inject
	@Channel("ingestion-datasource")
	Emitter<IngestionDatasourcePayload> ingestionDatasourceEmitter;

}
