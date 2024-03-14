/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.ReceiveBuilder;
import akka.actor.typed.receptionist.Receptionist;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;
import com.typesafe.config.Config;
import io.openk9.common.util.SchedulingKey;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.actor.AkkaUtils;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.NotificationSender;
import io.openk9.datasource.pipeline.actor.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.actor.mapper.SchedulerMapper;
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

public class Scheduling extends AbstractBehavior<Scheduling.Command> {

	public static final EntityTypeKey<Command> ENTITY_TYPE_KEY =
		EntityTypeKey.create(Command.class, "scheduling");
	public static final String SCHEDULING_TIMEOUT = "io.openk9.scheduling.timeout";
	public static final String WORKERS_PER_NODE = "io.openk9.scheduling.workers-per-node";
	public static final int WORKERS_PER_NODE_DEFAULT = 2;
	private static final String INIT_BEHAVIOR = "Init";
	private static final String READY_BEHAVIOR = "Ready";
	private static final String BUSY_BEHAVIOR = "Busy";
	private static final String NEXT_BEHAVIOR = "Next";
	private static final String CLOSING_BEHAVIOR = "Closing";
	private static final String STOPPED_BEHAVIOR = "Stopped";
	private static final Logger log = Logger.getLogger(Scheduling.class);
	private final SchedulingKey key;
	private final Mutiny.SessionFactory sessionFactory;
	private final Deque<Command> lag = new ArrayDeque<>();
	private final Set<ActorRef<Response>> consumers = new HashSet<>();
	private final SchedulerMapper schedulerMapper;
	private final Duration timeout;
	private final int workersPerNode;
	private SchedulerDTO scheduler;
	private LocalDateTime lastRequest = LocalDateTime.now();
	private OffsetDateTime lastIngestionDate;
	private boolean failureTracked = false;
	private boolean lastReceived = false;
	private int maxWorkers;
	private int workers = 0;

	public Scheduling(
		ActorContext<Command> context,
		SchedulingKey key,
		Mutiny.SessionFactory sessionFactory,
		SchedulerMapper schedulerMapper) {

		super(context);
		this.key = key;
		this.sessionFactory = sessionFactory;
		this.schedulerMapper = schedulerMapper;
		this.timeout = getTimeout(context);
		this.workersPerNode = getWorkersPerNode(context);
		this.maxWorkers = workersPerNode;

		ActorRef<Receptionist.Listing> messageAdapter =
			getContext().messageAdapter(
				Receptionist.Listing.class,
				MessageGatewaySubscription::new
			);

		getContext().getSystem().receptionist().tell(
			Receptionist.subscribe(MessageGateway.SERVICE_KEY, messageAdapter)
		);

		getContext().getSelf().tell(Start.INSTANCE);
	}

	public static Behavior<Command> create(
		SchedulingKey schedulingKey,
		Mutiny.SessionFactory sessionFactory,
		SchedulerMapper schedulerMapper
	) {

		return Behaviors
			.<Command>supervise(
				Behaviors.setup(ctx -> new Scheduling(
					ctx,
					schedulingKey,
					sessionFactory,
					schedulerMapper
				))
			)
			.onFailure(SupervisorStrategy.restartWithBackoff(
					Duration.ofSeconds(3),
					Duration.ofSeconds(60),
					0.2
				)
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
			.onMessage(MessageGatewaySubscription.class, this::onMessageGatewaySubscription)
			.onMessage(EnrichPipelineResponseWrapper.class, this::onEnrichPipelineResponse)
			.onMessage(PersistLastIngestionDate.class, this::onPersistLastIngestionDate)
			.onMessage(SetLastIngestionDate.class, this::onSetLastIngestionDate)
			.onMessage(SetScheduler.class, this::onSetScheduler)
			.onMessageEquals(PersistDataIndex.INSTANCE, this::onPersistDataIndex)
			.onMessageEquals(PersistStatusFinished.INSTANCE, this::onPersistStatusFinished)
			.onMessage(NotificationSenderResponseWrapper.class, this::onNotificationResponse)
			.onMessageEquals(Stop.INSTANCE, this::onStop)
			.onSignal(PostStop.class, this::onPostStop);
	}

	private static Duration getTimeout(ActorContext<?> context) {
		Config config = context.getSystem().settings().config();

		return AkkaUtils.getDuration(config, SCHEDULING_TIMEOUT, Duration.ofHours(6));
	}

	private static int getWorkersPerNode(ActorContext<Command> context) {
		Config config = context.getSystem().settings().config();

		return AkkaUtils.getInteger(config, WORKERS_PER_NODE, WORKERS_PER_NODE_DEFAULT);
	}

	private Behavior<Command> onMessageGatewaySubscription(
		MessageGatewaySubscription messageGatewaySubscription) {

		int nodes = messageGatewaySubscription
			.listing()
			.getServiceInstances(MessageGateway.SERVICE_KEY)
			.size();

		maxWorkers = workersPerNode * nodes;

		if (log.isDebugEnabled()) {
			log.debugf(
				"Max Workers updated to %d for %s",
				maxWorkers,
				key
			);
		}

		return Behaviors.same();
	}

	private Receive<Command> init() {
		logBehavior(INIT_BEHAVIOR);

		return newReceiveBuilder()
			.onMessage(Start.class, this::onStart)
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

	private Behavior<Command> next() {
		logBehavior(NEXT_BEHAVIOR);

		if (!lag.isEmpty()) {
			Command command = lag.pop();
			getContext().getSelf().tell(command);
		}

		return ready();
	}

	private Behavior<Command> closing() {
		logBehavior(CLOSING_BEHAVIOR);

		return newReceiveBuilder().build();
	}

	private Behavior<Command> onStart(Start start) {
		VertxUtil.runOnContext(() -> sessionFactory
			.withStatelessTransaction(key.tenantId(), (s, t) -> s
				.createNamedQuery(Scheduler.FETCH_BY_SCHEDULE_ID, Scheduler.class)
				.setParameter("scheduleId", key.scheduleId())
				.setPlan(s.getEntityGraph(Scheduler.class, Scheduler.ENRICH_ITEMS_ENTITY_GRAPH))
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
		this.scheduler = schedulerMapper.map(setScheduler.scheduler);
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

			String contentId = dataPayload.getContentId();

			ActorRef<EnrichPipeline.Response> responseActorRef = getContext()
				.messageAdapter(EnrichPipeline.Response.class, EnrichPipelineResponseWrapper::new);

			ActorSystem<Void> system = getContext().getSystem();

			ClusterSharding clusterSharding = ClusterSharding.get(system);

			EntityRef<EnrichPipeline.Command> enrichPipelineRef = clusterSharding.entityRefFor(
				EnrichPipeline.ENTITY_TYPE_KEY,
				EnrichPipelineKey.of(key, contentId).asString()
			);

			enrichPipelineRef.tell(new EnrichPipeline.Setup(
					responseActorRef,
					ingest.replyTo,
					Json.encodeToBuffer(dataPayload).getBytes(),
					scheduler
				)
			);

			consumers.add(ingest.replyTo());
			workers++;

		}
		else if (!dataPayload.isLast()) {
			ingest.replyTo.tell(new Failure("content-id is null"));
		}
		else {
			ingest.replyTo.tell(Success.INSTANCE);
			lastReceived = true;
			log.infof("%s received last message", key);
		}

		return workers < maxWorkers ? next() : busy();

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
			.withTransaction(key.tenantId(), (s, t) -> s
				.find(Scheduler.class, scheduler.getId())
				.chain(scheduler -> {
					scheduler.setStatus(Scheduler.SchedulerStatus.CANCELLED);
					return s.persist(scheduler);
				})
				.invoke(this::destroyQueue)
				.invoke(() -> getContext().getSelf().tell(Stop.INSTANCE))
			)
		);

		return closing();
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
				success.contentId(), success.replyTo()
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

		return next();
	}

	private Behavior<Command> onPersistDataIndex() {
		Long newDataIndexId = scheduler.getNewDataIndexId();
		Long datasourceId = scheduler.getDatasourceId();

		if (newDataIndexId != null) {
			String tenantId = key.tenantId();

			log.infof(
				"replacing dataindex %s for datasource %s on tenant %s",
				newDataIndexId, datasourceId, tenantId
			);
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
			key.tenantId(), (s, t) -> s
				.find(Scheduler.class, scheduler.getId())
				.chain(entity -> {
					entity.setStatus(Scheduler.SchedulerStatus.FINISHED);
					return s.persist(entity);
				})
				.invoke(this::destroyQueue)
				.invoke(() -> getContext().getSelf().tell(Stop.INSTANCE))
			)
		);

		Long newDataIndexId = scheduler.getNewDataIndexId();
		Long oldDataIndexId = scheduler.getOldDataIndexId();

		if (newDataIndexId != null && oldDataIndexId != null) {
			ActorRef<NotificationSender.Response> messageAdapter = getContext().messageAdapter(
				NotificationSender.Response.class, NotificationSenderResponseWrapper::new);
			getContext().spawnAnonymous(
				NotificationSender.create(scheduler, key, messageAdapter));
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
				log.tracef(
					"a manual operation is needed for scheduler with id %s",
					scheduler.getId()
				);
			}

			return Behaviors.same();
		}

		if (workers > 0) {
			if (log.isDebugEnabled()) {
				log.debugf("There are %s busy workers, for %s", workers, key);
			}

			return Behaviors.same();
		}

		if (!failureTracked && lastReceived) {
			log.infof("%s is done", key);

			getContext().getSelf().tell(PersistDataIndex.INSTANCE);

			return closing();
		}

		if (log.isTraceEnabled()) {
			log.tracef("check %s expiration", key);
		}

		if (Duration.between(lastRequest, LocalDateTime.now()).compareTo(timeout) > 0) {
			log.infof("%s ingestion is expired", key);

			getContext().getSelf().tell(PersistDataIndex.INSTANCE);

			return closing();
		}

		return Behaviors.same();
	}

	private Behavior<Command> onPersistLastIngestionDate(PersistLastIngestionDate persistLastIngestionDate) {
		OffsetDateTime lastIngestionDate = persistLastIngestionDate.lastIngestionDate();
		Long datasourceId = scheduler.getDatasourceId();
		VertxUtil.runOnContext(() -> sessionFactory.withTransaction(
			key.tenantId(), (s, tx) -> s
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
						log.infof(
							"update last ingestion date for datasource with id %s",
							datasourceId
						);
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
				.flatMap(entity -> {
					entity.setStatus(status);
					return s
						.persist(entity)
						.flatMap(ignore -> s.createNamedQuery(
								Scheduler.FETCH_BY_SCHEDULE_ID, Scheduler.class)
							.setParameter("scheduleId", entity.getScheduleId())
							.setPlan(s.getEntityGraph(
								Scheduler.class,
								Scheduler.ENRICH_ITEMS_ENTITY_GRAPH
							))
							.getSingleResult()
							.invoke(fetch -> getContext().getSelf().tell(new SetScheduler(fetch)))
						);
				})
			)
			.onItemOrFailure()
			.invoke((r, t) -> {
				if (t != null) {
					persistStatus.replyTo.tell(new Failure(ExceptionUtil.generateStackTrace(t)));
				}
				else {
					log.infof(
						"status updated to %s for scheduling with id %s",
						status,
						r.getId()
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
		String newDataIndexName = scheduler.getNewDataIndexName();

		return newDataIndexName != null
			? newDataIndexName
			: scheduler.getOldDataIndexName();
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
		log.infof("%s behavior is %s", key, behavior);
	}

	private void destroyQueue() {
		ClusterSingleton clusterSingleton = ClusterSingleton.get(getContext().getSystem());

		ActorRef<QueueManager.Command> queueManager = clusterSingleton.init(
			SingletonActor.of(QueueManager.create(), QueueManager.INSTANCE_NAME));

		queueManager.tell(new QueueManager.DestroyQueue(
			SchedulingKey.asString(key.tenantId(), key.scheduleId())));
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

	public enum Cancel implements Command {
		INSTANCE
	}

	public enum PersistDataIndex implements Command {
		INSTANCE
	}

	private enum PersistStatusFinished implements Command {
		INSTANCE
	}

	private enum Start implements Command {
		INSTANCE
	}

	private enum Stop implements Command {
		INSTANCE
	}

	private enum Tick implements Command {
		INSTANCE
	}

	public enum Success implements Response {
		INSTANCE
	}

	public sealed interface Command extends CborSerializable {}

	public sealed interface Response extends CborSerializable {}

	public record Ingest(byte[] payload, ActorRef<Response> replyTo) implements Command {}

	public record TrackError(ActorRef<Response> replyTo) implements Command {}

	public record Restart(ActorRef<Response> replyTo) implements Command {}

	private record PersistLastIngestionDate(OffsetDateTime lastIngestionDate) implements Command {}

	private record PersistStatus(Scheduler.SchedulerStatus status, ActorRef<Response> replyTo)
		implements Command {}

	private record SetLastIngestionDate(OffsetDateTime lastIngestionDate) implements Command {}

	private record SetScheduler(Scheduler scheduler) implements Command {}

	private record EnrichPipelineResponseWrapper(EnrichPipeline.Response response)
		implements Command {}

	private record NotificationSenderResponseWrapper(NotificationSender.Response response)
		implements Command {}

	private record MessageGatewaySubscription(Receptionist.Listing listing) implements Command {}

	public record Failure(String error) implements Response {}

}
