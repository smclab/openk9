package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.ReceiveBuilder;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;
import com.typesafe.config.Config;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.NotificationSender;
import io.openk9.datasource.pipeline.SchedulationKeyUtils;
import io.openk9.datasource.pipeline.actor.util.AbstractLoggerBehavior;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.datasource.util.CborSerializable;
import io.quarkus.runtime.util.ExceptionUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;

public class Schedulation extends AbstractLoggerBehavior<Schedulation.Command> {

	public static final EntityTypeKey<Command> ENTITY_TYPE_KEY =
		EntityTypeKey.create(Command.class, "schedulation");

	private static final String INIT_BEHAVIOR = "Init";
	private static final String READY_BEHAVIOR = "Ready";
	private static final String BUSY_BEHAVIOR = "Busy";
	private static final String NEXT_BEHAVIOR = "Next";
	private static final String FINISH_BEHAVIOR = "Finish";
	private static final String STOPPED_BEHAVIOR = "Stopped";

	public sealed interface Command extends CborSerializable {}
	public enum Cancel implements Command {INSTANCE}
	public record Ingest(byte[] payload, ActorRef<Response> replyTo) implements Command {}
	private enum PersistDataIndex implements Command {INSTANCE}
	private enum PersistStatusFinished implements Command {INSTANCE}
	private record PersistLastIngestionDate(OffsetDateTime lastIngestionDate) implements Command {}
	private record SetLastIngestionDate(OffsetDateTime lastIngestionDate) implements Command {}
	private record SetScheduler(Scheduler scheduler) implements Command {}
	private record EnrichPipelineResponseWrapper(EnrichPipeline.Response response) implements Command {}
	private record NotificationSenderResponseWrapper(NotificationSender.Response response) implements Command {}
	private enum Start implements Command {INSTANCE}
	private enum Tick implements Command {INSTANCE}

	public sealed interface Response extends CborSerializable {}
	public enum Success implements Response {INSTANCE}
	public record Failure(String error) implements Response {}

	public record SchedulationKey(String tenantId, String scheduleId) {
		public String value() {
			return SchedulationKeyUtils.getValue(this);
		}

		public int hash() {
			return value().hashCode();
		}
	}

	private final SchedulationKey key;
	private final TransactionInvoker txInvoker;
	private final DatasourceService datasourceService;
	private final Deque<Command> lag = new ArrayDeque<>();
	private final Duration timeout;
	private Ingest currentIngest;
	private Scheduler scheduler;
	private LocalDateTime lastRequest = LocalDateTime.now();
	private OffsetDateTime lastIngestionDate;

	public Schedulation(
		ActorContext<Command> context,
		SchedulationKey key,
		TransactionInvoker txInvoker,
		DatasourceService datasourceService) {

		super(context);
		this.key = key;
		this.txInvoker = txInvoker;
		this.datasourceService = datasourceService;
		this.timeout = getTimeout(context);

		getContext().getSelf().tell(Start.INSTANCE);
	}

	public static Behavior<Command> create(
		SchedulationKey schedulationKey, TransactionInvoker txInvoker,
		DatasourceService datasourceService) {

		return Behaviors
			.<Command>supervise(
				Behaviors.setup(ctx -> new Schedulation(
					ctx, schedulationKey, txInvoker, datasourceService)))
			.onFailure(SupervisorStrategy.resume());
	}


	@Override
	public Receive<Command> createReceive() {
		return init();
	}

	@Override
	public ReceiveBuilder<Command> newReceiveBuilder() {
		return super.newReceiveBuilder().onMessageEquals(Tick.INSTANCE, this::onTick);
	}

	private Receive<Command> init() {
		logBehavior(INIT_BEHAVIOR);

		return newReceiveBuilder()
			.onMessage(Start.class, this::onStart)
			.onMessage(SetScheduler.class, this::onSetScheduler)
			.onAnyMessage(this::enqueue)
			.build();
	}

	private Receive<Command> ready() {
		logBehavior(READY_BEHAVIOR);

		return newReceiveBuilder()
			.onMessage(Ingest.class, this::onIngestReady)
			.onMessageEquals(Cancel.INSTANCE, this::onCancel)
			.build();
	}

	private Receive<Command> busy() {
		logBehavior(BUSY_BEHAVIOR);

		return newReceiveBuilder()
			.onMessage(EnrichPipelineResponseWrapper.class, this::onEnrichPipelineResponse)
			.onMessage(PersistLastIngestionDate.class, this::onPersistLastIngestionDate)
			.onMessage(SetLastIngestionDate.class, this::onSetLastIngestionDate)
			.onAnyMessage(this::enqueue)
			.build();
	}

	private Receive<Command> finish() {
		logBehavior(FINISH_BEHAVIOR);

		return newReceiveBuilder()
			.onMessageEquals(PersistDataIndex.INSTANCE, this::onPersistDataIndex)
			.onMessageEquals(PersistStatusFinished.INSTANCE, this::onPersistStatusFinished)
			.onMessage(NotificationSenderResponseWrapper.class, this::onNotificationResponse)
			.build();
	}

	private Behavior<Command> next() {
		logBehavior(NEXT_BEHAVIOR);

		currentIngest = null;

		if (!lag.isEmpty()) {
			Command command = lag.pop();
			getContext().getSelf().tell(command);
		}

		return this.ready();
	}

	private Behavior<Command> onStart(Start start) {
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

		return Behaviors.withTimers(timer -> {
			timer.startTimerAtFixedRate(Tick.INSTANCE, Duration.ofSeconds(1));
			return Behaviors.same();
		});
	}

	private Behavior<Command> onSetScheduler(SetScheduler setScheduler) {
		this.scheduler = setScheduler.scheduler;
		return this.next();
	}

	private Behavior<Command> onIngestReady(Ingest ingest) {
		this.currentIngest = ingest;
		this.lastRequest = LocalDateTime.now();

		String indexName = getIndexName();

		byte[] payloadArray = ingest.payload;

		DataPayload dataPayload =
			Json.decodeValue(Buffer.buffer(payloadArray), DataPayload.class);

		if (dataPayload.getContentId() != null) {

			updateLastIngestionDate(dataPayload);

			dataPayload.setIndexName(indexName);

			ActorRef<EnrichPipeline.Response> responseActorRef = getContext()
				.messageAdapter(EnrichPipeline.Response.class, EnrichPipelineResponseWrapper::new);

			ActorRef<EnrichPipeline.Command> enrichPipelineActorRef = getContext()
				.spawnAnonymous(
					EnrichPipeline.create(
						key, responseActorRef, dataPayload, scheduler));

			enrichPipelineActorRef.tell(EnrichPipeline.Start.INSTANCE);

			return this.busy();
		}
		else if (!dataPayload.isLast()) {
			currentIngest.replyTo.tell(new Failure("content-id is null"));
			return this.next();
		}
		else {
			log.info("{} ingestion is done, replyTo {}", key, currentIngest.replyTo);

			currentIngest.replyTo.tell(Success.INSTANCE);

			getContext().getSelf().tell(PersistDataIndex.INSTANCE);

			return this.finish();
		}
	}

	private Behavior<Command> onCancel() {

		VertxUtil.runOnContext(() -> txInvoker
			.withTransaction(key.tenantId, s -> s
				.find(Scheduler.class, scheduler.getId())
				.chain(scheduler -> {
					scheduler.setStatus(Scheduler.SchedulerStatus.CANCELLED);
					return s.persist(scheduler);
				})
				.invoke(this::destroyQueue)
			)
		);

		logBehavior(STOPPED_BEHAVIOR);

		return Behaviors.stopped();
	}

	private Behavior<Command> enqueue(Command command) {
		this.lag.add(command);
		log.info("There are {} commands waiting", lag.size());
		return Behaviors.same();
	}

	private Behavior<Command> onEnrichPipelineResponse(EnrichPipelineResponseWrapper eprw) {
		EnrichPipeline.Response response = eprw.response;

		if (response instanceof EnrichPipeline.Success) {

			log.info(
				"enrich pipeline success for content-id {} replyTo {}",
				getContentId(currentIngest.payload), currentIngest.replyTo);

			currentIngest.replyTo.tell(Success.INSTANCE);
		}
		else if (response instanceof EnrichPipeline.Failure) {
			Throwable exception = ((EnrichPipeline.Failure) response).exception();
			log.error("enrich pipeline failure", exception);
			currentIngest.replyTo.tell(new Failure(ExceptionUtil.generateStackTrace(exception)));
		}

		return this.next();
	}

	private String getContentId(byte[] payload) {
		return Json.decodeValue(Buffer.buffer(payload), DataPayload.class).getContentId();
	}

	private Behavior<Command> onPersistDataIndex() {
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
				.invoke(() -> getContext().getSelf().tell(PersistStatusFinished.INSTANCE))
			);
		}
		else {
			getContext().getSelf().tell(PersistStatusFinished.INSTANCE);
		}

		return Behaviors.same();
	}

	private Behavior<Command> onPersistStatusFinished() {
		VertxUtil.runOnContext(() -> txInvoker.withTransaction(
			key.tenantId, s -> s
				.find(Scheduler.class, scheduler.getId())
				.chain(entity -> {
					entity.setStatus(Scheduler.SchedulerStatus.FINISHED);
					return s.persist(entity);
				})
				.invoke(this::destroyQueue)
			)
		);

		if (scheduler.getOldDataIndex() != null && scheduler.getNewDataIndex() != null) {
			ActorRef<NotificationSender.Response> messageAdapter = getContext().messageAdapter(
				NotificationSender.Response.class, NotificationSenderResponseWrapper::new);
			getContext().spawnAnonymous(
				NotificationSender.create(scheduler, key, messageAdapter));
			return Behaviors.same();
		}

		logBehavior(STOPPED_BEHAVIOR);

		return Behaviors.stopped();

	}

	private Behavior<Command> onNotificationResponse(
		NotificationSenderResponseWrapper notificationResponseWrapper) {

		logBehavior(STOPPED_BEHAVIOR);

		return Behaviors.stopped();
	}

	private Behavior<Command> onTick() {
		if (log.isTraceEnabled()) {
			log.trace("Check {} expiration", key);
		}

		if (Duration.between(lastRequest, LocalDateTime.now()).compareTo(timeout) > 0) {
			log.info("{} ingestion is expired ", key);

			getContext().getSelf().tell(PersistDataIndex.INSTANCE);

			return this.finish();
		}
		return Behaviors.same();
	}

	private Behavior<Command> onPersistLastIngestionDate(PersistLastIngestionDate persistLastIngestionDate) {
		OffsetDateTime lastIngestionDate = persistLastIngestionDate.lastIngestionDate();
		Long datasourceId = scheduler.getDatasource().getId();
		VertxUtil.runOnContext(() -> txInvoker.withTransaction(
			key.tenantId, s -> s
				.find(Datasource.class, datasourceId)
				.chain(entity -> {
					entity.setLastIngestionDate(lastIngestionDate);
					return s.persist(entity);
				})
				.onItemOrFailure()
				.invoke((r, t) -> {
					if (t != null) {
						log.warn("last ingestion date cannot be persisted", t);
					}
					else {
						log.info("update last ingestion date for datasource with id {}", datasourceId);
						getContext().getSelf().tell(new SetLastIngestionDate(lastIngestionDate));
					}
				})
			)
		);
		return Behaviors.same();
	}

	private Behavior<Command> onSetLastIngestionDate(SetLastIngestionDate setLastIngestionDate) {
		this.lastIngestionDate = setLastIngestionDate.lastIngestionDate;
		return Behaviors.same();
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

	private static Duration getTimeout(ActorContext<?> context) {
		Config config = context.getSystem().settings().config();

		String configPath = "io.openk9.schedulation.timeout";

		if (config.hasPathOrNull(configPath)) {
			if (config.getIsNull(configPath)) {
				return Duration.ofHours(6);
			} else {
				return config.getDuration(configPath);
			}
		} else {
			return Duration.ofHours(6);
		}

	}

	private void updateLastIngestionDate(DataPayload dataPayload) {
		OffsetDateTime parsingDate =
			OffsetDateTime.ofInstant(
				Instant.ofEpochMilli(dataPayload.getParsingDate()),
				ZoneOffset.UTC
			);

		if (this.lastIngestionDate == null || !this.lastIngestionDate.isEqual(parsingDate)) {
			getContext().getSelf().tell(new PersistLastIngestionDate(parsingDate));
		}
	}

	private void destroyQueue() {
		ClusterSingleton clusterSingleton = ClusterSingleton.get(getContext().getSystem());

		ActorRef<QueueManager.Command> queueManager = clusterSingleton.init(
			SingletonActor.of(QueueManager.create(), QueueManager.INSTANCE_NAME));

		queueManager.tell(new QueueManager.DestroyQueue(
			SchedulationKeyUtils.getValue(key.tenantId(), key.scheduleId())));
	}
}
