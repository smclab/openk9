package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
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
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.util.CborSerializable;
import io.quarkus.runtime.util.ExceptionUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class Schedulation extends AbstractBehavior<Schedulation.Command> {

	public static final EntityTypeKey<Command> ENTITY_TYPE_KEY =
		EntityTypeKey.create(Command.class, "schedulation");

	private static final String INIT_BEHAVIOR = "Init";
	private static final String READY_BEHAVIOR = "Ready";
	private static final String BUSY_BEHAVIOR = "Busy";
	private static final String NEXT_BEHAVIOR = "Next";
	private static final String FINISH_BEHAVIOR = "Finish";
	private static final String STOPPED_BEHAVIOR = "Stopped";
	private static final Logger log = Logger.getLogger(Schedulation.class);

	public sealed interface Command extends CborSerializable {}
	public enum Cancel implements Command {INSTANCE}
	public record Ingest(byte[] payload, ActorRef<Response> replyTo) implements Command {}
	public record TrackError(ActorRef<Response> replyTo) implements Command {}
	public record Restart(ActorRef<Response> replyTo) implements Command {}
	private enum PersistDataIndex implements Command {INSTANCE}
	private enum PersistStatusFinished implements Command {INSTANCE}
	private record PersistLastIngestionDate(OffsetDateTime lastIngestionDate) implements Command {}
	private record PersistStatus(Scheduler.SchedulerStatus status, ActorRef<Response> replyTo) implements Command {}
	private record SetLastIngestionDate(OffsetDateTime lastIngestionDate) implements Command {}
	private record SetScheduler(Scheduler scheduler) implements Command {}
	private record EnrichPipelineResponseWrapper(EnrichPipeline.Response response) implements Command {}
	private record NotificationSenderResponseWrapper(NotificationSender.Response response) implements Command {}
	private enum Start implements Command {INSTANCE}
	private enum Stop implements Command {INSTANCE}
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
	private final Mutiny.SessionFactory sessionFactory;
	private final Deque<Command> lag = new ArrayDeque<>();
	private final Duration timeout;
	private Scheduler scheduler;
	private LocalDateTime lastRequest = LocalDateTime.now();
	private OffsetDateTime lastIngestionDate;
	private boolean failureTracked = false;
	private int maxWorkers = 3;
	private int workers = 0;
	private Set<ActorRef<Response>> consumers = new HashSet<>();

	public Schedulation(
		ActorContext<Command> context,
		SchedulationKey key,
		Mutiny.SessionFactory sessionFactory
	) {

		super(context);
		this.key = key;
		this.sessionFactory = sessionFactory;
		this.timeout = getTimeout(context);

		getContext().getSelf().tell(Start.INSTANCE);
	}

	public static Behavior<Command> create(
		SchedulationKey schedulationKey, Mutiny.SessionFactory sessionFactory) {

		return Behaviors
			.<Command>supervise(
				Behaviors.setup(ctx -> new Schedulation(ctx, schedulationKey, sessionFactory)
				)
			)
			.onFailure(SupervisorStrategy.restartWithBackoff(
				Duration.ofSeconds(3),
				Duration.ofSeconds(60),
				0.2)
			);
	}


	@Override
	public Receive<Command> createReceive() {
		return init();
	}

	@Override
	public ReceiveBuilder<Command> newReceiveBuilder() {
		return super.newReceiveBuilder()
			.onMessageEquals(Tick.INSTANCE, this::onTick)
			.onMessage(EnrichPipelineResponseWrapper.class, this::onEnrichPipelineResponse)
			.onMessage(PersistLastIngestionDate.class, this::onPersistLastIngestionDate)
			.onMessage(SetLastIngestionDate.class, this::onSetLastIngestionDate)
			.onSignal(PostStop.class, this::onPostStop);
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
			.onMessage(Ingest.class, this::onIngest)
			.onMessage(TrackError.class, this::onTrackError)
			.onMessage(Restart.class, this::onRestart)
			.onMessage(PersistStatus.class, this::onPersistStatus)
			.onMessageEquals(Cancel.INSTANCE, this::onCancel)
			.build();
	}

	private Receive<Command> busy() {
		logBehavior(BUSY_BEHAVIOR);

		return newReceiveBuilder()
			.onAnyMessage(this::enqueue)
			.build();
	}

	private Receive<Command> finish() {
		logBehavior(FINISH_BEHAVIOR);

		return newReceiveBuilder()
			.onMessageEquals(PersistDataIndex.INSTANCE, this::onPersistDataIndex)
			.onMessageEquals(PersistStatusFinished.INSTANCE, this::onPersistStatusFinished)
			.onMessage(NotificationSenderResponseWrapper.class, this::onNotificationResponse)
			.onMessageEquals(Stop.INSTANCE, this::onStop)
			.build();
	}

	private Behavior<Command> next() {
		logBehavior(NEXT_BEHAVIOR);

		if (!lag.isEmpty()) {
			Command command = lag.pop();
			getContext().getSelf().tell(command);
		}

		return this.ready();
	}

	private Behavior<Command> onStart(Start start) {
		VertxUtil.runOnContext(() -> sessionFactory
			.withStatelessTransaction(key.tenantId, (s, t) -> s
				.createNamedQuery(Scheduler.FETCH_SCHEDULATION_QUERY, Scheduler.class)
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

	private Behavior<Command> onIngest(Ingest ingest) {
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
						key, responseActorRef, ingest.replyTo, dataPayload, scheduler)
				);

			enrichPipelineActorRef.tell(EnrichPipeline.Start.INSTANCE);

			consumers.add(ingest.replyTo());
			workers++;

			return workers < maxWorkers ? this.next() : this.busy();
		}
		else if (!dataPayload.isLast()) {
			ingest.replyTo.tell(new Failure("content-id is null"));
			return this.next();
		}
		else {
			ingest.replyTo.tell(Success.INSTANCE);

			if (!failureTracked) {
				log.infof("%s ingestion is done, replyTo %s", key, ingest.replyTo);

				getContext().getSelf().tell(PersistDataIndex.INSTANCE);

				return this.finish();
			}
			else {
				log.infof(
					"%s ingestion is done, but a failure was tracked, wait for the next message",
					key, ingest.replyTo
				);

				return this.next();
			}
		}
	}

	private Behavior<Command> onTrackError(TrackError trackError) {
		if (scheduler.getStatus() != Scheduler.SchedulerStatus.ERROR) {
			getContext().getSelf().tell(
				new PersistStatus(Scheduler.SchedulerStatus.ERROR, trackError.replyTo)
			);
		}
		return next();
	}

	private Behavior<Command> onRestart(Restart restart) {
		failureTracked = false;
		lastRequest = LocalDateTime.now();
		getContext().getSelf().tell(
			new PersistStatus(Scheduler.SchedulerStatus.STARTED, restart.replyTo)
		);

		return next();
	}

	private Behavior<Command> onCancel() {

		VertxUtil.runOnContext(() -> sessionFactory
			.withTransaction(key.tenantId, (s, t) -> s
				.find(Scheduler.class, scheduler.getId())
				.chain(scheduler -> {
					scheduler.setStatus(Scheduler.SchedulerStatus.CANCELLED);
					return s.persist(scheduler);
				})
				.invoke(this::destroyQueue)
				.invoke(() -> getContext().getSelf().tell(Stop.INSTANCE))
			)
		);

		return finish();
	}

	private Behavior<Command> enqueue(Command command) {
		this.lag.add(command);
		log.infof("There are %s commands waiting", lag.size());
		return Behaviors.same();
	}

	private Behavior<Command> onEnrichPipelineResponse(EnrichPipelineResponseWrapper eprw) {
		EnrichPipeline.Response response = eprw.response;

		if (response instanceof EnrichPipeline.Success success) {
			log.infof(
				"enrich pipeline success for content-id %s replyTo %s",
				success.dataPayload().getContentId(), success.replyTo()
			);

			response.replyTo().tell(Success.INSTANCE);
		}
		else if (response instanceof EnrichPipeline.Failure failure) {
			Throwable exception = failure.exception();
			log.error("enrich pipeline failure", exception);
			this.failureTracked = true;
			response.replyTo().tell(new Failure(ExceptionUtil.generateStackTrace(exception)));
		}

		workers--;
		consumers.remove(response.replyTo());

		return this.next();
	}

	private Behavior<Command> onPersistDataIndex() {
		DataIndex newDataIndex = scheduler.getNewDataIndex();
		io.openk9.datasource.model.Datasource datasource = scheduler.getDatasource();

		if (newDataIndex != null) {
			Long newDataIndexId = newDataIndex.getId();
			Long datasourceId = datasource.getId();
			String tenantId = key.tenantId;

			log.infof(
				"replacing dataindex %s for datasource %s on tenant %s",
				newDataIndexId, datasourceId, tenantId);
			VertxUtil.runOnContext(() -> sessionFactory
				.withTransaction(
					tenantId,
					(s, t) -> s
						.find(Datasource.class, datasourceId)
						.onItem()
						.transformToUni(ds -> s
							.find(DataIndex.class, newDataIndexId)
							.onItem()
							.transformToUni(di -> {
								ds.setDataIndex(di);
								return s.persist(ds);
							}))
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
		VertxUtil.runOnContext(() -> sessionFactory.withTransaction(
			key.tenantId, (s, t) -> s
				.find(Scheduler.class, scheduler.getId())
				.chain(entity -> {
					entity.setStatus(Scheduler.SchedulerStatus.FINISHED);
					return s.persist(entity);
				})
				.invoke(this::destroyQueue)
				.invoke(() -> getContext().getSelf().tell(Stop.INSTANCE))
			)
		);

		if (scheduler.getOldDataIndex() != null && scheduler.getNewDataIndex() != null) {
			ActorRef<NotificationSender.Response> messageAdapter = getContext().messageAdapter(
				NotificationSender.Response.class, NotificationSenderResponseWrapper::new);
			getContext().spawnAnonymous(
				NotificationSender.create(scheduler, key, messageAdapter));
			return Behaviors.same();
		}

		return Behaviors.same();

	}

	private Behavior<Command> onNotificationResponse(
		NotificationSenderResponseWrapper notificationResponseWrapper) {

		getContext().getSelf().tell(Stop.INSTANCE);

		return Behaviors.same();
	}

	private Behavior<Command> onTick() {
		if (scheduler.getStatus() == Scheduler.SchedulerStatus.ERROR) {
			if (log.isTraceEnabled()) {
				log.tracef("a manual operation is needed for scheduler with id %s", scheduler.getId());
			}
			return Behaviors.same();
		}

		if (log.isTraceEnabled()) {
			log.tracef("check %s expiration", key);
		}

		if (Duration.between(lastRequest, LocalDateTime.now()).compareTo(timeout) > 0) {
			log.infof("%s ingestion is expired ", key);

			getContext().getSelf().tell(PersistDataIndex.INSTANCE);

			return this.finish();
		}
		return Behaviors.same();
	}

	private Behavior<Command> onPersistLastIngestionDate(PersistLastIngestionDate persistLastIngestionDate) {
		OffsetDateTime lastIngestionDate = persistLastIngestionDate.lastIngestionDate();
		Long datasourceId = scheduler.getDatasource().getId();
		VertxUtil.runOnContext(() -> sessionFactory.withTransaction(
			key.tenantId, (s, tx) -> s
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
						log.infof("update last ingestion date for datasource with id %s", datasourceId);
						getContext().getSelf().tell(new SetLastIngestionDate(lastIngestionDate));
					}
				})
			)
		);
		return Behaviors.same();
	}

	private Behavior<Command> onPersistStatus(PersistStatus persistStatus) {
		Scheduler.SchedulerStatus status = persistStatus.status;
		VertxUtil.runOnContext(() -> sessionFactory
			.withTransaction(key.tenantId(), (s, tx) -> s
				.find(Scheduler.class, scheduler.getId())
				.call(entity -> {
					entity.setStatus(status);
					return s.persist(entity);
				})
				.call(entity -> s.createNamedQuery(
						Scheduler.FETCH_SCHEDULATION_QUERY, Scheduler.class)
					.setParameter("scheduleId", entity.getScheduleId())
					.getSingleResult()
					.invoke(fetch -> getContext().getSelf().tell(new SetScheduler(fetch)))
				)
			)
			.onItemOrFailure()
			.invoke((r, t) -> {
				if (t != null) {
					persistStatus.replyTo.tell(new Failure(ExceptionUtil.generateStackTrace(t)));
				}
				else {
					log.infof(
						"status updated to %s for schedulation with id %s",
						status,
						scheduler.getId()
					);

					persistStatus.replyTo.tell(Success.INSTANCE);
				}
			})
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

	private Behavior<Command> onStop() {
		logBehavior(STOPPED_BEHAVIOR);
		return Behaviors.stopped();
	}

	private Behavior<Command> onPostStop(PostStop postStop) {
		for (ActorRef<Response> consumer : consumers) {
			consumer.tell(new Failure("stopped for unexpected reason"));
		}

		return Behaviors.same();
	}

	private void logBehavior(String behavior) {
		log.infof("Schedulation with key %s behavior is %s", key, behavior);
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
