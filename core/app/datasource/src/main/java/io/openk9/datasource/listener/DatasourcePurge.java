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

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.typesafe.config.Config;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.util.ActorActionListener;
import org.hibernate.reactive.mutiny.Mutiny;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.support.IndicesOptions;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

public class DatasourcePurge extends AbstractBehavior<DatasourcePurge.Command> {

	public sealed interface Command {}
	private enum Start implements Command {INSTANCE}
	private enum Stop implements Command {INSTANCE}
	private enum FetchDataIndexOrphans implements Command {INSTANCE}
	private record PrepareChunks(List<DataIndex> dataIndices) implements Command {}
	private enum WorkNextChunk implements Command {INSTANCE}
	private enum DeleteEsIndices implements Command {INSTANCE}

	private Behavior<Command> onDeleteEsIndices() {
		String[] names = currentChunk
			.stream()
			.map(DataIndex::getName)
			.toArray(String[]::new);

		DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(names);

		deleteIndexRequest
			.indicesOptions(
				IndicesOptions.fromMap(
					Map.of("ignore_unavailable", true),
					deleteIndexRequest.indicesOptions()
				)
			);

		getContext().getLog().info(
			"Deleting ElasticSearch orphans indices for datasource {}-{}",
			tenantName, datasourceId);

		esClient
			.indices()
			.deleteAsync(
				deleteIndexRequest,
				RequestOptions.DEFAULT,
				ActorActionListener.of(
					getContext().getSelf(),
					(res, err) -> {
						if (err != null) {
							return new EsDeleteError(new DatasourcePurgeException(err));
						}
						else {
							return DeleteDataIndices.INSTANCE;
						}
					})
			);

		return Behaviors.same();
	}
	private enum DeleteDataIndices implements Command {INSTANCE}

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

	private final String tenantName;
	private final long datasourceId;
	private final RestHighLevelClient esClient;
	private final Mutiny.SessionFactory sessionFactory;
	private final Duration maxAge;
	private final Deque<List<DataIndex>> chunks = new ArrayDeque<>();
	private List<DataIndex> currentChunk;

	public DatasourcePurge(
		ActorContext<Command> context, String tenantName, long datasourceId,
		RestHighLevelClient esClient, Mutiny.SessionFactory sessionFactory) {
		super(context);
		this.tenantName = tenantName;
		this.datasourceId = datasourceId;
		this.esClient = esClient;
		this.sessionFactory = sessionFactory;
		this.maxAge = getMaxAge(context);
		getContext().getSelf().tell(Start.INSTANCE);
	}

	public static Behavior<Command> create(
		String tenantName, long datasourceId, RestHighLevelClient esClient,
		Mutiny.SessionFactory sessionFactory) {

		return Behaviors.setup(ctx ->
			new DatasourcePurge(ctx, tenantName, datasourceId, esClient, sessionFactory));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessageEquals(Start.INSTANCE, this::onStart)
			.onMessageEquals(FetchDataIndexOrphans.INSTANCE, this::onFetchDataIndexOrphans)
			.onMessage(PrepareChunks.class, this::onPrepareChunks)
			.onMessageEquals(WorkNextChunk.INSTANCE, this::onWorkNextChunk)
			.onMessageEquals(DeleteEsIndices.INSTANCE, this::onDeleteEsIndices)
			.onMessageEquals(DeleteDataIndices.INSTANCE, this::onDeleteDataIndices)
			.onMessage(EsDeleteError.class, this::onEsDeleteError)
			.onMessageEquals(Stop.INSTANCE, this::onStop)
			.build();
	}

	private Behavior<Command> onStart() {
		getContext().getLog().info(
			"Job DatasourcePurge started for datasource {}-{}", tenantName, datasourceId);

		getContext().getSelf().tell(FetchDataIndexOrphans.INSTANCE);

		return Behaviors.same();
	}

	private Behavior<Command> onFetchDataIndexOrphans() {
		OffsetDateTime maxAgeDate = OffsetDateTime.of(
			LocalDateTime.now().minus(maxAge), ZoneOffset.UTC);

		getContext().getLog().info(
			"Fetching DataIndex orphans for datasource {}-{}, older than {}",
			tenantName, datasourceId, maxAgeDate);

		VertxUtil.runOnContext(() -> sessionFactory.withStatelessTransaction(
			tenantName, (s, t) -> s.createQuery(JPQL_QUERY_DATA_INDEX_ORPHANS, DataIndex.class)
				.setParameter("id", datasourceId)
				.setParameter("maxAgeDate", maxAgeDate
				)
				.getResultList()
		).invoke(dataIndices ->
			getContext().getSelf().tell(new PrepareChunks(dataIndices))));

		return Behaviors.same();
	}

	private Behavior<Command> onPrepareChunks(PrepareChunks prepareChunks) {
		List<DataIndex> dataIndices = prepareChunks.dataIndices;

		if (dataIndices != null && !dataIndices.isEmpty()) {
			int chunkSize = 10;

			int lastIndex = dataIndices.size();

			boolean lastChunk = false;

			int i = 0;

			while (!lastChunk) {
				int fromIndex = chunkSize * i;
				int toIndex = chunkSize * ++i;
				lastChunk = toIndex >= lastIndex;

				chunks.add(new ArrayList<>(
					dataIndices.subList(fromIndex, lastChunk ? lastIndex : toIndex))
				);
			}

			getContext().getLog().info(
				"DataIndex orphans found for datasource {}-{}: {}",
				tenantName, datasourceId, lastIndex);

			getContext().getLog().info("Chunks to work for datasource {}-{}: {}",
				tenantName, datasourceId, i);

			getContext().getSelf().tell(WorkNextChunk.INSTANCE);
		}
		else {
			getContext().getLog().info(
				"No DataIndex orphans found for datasource {}-{}", tenantName, datasourceId);
			getContext().getSelf().tell(Stop.INSTANCE);
		}

		return Behaviors.same();
	}

	private Behavior<Command> onWorkNextChunk() {
		try {
			this.currentChunk = chunks.pop();
			getContext().getLog().info(
				"Working on a chunk for datasource {}-{}", tenantName, datasourceId);
			getContext().getSelf().tell(DeleteEsIndices.INSTANCE);
		}
		catch (NoSuchElementException e) {
			getContext().getLog().info(
				"No more chunks to work for datasource {}-{}", tenantName, datasourceId);
			getContext().getSelf().tell(Stop.INSTANCE);
		}

		return Behaviors.same();
	}

	private record EsDeleteError(DatasourcePurgeException error) implements Command {}

	private Behavior<Command> onDeleteDataIndices() {

		getContext().getLog().info(
			"Deleting DataIndex orphans for datasource {}-{}", tenantName, datasourceId);

		Set<Long> ids = currentChunk
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		VertxUtil.runOnContext(() -> sessionFactory
			.withTransaction(tenantName, (s, t) ->  s
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
			.invoke(ignore -> getContext().getSelf().tell(WorkNextChunk.INSTANCE)));

		return Behaviors.same();
	}

	private Behavior<Command> onEsDeleteError(EsDeleteError ede) {
		getContext().getLog().error("ElasticSearch DeleteIndexRequest went wrong.", ede.error);

		getContext().getSelf().tell(WorkNextChunk.INSTANCE);

		return Behaviors.same();
	}


	private Behavior<Command> onStop() {
		getContext().getLog().info(
			"Job DatasourcePurge finished for datasource {}-{}", tenantName, datasourceId);
		return Behaviors.stopped();
	}

	private static Duration getMaxAge(ActorContext<?> context) {
		Config config = context.getSystem().settings().config();

		String configPath = "io.openk9.scheduling.purge.max-age";

		if (config.hasPathOrNull(configPath)) {
			if (config.getIsNull(configPath)) {
				return Duration.ofDays(2);
			} else {
				return config.getDuration(configPath);
			}
		} else {
			return Duration.ofDays(2);
		}

	}

}
