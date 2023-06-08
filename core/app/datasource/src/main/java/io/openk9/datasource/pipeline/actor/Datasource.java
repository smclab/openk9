package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.pipeline.actor.dto.GetDatasourceDTO;
import io.openk9.datasource.pipeline.actor.mapper.DatasourceMapper;
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
	private record Finished(io.openk9.datasource.model.Datasource datasource, Throwable throwable) implements Command {}
	private record DatasourceModel(
		io.openk9.datasource.model.Datasource datasource
	) implements Command {}
	private record DatasourceModelError(
		Throwable exception) implements Command {}
	private record CreateRandomDataIndex(
		String tenantId, long parsingDate, io.openk9.datasource.model.Datasource datasource,
		ActorRef<Response> replyTo) implements Command {}

	public sealed interface Response extends CborSerializable {}
	public record Success(GetDatasourceDTO datasource) implements Response {}
	public record Failure(Throwable exception) implements Response {}


	public static Behavior<Command> create(DatasourceMapper datasourceMapper) {
		return Behaviors.setup(ctx -> {

			TransactionInvoker transactionInvoker =
				CDI.current().select(TransactionInvoker.class).get();

			return idle(ctx, transactionInvoker, datasourceMapper);

		});
	}

	private static Behavior<Command> idle(
		ActorContext<Command> ctx, TransactionInvoker txInvoker,
		DatasourceMapper datasourceMapper) {

		ctx.getLog().info("Start idle state");

		return Behaviors.receive(Command.class)
			.onMessage(GetDatasource.class, message -> onGetDatasource(
				message, ctx, txInvoker, datasourceMapper))
			.build();
	}

	private static Behavior<Command> onDatasourceModelError(
		ActorContext<Command> ctx, DatasourceModelError message) {
		Throwable exception = message.exception;
		ctx.getSelf().tell(new Finished(null, exception));
		return Behaviors.same();
	}

	private static Behavior<Command> onDatasourceModel(
		DatasourceModel message, ActorContext<Command> ctx, TransactionInvoker txInvoker,
		String tenantId, long parsingDate, ActorRef<Response> replyTo) {

		io.openk9.datasource.model.Datasource datasource = message.datasource;

		DataIndex dataIndex = datasource.getDataIndex();

		Logger log = ctx.getLog();

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
			mergeDatasource(ctx.getSelf(), tenantId, txInvoker, datasource);
			return Behaviors.same();
		}

		log.info("start pipeline for datasource with id {}", datasource.getId());

		ctx.getSelf().tell(new Finished(datasource, null));

		return Behaviors.same();
	}

	private static Behavior<Command> onGetDatasource(
		GetDatasource message, ActorContext<Command> ctx, TransactionInvoker txInvoker,
		DatasourceMapper datasourceMapper) {


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
						ctx.getSelf().tell(new DatasourceModelError(t));
					}
					if (d != null) {
						ctx.getSelf().tell(new DatasourceModel(d));
					}
				})
			)
		);

		return busy(
			ctx, txInvoker, datasourceMapper, new ArrayList<>(),
			message.tenantId, message.parsingDate,
			message.replyTo);
	}

	private static Behavior<Command> busy(
		ActorContext<Command> ctx, TransactionInvoker txInvoker, DatasourceMapper datasourceMapper,
		List<Command> lags, String tenantId, long parsingDate, ActorRef<Response> replyTo) {

		return Behaviors.receive(Command.class)
			.onMessage(GetDatasource.class, message -> onBusyGetMessage(ctx, txInvoker, datasourceMapper, lags, message, tenantId, parsingDate, replyTo))
			.onMessage(DatasourceModel.class, message -> onDatasourceModel(message, ctx, txInvoker, tenantId, parsingDate, replyTo))
			.onMessage(DatasourceModelError.class, message -> onDatasourceModelError(ctx, message))
			.onMessage(CreateRandomDataIndex.class, message -> onCreateRandomDataIndex(ctx, txInvoker, message))
			.onMessage(Finished.class, (message) -> onFinished(ctx, txInvoker, datasourceMapper, lags, replyTo, message))
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
			ctx.getSelf(), tenantId, transactionInvoker, datasource);

		return Behaviors.same();
	}

	private static Behavior<Command> onFinished(
		ActorContext<Command> ctx, TransactionInvoker txInvoker, DatasourceMapper datasourceMapper, List<Command> lags,
		ActorRef<Response> replyTo, Finished finished) {

		Throwable throwable = finished.throwable;

		if (throwable != null) {
			replyTo.tell(new Failure(throwable));
		}
		else {
			io.openk9.datasource.model.Datasource datasource = finished.datasource;
			GetDatasourceDTO datasourceDTO = datasourceMapper.map(datasource);
			replyTo.tell(new Success(datasourceDTO));
		}

		for (Command message : lags) {
			ctx.getSelf().tell(message);
		}

		return idle(ctx, txInvoker, datasourceMapper);
	}

	private static Behavior<Command> onBusyGetMessage(
		ActorContext<Command> ctx, TransactionInvoker txInvoker,
		DatasourceMapper datasourceMapper, List<Command> lags, Command message, String tenantId,
		long parsingDate, ActorRef<Response> replyTo) {
		List<Command> newLags = new ArrayList<>(lags);
		newLags.add(message);
		ctx.getLog().info("Datasource actor is busy... lags count: " + newLags.size());
		return busy(
			ctx, txInvoker, datasourceMapper, newLags, tenantId, parsingDate, replyTo);
	}

	private static void mergeDatasource(
		ActorRef<Command> self, String tenantId,
		TransactionInvoker transactionInvoker, io.openk9.datasource.model.Datasource datasource) {

		VertxUtil.runOnContext(() ->
			transactionInvoker
				.withTransaction(tenantId, s -> s.merge(datasource))
				.invoke(d -> self.tell(new DatasourceModel(d)))
		);
	}

}
