package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.pipeline.actor.dto.GetDatasourceDTO;
import io.openk9.datasource.pipeline.actor.mapper.PipelineMapper;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.datasource.util.CborSerializable;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.CDI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class Datasource {

	public sealed interface Command extends CborSerializable {}
	public record GetDatasource(
		String tenantId, long datasourceId, long parsingDate, ActorRef<Response> replyTo)
		implements Command {}
	public record SetDataIndex(String tenantId, long datasourceId, long dataIndexId) implements Command {}
	private record Finished(io.openk9.datasource.model.Datasource datasource, Throwable throwable) implements Command {}
	private record DatasourceModel(
		io.openk9.datasource.model.Datasource datasource
	) implements Command {}
	private record DatasourceModelError(
		Throwable exception) implements Command {}

	public sealed interface Response extends CborSerializable {}
	public record Success(GetDatasourceDTO datasource) implements Response {}
	public record Failure(Throwable exception) implements Response {}


	public static Behavior<Command> create(PipelineMapper pipelineMapper) {
		return Behaviors.setup(ctx -> {

			TransactionInvoker transactionInvoker =
				CDI.current().select(TransactionInvoker.class).get();

			DatasourceService datasourceService = CDI.current().select(DatasourceService.class).get();

			return idle(ctx, transactionInvoker, pipelineMapper, datasourceService);

		});
	}

	private static Behavior<Command> idle(
		ActorContext<Command> ctx, TransactionInvoker txInvoker,
		PipelineMapper pipelineMapper, DatasourceService datasourceService) {

		ctx.getLog().info("Start idle state");

		return Behaviors.receive(Command.class)
			.onMessage(GetDatasource.class, message -> onGetDatasource(
				message, ctx, txInvoker, pipelineMapper, datasourceService))
			.onMessage(SetDataIndex.class, sdi -> onSetDataIndex(
				sdi, ctx, txInvoker, datasourceService))
			.build();
	}

	private static Behavior<Command> onSetDataIndex(
		SetDataIndex sdi, ActorContext<Command> ctx, TransactionInvoker txInvoker,
		DatasourceService datasourceService) {

		VertxUtil.runOnContext(() -> txInvoker.withTransaction(sdi.tenantId, s ->
			datasourceService.setDataIndex(sdi.datasourceId, sdi.dataIndexId)));

		return Behaviors.same();
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

		Logger log = ctx.getLog();

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
		PipelineMapper pipelineMapper, DatasourceService datasourceService) {


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
			ctx, txInvoker, pipelineMapper, datasourceService, new ArrayList<>(),
			message.tenantId, message.parsingDate,
			message.replyTo);
	}

	private static Behavior<Command> busy(
		ActorContext<Command> ctx, TransactionInvoker txInvoker, PipelineMapper pipelineMapper,
		DatasourceService datasourceService,
		List<Command> lags, String tenantId, long parsingDate, ActorRef<Response> replyTo) {

		return Behaviors.receive(Command.class)
			.onMessage(GetDatasource.class, message -> onBusyGetMessage(ctx, txInvoker, pipelineMapper, datasourceService, lags, message, tenantId, parsingDate, replyTo))
			.onMessage(DatasourceModel.class, message -> onDatasourceModel(message, ctx, txInvoker, tenantId, parsingDate, replyTo))
			.onMessage(DatasourceModelError.class, message -> onDatasourceModelError(ctx, message))
			.onMessage(Finished.class, (message) -> onFinished(ctx, txInvoker, pipelineMapper, datasourceService, lags, replyTo, message))
			.build();
	}

	private static Behavior<Command> onFinished(
		ActorContext<Command> ctx, TransactionInvoker txInvoker, PipelineMapper pipelineMapper, DatasourceService datasourceService, List<Command> lags,
		ActorRef<Response> replyTo, Finished finished) {

		Throwable throwable = finished.throwable;

		if (throwable != null) {
			replyTo.tell(new Failure(throwable));
		}
		else {
			io.openk9.datasource.model.Datasource datasource = finished.datasource;
			GetDatasourceDTO datasourceDTO = pipelineMapper.map(datasource);
			replyTo.tell(new Success(datasourceDTO));
		}

		for (Command message : lags) {
			ctx.getSelf().tell(message);
		}

		return idle(ctx, txInvoker, pipelineMapper, datasourceService);
	}

	private static Behavior<Command> onBusyGetMessage(
		ActorContext<Command> ctx, TransactionInvoker txInvoker,
		PipelineMapper pipelineMapper, DatasourceService datasourceService, List<Command> lags, Command message, String tenantId,
		long parsingDate, ActorRef<Response> replyTo) {
		List<Command> newLags = new ArrayList<>(lags);
		newLags.add(message);
		ctx.getLog().info("Datasource actor is busy... lags count: " + newLags.size());
		return busy(
			ctx, txInvoker, pipelineMapper, datasourceService, newLags, tenantId, parsingDate, replyTo);
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
