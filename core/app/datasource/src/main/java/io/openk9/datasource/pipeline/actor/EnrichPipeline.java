package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;
import io.openk9.common.util.collection.Collections;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipelineItem;
import io.openk9.datasource.pipeline.actor.enrichitem.EnrichItemSupervisor;
import io.openk9.datasource.pipeline.actor.enrichitem.HttpSupervisor;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.util.JsonMerge;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Set;

public class EnrichPipeline {

  public sealed interface Command {}
  private record IndexWriterResponseWrapper(
    IndexWriterActor.Response response) implements Command {}
  private record EnrichItemSupervisorResponseWrapper(
    EnrichItemSupervisor.Response response) implements Command {}
  private record EnrichItemError(EnrichItem enrichItem, Throwable exception) implements Command {}
  private record InternalResponseWrapper(byte[] jsonObject) implements Command {}
  private record InternalError(String error) implements Command {}
  public sealed interface Response {}
  public enum Success implements Response {INSTANCE}
  public record Failure(Throwable exception) implements Response {}

  public static Behavior<Command> create(
    ActorRef<HttpSupervisor.Command> supervisorActorRef, ActorRef<Response> replyTo,
    DataPayload dataPayload, Datasource datasource) {

    return Behaviors.setup(ctx -> {

      Logger log = ctx.getLog();

      ActorRef<IndexWriterActor.Response> responseActorRef =
        ctx.messageAdapter(
          IndexWriterActor.Response.class,
          IndexWriterResponseWrapper::new);

      DataIndex dataIndex = datasource.getDataIndex();

      dataPayload.setIndexName(dataIndex.getName());

      io.openk9.datasource.model.EnrichPipeline enrichPipeline = datasource.getEnrichPipeline();

      Set<EnrichPipelineItem> enrichPipelineItems;

      if (enrichPipeline == null) {
        enrichPipelineItems = Set.of();
      }
      else {
        enrichPipelineItems = enrichPipeline.getEnrichPipelineItems();
      }

      log.info("start pipeline for datasource with id {}", datasource.getId());

      return initPipeline(
        ctx, supervisorActorRef, responseActorRef, replyTo, dataPayload, datasource,
        enrichPipelineItems);
    });
  }

  private static Behavior<Command> initPipeline(
      ActorContext<EnrichPipeline.Command> ctx,
      ActorRef<HttpSupervisor.Command> supervisorActorRef,
      ActorRef<IndexWriterActor.Response> responseActorRef,
      ActorRef<Response> replyTo, DataPayload dataPayload,
      Datasource datasource, Set<EnrichPipelineItem> enrichPipelineItems) {

    Logger logger = ctx.getLog();

    if (enrichPipelineItems.isEmpty()) {

      logger.info("pipeline is empty, start index writer");

      ClusterSingleton clusterSingleton =
          ClusterSingleton.get(ctx.getSystem());

      ActorRef<IndexWriterActor.Command> indexWriterActorRef =
          clusterSingleton.init(
              SingletonActor.of(
                  IndexWriterActor.create(), "index-writer")
          );

      Buffer buffer = Json.encodeToBuffer(dataPayload);

      indexWriterActorRef.tell(
          new IndexWriterActor.Start(
              datasource.getDataIndex(),
              buffer.getBytes(), responseActorRef)
      );

      return Behaviors.receive(Command.class)
          .onMessage(
              IndexWriterResponseWrapper.class,
              indexWriterResponseWrapper -> {

                IndexWriterActor.Response response =
                    indexWriterResponseWrapper.response();

                if (response instanceof IndexWriterActor.Success) {
                  replyTo.tell(Success.INSTANCE);
                }
                else if (response instanceof IndexWriterActor.Failure) {
                  replyTo.tell(new Failure(((IndexWriterActor.Failure) response).exception()));
                }

                return Behaviors.stopped();
              })
          .build();

    }

    EnrichPipelineItem enrichPipelineItem = Collections.head(enrichPipelineItems);
    Set<EnrichPipelineItem> tail = Collections.tail(enrichPipelineItems);

    EnrichItem enrichItem = enrichPipelineItem.getEnrichItem();

    logger.info("start enrich for enrichItem with id {}", enrichItem.getId());

    String jsonPath = enrichItem.getJsonPath();
    EnrichItem.BehaviorMergeType behaviorMergeType = enrichItem.getBehaviorMergeType();

    ActorRef<EnrichItemSupervisor.Command> enrichItemSupervisorRef =
        ctx.spawnAnonymous(EnrichItemSupervisor.create(supervisorActorRef));

    Long requestTimeout = enrichItem.getRequestTimeout();

    LocalDateTime expiredDate =
        LocalDateTime
            .now()
            .plus(requestTimeout, ChronoUnit.MILLIS);

    ctx.ask(
        EnrichItemSupervisor.Response.class,
        enrichItemSupervisorRef,
        Duration.ofMillis(requestTimeout),
        enrichItemReplyTo ->
            new EnrichItemSupervisor.Execute(
                enrichItem, dataPayload, expiredDate, enrichItemReplyTo),
        (r, t) -> {
          if (t != null) {
            return new EnrichItemError(enrichItem, t);
          }
          else if (r instanceof EnrichItemSupervisor.Error) {
            EnrichItemSupervisor.Error error =(EnrichItemSupervisor.Error)r;
            return new EnrichItemError(enrichItem, new RuntimeException(error.error()));
          }
          else {
            return new EnrichItemSupervisorResponseWrapper(r);
          }
        }
    );

    return Behaviors.receive(Command.class)
        .onMessage(EnrichItemError.class, param -> {

          EnrichItem enrichItemError = param.enrichItem();

          EnrichItem.BehaviorOnError behaviorOnError =
              enrichItem.getBehaviorOnError();

          switch (behaviorOnError) {
            case SKIP -> {

              logger.error(
                  "behaviorOnError is SKIP, call next enrichItem: " + enrichItemError.getId(), param.exception);

              if (!tail.isEmpty()) {
                ctx.getLog().info("call next enrichItem");
              }

              return initPipeline(
                  ctx, supervisorActorRef, responseActorRef, replyTo,
                  dataPayload, datasource, tail);

            }
            case FAIL -> {

              logger.info(
                  "behaviorOnError is FAIL, stop pipeline: " + enrichItemError.getId(), param.exception);

              Throwable throwable = param.exception;

              ctx.getSelf().tell(
                  new InternalError(throwable.getMessage()));

              return Behaviors.same();
            }
            case REJECT -> {

              logger.error(
                  "behaviorOnError is REJECT, stop pipeline: " + enrichItemError.getId(), param.exception);

              replyTo.tell(Success.INSTANCE);

              return Behaviors.stopped();
            }
            default -> {

              ctx.getSelf().tell(
                  new InternalError(
                      "behaviorOnError is not valid: " + behaviorOnError));

              return Behaviors.same();

            }
          }

        })
        .onMessage(EnrichItemSupervisorResponseWrapper.class, garw -> {
          EnrichItemSupervisor.Response response = garw.response;

          if (response instanceof EnrichItemSupervisor.Body) {
            EnrichItemSupervisor.Body body =(EnrichItemSupervisor.Body)response;
            ctx.getSelf().tell(new InternalResponseWrapper(body.body()));
          }
          else {
            EnrichItemSupervisor.Error error =(EnrichItemSupervisor.Error)response;
            ctx.getSelf().tell(new InternalError(error.error()));
          }

          return Behaviors.same();

        })
        .onMessage(InternalResponseWrapper.class, srw -> {

          JsonObject result = new JsonObject(new String(srw.jsonObject()));

          logger.info("enrichItem: " + enrichItem.getId() + " OK ");

          if (!tail.isEmpty()) {
            logger.info("call next enrichItem");
          }

          JsonObject newJsonPayload = result.getJsonObject("payload");

          if (newJsonPayload == null) {
            newJsonPayload = result;
          }

          DataPayload newDataPayload =
              mergeResponse(
                  jsonPath, behaviorMergeType, dataPayload,
                  newJsonPayload.mapTo(DataPayload.class));

          return initPipeline(
              ctx, supervisorActorRef,
              responseActorRef, replyTo, newDataPayload,
              datasource, tail);

        })
        .onMessage(InternalError.class, srw -> {

          String error = srw.error();

          logger.error("enrichItem: " + enrichItem.getId() + " occurred error: " + error);
          logger.error("terminating pipeline");
          replyTo.tell(new Failure(new RuntimeException(error)));

          return Behaviors.stopped();

        })
        .build();

  }

  private static DataPayload mergeResponse(
    String jsonPath, EnrichItem.BehaviorMergeType behaviorMergeType,
    DataPayload prevDataPayload, DataPayload newDataPayload) {

    JsonObject prevJsonObject = new JsonObject(new LinkedHashMap<>(prevDataPayload.getRest()));
    JsonObject newJsonObject = new JsonObject(new LinkedHashMap<>(newDataPayload.getRest()));

    if (jsonPath == null || jsonPath.isBlank()) {
      jsonPath = "$";
    }

    if (behaviorMergeType == null) {
      behaviorMergeType = EnrichItem.BehaviorMergeType.REPLACE;
    }

    JsonMerge jsonMerge = JsonMerge.of(
      behaviorMergeType == EnrichItem.BehaviorMergeType.REPLACE,
      prevJsonObject, newJsonObject);

    return prevDataPayload.rest(jsonMerge.merge(jsonPath).getMap());

  }

}
