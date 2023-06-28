package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.Scheduler_;
import io.openk9.datasource.pipeline.SchedulationKeyUtils;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.datasource.util.CborSerializable;
import io.quarkus.runtime.util.ExceptionUtil;
import org.slf4j.Logger;

import javax.persistence.criteria.CriteriaUpdate;
import java.util.ArrayDeque;
import java.util.Deque;

public class Schedulation extends AbstractBehavior<Schedulation.Command> {

	public static final EntityTypeKey<Command> ENTITY_TYPE_KEY =
		EntityTypeKey.create(Command.class, "schedulation");

	private static final String INIT_BEHAVIOR = "Init";
	private static final String READY_BEHAVIOR = "Ready";
	private static final String BUSY_BEHAVIOR = "Busy";
	private static final String NEXT_BEHAVIOR = "Next";
	private static final String FINISH_BEHAVIOR = "Finish";
	private static final String STOPPED_BEHAVIOR = "Stopped";

	public sealed interface Command extends CborSerializable {}
	public enum Start implements Command {INSTANCE}
	public enum SetDataIndex implements Command {INSTANCE}
	public enum SetStatusFinished implements Command {INSTANCE}
	public record Ingest(DataPayload payload, ActorRef<Response> replyTo) implements Command {}
	private record SetScheduler(Scheduler scheduler) implements Command {}
	private record EnrichPipelineResponseWrapper(EnrichPipeline.Response response) implements Command {}

	public sealed interface Response extends CborSerializable {}
	public enum Success implements Response {INSTANCE}
	public record Failure(String error) implements Response {}

	public record SchedulationKey(String tenantId, String scheduleId) {
		public String value() {
			return SchedulationKeyUtils.getValue(this);
		}
	}

	private final SchedulationKey key;
	private final TransactionInvoker txInvoker;
	private final DatasourceService datasourceService;
	private final Deque<Command> lag = new ArrayDeque<>();
	private final Logger log;
	private Ingest currentIngest;
	private Scheduler scheduler;

	public Schedulation(
		ActorContext<Command> context,
		SchedulationKey key,
		TransactionInvoker txInvoker,
		DatasourceService datasourceService) {

		super(context);
		this.key = key;
		this.txInvoker = txInvoker;
		this.datasourceService = datasourceService;
		this.log = context.getLog();
		context.getSelf().tell(Start.INSTANCE);
	}

	public static Behavior<Command> create(
		SchedulationKey schedulationKey, TransactionInvoker txInvoker,
		DatasourceService datasourceService) {

		return Behaviors
			.<Command>supervise(
				Behaviors.setup(ctx -> new Schedulation(
					ctx, schedulationKey, txInvoker, datasourceService)))
			.onFailure(SupervisorStrategy.restart());
	}


	@Override
	public Receive<Command> createReceive() {
		return init();
	}

	private Receive<Command> init() {
		logBehavior(INIT_BEHAVIOR);

		return newReceiveBuilder()
			.onMessageEquals(Start.INSTANCE, this::onStart)
			.onMessage(SetScheduler.class, this::onSetScheduler)
			.onAnyMessage(this::onBusy)
			.build();
	}

	private Receive<Command> ready() {
		logBehavior(READY_BEHAVIOR);

		return newReceiveBuilder()
			.onMessage(Ingest.class, this::onIngestReady)
			.build();
	}

	private Receive<Command> busy() {
		logBehavior(BUSY_BEHAVIOR);

		return newReceiveBuilder()
			.onMessage(
				EnrichPipelineResponseWrapper.class, this::onEnrichPipelineResponse)
			.onAnyMessage(this::onBusy)
			.build();
	}

	private Receive<Command> finish() {
		logBehavior(FINISH_BEHAVIOR);

		return newReceiveBuilder()
			.onMessageEquals(SetDataIndex.INSTANCE, this::onSetDataIndex)
			.onMessageEquals(SetStatusFinished.INSTANCE, this::onSetStatusFinished)
			.build();
	}

	private Behavior<Command> next() {
		logBehavior(NEXT_BEHAVIOR);

		if (currentIngest != null && currentIngest.payload.isLast()) {
			getContext().getSelf().tell(SetDataIndex.INSTANCE);
			return this.finish();
		}

		currentIngest = null;

		if (!lag.isEmpty()) {
			Command command = lag.pop();
			getContext().getSelf().tell(command);
		}

		return this.ready();
	}

	private Behavior<Command> onStart() {
		VertxUtil.runOnContext(() -> txInvoker
			.withStatelessTransaction(key.tenantId, s -> s
				.createQuery("select s " +
					"from Scheduler s " +
					"join fetch s.datasource d " +
					"left join fetch d.enrichPipeline ep " +
					"left join fetch ep.enrichPipelineItems epi " +
					"left join fetch epi.enrichItem " +
					"left join fetch s.oldDataIndex " +
					"left join fetch s.newDataIndex " +
					"where s.scheduleId = :scheduleId", Scheduler.class)
				.setParameter("scheduleId", key.scheduleId)
				.getSingleResult()
				.invoke(scheduler -> getContext().getSelf().tell(new SetScheduler(scheduler)))
			)
		);
		return Behaviors.same();
	}

	private Behavior<Command> onSetScheduler(SetScheduler setScheduler) {
		this.scheduler = setScheduler.scheduler;
		return this.next();
	}

	private Behavior<Command> onIngestReady(Ingest ingest) {
		this.currentIngest = ingest;

		String indexName = getIndexName();

		DataPayload dataPayload = ingest.payload;

		if (!dataPayload.isLast()) {
			dataPayload.setIndexName(indexName);

			ActorRef<EnrichPipeline.Response> responseActorRef = getContext()
				.messageAdapter(EnrichPipeline.Response.class, EnrichPipelineResponseWrapper::new);

			ActorRef<EnrichPipeline.Command> enrichPipelineActorRef = getContext()
				.spawn(
					EnrichPipeline.create(key, responseActorRef, dataPayload, scheduler),
					"enrich-pipeline");

			enrichPipelineActorRef.tell(EnrichPipeline.Start.INSTANCE);

			return this.busy();
		}
		else {
			currentIngest.replyTo.tell(Success.INSTANCE);

			getContext().getSelf().tell(SetDataIndex.INSTANCE);

			return this.finish();
		}
	}

	private Behavior<Command> onBusy(Command ingest) {
		this.lag.add(ingest);
		log.info("There are {} commands waiting", lag.size());
		return Behaviors.same();
	}

	private Behavior<Command> onEnrichPipelineResponse(EnrichPipelineResponseWrapper eprw) {
		EnrichPipeline.Response response = eprw.response;

		if (response instanceof EnrichPipeline.Success) {
			log.info("enrich pipeline success");
			currentIngest.replyTo.tell(Success.INSTANCE);
		}
		else if (response instanceof EnrichPipeline.Failure) {
			Throwable exception = ((EnrichPipeline.Failure) response).exception();
			log.error("enrich pipeline failure", exception);
			currentIngest.replyTo.tell(new Failure(ExceptionUtil.generateStackTrace(exception)));
		}

		return this.next();
	}

	private Behavior<Command> onSetDataIndex() {
		DataIndex newDataIndex = scheduler.getNewDataIndex();
		io.openk9.datasource.model.Datasource datasource = scheduler.getDatasource();

		if (newDataIndex != null) {
			Long newDataIndexId = newDataIndex.getId();
			Long datasourceId = datasource.getId();
			String tenantId = key.tenantId;

			log.info(
				"replacing dataindex {} for datasource {} on tenant {}",
				newDataIndexId, datasourceId, tenantId);
			VertxUtil.runOnContext(() -> txInvoker
				.withTransaction(
					tenantId,
					s -> datasourceService.setDataIndex(datasourceId, newDataIndexId)
				)
				.invoke(() -> getContext().getSelf().tell(SetStatusFinished.INSTANCE))
			);
		}
		else {
			getContext().getSelf().tell(SetStatusFinished.INSTANCE);
		}

		return Behaviors.same();
	}

	private Behavior<Command> onSetStatusFinished() {
		VertxUtil.runOnContext(() -> txInvoker.withTransaction(
			key.tenantId,
			s -> {
				CriteriaUpdate<Scheduler> criteriaUpdate =
					txInvoker.getCriteriaBuilder().createCriteriaUpdate(Scheduler.class);
				criteriaUpdate.from(Scheduler.class);
				criteriaUpdate.set(Scheduler_.status, Scheduler.SchedulerStatus.FINISHED);
				return s.createQuery(criteriaUpdate).executeUpdate();
			})
		);

		logBehavior(STOPPED_BEHAVIOR);

		return Behaviors.stopped();

	}

	private String getIndexName() {
		String indexName = null;
		DataIndex newDataIndex = scheduler.getNewDataIndex();

		if (newDataIndex != null) {
			indexName = newDataIndex.getName();
		}

		if (indexName == null) {
			indexName = scheduler.getOldDataIndex().getName();
		}
		return indexName;
	}

	private void logBehavior(String behavior) {
		log.info("Schedulation with key {} behavior is {}", key, behavior);
	}

}
