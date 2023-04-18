package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;
import io.openk9.common.util.VertxUtil;
import io.openk9.common.util.collection.Collections;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.EnrichPipelineItem;
import io.openk9.datasource.pipeline.actor.enrichitem.EnrichItemSupervisor;
import io.openk9.datasource.pipeline.actor.enrichitem.HttpSupervisor;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.datasource.util.JsonMerge;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.CDI;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class DatasourceActor {

	public sealed interface Command {}
	public enum Start implements Command {INSTANCE}
	private record DatasourceModel(
		io.openk9.datasource.model.Datasource datasource) implements Command {}
	private record DatasourceModelError(Throwable exception) implements Command {}
	private record CreateRandomDataIndex(
		io.openk9.datasource.model.Datasource datasource) implements Command {}
	private record IndexWriterResponseWrapper(
		IndexWriterActor.Response response) implements Command {}
	private record EnrichItemSupervisorResponseWrapper(
		EnrichItemSupervisor.Response response) implements Command {}
	private record EnrichItemError(EnrichItem enrichItem, Throwable exception) implements Command {}
	private record InternalResponseWrapper(JsonObject jsonObject) implements Command {}
	private record InternalError(String error) implements Command {}
	public sealed interface Response {}
	public enum Success implements Response {INSTANCE}
	public record Failure(Throwable exception) implements Response {}


	public static Behavior<Command> create(
		DataPayload dataPayload,
		ActorRef<HttpSupervisor.Command> supervisorActorRef,
		ActorRef<Response> replyTo) {
		return Behaviors.setup(ctx -> {

			ActorRef<IndexWriterActor.Response> responseActorRef =
				ctx.messageAdapter(
					IndexWriterActor.Response.class,
					IndexWriterResponseWrapper::new);

			TransactionInvoker transactionInvoker =
				CDI.current().select(TransactionInvoker.class).get();

			return initial(
				ctx, supervisorActorRef, responseActorRef, replyTo, dataPayload,
				transactionInvoker);
		});
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx,
		ActorRef<HttpSupervisor.Command> supervisorActorRef,
		ActorRef<IndexWriterActor.Response> responseActorRef,
		ActorRef<Response> replyTo, DataPayload dataPayload,
		TransactionInvoker transactionInvoker) {

		return Behaviors.receive(Command.class)
			.onMessageEquals(
				Start.INSTANCE,
				() ->
					onCreatePipelineContext(
						ctx, dataPayload.getTenantId(), dataPayload.getDatasourceId(),
						transactionInvoker))
			.onMessage(
				CreateRandomDataIndex.class,
				createRandomDataIndex -> onCreateRandomDataIndex(
					ctx, dataPayload.getTenantId(), transactionInvoker, createRandomDataIndex))
			.onMessage(DatasourceModelError.class, (dme) -> {

				Throwable exception = dme.exception;

				replyTo.tell(new Failure(exception));

				return Behaviors.stopped();

			})
			.onMessage(DatasourceModel.class, datasourceModel -> onDatasourceModel(
				ctx, supervisorActorRef, responseActorRef, replyTo, dataPayload, transactionInvoker,
				datasourceModel))
			.build();
	}

	private static Behavior<Command> onDatasourceModel(
		ActorContext<Command> ctx,
		ActorRef<HttpSupervisor.Command> supervisorActorRef,
		ActorRef<IndexWriterActor.Response> responseActorRef,
		ActorRef<Response> replyTo, DataPayload dataPayload,
		TransactionInvoker transactionInvoker, DatasourceModel datasourceModel) {

		io.openk9.datasource.model.Datasource datasource =
			datasourceModel.datasource;

		DataIndex dataIndex = datasource.getDataIndex();

		Logger log = ctx.getLog();

		if (dataIndex == null) {
			log.info("datasource with id {} has no dataIndex, start random creation", datasource.getId());
			ctx.getSelf().tell(new CreateRandomDataIndex(datasource));
			return Behaviors.same();
		}

		OffsetDateTime lastIngestionDate = datasource.getLastIngestionDate();
		long parsingDate = dataPayload.getParsingDate();

		OffsetDateTime offsetDateTime =
			OffsetDateTime.ofInstant(Instant.ofEpochMilli(parsingDate),
				ZoneOffset.UTC);

		if (lastIngestionDate == null || !lastIngestionDate.isEqual(offsetDateTime)) {
			log.info("update last ingestion date for datasource with id {}", datasource.getId());
			datasource.setLastIngestionDate(offsetDateTime);
			mergeDatasource(
				ctx.getSelf(), dataPayload.getTenantId(), transactionInvoker, datasource);
			return Behaviors.same();
		}

		dataPayload.setIndexName(dataIndex.getName());

		EnrichPipeline enrichPipeline = datasource.getEnrichPipeline();

		Set<EnrichPipelineItem> enrichPipelineItems;

		if (enrichPipeline == null) {
			enrichPipelineItems = Set.of();
		}
		else {
			enrichPipelineItems = enrichPipeline.getEnrichPipelineItems();
		}

		log.info("start pipeline for datasource with id {}", datasource.getId());

		return initPipeline(
			ctx, supervisorActorRef, responseActorRef, replyTo, dataPayload,
			datasourceModel, enrichPipelineItems);
	}

	private static Behavior<Command> initPipeline(
		ActorContext<Command> ctx,
		ActorRef<HttpSupervisor.Command> supervisorActorRef,
		ActorRef<IndexWriterActor.Response> responseActorRef,
		ActorRef<Response> replyTo, DataPayload dataPayload,
		DatasourceModel datasourceModel, Set<EnrichPipelineItem> enrichPipelineItems) {

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

			indexWriterActorRef.tell(
				new IndexWriterActor.Start(
					datasourceModel.datasource.getDataIndex(),
					dataPayload, responseActorRef)
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

		ctx.ask(
			EnrichItemSupervisor.Response.class,
			enrichItemSupervisorRef,
			Duration.ofMillis(requestTimeout),
			enrichItemReplyTo ->
				new EnrichItemSupervisor.Execute(
					enrichItem, dataPayload, enrichItemReplyTo),
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
							dataPayload, datasourceModel, tail);

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

				JsonObject result = srw.jsonObject();

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
					datasourceModel, tail);

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

	private static int normalize(String contentId) {
		return contentId.hashCode();
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

	private static Behavior<Command> onCreateRandomDataIndex(
		ActorContext<Command> ctx, String tenantId,
		TransactionInvoker transactionInvoker,
		CreateRandomDataIndex createRandomDataIndex) {

		io.openk9.datasource.model.Datasource datasource =
			createRandomDataIndex.datasource;

		String indexName = datasource.getId() + "-data-" + UUID.randomUUID();

		DataIndex dataIndex = DataIndex.of(
			indexName, "auto-generated",
			new LinkedHashSet<>());

		datasource.setDataIndex(dataIndex);

		ctx.getLog().info(
			"creating random dataIndex: {} for datasource: {}", indexName, datasource.getId());

		mergeDatasource(ctx.getSelf(), tenantId, transactionInvoker, datasource);

		return Behaviors.same();
	}

	private static void mergeDatasource(
		ActorRef<Command> self, String tenantId,
		TransactionInvoker transactionInvoker, Datasource datasource) {
		VertxUtil.runOnContext(() ->
			transactionInvoker
				.withTransaction(tenantId, s -> s.merge(datasource))
				.invoke(d -> self.tell(new DatasourceModel(d)))
		);
	}

	private static Behavior<Command> onCreatePipelineContext(
		ActorContext<Command> ctx, String tenantId, long datasourceId,
		TransactionInvoker transactionInvoker) {

		VertxUtil.runOnContext(() ->
			transactionInvoker.withStatelessTransaction(tenantId, s ->
				s.createQuery(
					"select d from Datasource d " +
					"left join fetch d.dataIndex di " +
					"left join fetch d.enrichPipeline ep " +
					"left join fetch ep.enrichPipelineItems epi " +
					"left join fetch epi.enrichItem ei " +
					"where d.id = :datasourceId ", io.openk9.datasource.model.Datasource.class)
					.setParameter("datasourceId", datasourceId)
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

		return Behaviors.same();
	}

}
