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
import io.openk9.common.util.ingestion.PayloadType;
import io.openk9.datasource.actor.AkkaUtils;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.closing.DeletionCompareNotifier;
import io.openk9.datasource.pipeline.actor.closing.EvaluateStatus;
import io.openk9.datasource.pipeline.actor.closing.UpdateDatasource;
import io.openk9.datasource.pipeline.service.SchedulingService;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.processor.payload.DataPayload;
import io.openk9.datasource.util.CborSerializable;
import io.quarkus.runtime.util.ExceptionUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import lombok.Getter;
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
	private static final String GRACEFUL_ENDING_BEHAVIOR = "Graceful Ending";
	private static final String STOPPED_BEHAVIOR = "Stopped";
	private static final Logger log = Logger.getLogger(Scheduling.class);
	@Getter
	private final SchedulingKey key;
	private final Deque<Command> lag = new ArrayDeque<>();
	private final Set<ActorRef<Response>> consumers = new HashSet<>();
	private final Duration timeout;
	private final int workersPerNode;
	@Getter
	private SchedulerDTO scheduler;
	private LocalDateTime lastRequest = LocalDateTime.now();
	private boolean failureTracked = false;
	private boolean lastReceived = false;
	private int maxWorkers;
	private int busyWorkers = 0;
	private int expectedReplies;
	private Scheduler.SchedulerStatus endingStatus = Scheduler.SchedulerStatus.FINISHED;

	public Scheduling(
		ActorContext<Command> context, SchedulingKey key) {

		super(context);
		this.key = key;
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
		SchedulingKey schedulingKey) {

		return Behaviors
			.<Command>supervise(
				Behaviors.setup(ctx -> new Scheduling(
					ctx, schedulingKey)
				)
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
			.onMessage(MessageGatewaySubscription.class, this::onMessageGatewaySubscription)
			.onMessage(RefreshScheduler.class, this::onRefreshScheduler)
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

	private ReceiveBuilder<Command> afterSetup() {

		return newReceiveBuilder()
			.onMessageEquals(Tick.INSTANCE, this::onTick)
			.onMessage(EnrichPipelineResponseWrapper.class, this::onEnrichPipelineResponse)
			.onMessage(PersistLastIngestionDate.class, this::onPersistLastIngestionDate)
			.onMessage(PersistStatus.class, this::onPersistStatus)
			.onMessageEquals(Close.INSTANCE, this::onClose)
			.onMessage(GracefulEnd.class, this::onGracefulEnd);
	}

	private Behavior<Command> waitFetchedScheduler() {

		return Behaviors.withTimers(timers -> {
			timers.cancel(Tick.INSTANCE);

			return newReceiveBuilder()
				.onMessage(FetchedScheduler.class, this::onFetchedScheduler)
				.onAnyMessage(this::onEnqueue)
				.build();
		});
	}

	private Receive<Command> init() {
		logBehavior(INIT_BEHAVIOR);

		return newReceiveBuilder()
			.onMessageEquals(Start.INSTANCE, this::onStart)
			.onAnyMessage(this::onEnqueue)
			.build();
	}

	private Receive<Command> ready() {
		logBehavior(READY_BEHAVIOR);

		return afterSetup()
			.onMessage(Ingest.class, this::onIngest)
			.onMessage(TrackError.class, this::onTrackError)
			.onMessage(Restart.class, this::onRestart)
			.build();
	}

	private Receive<Command> busy() {
		logBehavior(BUSY_BEHAVIOR);

		return afterSetup()
			.onAnyMessage(this::onEnqueue)
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

	private Behavior<Command> gracefulEnding() {
		logBehavior(GRACEFUL_ENDING_BEHAVIOR);

		return newReceiveBuilder()
			.onMessage(PersistStatus.class, this::onPersistStatusEnding)
			.onMessage(FetchedScheduler.class, this::onFetchedSchedulerEnding)
			.onMessage(DestroyQueue.class, this::onDestroyQueue)
			.onMessage(DestroyQueueResult.class, this::onDestroyQueueResult)
			.onAnyMessage(this::onDiscard)
			.build();
	}

	private Behavior<Command> closing() {
		logBehavior(CLOSING_BEHAVIOR);

		return Behaviors.withTimers(timers -> {

			timers.cancel(Tick.INSTANCE);

			if (expectedReplies > 0) {

				log.infof("Waiting for %s CloseHandlers.", expectedReplies);

				return newReceiveBuilder()
					.onMessage(CloseHandlerReply.class, this::onCloseHandlerReply)
					.onAnyMessage(this::onDiscard)
					.build();
			}
			else {

				getContext()
					.getSelf()
					.tell(new GracefulEnd(endingStatus));

				return newReceiveBuilder()
					.onMessage(GracefulEnd.class, this::onGracefulEnd)
					.onAnyMessage(this::onDiscard)
					.build();
			}
		});
	}

	private Behavior<Command> onFetchedScheduler(FetchedScheduler fetchedScheduler) {

		var scheduler = fetchedScheduler.scheduler();
		var exception = fetchedScheduler.exception();
		var replyTo = fetchedScheduler.replyTo();

		if (exception != null) {
			replyTo.tell(new Failure(ExceptionUtil.generateStackTrace(exception)));
		}
		else {
			log.infof(
				"Fetched Scheduling with id %s, status %s, lastIngestionDate %s.",
				scheduler.getId(),
				scheduler.getStatus(),
				scheduler.getLastIngestionDate()
			);

			getContext().getSelf().tell(new RefreshScheduler(scheduler));
			replyTo.tell(Success.INSTANCE);
		}

		return Behaviors.same();
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

	private Behavior<Command> onDiscard(Command command) {
		log.warnf("A message of type %s has been discarded.", command.getClass());

		return Behaviors.same();
	}

	private Behavior<Command> onIngest(Ingest ingest) {
		this.lastRequest = LocalDateTime.now();

		String indexName = scheduler.getIndexName();

		byte[] payloadArray = ingest.payload;

		DataPayload dataPayload =
			Json.decodeValue(Buffer.buffer(payloadArray), DataPayload.class);

		if (dataPayload.getType() != null && dataPayload.getType() == PayloadType.HALT) {
			log.warnf("The publisher has sent an HALT message. So %s will be cancelled.", key);

			getContext().getSelf().tell(new GracefulEnd(Scheduler.SchedulerStatus.FAILURE));

			return Behaviors.same();
		}

		if (dataPayload.getContentId() != null) {

			// TODO maybe could be asked to self
			doUpdateLastIngestionDate(dataPayload);

			dataPayload.setIndexName(indexName);

			String contentId = dataPayload.getContentId();

			ActorRef<EnrichPipeline.Response> responseActorRef = getContext()
				.messageAdapter(EnrichPipeline.Response.class, EnrichPipelineResponseWrapper::new);

			ActorSystem<Void> system = getContext().getSystem();

			ClusterSharding clusterSharding = ClusterSharding.get(system);

			EntityRef<EnrichPipeline.Command> enrichPipelineRef = clusterSharding.entityRefFor(
				EnrichPipeline.ENTITY_TYPE_KEY,
				EnrichPipelineKey.of(key, contentId, ingest.messageKey()).asString()
			);

			enrichPipelineRef.tell(new EnrichPipeline.Setup(
					responseActorRef,
					ingest.replyTo,
					Json.encodeToBuffer(dataPayload).getBytes(),
					scheduler
				)
			);

			consumers.add(ingest.replyTo());
			busyWorkers++;

		}
		else if (!dataPayload.isLast()) {
			ingest.replyTo.tell(new Failure("content-id is null"));
		}
		else {
			ingest.replyTo.tell(Success.INSTANCE);
			lastReceived = true;
			log.infof("%s received last message", key);
		}

		return busyWorkers < maxWorkers ? next() : busy();

	}

	private Behavior<Command> onTrackError(TrackError trackError) {

		if (scheduler.getStatus() != Scheduler.SchedulerStatus.ERROR) {
			getContext().getSelf().tell(
				new PersistStatus(Scheduler.SchedulerStatus.ERROR, trackError.replyTo)
			);
		}
		else {
			trackError.replyTo().tell(Success.INSTANCE);
		}

		return next();
	}

	private Behavior<Command> onRestart(Restart restart) {
		failureTracked = false;
		lastRequest = LocalDateTime.now();
		getContext().getSelf().tell(
			new PersistStatus(Scheduler.SchedulerStatus.RUNNING, restart.replyTo)
		);

		return next();
	}

	private Behavior<Command> onDestroyQueue(DestroyQueue destroyQueue) {

		var persistStatusResponse = destroyQueue.persistStatusResponse();

		if (persistStatusResponse instanceof Success) {
			ClusterSingleton clusterSingleton = ClusterSingleton.get(getContext().getSystem());

			ActorRef<QueueManager.Command> queueManager = clusterSingleton.init(
				SingletonActor.of(QueueManager.create(), QueueManager.INSTANCE_NAME));

			var replyTo = getContext().messageAdapter(
				QueueManager.Response.class,
				DestroyQueueResult::new
			);

			queueManager.tell(new QueueManager.DestroyQueue(
					SchedulingKey.asString(key.tenantId(), key.scheduleId()),
					replyTo
				)
			);

		}
		else {
			log.warn("Graceful end failed");
			getContext().getSelf().tell(Stop.INSTANCE);
		}

		return Behaviors.same();
	}

	private Behavior<Command> onDestroyQueueResult(DestroyQueueResult destroyQueueResult) {
		getContext().getSelf().tell(Stop.INSTANCE);

		return Behaviors.same();
	}

	private Behavior<Command> onEnqueue(Command command) {
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
			EnrichPipelineException epe = failure.exception();
			log.error("enrich pipeline failure", epe);
			this.failureTracked = true;
			response.replyTo().tell(new Failure(ExceptionUtil.generateStackTrace(epe)));
		}

		busyWorkers--;
		consumers.remove(response.replyTo());

		return next();
	}

	private Behavior<Command> onTick() {
		var status = scheduler.getStatus();

		switch (status) {
			case ERROR:

				if (log.isTraceEnabled()) {
					log.tracef(
						"a manual operation is needed for scheduler with id %s",
						scheduler.getId()
					);
				}

				return Behaviors.same();

			case RUNNING:

				if (busyWorkers > 0) {
					if (log.isDebugEnabled()) {
						log.debugf("There are %s busy workers, for %s", busyWorkers, key);
					}

					if (isExpired()) {
						getContext().getSelf().tell(new PersistStatus(
								Scheduler.SchedulerStatus.STALE,
								getContext().getSystem().ignoreRef()
							)
						);
					}

					return Behaviors.same();
				}

				if (!failureTracked && lastReceived) {
					log.infof("%s is done", key);

					getContext().getSelf().tell(Close.INSTANCE);

					return Behaviors.same();
				}

				if (log.isTraceEnabled()) {
					log.tracef("check %s expiration", key);
				}

				if (isExpired()) {
					log.infof("%s ingestion is expired", key);

					getContext().getSelf().tell(Close.INSTANCE);

					return Behaviors.same();
				}

			case STALE:

				if (busyWorkers == 0) {
					log.infof("%s is recovered", key);
					getContext().getSelf().tell(
						new PersistStatus(
							Scheduler.SchedulerStatus.RUNNING,
							getContext().getSystem().ignoreRef()
						)
					);

					return Behaviors.same();

				}

			default:

				return Behaviors.same();
		}

	}

	private Behavior<Command> onFetchedSchedulerEnding(FetchedScheduler fetchedScheduler) {
		var scheduler = fetchedScheduler.scheduler();
		var exception = fetchedScheduler.exception();
		var replyTo = fetchedScheduler.replyTo();

		if (exception != null) {
			log.errorf(
				"Error during graceful ending, Scheduler with id %s cannot be persisted",
				this.scheduler.getId()
			);
		}
		else {
			this.scheduler = scheduler;
		}

		replyTo.tell(Success.INSTANCE);

		return Behaviors.same();
	}

	private Behavior<Command> onRefreshScheduler(RefreshScheduler refreshScheduler) {
		this.scheduler = refreshScheduler.scheduler;

		return Behaviors.withTimers(timers -> {
			timers.startTimerAtFixedRate(Tick.INSTANCE, Duration.ofSeconds(1));
			return this.next();
		});
	}

	private Behavior<Command> onStart() {
		ActorRef<Response> ignoreRef = getContext().getSystem().ignoreRef();

		getContext().pipeToSelf(
			SchedulingService.fetchScheduler(key),
			(scheduler, throwable) -> new FetchedScheduler(
				scheduler, (Exception) throwable, ignoreRef)
		);

		return waitFetchedScheduler();
	}

	private Behavior<Command> onPersistLastIngestionDate(PersistLastIngestionDate persistLastIngestionDate) {

		OffsetDateTime lastIngestionDate = persistLastIngestionDate.lastIngestionDate();
		ActorRef<Response> ignoreRef = getContext().getSystem().ignoreRef();

		getContext().pipeToSelf(
			SchedulingService.persistLastIngestionDate(key, lastIngestionDate),
			(scheduler, throwable) -> new FetchedScheduler(
				scheduler, (Exception) throwable, ignoreRef)
		);

		return waitFetchedScheduler();
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

	private Behavior<Command> onPersistStatus(PersistStatus persistStatus) {

		getContext().pipeToSelf(
			SchedulingService.persistStatus(key, persistStatus.status()),
			(scheduler, throwable) -> new FetchedScheduler(
				scheduler, (Exception) throwable, persistStatus.replyTo())
		);

		return waitFetchedScheduler();
	}

	private Behavior<Command> onPersistStatusEnding(PersistStatus persistStatus) {

		getContext().pipeToSelf(
			SchedulingService.persistStatus(key, persistStatus.status()),
			(scheduler, throwable) -> new FetchedScheduler(
				scheduler, (Exception) throwable, persistStatus.replyTo())
		);

		return Behaviors.same();
	}

	private Behavior<Command> onCloseHandlerReply(CloseHandlerReply closeHandlerReply) {
		var reply = closeHandlerReply.reply();

		if (reply instanceof EvaluateStatus.Success success) {
			this.endingStatus = success.status();
		}

		this.expectedReplies--;

		return closing();
	}

	private Behavior<Command> onGracefulEnd(GracefulEnd gracefulEnd) {

		var replyTo = getContext().messageAdapter(
			Response.class,
			DestroyQueue::new
		);

		getContext().getSelf().tell(new PersistStatus(gracefulEnd.status(), replyTo));

		return gracefulEnding();
	}

	private Behavior<Command> onClose() {

		var updateDatasource = getContext().spawnAnonymous(UpdateDatasource.create(getKey()));
		var deletionCompareNotifier = getContext().spawnAnonymous(DeletionCompareNotifier.create(
			getKey()));
		var evaluateStatus = getContext().spawnAnonymous(EvaluateStatus.create(getKey()));

		var replyTo = getContext().messageAdapter(
			Object.class,
			CloseHandlerReply::new
		);

		updateDatasource.tell(new UpdateDatasource.Start(getScheduler(), replyTo.narrow()));
		deletionCompareNotifier.tell(new DeletionCompareNotifier.Start(
			getScheduler(),
			replyTo.narrow()
		));
		evaluateStatus.tell(new EvaluateStatus.Start(getScheduler(), replyTo.narrow()));

		expectedReplies = 3;

		return closing();
	}

	private void doUpdateLastIngestionDate(DataPayload dataPayload) {
		OffsetDateTime parsingDate =
			OffsetDateTime.ofInstant(
				Instant.ofEpochMilli(dataPayload.getParsingDate()),
				ZoneOffset.UTC
			);

		var lastIngestionDate = this.scheduler.getLastIngestionDate();

		if (lastIngestionDate == null || !lastIngestionDate.isEqual(parsingDate)) {
			getContext().getSelf().tell(new PersistLastIngestionDate(parsingDate));
		}
	}

	private void logBehavior(String behavior) {
		log.infof("%s behavior is %s", key, behavior);
	}

	private boolean isExpired() {
		return Duration.between(lastRequest, LocalDateTime.now()).compareTo(timeout) > 0;
	}

	public sealed interface Command extends CborSerializable {}

	public sealed interface Response extends CborSerializable {}

	public enum Close implements Command {
		INSTANCE
	}

	public record Ingest(byte[] payload, ActorRef<Response> replyTo, String messageKey)
		implements Command {}

	public record TrackError(ActorRef<Response> replyTo) implements Command {}

	public record Restart(ActorRef<Response> replyTo) implements Command {}

	public enum Success implements Response {
		INSTANCE
	}

	public record GracefulEnd(Scheduler.SchedulerStatus status) implements Command {}

	private enum Start implements Command {
		INSTANCE
	}

	private enum Stop implements Command {
		INSTANCE
	}

	private enum Tick implements Command {
		INSTANCE
	}

	public record Failure(String error) implements Response {}

	private record PersistLastIngestionDate(OffsetDateTime lastIngestionDate) implements Command {}

	private record PersistStatus(Scheduler.SchedulerStatus status, ActorRef<Response> replyTo)
		implements Command {}

	private record RefreshScheduler(SchedulerDTO scheduler) implements Command {}

	private record EnrichPipelineResponseWrapper(EnrichPipeline.Response response)
		implements Command {}

	private record MessageGatewaySubscription(Receptionist.Listing listing) implements Command {}

	private record FetchedScheduler(
		SchedulerDTO scheduler,
		Exception exception,
		ActorRef<Response> replyTo
	) implements Command {}

	private record DestroyQueue(Response persistStatusResponse) implements Command {}

	private record DestroyQueueResult(QueueManager.Response response) implements Command {}

	private record CloseHandlerReply(Object reply) implements Command {}

}
