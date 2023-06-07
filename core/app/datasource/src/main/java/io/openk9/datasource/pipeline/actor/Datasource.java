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

public class Datasource {

  public sealed interface Command extends CborSerializable {}
  public record GetDatasource(
    String tenantId, long datasourceId, long parsingDate, ActorRef<Response> replyTo)
    implements Command {}
  private record DatasourceModel(
    io.openk9.datasource.model.Datasource datasource, String tenantId, long parsingDate,
    ActorRef<Response> replyTo)
    implements Command {}
  private record DatasourceModelError(
    Throwable exception, ActorRef<Response> replyTo) implements Command {}
  private record CreateRandomDataIndex(
    io.openk9.datasource.model.Datasource datasource) implements Command {}

  public sealed interface Response extends CborSerializable {}
  public record Success(io.openk9.datasource.model.Datasource datasource) implements Response {}
  public record Failure(Throwable exception) implements Response {}


  public static Behavior<Command> create() {
    return Behaviors.setup(ctx -> {

      TransactionInvoker transactionInvoker =
          CDI.current().select(TransactionInvoker.class).get();

      return initial(ctx, transactionInvoker);
    });
  }

  private static Behavior<Command> initial(
    ActorContext<Command> ctx, TransactionInvoker txInvoker) {

    return Behaviors.receive(Command.class)
        .onMessage(GetDatasource.class, message -> onGetDatasource(message, ctx, txInvoker))
        .onMessage(DatasourceModel.class, message -> onDatasourceModel(message, ctx, txInvoker))
        .onMessage(DatasourceModelError.class, Datasource::onDatasourceModelError)
        .build();
  }

  private static Behavior<Command> onDatasourceModelError(DatasourceModelError message) {
    Throwable exception = message.exception;
    message.replyTo.tell(new Failure(exception));

    return Behaviors.same();
  }

  private static Behavior<Command> onDatasourceModel(
    DatasourceModel message, ActorContext<Command> ctx, TransactionInvoker txInvoker) {

    io.openk9.datasource.model.Datasource datasource = message.datasource;

    DataIndex dataIndex = datasource.getDataIndex();

    Logger log = ctx.getLog();

    if (dataIndex == null) {
      log.info("datasource with id {} has no dataIndex, start random creation", datasource.getId());
      ctx.getSelf().tell(new CreateRandomDataIndex(datasource));
      return Behaviors.same();
    }

    OffsetDateTime lastIngestionDate = datasource.getLastIngestionDate();
    long parsingDate = message.parsingDate;

    OffsetDateTime offsetDateTime =
      OffsetDateTime.ofInstant(Instant.ofEpochMilli(parsingDate),
        ZoneOffset.UTC);

    if (lastIngestionDate == null || !lastIngestionDate.isEqual(offsetDateTime)) {
      log.info("update last ingestion date for datasource with id {}", datasource.getId());
      datasource.setLastIngestionDate(offsetDateTime);
      mergeDatasource(
        ctx.getSelf(), message.tenantId, txInvoker, datasource, parsingDate, message.replyTo);
      return Behaviors.same();
    }

    log.info("start pipeline for datasource with id {}", datasource.getId());

    message.replyTo.tell(new Success(datasource));

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

    return Behaviors.same();
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
