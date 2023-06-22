package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.ScheduleId;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.SchedulerManager;
import io.openk9.datasource.pipeline.actor.dto.GetDatasourceDTO;
import io.openk9.datasource.pipeline.actor.dto.GetEnrichItemDTO;
import io.openk9.datasource.pipeline.actor.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.actor.enrichitem.EnrichItemSupervisor;
import io.openk9.datasource.pipeline.actor.enrichitem.HttpSupervisor;
import io.openk9.datasource.pipeline.actor.mapper.PipelineMapper;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.sql.TransactionInvoker;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.enterprise.inject.spi.CDI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IngestionActor {
	public sealed interface Command {}
	public record IngestionMessage(DataPayload dataPayload, Message<?> message) implements Command {}
	public record Callback(String tokenId, byte[] body) implements Command {}
	private record DatasourceResponseWrapper(Message<?> message, DataPayload dataPayload, Datasource.Response response) implements Command {}
	private record EnrichItemResponseWrapper(EnrichItemActor.EnrichItemCallbackResponse response, Map<String, Object> datasourcePayload, ActorRef<Response> replyTo) implements Command {}
	private record SupervisorResponseWrapper(EnrichItemSupervisor.Response response, ActorRef<Response> replyTo) implements Command {}
	public record EnrichItemCallback(long enrichItemId, String tenantId, Map<String, Object> datasourcePayload, ActorRef<Response> replyTo) implements Command {}
	private record InitPipeline(Message<?> message, DataPayload dataPayload, GetDatasourceDTO datasource) implements Command {}
	private record CreateEnrichPipeline(Message<?> message, DataPayload dataPayload, GetDatasourceDTO datasource, SchedulerDTO scheduler) implements Command {}
	private record EnrichPipelineResponseWrapper(Message<?> message, EnrichPipeline.Response response) implements Command {}
	public sealed interface Response {}
	public record EnrichItemCallbackResponse(byte[] jsonObject) implements Response {}
	public record EnrichItemCallbackError(String message) implements Response {}

	public static Behavior<Command> create(PipelineMapper pipelineMapper) {
		return Behaviors
			.supervise(Behaviors.<Command>setup(ctx -> {

				ActorRef<HttpSupervisor.Command> supervisorActorRef =
					ctx.spawn(
						HttpSupervisor.create(),
						"enrich-pipeline-supervisor");

				ActorRef<EnrichItemActor.Command> enrichItemActorRef =
					ctx.spawn(
						EnrichItemActor.create(),
						"enrich-item-actor");

				ClusterSingleton clusterSingleton = ClusterSingleton.get(ctx.getSystem());

				ActorRef<Datasource.Command> datasourceActorRef =
					clusterSingleton.init(SingletonActor.of(Datasource.create(pipelineMapper), "datasource"));

				TransactionInvoker transactionInvoker = CDI.current().select(TransactionInvoker.class).get();

				ActorRef<SchedulerManager.Command> schedulerManagerActorRef =
					ctx.spawn(SchedulerManager.create(transactionInvoker, datasourceActorRef), "scheduler-manager");

				return initial(
					ctx, transactionInvoker, datasourceActorRef, pipelineMapper,
					supervisorActorRef, enrichItemActorRef, schedulerManagerActorRef, new ArrayList<>());
			}))
			.onFailure(SupervisorStrategy.restartWithBackoff(
				Duration.ofMillis(500), Duration.ofSeconds(5), 0.1));
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx,
		TransactionInvoker transactionInvoker,
		ActorRef<Datasource.Command> datasourceActorRef,
		PipelineMapper pipelineMapper,
		ActorRef<HttpSupervisor.Command> supervisorActorRef,
		ActorRef<EnrichItemActor.Command> enrichItemActorRef,
		ActorRef<SchedulerManager.Command> schedulerManagerActorRef,
		List<Message<?>> messages) {

		return Behaviors.receive(Command.class)
			.onMessage(IngestionMessage.class, ingestionMessage -> {

				DataPayload dataPayload = ingestionMessage.dataPayload;
				Message<?> message = ingestionMessage.message;
				String ingestionId = dataPayload.getIngestionId();

				String contentId = dataPayload.getContentId();

				if (contentId != null) {

					ctx.getLog().info(
						"read message ingestionId: {}, contentId: {}",
						ingestionId, contentId);

					ActorRef<Datasource.Response> responseActorRef =
						ctx.messageAdapter(
							Datasource.Response.class,
							response -> new DatasourceResponseWrapper(message, dataPayload, response)
						);

					datasourceActorRef.tell(new Datasource.GetDatasource(
						dataPayload.getTenantId(), dataPayload.getDatasourceId(), dataPayload.getParsingDate(),
						responseActorRef));

					List<Message<?>> newMessages = new ArrayList<>(messages);
					newMessages.add(message);

					return initial(
						ctx, transactionInvoker, datasourceActorRef, pipelineMapper, supervisorActorRef, enrichItemActorRef, schedulerManagerActorRef, newMessages);

				}
				else {

					ctx
						.getLog()
						.info(
							"contentId is null for scheduleId {} and tenantId {}",
							dataPayload.getScheduleId(), dataPayload.getTenantId());

					schedulerManagerActorRef.tell(
						new SchedulerManager.LastMessage(
							dataPayload.getScheduleId(),
							dataPayload.getTenantId()));

					message.ack();

					return Behaviors.same();

				}


			})
			.onMessage(DatasourceResponseWrapper.class, drw -> {

				Datasource.Response response = drw.response;
				Message<?> message = drw.message;
				DataPayload dataPayload = drw.dataPayload;

				if (response instanceof Datasource.GetDatasourceSuccess) {
					GetDatasourceDTO getDatasourceDTO = ((Datasource.GetDatasourceSuccess) response).datasource();
					ctx.getSelf().tell(new InitPipeline(message, dataPayload, getDatasourceDTO));
				}
				else if (response instanceof Datasource.Failure) {
					Throwable exception = ((Datasource.Failure) response).exception();
					ctx.getLog().error("get datasource failure, nack message", exception);
					message.nack(exception);
					List<Message<?>> newMessages = new ArrayList<>(messages);
					newMessages.remove(message);

					return initial(
						ctx, transactionInvoker, datasourceActorRef, pipelineMapper,
						supervisorActorRef,	enrichItemActorRef, schedulerManagerActorRef, newMessages);
				}
				return Behaviors.same();

			})
			.onMessage(InitPipeline.class, ip -> onInitPipeline(ctx, transactionInvoker, pipelineMapper, ip))
			.onMessage(CreateEnrichPipeline.class, cep -> onCreateEnrichPipeline(ctx, supervisorActorRef, cep))
			.onMessage(EnrichPipelineResponseWrapper.class, eprw ->
				onEnrichPipelineResponseWrapper(
					ctx, transactionInvoker, datasourceActorRef, pipelineMapper, eprw,
					supervisorActorRef, enrichItemActorRef, schedulerManagerActorRef, messages))
			.onMessage(Callback.class, callback -> {

				ctx.getLog().info("callback with tokenId: {}", callback.tokenId());

				supervisorActorRef.tell(
					new HttpSupervisor.Callback(
						callback.tokenId(), callback.body()));

				return Behaviors.same();

			})
			.onMessage(EnrichItemResponseWrapper.class, eirw -> onEnrichItemResponseWrapper(ctx, pipelineMapper, supervisorActorRef, eirw))
			.onMessage(EnrichItemCallback.class, eic -> onEnrichItemCallback(ctx, enrichItemActorRef, eic))
			.onMessage(SupervisorResponseWrapper.class, srw -> onSupervisorResponseWrapper(ctx, srw))
			.onSignal(PreRestart.class, signal -> onSignal(ctx, "ingestion actor restarting", messages))
			.onSignal(PostStop.class, signal -> onSignal(ctx, "ingestion actor stopped", messages))
			.build();
	}

	private static Behavior<Command> onEnrichPipelineResponseWrapper(
		ActorContext<Command> ctx, TransactionInvoker transactionInvoker,
		ActorRef<Datasource.Command> datasourceActorRef,
		PipelineMapper pipelineMapper, EnrichPipelineResponseWrapper eprw,
		ActorRef<HttpSupervisor.Command> supervisorActorRef,
		ActorRef<EnrichItemActor.Command> enrichItemActorRef,
		ActorRef<SchedulerManager.Command> schedulerManagerActorRef,
		List<Message<?>> messages) {

		EnrichPipeline.Response response = eprw.response;
		Message<?> message = eprw.message;

		schedulerManagerActorRef.tell(new SchedulerManager.Ping(response.scheduleId(), response.tenantId()));

		if (response instanceof EnrichPipeline.Success) {
			ctx.getLog().info("enrich pipeline success, ack message");
			if (response instanceof EnrichPipeline.LastMessage) {
				ctx.getLog().info("last message processed on pipeline");

				EnrichPipeline.LastMessage lastMessage = (EnrichPipeline.LastMessage) response;

				schedulerManagerActorRef.tell(new SchedulerManager.LastMessage(lastMessage.scheduleId(), lastMessage.tenantId()));
			}
			message.ack();
		}
		else if (response instanceof EnrichPipeline.Failure) {
			Throwable exception =
				((EnrichPipeline.Failure) response).exception();
			ctx.getLog().error(
				"enrich pipeline failure, nack message", exception);
			message.nack(exception);
		}

		List<Message<?>> newMessages = new ArrayList<>(messages);
		newMessages.remove(message);

		return initial(
			ctx, transactionInvoker, datasourceActorRef, pipelineMapper, supervisorActorRef,
			enrichItemActorRef, schedulerManagerActorRef, newMessages);
	}

	private static Behavior<Command> onInitPipeline(
		ActorContext<Command> ctx, TransactionInvoker transactionInvoker, PipelineMapper pipelineMapper, InitPipeline initPipeline) {

		Message<?> message = initPipeline.message;
		DataPayload dataPayload = initPipeline.dataPayload;
		GetDatasourceDTO datasource = initPipeline.datasource;

		ScheduleId scheduleId = new ScheduleId(UUID.fromString(dataPayload.getScheduleId()));
		VertxUtil.runOnContext(() -> transactionInvoker
			.withStatelessTransaction(dataPayload.getTenantId(), s -> s
				.createQuery("select s " +
					"from Scheduler s " +
					"join fetch s.datasource " +
					"left join fetch s.oldDataIndex " +
					"left join fetch s.newDataIndex " +
					"where s.scheduleId = :scheduleId", Scheduler.class)
				.setParameter("scheduleId", scheduleId)
				.getSingleResult()
				.map(pipelineMapper::map)
				.invoke(scheduler ->
					ctx.getSelf().tell(new CreateEnrichPipeline(message, dataPayload, datasource, scheduler))
				)
			)
		);

		return Behaviors.same();
	}

	private static Behavior<Command> onCreateEnrichPipeline(
		ActorContext<Command> ctx, ActorRef<HttpSupervisor.Command> supervisorActorRef,
		CreateEnrichPipeline cep) {

		Message<?> message = cep.message;
		DataPayload dataPayload = cep.dataPayload;
		GetDatasourceDTO datasource = cep.datasource;
		SchedulerDTO scheduler = cep.scheduler;

		String newDataIndexName = scheduler.getNewDataIndexName();

		if (newDataIndexName == null) {
			newDataIndexName = scheduler.getOldDataIndexName();
		}

		dataPayload.setIndexName(newDataIndexName);

		ActorRef<EnrichPipeline.Response> responseActorRef =
			ctx.messageAdapter(EnrichPipeline.Response.class, response ->
				new EnrichPipelineResponseWrapper(message, response));

		ActorRef<EnrichPipeline.Command> enrichPipelineActorRef = ctx.spawn(
			EnrichPipeline.create(
				supervisorActorRef, responseActorRef, dataPayload, datasource, scheduler),
			"enrich-pipeline");

		enrichPipelineActorRef.tell(EnrichPipeline.Start.INSTANCE);

		return Behaviors.same();
	}

	private static Behavior<Command> onSupervisorResponseWrapper(
		ActorContext<Command> ctx, SupervisorResponseWrapper srw) {

		EnrichItemSupervisor.Response response = srw.response;
		ActorRef<Response> replyTo = srw.replyTo;

		if (response instanceof EnrichItemSupervisor.Body) {
			replyTo.tell(new EnrichItemCallbackResponse(((EnrichItemSupervisor.Body)response).body()));
		}
		else {
			replyTo.tell(new EnrichItemCallbackError(((EnrichItemSupervisor.Error)response).error()));
		}

		return Behaviors.same();

	}

	private static Behavior<Command> onEnrichItemResponseWrapper(
		ActorContext<Command> ctx,
		PipelineMapper pipelineMapper,
		ActorRef<HttpSupervisor.Command> supervisorActorRef,
		EnrichItemResponseWrapper eirw) {

		ActorRef<Response> replyTo = eirw.replyTo;
		EnrichItemActor.EnrichItemCallbackResponse response = eirw.response;
		Map<String, Object> datasourcePayload = eirw.datasourcePayload;

		EnrichItem enrichItemEntity = response.enrichItem();
		GetEnrichItemDTO enrichItem = pipelineMapper.map(enrichItemEntity);

		JsonObject datasourcePayloadJson = new JsonObject(datasourcePayload);

		DataPayload dataPayload =
			DataPayload
				.builder()
				.acl(Map.of())
				.rawContent(datasourcePayloadJson.toString())
				.parsingDate(Instant.now().toEpochMilli())
				.documentTypes(
					datasourcePayloadJson
						.fieldNames()
						.toArray(new String[0])
				)
				.rest(datasourcePayload)
				.build();

		ActorRef<EnrichItemSupervisor.Response> responseActorRef =
			ctx.messageAdapter(
				EnrichItemSupervisor.Response.class,
				param -> new SupervisorResponseWrapper(param, replyTo)
			);

		ActorRef<EnrichItemSupervisor.Command> enrichItemActorRef =
			ctx.spawnAnonymous(EnrichItemSupervisor.create(supervisorActorRef));

		Long requestTimeout = enrichItem.getRequestTimeout();

		requestTimeout = requestTimeout != null && requestTimeout > 0 ? requestTimeout : 30_000;

		LocalDateTime expiredDate =
			LocalDateTime.now().plusSeconds(requestTimeout);

		enrichItemActorRef.tell(
			new EnrichItemSupervisor.Execute(
				enrichItem, dataPayload, expiredDate, responseActorRef
			)
		);

		return Behaviors.same();
	}

	private static Behavior<Command> onEnrichItemCallback(
		ActorContext<Command> ctx,
		ActorRef<EnrichItemActor.Command> enrichItemActorRef,
		EnrichItemCallback eic) {

		Map<String, Object> datasourcePayload = eic.datasourcePayload;

		ActorRef<EnrichItemActor.EnrichItemCallbackResponse> enrichItemCallbackResponseActorRef =
			ctx.messageAdapter(
				EnrichItemActor.EnrichItemCallbackResponse.class,
				param -> new EnrichItemResponseWrapper(param, datasourcePayload, eic.replyTo)
			);

		enrichItemActorRef.tell(
			new EnrichItemActor.EnrichItemCallback(
				eic.enrichItemId, eic.tenantId, enrichItemCallbackResponseActorRef));

		return Behaviors.same();

	}

	private static Behavior<Command> onSignal(
		ActorContext<Command> ctx, String signalMessage, List<Message<?>> messages) {

		ctx.getLog().error(signalMessage);

		RuntimeException runtimeException = new RuntimeException(signalMessage);

		for (Message<?> message : messages) {
			message.nack(runtimeException);
		}

		return Behaviors.same();
	}

}
