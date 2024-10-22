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

import io.openk9.datasource.actor.EventBusInstanceHolder;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.util.K9Entity;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.Message;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.support.IndicesOptions;
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.core.action.ActionListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@ApplicationScoped
public class DatasourcePurgeService {

	private static final Logger log = Logger.getLogger(DatasourcePurgeService.class);

	private static final String BULK_DELETE_DATA_INDEX_DOC_TYPE_RELATIONSHIP =
		"DELETE FROM data_index_doc_types t WHERE t.data_index_id IN (:ids)";
	private static final String BULK_UPDATE_DEREFERENCE_OLD_DATAINDEX =
		"UPDATE scheduler SET old_data_index_id = null WHERE old_data_index_id in (:ids)";
	private static final String BULK_UPDATE_DEREFERENCE_NEW_DATAINDEX =
		"UPDATE scheduler SET new_data_index_id = null WHERE new_data_index_id in (:ids)";
	private static final String BULK_DELETE_DATA_INDEX =
		"DELETE FROM data_index t WHERE t.id in (:ids)";
	private static final String JPQL_QUERY_DATA_INDEX_ORPHANS =
		"select di " +
		"from DataIndex di " +
		"inner join di.datasource d on di.datasource = d and d.dataIndex <> di " +
		"where d.id = :id " +
		"and di.modifiedDate < :maxAgeDate";

	private static final String DELETE_DATA_INDICES = "DatasourcePurgeService#deleteDataIndices";
	private static final String DELETE_INDICES = "DatasourcePurgeService#deleteIndices";
	private static final String FETCH_ORPHANS = "DatasourcePurgeService#fetchOrphans";

	@Inject
	RestHighLevelClient restHighLevelClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	public static CompletableFuture<Void> deleteIndices(List<DataIndex> dataIndices) {

		var request = new DeleteIndicesRequest(dataIndices);

		return EventBusInstanceHolder.getEventBus()
			.<Void>request(DELETE_INDICES, request)
			.map(Message::body)
			.subscribeAsCompletionStage();
	}

	public static CompletableFuture<Void> deleteDataIndices(
		String tenantId, long datasourceId, List<DataIndex> dataIndices) {

		var request = new DeleteDataIndicesRequest(tenantId, datasourceId, dataIndices);

		return EventBusInstanceHolder.getEventBus()
			.<Void>request(DELETE_DATA_INDICES, request)
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

		var first = dataIndices.iterator().next();
		var tenantId = first.getTenant();
		var datasource = first.getDatasource();
		var datasourceId = datasource.getId();

		String[] names = dataIndices
			.stream()
			.map(DataIndex::getIndexName)
			.toArray(String[]::new);

		DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(names);

		deleteIndexRequest
			.indicesOptions(
				IndicesOptions.fromMap(
					Map.of("ignore_unavailable", true),
					deleteIndexRequest.indicesOptions()
				)
			);

		log.infof(
			"Deleting Opensearch orphans indices for datasource {}-{}",
			tenantId, datasourceId
		);


		return Uni.createFrom().emitter(emitter ->
			restHighLevelClient.indices().deleteAsync(
				deleteIndexRequest,
				RequestOptions.DEFAULT,
				new ActionListener<AcknowledgedResponse>() {
					@Override
					public void onResponse(AcknowledgedResponse acknowledgedResponse) {
						emitter.complete(acknowledgedResponse);
					}

					@Override
					public void onFailure(Exception e) {
						emitter.fail(e);
					}
				}
			)
		).replaceWithVoid();
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

		return sessionFactory.withStatelessTransaction(
			tenantId, (s, t) -> s.createQuery(JPQL_QUERY_DATA_INDEX_ORPHANS, DataIndex.class)
				.setParameter("id", datasourceId)
				.setParameter("maxAgeDate", maxAgeDate)
				.getResultList()
		);

	}

	private record DeleteIndicesRequest(List<DataIndex> dataIndices) {}

	private record FetchOrphansRequest(String tenantId, long datasourceId, Duration maxAge) {}

	private record DeleteDataIndicesRequest(
		String tenantId,
		long datasourceId,
		List<DataIndex> dataIndices
	) {}

}
