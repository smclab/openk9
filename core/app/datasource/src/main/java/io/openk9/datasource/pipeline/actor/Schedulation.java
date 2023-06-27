package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.ScheduleId;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.Scheduler_;
import io.openk9.datasource.pipeline.SchedulationKeyUtils;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.datasource.util.CborSerializable;
import io.quarkus.runtime.util.ExceptionUtil;

import javax.persistence.criteria.CriteriaUpdate;
import java.util.ArrayDeque;
import java.util.Deque;

public class Schedulation extends AbstractBehavior<Schedulation.Command> {

	public static final EntityTypeKey<Command> ENTITY_TYPE_KEY =
		EntityTypeKey.create(Command.class, "schedulation");

	public sealed interface Command extends CborSerializable {}
	public enum Start implements Command {INSTANCE}
	public enum SetDataIndex implements Command {INSTANCE}
	public enum SetStatusFinished implements Command {INSTANCE}
	public record Ingest(DataPayload payload, ActorRef<Response> replyTo) implements Command {}
	private record SetScheduler(Scheduler scheduler) implements Command {}
	private record EnrichPipelineResponseWrapper(EnrichPipeline.Response response) implements Command {}

	public sealed interface Response extends CborSerializable {}
	public record Success() implements Response {}
	public record Failure(String error) implements Response {}

	public record SchedulationKey(String tenantId, ScheduleId scheduleId) {
		public String value() {
			return SchedulationKeyUtils.getValue(this);
		}
	}

	private final SchedulationKey key;
	private final TransactionInvoker txInvoker;
	private final DatasourceService datasourceService;
	private final Deque<Command> lag = new ArrayDeque<>();
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
		context.getSelf().tell(Start.INSTANCE);
	}

	public static Behavior<Command> create(
		SchedulationKey schedulationKey, TransactionInvoker txInvoker,
		DatasourceService datasourceService) {

		return Behaviors.setup(ctx -> new Schedulation(
			ctx, schedulationKey, txInvoker, datasourceService));
	}


	@Override
	public Receive<Command> createReceive() {
		return init();
	}

	private Receive<Command> init() {
		return newReceiveBuilder()
			.onMessageEquals(Start.INSTANCE, this::onStart)
			.onMessage(SetScheduler.class, this::onSetScheduler)
			.build();
	}

	private Receive<Command> ready() {
		return newReceiveBuilder()
			.onMessage(Ingest.class, this::onIngestReady)
			.build();
	}

	private Receive<Command> busy() {
		return newReceiveBuilder()
			.onMessage(
				EnrichPipelineResponseWrapper.class, this::onEnrichPipelineResponse)
			.onAnyMessage(this::onBusy)
			.build();
	}

	private Receive<Command> finish() {
		return newReceiveBuilder()
			.onMessageEquals(SetDataIndex.INSTANCE, this::onSetDataIndex)
			.onMessageEquals(SetStatusFinished.INSTANCE, this::onSetStatusFinished)
			.build();
	}

	private Behavior<Command> next() {
		if (currentIngest.payload.isLast()) {
			getContext().getSelf().tell(SetDataIndex.INSTANCE);
			return finish();
		}

		currentIngest = null;

		if (!lag.isEmpty()) {
			Command command = lag.pop();
			getContext().getSelf().tell(command);
		}

		return ready();
	}

	private Behavior<Command> onStart() {
		VertxUtil.runOnContext(() -> txInvoker
			.withStatelessTransaction(key.tenantId, s -> s
				.createQuery("select s " +
					"from Scheduler s " +
					"join fetch s.datasource d" +
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
		return this.ready();
	}

	private Behavior<Command> onIngestReady(Ingest ingest) {
		this.currentIngest = ingest;

		String indexName = getIndexName();

		DataPayload dataPayload = ingest.payload;

		dataPayload.setIndexName(indexName);

		ActorRef<EnrichPipeline.Response> responseActorRef = getContext()
			.messageAdapter(EnrichPipeline.Response.class, EnrichPipelineResponseWrapper::new);

		ActorRef<EnrichPipeline.Command> enrichPipelineActorRef = getContext().spawnAnonymous(
			EnrichPipeline.create(key, responseActorRef, dataPayload, scheduler));

		enrichPipelineActorRef.tell(EnrichPipeline.Start.INSTANCE);

		return this.busy();
	}

	private Behavior<Command> onBusy(Command ingest) {
		this.lag.add(ingest);
		getContext().getLog().info("There are {} commands waiting", lag.size());
		return Behaviors.same();
	}

	private Behavior<Command> onEnrichPipelineResponse(EnrichPipelineResponseWrapper eprw) {
		EnrichPipeline.Response response = eprw.response;

		if (response instanceof EnrichPipeline.Success) {
			getContext().getLog().info("enrich pipeline success");
			currentIngest.replyTo.tell(new Success());
		}
		else if (response instanceof EnrichPipeline.Failure) {
			Throwable exception = ((EnrichPipeline.Failure) response).exception();
			getContext().getLog().error("enrich pipeline failure", exception);
			currentIngest.replyTo.tell(new Failure(ExceptionUtil.generateStackTrace(exception)));
		}

		return next();
	}

	private Behavior<Command> onSetDataIndex() {
		DataIndex newDataIndex = scheduler.getNewDataIndex();
		io.openk9.datasource.model.Datasource datasource = scheduler.getDatasource();

		if (newDataIndex != null) {
			Long newDataIndexId = newDataIndex.getId();
			Long datasourceId = datasource.getId();
			String tenantId = key.tenantId;

			getContext().getLog().info(
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

		getContext().getLog().info("Stopping " + key);

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
}
