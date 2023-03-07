package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.common.util.VertxUtil;
import io.openk9.common.util.collection.Collections;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipelineItem;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.datasource.util.JsonMerge;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;

import javax.enterprise.inject.spi.CDI;
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
	private record SupervisorResponseWrapper(
		Supervisor.Response response) implements Command {}
	private record IndexWriterResponseWrapper(
		IndexWriterActor.Response response) implements Command {}
	private record GroovyActorResponseWrapper(
		GroovyActor.Response response) implements Command {}
	private record InternalResponseWrapper(JsonObject jsonObject) implements Command {}
	private record InternalError(String error) implements Command {}
	public sealed interface Response {}
	public enum Success implements Response {INSTANCE}
	public record Failure(Throwable exception) implements Response {}


	public static Behavior<Command> create(
		DataPayload dataPayload,
		ActorRef<Supervisor.Command> supervisorActorRef,
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
		ActorRef<Supervisor.Command> supervisorActorRef,
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
				ctx, supervisorActorRef, responseActorRef, replyTo, dataPayload, datasourceModel))
			.build();
	}

	private static Behavior<Command> onDatasourceModel(
		ActorContext<Command> ctx,
		ActorRef<Supervisor.Command> supervisorActorRef,
		ActorRef<IndexWriterActor.Response> responseActorRef,
		ActorRef<Response> replyTo, DataPayload dataPayload,
		DatasourceModel datasourceModel) {

		io.openk9.datasource.model.Datasource datasource =
			datasourceModel.datasource;

		DataIndex dataIndex = datasource.getDataIndex();

		Logger log = ctx.getLog();

		if (dataIndex == null) {
			log.info("datasource with id {} has no dataIndex, start random creation", datasource.getId());
			ctx.getSelf().tell(new CreateRandomDataIndex(datasource));
			return Behaviors.same();
		}

		Set<EnrichPipelineItem> enrichPipelineItems =
			datasource.getEnrichPipeline().getEnrichPipelineItems();

		log.info("start pipeline for datasource with id {}", datasource.getId());

		return initPipeline(
			ctx, supervisorActorRef, responseActorRef, replyTo, dataPayload,
			datasourceModel, enrichPipelineItems);
	}

	private static Behavior<Command> initPipeline(
		ActorContext<Command> ctx,
		ActorRef<Supervisor.Command> supervisorActorRef,
		ActorRef<IndexWriterActor.Response> responseActorRef,
		ActorRef<Response> replyTo, DataPayload dataPayload,
		DatasourceModel datasourceModel, Set<EnrichPipelineItem> enrichPipelineItems) {

		Logger logger = ctx.getLog();

		if (enrichPipelineItems.isEmpty()) {

			logger.info("pipeline is empty, start index writer");

			String contentId = dataPayload.getContentId();

			ActorRef<IndexWriterActor.Command> indexWriterActorRef =
				ctx.spawn(
					IndexWriterActor.create(
						datasourceModel.datasource.getDataIndex(),
						dataPayload, responseActorRef),
					"index-writer-" + contentId);

			indexWriterActorRef.tell(IndexWriterActor.Start.INSTANCE);

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

		String serviceName = enrichItem.getServiceName();
		String jsonConfig = enrichItem.getJsonConfig();
		EnrichItem.EnrichItemType type = enrichItem.getType();
		String validationScript = enrichItem.getValidationScript();
		String jsonPath = enrichItem.getJsonPath();
		EnrichItem.BehaviorMergeType behaviorMergeType = enrichItem.getBehaviorMergeType();

		JsonObject enrichItemConfig =
			jsonConfig == null || jsonConfig.isBlank()
				? new JsonObject()
				: new JsonObject(jsonConfig);

		JsonObject payload = JsonObject.of(
			"payload", dataPayload,
			"enrichItemConfig", enrichItemConfig
		);

		if (type == EnrichItem.EnrichItemType.HTTP_ASYNC || type == EnrichItem.EnrichItemType.HTTP_SYNC) {

			boolean async = type == EnrichItem.EnrichItemType.HTTP_ASYNC;

			ActorRef<Supervisor.Response> supervisorAdapter =
				ctx.messageAdapter(
					Supervisor.Response.class,
					SupervisorResponseWrapper::new);

			supervisorActorRef.tell(
				new Supervisor.Call(async, serviceName, payload, supervisorAdapter));

		}
		else if (type == EnrichItem.EnrichItemType.GROOVY_SCRIPT) {

			ActorRef<GroovyActor.Command> groovyActorRef =
				ctx.spawnAnonymous(GroovyActor.create());

			ActorRef<GroovyActor.Response> groovyActorAdapter =
				ctx.messageAdapter(
					GroovyActor.Response.class,
					GroovyActorResponseWrapper::new);

			groovyActorRef.tell(
				new GroovyActor.Execute(
					dataPayload, validationScript, groovyActorAdapter));

		}
		else {
			throw new RuntimeException("invalid type: " + type);
		}

		return Behaviors.receive(Command.class)
			.onMessage(GroovyActorResponseWrapper.class, garw -> {
				GroovyActor.Response response = garw.response;

				if (response instanceof GroovyActor.GroovyResponse) {
					GroovyActor.GroovyResponse groovyResponse =(GroovyActor.GroovyResponse)response;
					ctx.getSelf().tell(new InternalResponseWrapper(groovyResponse.response()));
				}
				else {
					GroovyActor.GroovyError groovyError = (GroovyActor.GroovyError) response;
					ctx.getSelf().tell(new InternalError(groovyError.error()));
				}

				return Behaviors.same();

			})
			.onMessage(SupervisorResponseWrapper.class, srw -> {
				Supervisor.Response response = srw.response;

				if (response instanceof Supervisor.Body) {
					Supervisor.Body body = (Supervisor.Body) response;
					ctx.getSelf().tell(new InternalResponseWrapper(body.jsonObject()));
				}
				else {
					Supervisor.Error error = (Supervisor.Error) response;
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

		VertxUtil.runOnContext(() ->
			transactionInvoker
				.withTransaction(tenantId, s -> s.merge(datasource))
				.invoke(d -> ctx.getSelf().tell(new DatasourceModel(d)))
		);

		return Behaviors.same();
	}

	private static Behavior<Command> onCreatePipelineContext(
		ActorContext<Command> ctx, String tenantId, long datasourceId,
		TransactionInvoker transactionInvoker) {

		VertxUtil.runOnContext(() ->
			transactionInvoker.withStatelessTransaction(tenantId, s ->
				s.createQuery(
					"select d from Datasource d " +
					"left join fetch d.dataIndex di " +
					"join fetch d.enrichPipeline ep " +
					"join fetch ep.enrichPipelineItems epi " +
					"join fetch epi.enrichItem ei " +
					"where d.id = :datasourceId ", io.openk9.datasource.model.Datasource.class)
					.setParameter("datasourceId", datasourceId)
					.getSingleResult()
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
