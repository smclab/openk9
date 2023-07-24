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
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.datasource.util.ActorActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DatasourcePurge extends AbstractBehavior<DatasourcePurge.Command> {

	public sealed interface Command {}
	private enum Start implements Command {INSTANCE}
	private enum Stop implements Command {INSTANCE}
	private enum FetchDataIndexOrphans implements Command {INSTANCE}
	private record SetDataIndexOrphan(List<DataIndex> dataIndices) implements Command {}
	private enum DeleteEsIndices implements Command {INSTANCE}
	private record EsDeleteError(Throwable error) implements Command {}
	private enum DeleteDataIndices implements Command {INSTANCE}

	private final String tenantName;
	private final long datasourceId;
	private final RestHighLevelClient esClient;
	private final TransactionInvoker txInvoker;
	private final Duration maxAge;
	private Set<DataIndex> dataIndexOrphans;

	public DatasourcePurge(
		ActorContext<Command> context, String tenantName, long datasourceId,
		RestHighLevelClient esClient, TransactionInvoker txInvoker) {
		super(context);
		this.tenantName = tenantName;
		this.datasourceId = datasourceId;
		this.esClient = esClient;
		this.txInvoker = txInvoker;
		this.maxAge = getMaxAge(context);
		getContext().getSelf().tell(Start.INSTANCE);
	}

	public static Behavior<Command> create(
		String tenantName, long datasourceId, RestHighLevelClient esClient,
		TransactionInvoker txInvoker) {

		return Behaviors.setup(ctx ->
			new DatasourcePurge(ctx, tenantName, datasourceId, esClient, txInvoker));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessageEquals(Start.INSTANCE, this::onStart)
			.onMessageEquals(FetchDataIndexOrphans.INSTANCE, this::onFetchDataIndexOrphans)
			.onMessage(SetDataIndexOrphan.class, this::onSetDataIndexOrphanIds)
			.onMessageEquals(DeleteEsIndices.INSTANCE, this::onDeleteEsIndices)
			.onMessageEquals(DeleteDataIndices.INSTANCE, this::onDeleteDataIndices)
			.onMessage(EsDeleteError.class, this::onEsDeleteError)
			.onMessageEquals(Stop.INSTANCE, this::onStop)
			.build();
	}

	private Behavior<Command> onStart() {
		getContext().getSelf().tell(FetchDataIndexOrphans.INSTANCE);

		return Behaviors.same();
	}

	private Behavior<Command> onFetchDataIndexOrphans() {
		VertxUtil.runOnContext(() -> txInvoker.withStatelessTransaction(
			tenantName, s -> s.createQuery(
				"select di " +
				"from DataIndex di " +
				"inner join di.datasource d on di.datasource = d and d.dataIndex <> di " +
				"where d.id = :id " +
				"and di.modifiedDate < :maxAgeDate", DataIndex.class)
				.setParameter("id", datasourceId)
				.setParameter("maxAgeDate", OffsetDateTime.of(
					LocalDateTime.now().minus(maxAge), ZoneOffset.UTC)
				)
				.getResultList()
		).invoke(dataIndices ->
			getContext().getSelf().tell(new SetDataIndexOrphan(dataIndices))));
		return Behaviors.same();
	}

	private Behavior<Command> onSetDataIndexOrphanIds(SetDataIndexOrphan sdioi) {
		this.dataIndexOrphans = new HashSet<>(sdioi.dataIndices);

		getContext().getSelf().tell(DeleteEsIndices.INSTANCE);

		return Behaviors.same();
	}

	private Behavior<Command> onDeleteEsIndices() {
		String[] names = dataIndexOrphans
			.stream()
			.map(DataIndex::getName)
			.toArray(String[]::new);

		DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(names);

		esClient
			.indices()
			.deleteAsync(
				deleteIndexRequest,
				RequestOptions.DEFAULT,
				ActorActionListener.of(
					getContext().getSelf(),
					(res, err) -> {
						if (err != null) {
							return new EsDeleteError(err);
						}
						else {
							return DeleteDataIndices.INSTANCE;
						}
					})
			);

		return Behaviors.same();
	}

	private Behavior<Command> onDeleteDataIndices() {

		Set<Long> ids = dataIndexOrphans
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		VertxUtil.runOnContext(() -> txInvoker
			.withTransaction(tenantName, s ->  s
				.createNativeQuery(
					"DELETE FROM data_index_doc_types t " +
						"WHERE t.data_index_id IN (:ids)")
				.setParameter("ids", ids)
				.executeUpdate()
				.chain(ignore -> s
					.createNativeQuery(
					"DELETE FROM data_index t " +
						"WHERE t.id in (:ids)")
					.setParameter("ids", ids)
					.executeUpdate()
				)
			)
			.invoke(ignore -> getContext().getSelf().tell(Stop.INSTANCE)));

		return Behaviors.same();
	}

	private Behavior<Command> onEsDeleteError(EsDeleteError ede) {
		getContext().getLog().error("ElasticSearch DeleteIndexRequest went wrong.", ede.error);

		return Behaviors.stopped();
	}


	private Behavior<Command> onStop() {
		return Behaviors.stopped();
	}

	private static Duration getMaxAge(ActorContext<?> context) {
		Config config = context.getSystem().settings().config();

		String configPath = "io.openk9.schedulation.purge.maxAge";

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
