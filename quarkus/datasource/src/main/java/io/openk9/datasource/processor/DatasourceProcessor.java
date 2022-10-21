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
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.processor.enrich.EnrichStepHandler;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.processor.payload.IngestionIndexWriterPayload;
import io.openk9.datasource.processor.payload.IngestionPayload;
import io.openk9.datasource.service.EnrichPipelineService;
import io.openk9.datasource.tenant.TenantResolver;
import io.openk9.datasource.util.MessageUtil;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.UUID;

@ApplicationScoped
public class DatasourceProcessor {

	@Incoming("ingestion")
	public Uni<Void> process(Message<IngestionIndexWriterPayload> message) {

		IngestionIndexWriterPayload payload = MessageUtil.toObj(
			message, IngestionIndexWriterPayload.class);

		IngestionPayload ingestionPayload = payload.getIngestionPayload();

		long datasourceId = ingestionPayload.getDatasourceId();

		ingestionPayload.setTenantId(tenantResolver.getTenantId());

		DataPayload dataPayload =
			ingestionPayloadMapper.map(ingestionPayload);

		return sf.withTransaction(s ->
				_getDatasourceAndDataIndex(s, datasourceId)
				.onItem()
				.transformToUni(datasource -> {

					long parsingDate = ingestionPayload.getParsingDate();

					OffsetDateTime instantParsingDate =
						OffsetDateTime.ofInstant(
							Instant.ofEpochMilli(parsingDate), ZoneOffset.UTC);

					datasource.setLastIngestionDate(instantParsingDate);

					DataIndex dataIndex = datasource.getDataIndex();

					if (dataIndex == null) {
						String indexName =
							datasource.getId() + "-data-" +
							UUID.randomUUID();

						dataIndex = DataIndex.of(
							indexName, "auto-generated",
							new LinkedHashSet<>());

						datasource.setDataIndex(dataIndex);

					}

					dataPayload.addRest("indexName", dataIndex.getName());

					return s
						.persist(datasource)
						.map(v -> datasource);

				})
				.flatMap(datasource -> Mutiny2
					.fetch(s, datasource.getEnrichPipeline())
					.flatMap(ep -> {

						if (ep == null) {
							return enrichStepHandler.consume(
								EnrichStepHandler.EnrichStep.of(
									dataPayload,
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
											dataPayload,
											0L,
											0L
										)
									);
								}

								return enrichStepHandler.consume(
									EnrichStepHandler.EnrichStep.of(
										dataPayload,
										ep.getId(),
										ei.getId()
									)
								);

							});
					})
				)
		)
			.onItemOrFailure()
			.transformToUni((item, failure) -> {

				if (failure != null) {
					logger.error(failure.getMessage(), failure);
					return Uni.createFrom().completionStage(
						() -> message.nack(failure));
				}

				return Uni.createFrom().completionStage(message::ack);

			});

	}

	private Uni<Datasource> _getDatasourceAndDataIndex(
		Mutiny.Session session, long datasourceId) {

		CriteriaBuilder criteriaBuilder = sf.getCriteriaBuilder();

		CriteriaQuery<Datasource> query =
			criteriaBuilder.createQuery(Datasource.class);

		Root<Datasource> rootDatasource = query.from(Datasource.class);

		rootDatasource.fetch(Datasource_.dataIndex);

		query.where(
			criteriaBuilder.equal(
				rootDatasource.get(Datasource_.id), datasourceId));

		return session.createQuery(query).getSingleResultOrNull();

	}

	@Inject
	Mutiny.SessionFactory sf;

	@Inject
	EnrichPipelineService enrichPipelineService;

	@Inject
	EnrichStepHandler enrichStepHandler;

	@Inject
	IngestionPayloadMapper ingestionPayloadMapper;

	@Inject
	TenantResolver tenantResolver;

	@Inject
	Logger logger;

}
