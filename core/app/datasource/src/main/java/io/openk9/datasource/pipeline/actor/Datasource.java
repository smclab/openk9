package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.datasource.util.CborSerializable;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.CDI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

public class Datasource {

	public sealed interface Command extends CborSerializable {}
	public record GetDatasource(
		String tenantId, long datasourceId, long parsingDate, ActorRef<Response> replyTo)
		implements Command {}
	private enum BusyFinished implements Command {INSTANCE}
	private record DatasourceModel(
		io.openk9.datasource.model.Datasource datasource, String tenantId, long parsingDate,
		ActorRef<Response> replyTo) implements Command {}
	private record DatasourceModelError(
		Throwable exception, ActorRef<Response> replyTo) implements Command {}
	private record CreateRandomDataIndex(
		String tenantId, long parsingDate, io.openk9.datasource.model.Datasource datasource,
		ActorRef<Response> replyTo) implements Command {}

	public sealed interface Response extends CborSerializable {}
	public record Success(io.openk9.datasource.model.Datasource datasource) implements Response {}
	public record Failure(Throwable exception) implements Response {}


	public static Behavior<Command> create() {
		return Behaviors.setup(ctx -> {

			TransactionInvoker transactionInvoker =
				CDI.current().select(TransactionInvoker.class).get();

			return idle(ctx, transactionInvoker);

		});
	}

	private static Behavior<Command> idle(
		ActorContext<Command> ctx, TransactionInvoker txInvoker) {

		return Behaviors.receive(Command.class)
			.onMessage(GetDatasource.class, message -> onGetDatasource(message, ctx, txInvoker))
			.build();
	}

	private static Behavior<Command> onDatasourceModelError(ActorContext<Command> ctx, DatasourceModelError message) {
		Throwable exception = message.exception;
		message.replyTo.tell(new Failure(exception));
		ctx.getSelf().tell(BusyFinished.INSTANCE);
		return Behaviors.same();
	}

	private static Behavior<Command> onDatasourceModel(
		DatasourceModel message, ActorContext<Command> ctx, TransactionInvoker txInvoker) {

		io.openk9.datasource.model.Datasource datasource = message.datasource;

		DataIndex dataIndex = datasource.getDataIndex();

		Logger log = ctx.getLog();

		long parsingDate = message.parsingDate;
		ActorRef<Response> replyTo = message.replyTo;
		String tenantId = message.tenantId;

		if (dataIndex == null) {
			log.info("datasource with id {} has no dataIndex, start random creation", datasource.getId());
			ctx.getSelf().tell(new CreateRandomDataIndex(tenantId, parsingDate, datasource, replyTo));
			return Behaviors.same();
		}

		OffsetDateTime lastIngestionDate = datasource.getLastIngestionDate();

		OffsetDateTime offsetDateTime =
			OffsetDateTime.ofInstant(Instant.ofEpochMilli(parsingDate),
				ZoneOffset.UTC);

		if (lastIngestionDate == null || !lastIngestionDate.isEqual(offsetDateTime)) {
			log.info("update last ingestion date for datasource with id {}", datasource.getId());
			datasource.setLastIngestionDate(offsetDateTime);
			mergeDatasource(
				ctx.getSelf(), tenantId, txInvoker, datasource, parsingDate,
				replyTo);
			return Behaviors.same();
		}

		log.info("start pipeline for datasource with id {}", datasource.getId());

		replyTo.tell(new Success(datasource));
		ctx.getSelf().tell(BusyFinished.INSTANCE);

		return Behaviors.same();
	}

	private static Behavior<Command> onGetDatasource(
		GetDatasource message, ActorContext<Command> ctx, TransactionInvoker txInvoker) {

		VertxUtil.runOnContext(() ->
			txInvoker.withStatelessTransaction(message.tenantId, s ->
				s.createQuery(
					"select d from Datasource d " +
					"left join fetch d.dataIndex di " +
					"left join fetch d.enrichPipeline ep " +
					"left join fetch ep.enrichPipelineItems epi " +
					"left join fetch epi.enrichItem ei " +
					"where d.id = :datasourceId ", io.openk9.datasource.model.Datasource.class)
				.setParameter("datasourceId", message.datasourceId)
				.getSingleResultOrNull()
				.onItemOrFailure()
				.invoke((d, t) -> {
					if (t != null) {
						ctx.getSelf().tell(new DatasourceModelError(t, message.replyTo));
					}
					if (d != null) {
						ctx.getSelf().tell(
							new DatasourceModel(
								d, message.tenantId, message.parsingDate, message.replyTo));
					}
				})
			)
		);

		return busy(ctx, txInvoker, new ArrayList<>());
	}

	private static Behavior<Command> busy(
		ActorContext<Command> ctx, TransactionInvoker txInvoker, List<Command> lags) {

		return Behaviors.receive(Command.class)
			.onMessage(GetDatasource.class, message -> onBusyGetMessage(ctx, txInvoker, lags, message))
			.onMessage(DatasourceModel.class, message -> onDatasourceModel(message, ctx, txInvoker))
			.onMessage(DatasourceModelError.class, message -> onDatasourceModelError(ctx, message))
			.onMessage(CreateRandomDataIndex.class, message -> onCreateRandomDataIndex(ctx, txInvoker, message))
			.onMessageEquals(BusyFinished.INSTANCE, () -> onBusyFinished(ctx, txInvoker, lags))
			.build();
	}

	private static Behavior<Command> onCreateRandomDataIndex(
		ActorContext<Command> ctx,
		TransactionInvoker transactionInvoker,
		CreateRandomDataIndex createRandomDataIndex) {

		io.openk9.datasource.model.Datasource datasource =
			createRandomDataIndex.datasource;

		String tenantId = createRandomDataIndex.tenantId;

		String indexName = datasource.getId() + "-data-" + UUID.randomUUID();

		DataIndex dataIndex = DataIndex.of(
			indexName, "auto-generated",
			new LinkedHashSet<>());

		datasource.setDataIndex(dataIndex);

		ctx.getLog().info(
			"creating random dataIndex: {} for datasource: {}", indexName, datasource.getId());

		mergeDatasource(
			ctx.getSelf(), tenantId, transactionInvoker, datasource,
			createRandomDataIndex.parsingDate, createRandomDataIndex.replyTo);

		return Behaviors.same();
	}

	private static Behavior<Command> onBusyFinished(
		ActorContext<Command> ctx, TransactionInvoker txInvoker, List<Command> lags) {

		for (Command message : lags) {
			ctx.getSelf().tell(message);
		}

		return idle(ctx, txInvoker);
	}

	private static Behavior<Command> onBusyGetMessage(
		ActorContext<Command> ctx, TransactionInvoker txInvoker,
		List<Command> lags, GetDatasource message) {
		List<Command> newLags = new ArrayList<>(lags);
		newLags.add(message);
		ctx.getLog().info("Datasource actor is busy... lags count: " + newLags.size());
		return busy(ctx, txInvoker, newLags);
	}

	private static void mergeDatasource(
		ActorRef<Command> self, String tenantId,
		TransactionInvoker transactionInvoker, io.openk9.datasource.model.Datasource datasource,
		long parsingDate, ActorRef<Response> replyTo) {

		VertxUtil.runOnContext(() ->
			transactionInvoker
				.withTransaction(tenantId, s -> s.merge(datasource))
				.invoke(d -> self.tell(new DatasourceModel(d, tenantId, parsingDate, replyTo)))
		);
	}

}
