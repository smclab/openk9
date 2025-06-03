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

package io.openk9.datasource.listener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.datasource.actor.EventBusInstanceHolder;
import io.openk9.datasource.index.IndexService;
import io.openk9.datasource.index.model.IndexName;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.util.K9Entity;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.Message;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;


@ApplicationScoped
public class DatasourcePurgeService {

	private static final String BULK_DELETE_DATA_INDEX =
		"DELETE FROM data_index t WHERE t.id in (:ids)";
	private static final String BULK_DELETE_DATA_INDEX_DOC_TYPE_RELATIONSHIP =
		"DELETE FROM data_index_doc_types t WHERE t.data_index_id IN (:ids)";
	private static final String BULK_UPDATE_DEREFERENCE_NEW_DATAINDEX =
		"UPDATE scheduler SET new_data_index_id = null WHERE new_data_index_id in (:ids)";
	private static final String BULK_UPDATE_DEREFERENCE_OLD_DATAINDEX =
		"UPDATE scheduler SET old_data_index_id = null WHERE old_data_index_id in (:ids)";
	private static final String DELETE_DATA_INDICES = "DatasourcePurgeService#deleteDataIndices";
	private static final String DELETE_INDICES = "DatasourcePurgeService#deleteIndices";
	private static final String FETCH_ORPHANS = "DatasourcePurgeService#fetchOrphans";
	private static final String JPQL_QUERY_DATA_INDEX_ORPHANS =
		"select di " +
		"from DataIndex di " +
		"inner join di.datasource d on di.datasource = d and d.dataIndex <> di " +
		"where d.id = :id " +
		"and di.modifiedDate < :maxAgeDate";
	private static final Logger log = Logger.getLogger(DatasourcePurgeService.class);

	@Inject
	IndexService indexService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	public static CompletableFuture<Void> deleteDataIndices(
		String tenantId, long datasourceId, List<DataIndex> dataIndices) {

		var request = new DeleteDataIndicesRequest(tenantId, datasourceId, dataIndices);

		return EventBusInstanceHolder.getEventBus()
			.<Void>request(DELETE_DATA_INDICES, request)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<Void> deleteIndices(
		String tenantName,
		List<DataIndex> dataIndices) {

		var request = new DeleteIndicesRequest(tenantName, dataIndices);

		return EventBusInstanceHolder.getEventBus()
			.<Void>request(DELETE_INDICES, request)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<List<DataIndex>> fetchOrphans(
		String tenantId, long datasourceId, Duration maxAge) {

		var request = new FetchOrphansRequest(tenantId, datasourceId, maxAge);

		return EventBusInstanceHolder.getEventBus()
			.<List<DataIndex>>request(FETCH_ORPHANS, request)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	@ConsumeEvent(DELETE_DATA_INDICES)
	Uni<Void> deleteDataIndices(DeleteDataIndicesRequest request) {

		var tenantId = request.tenantId();
		var datasourceId = request.datasourceId();
		var dataIndices = request.dataIndices();

		log.infof(
			"Deleting DataIndex orphans for datasource %s-%s", tenantId, datasourceId);

		Set<Long> ids = dataIndices
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		return sessionFactory
			.withTransaction(tenantId, (s, t) -> s
				.createNativeQuery(BULK_DELETE_DATA_INDEX_DOC_TYPE_RELATIONSHIP)
				.setParameter("ids", ids)
				.executeUpdate()
				.chain(ignore -> s
					.createNativeQuery(BULK_UPDATE_DEREFERENCE_OLD_DATAINDEX)
					.setParameter("ids", ids)
					.executeUpdate()
				)
				.chain(ignore -> s
					.createNativeQuery(BULK_UPDATE_DEREFERENCE_NEW_DATAINDEX)
					.setParameter("ids", ids)
					.executeUpdate()
				)
				.chain(ignore -> s
					.createNativeQuery(BULK_DELETE_DATA_INDEX)
					.setParameter("ids", ids)
					.executeUpdate()
				)
			)
			.replaceWithVoid();
	}

	@ConsumeEvent(DELETE_INDICES)
	Uni<Void> deleteIndices(DeleteIndicesRequest request) {

		var dataIndices = request.dataIndices();

		if (request.dataIndices().isEmpty()) {

			log.info("Nothing to do, dataIndices is empty.");

			return Uni.createFrom().voidItem();
		}

		var first = dataIndices.getFirst();
		var datasource = first.getDatasource();
		var datasourceId = datasource.getId();

		log.infof(
			"Deleting Opensearch orphans indices for datasource %s-%s",
			request.tenantId(), datasourceId
		);

		var dataIndexNames = dataIndices.stream()
			.map(dataIndex -> IndexName.from(
				request.tenantId(), dataIndex))
			.collect(Collectors.toSet());

		return indexService.deleteIndices(dataIndexNames);
	}

	@ConsumeEvent(FETCH_ORPHANS)
	Uni<List<DataIndex>> fetchOrphans(FetchOrphansRequest request) {

		var maxAge = request.maxAge();
		var tenantId = request.tenantId();
		var datasourceId = request.datasourceId();

		OffsetDateTime maxAgeDate = OffsetDateTime.of(
			LocalDateTime.now().minus(maxAge), ZoneOffset.UTC);

		log.infof(
			"Fetching DataIndex orphans for datasource %s-%s, older than %s",
			tenantId, datasourceId, maxAgeDate
		);

		return sessionFactory.withTransaction(
			tenantId, (s, t) ->
				s.createQuery(JPQL_QUERY_DATA_INDEX_ORPHANS, DataIndex.class)
					.setParameter("id", datasourceId)
					.setParameter("maxAgeDate", maxAgeDate)
					.getResultList()
		);

	}

	private record DeleteDataIndicesRequest(
		String tenantId,
		long datasourceId,
		List<DataIndex> dataIndices
	) {}

	private record DeleteIndicesRequest(String tenantId, List<DataIndex> dataIndices) {}

	private record FetchOrphansRequest(String tenantId, long datasourceId, Duration maxAge) {}

}
