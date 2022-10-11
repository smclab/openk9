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


import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.processor.enrich.EnrichStepHandler;
import io.openk9.datasource.processor.payload.IngestionIndexWriterPayload;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.EnrichPipelineService;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@ApplicationScoped
public class DatasourceProcessor {

	@Incoming("ingestion")
	public Uni<Void> process(Message<IngestionIndexWriterPayload> message) {

		IngestionIndexWriterPayload payload = message.getPayload();

		IngestionPayload ingestionPayload = payload.getIngestionPayload();

		long datasourceId = ingestionPayload.getDatasourceId();

		return datasourceService.withTransaction(s ->
			s
				.find(Datasource.class, datasourceId)
				.onItem()
				.transformToUni(datasource -> {

					long parsingDate = ingestionPayload.getParsingDate();

					OffsetDateTime lastIngestionDate =
						datasource.getLastIngestionDate();

					OffsetDateTime instantParsingDate =
						OffsetDateTime.ofInstant(
							Instant.ofEpochMilli(parsingDate), ZoneOffset.UTC);

					if (lastIngestionDate != null) {

						if (!lastIngestionDate.equals(instantParsingDate)) {

							datasource.setLastIngestionDate(
								instantParsingDate);

							return s
								.persist(datasource)
								.map(v -> datasource);

						}

					}

					return Uni.createFrom().item(datasource);
				})
				.flatMap(datasource -> Mutiny2
					.fetch(s, datasource.getEnrichPipeline())
					.flatMap(ep -> {

						if (ep == null) {
							return enrichStepHandler.consume(
								EnrichStepHandler.EnrichStep.of(
									ingestionPayloadMapper.map(
										ingestionPayload),
									0L,
									0L
								)
							);
						}

						return enrichPipelineService
							.findFirstEnrichItem(ep.getId())
							.flatMap(ei -> {

								if (ei == null) {
									return enrichStepHandler.consume(
										EnrichStepHandler.EnrichStep.of(
											ingestionPayloadMapper.map(
												ingestionPayload),
											0L,
											0L
										)
									);
								}

								return enrichStepHandler.consume(
									EnrichStepHandler.EnrichStep.of(
										ingestionPayloadMapper.map(
											ingestionPayload),
										ep.getId(),
										ei.getId()
									)
								);

							});
					})
				)
		);

	}

	@Inject
	DatasourceService datasourceService;

	@Inject
	EnrichPipelineService enrichPipelineService;

	@Inject
	EnrichStepHandler enrichStepHandler;

	@Inject
	IngestionPayloadMapper ingestionPayloadMapper;

}
