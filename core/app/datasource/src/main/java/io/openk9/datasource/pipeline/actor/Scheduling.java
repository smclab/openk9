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
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.ReceiveBuilder;
import akka.actor.typed.javadsl.TimerScheduler;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;
import akka.cluster.typed.Subscribe;
import com.typesafe.config.Config;
import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.actor.AkkaUtils;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.common.AggregateBehavior;
import io.openk9.datasource.pipeline.service.SchedulingService;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.closing.CloseStage;
import io.openk9.datasource.pipeline.stages.working.HeldMessage;
import io.openk9.datasource.pipeline.stages.working.WorkStage;
import io.openk9.datasource.util.CborSerializable;
import io.quarkus.runtime.util.ExceptionUtil;
import lombok.Getter;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Scheduling extends AbstractBehavior<Scheduling.Command> {

	public static final String SCHEDULING_TIMEOUT = "io.openk9.scheduling.timeout";
	public static final String WORKERS_PER_NODE = "io.openk9.scheduling.workers-per-node";
	public static final int WORKERS_PER_NODE_DEFAULT = 2;
	private static final String SETTING_UP_BEHAVIOR = "Setting up";
	private static final String READY_BEHAVIOR = "Ready";
	private static final String BUSY_BEHAVIOR = "Busy";
	private static final String NEXT_BEHAVIOR = "Next";
	private static final String CLOSING_BEHAVIOR = "Closing";
	private static final String GRACEFUL_ENDING_BEHAVIOR = "Graceful Ending";
	private static final String STOPPED_BEHAVIOR = "Stopped";
	private static final Logger log = Logger.getLogger(Scheduling.class);
	@Getter
	private final ShardingKey shardingKey;
	private final Deque<Command> lag = new ArrayDeque<>();
	private final Map<HeldMessage, ActorRef<Response>> heldMessages = new HashMap<>();
	private final Duration timeout;
	private final int workersPerNode;
	private final TimerScheduler<Command> timers;
	private final ActorRef<WorkStage.Command> workStage;
	private final ActorRef<AggregateBehavior.Command> closeStage;
	@Getter
	private SchedulerDTO scheduler;
	private LocalDateTime lastRequest = LocalDateTime.now();
	private boolean failureTracked = false;
	private boolean lastReceived = false;
	private int maxWorkers;
	private OffsetDateTime lastIngestionDate;
	private int nodes = 0;

	public Scheduling(
		ActorContext<Command> context,
		TimerScheduler<Command> timers,
		ShardingKey shardingKey,
		WorkStage.Configurations workStageConfigurations,
		CloseStage.Configurations closeStageConfigurations) {

		super(context);
		this.shardingKey = shardingKey;
		this.timeout = getTimeout(context);
		this.workersPerNode = getWorkersPerNode(context);
		this.maxWorkers = workersPerNode;
		this.timers = timers;

		var cluster = Cluster.get(getContext().getSystem());
		var subscriber = getContext().messageAdapter(
			akka.cluster.ClusterEvent.MemberEvent.class,
			ClusterEvent::new
		);

		cluster.subscriptions().tell(Subscribe.create(
			subscriber,
			akka.cluster.ClusterEvent.MemberEvent.class
		));

		getContext().getSelf().tell(Setup.INSTANCE);

		var workStageAdapter = getContext().messageAdapter(
			WorkStage.Response.class,
			WorkStageResponse::new
		);

		this.workStage = getContext().spawnAnonymous(WorkStage.create(
			getShardingKey(),
			workStageAdapter,
			workStageConfigurations
		));

		var closeStageAdapter = getContext().messageAdapter(
			AggregateBehavior.Response.class,
			CloseStageResponse::new
		);

		this.closeStage = getContext().spawnAnonymous(CloseStage.create(
			getShardingKey(),
			closeStageAdapter,
			closeStageConfigurations
		));

	}

	public static Behavior<Command> create(
		ShardingKey shardingKey,
		WorkStage.Configurations workStageConfigurations,
		CloseStage.Configurations closeStageConfigurations) {

		return Behaviors.<Command>supervise(
				Behaviors.setup(ctx ->
					Behaviors.withTimers(timers -> new Scheduling(
						ctx, timers, shardingKey,
						workStageConfigurations,
						closeStageConfigurations
					))))
			.onFailure(SupervisorStrategy.restartWithBackoff(
					Duration.ofSeconds(3),
					Duration.ofSeconds(60),
					0.2
				)
			);
	}

	@Override
	public Receive<Command> createReceive() {
		return settingUp();
	}

	@Override
	public ReceiveBuilder<Command> newReceiveBuilder() {
		return super.newReceiveBuilder()
			.onMessage(ClusterEvent.class, this::onClusterEvent)
			.onMessage(UpdateScheduler.class, this::onUpdateScheduler)
			.onMessageEquals(Start.INSTANCE, this::onStart)
			.onMessageEquals(StartInternalClock.INSTANCE, this::onStartInternalClock)
			.onMessageEquals(StopInternalClock.INSTANCE, this::onStopInternalClock)
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
			.onMessage(WorkStageResponse.class, this::onWorkStageResponse)
			.onMessage(
				TrackDate.class,
				this::isNewLastIngestionDate,
				this::onTrackDate
			)
			.onMessage(
				TrackDate.class,
				this::isSameLastIngestionDate,
				(__) -> this.next()
			)
			.onMessage(UpdateStatus.class, this::onUpdateStatus)
			.onMessageEquals(Close.INSTANCE, this::onClose)
			.onMessage(Halt.class, this::onHalt)
			.onMessage(PersistException.class, this::onPersistException)
			.onMessage(TrackFailure.class, this::onTrackFailure)
			.onMessage(GracefulEnd.class, this::onGracefulEnd);
	}

	private Behavior<Command> onTrackFailure(TrackFailure trackFailure) {
		if (trackFailure.response() instanceof Success) {
			this.failureTracked = true;
		}

		return next();
	}

	private Behavior<Command> onPersistException(PersistException persistException) {

		if (!failureTracked) {
			var replyTo = getContext().messageAdapter(Response.class, TrackFailure::new);

			getContext().pipeToSelf(
				SchedulingService.persistErrorDescription(
					shardingKey, persistException.exception()),
				(scheduler, throwable) -> new UpdateScheduler(
					scheduler, (Exception) throwable, replyTo)
			);
		}

		return Behaviors.same();
	}

	private Behavior<Command> onHalt(Halt halt) {

		var replyTo = getContext().messageAdapter(
			Response.class,
			__ -> new GracefulEnd(Scheduler.SchedulerStatus.FAILURE)
		);

		getContext().pipeToSelf(
			SchedulingService.persistErrorDescription(shardingKey, halt.exception()),
			(scheduler, throwable) -> new UpdateScheduler(
				scheduler, (Exception) throwable, replyTo)
		);

		return newReceiveBuilder()
			.onMessage(GracefulEnd.class, this::onGracefulEnd)
			.build();
	}

	private Receive<Command> settingUp() {
		logBehavior(SETTING_UP_BEHAVIOR);

		return newReceiveBuilder()
			.onMessageEquals(Setup.INSTANCE, this::onSetup)
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

	private Behavior<Command> closing() {
		logBehavior(CLOSING_BEHAVIOR);

		return newReceiveBuilder()
			.onMessage(CloseStageResponse.class, this::onCloseStageResponse)
			.onMessage(GracefulEnd.class, this::onGracefulEnd)
			.onAnyMessage(this::onDiscard)
			.build();
	}

	private Behavior<Command> gracefulEnding() {
		logBehavior(GRACEFUL_ENDING_BEHAVIOR);

		return newReceiveBuilder()
			.onMessage(DestroyQueue.class, this::onDestroyQueue)
			.onMessage(DestroyQueueResult.class, this::onDestroyQueueResult)
			.onAnyMessage(this::onDiscard)
			.build();
	}

	private Behavior<Command> onStart() {

		getContext().getSelf().tell(StartInternalClock.INSTANCE);

		return next();
	}

	private Behavior<Command> onUpdateScheduler(UpdateScheduler updateScheduler) {

		var scheduler = updateScheduler.scheduler();
		var exception = updateScheduler.exception();
		var replyTo = updateScheduler.replyTo();

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

			this.scheduler = scheduler;

			replyTo.tell(Success.INSTANCE);
		}

		return Behaviors.same();
	}

	private Behavior<Command> onClusterEvent(ClusterEvent clusterEvent) {

		var memberEvent = clusterEvent.event();

		if (memberEvent instanceof akka.cluster.ClusterEvent.MemberUp) {
			nodes++;
		}
		else if (memberEvent instanceof akka.cluster.ClusterEvent.MemberDowned) {
			nodes--;
		}

		maxWorkers = workersPerNode * nodes;

		if (log.isDebugEnabled()) {
			log.debugf(
				"Max Workers updated to %d for %s",
				maxWorkers,
				shardingKey
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

		this.workStage.tell(new WorkStage.StartWorker(
			getScheduler(),
			ingest.payload(),
				ingest.replyTo()
			)
		);

		return Behaviors.same();
	}

	private Behavior<Command> onTrackError(TrackError trackError) {

		if (scheduler.getStatus() != Scheduler.SchedulerStatus.ERROR) {
			getContext().getSelf().tell(
				new UpdateStatus(Scheduler.SchedulerStatus.ERROR, trackError.replyTo)
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
			new UpdateStatus(Scheduler.SchedulerStatus.RUNNING, restart.replyTo)
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
				ShardingKey.asString(shardingKey.tenantId(), shardingKey.scheduleId()),
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

	private Behavior<Command> onWorkStageResponse(WorkStageResponse workStageResponse) {
		var response = workStageResponse.response();

		if (response instanceof WorkStage.Invalid invalid) {

			var requester = invalid.requester();

			requester.tell(new Failure(invalid.errorMessage()));

		}
		else if (response instanceof WorkStage.Halt halt) {

			var requester = halt.requester();

			requester.tell(Success.INSTANCE);
			getContext()
				.getSelf()
				.tell(new Scheduling.GracefulEnd(Scheduler.SchedulerStatus.FAILURE));

		}
		else if (response instanceof WorkStage.Last last) {

			var requester = last.requester();

			requester.tell(Success.INSTANCE);
			lastReceived = true;
			log.infof("%s received last message", shardingKey);

		}
		else if (response instanceof WorkStage.Working working) {

			var requester = working.requester();
			var heldMessage = working.heldMessage();

			heldMessages.put(heldMessage, requester);

		}
		else if (response instanceof WorkStage.Done done) {

			var heldMessage = done.heldMessage();
			var replyTo = heldMessages.remove(heldMessage);

			log.infof("work done for %s", heldMessage, replyTo);

			replyTo.tell(Success.INSTANCE);

			OffsetDateTime parsingDate =
				OffsetDateTime.ofInstant(
					Instant.ofEpochMilli(heldMessage.parsingDate()),
					ZoneOffset.UTC
				);

			getContext().getSelf().tell(new TrackDate(parsingDate));

			return busy();

		}
		else if (response instanceof WorkStage.Failed failed) {

			var heldMessage = failed.heldMessage();
			var exception = failed.exception();
			var replyTo = heldMessages.remove(heldMessage);

			log.errorf(exception, "work failed for %s", heldMessage);

			getContext().getSelf().tell(new PersistException(exception));
			this.failureTracked = true;

			replyTo.tell(new Failure("work stage failed"));

		}
		else {
			log.warn("unknown response type");
		}

		return heldMessages.size() < maxWorkers ? next() : busy();
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

				if (!heldMessages.isEmpty()) {
					if (log.isDebugEnabled()) {
						log.debugf("There are %s busy workers, for %s", heldMessages.size(),
							shardingKey
						);
					}

					if (isExpired()) {
						getContext().getSelf().tell(new UpdateStatus(
								Scheduler.SchedulerStatus.STALE,
								getContext().getSystem().ignoreRef()
							)
						);
					}

					return Behaviors.same();
				}

				if (!failureTracked && lastReceived) {
					log.infof("%s is done", shardingKey);

					getContext().getSelf().tell(Close.INSTANCE);

					return Behaviors.same();
				}

				if (log.isTraceEnabled()) {
					log.tracef("check %s expiration", shardingKey);
				}

				if (isExpired()) {
					log.infof("%s ingestion is expired", shardingKey);

					getContext().getSelf().tell(Close.INSTANCE);

					return Behaviors.same();
				}

			case STALE:

				if (heldMessages.isEmpty()) {
					log.infof("%s is recovered", shardingKey);
					getContext().getSelf().tell(
						new UpdateStatus(
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

	private Behavior<Command> onStartInternalClock() {

		timers.startTimerAtFixedRate(Tick.INSTANCE, Duration.ofSeconds(1));

		return this.next();
	}

	private Behavior<Command> onStopInternalClock() {

		timers.cancel(Tick.INSTANCE);

		return Behaviors.same();
	}

	private Behavior<Command> onSetup() {
		ActorRef<Response> ignoreRef = getContext().getSystem().ignoreRef();

		var startWrapper = getStartWrapper(ignoreRef);

		getContext().pipeToSelf(
			SchedulingService.fetchScheduler(shardingKey),
			(scheduler, throwable) -> new UpdateScheduler(
				scheduler, (Exception) throwable, startWrapper)
		);

		return settingUp();
	}

	private Behavior<Command> onTrackDate(TrackDate trackDate) {

		OffsetDateTime lastIngestionDate = trackDate.lastIngestionDate();
		ActorRef<Response> ignoreRef = getContext().getSystem().ignoreRef();

		var startWrapper = getStartWrapper(ignoreRef);

		this.lastIngestionDate = lastIngestionDate;

		getContext().pipeToSelf(
			SchedulingService.persistLastIngestionDate(shardingKey, lastIngestionDate),
			(scheduler, throwable) -> new UpdateScheduler(
				scheduler, (Exception) throwable, startWrapper)
		);

		return settingUp();
	}

	private Behavior<Command> onStop() {
		logBehavior(STOPPED_BEHAVIOR);
		return Behaviors.stopped();
	}

	private Behavior<Command> onPostStop(PostStop postStop) {
		Set<ActorRef<Response>> released = new HashSet<>();

		for (ActorRef<Response> replyTo : heldMessages.values()) {

			if (!released.contains(replyTo)) {
				replyTo.tell(new Failure("stopped for unexpected reason"));
				released.add(replyTo);
			}
		}

		return Behaviors.same();
	}

	private Behavior<Command> onUpdateStatus(UpdateStatus updateStatus) {

		var startWrapper = getStartWrapper(updateStatus.replyTo());

		getContext().pipeToSelf(
			SchedulingService.persistStatus(shardingKey, updateStatus.status()),
			(scheduler, throwable) -> new UpdateScheduler(
				scheduler, (Exception) throwable, startWrapper)
		);

		return settingUp();
	}

	private Behavior<Command> onClose() {

		getContext().getSelf().tell(StopInternalClock.INSTANCE);

		this.closeStage.tell(new CloseStage.Start(getScheduler()));

		return closing();
	}

	private Behavior<Command> onCloseStageResponse(CloseStageResponse closeStageResponse) {
		var response = closeStageResponse.response();

		if (response instanceof CloseStage.Aggregated aggregated) {

			getContext()
				.getSelf()
				.tell(new GracefulEnd(aggregated.status()));
		}
		else {
			log.warnf("Unexpected response from CloseStage for %s", getShardingKey());
		}

		return Behaviors.same();
	}

	private Behavior<Command> onGracefulEnd(GracefulEnd gracefulEnd) {

		var replyTo = getContext().messageAdapter(
			Response.class,
			DestroyQueue::new
		);

		getContext().pipeToSelf(
			SchedulingService.persistStatus(shardingKey, gracefulEnd.status()),
			(scheduler, throwable) -> new UpdateScheduler(
				scheduler, (Exception) throwable, replyTo)
		);

		return gracefulEnding();
	}

	private ActorRef<Response> getStartWrapper(ActorRef<Response> wrappedReplyTo) {
		getContext().getSelf().tell(StopInternalClock.INSTANCE);
		return getContext().messageAdapter(
			Response.class, response -> {
				wrappedReplyTo.tell(response);
				return Start.INSTANCE;
			});
	}

	private void logBehavior(String behavior) {
		log.infof("%s behavior is %s", shardingKey, behavior);
	}

	private boolean isExpired() {
		return Duration.between(lastRequest, LocalDateTime.now()).compareTo(timeout) > 0;
	}

	private boolean isNewLastIngestionDate(TrackDate msg) {
		return this.lastIngestionDate == null ||
			   !this.lastIngestionDate.isEqual(msg.lastIngestionDate());
	}

	private boolean isSameLastIngestionDate(TrackDate msg) {
		return this.lastIngestionDate != null &&
			   this.lastIngestionDate.isEqual(msg.lastIngestionDate());
	}

	public sealed interface Command extends CborSerializable {}

	public sealed interface Response extends CborSerializable {}

	public enum Close implements Command {
		INSTANCE
	}

	public record Ingest(byte[] payload, ActorRef<Response> replyTo)
		implements Command {}

	public record TrackError(ActorRef<Response> replyTo) implements Command {}

	public record Restart(ActorRef<Response> replyTo) implements Command {}

	public enum Success implements Response {
		INSTANCE
	}

	public record Failure(String error) implements Response {}

	public record GracefulEnd(Scheduler.SchedulerStatus status) implements Command {}

	public record Halt(Exception exception) implements Command {}

	private enum Setup implements Command {
		INSTANCE
	}

	private enum Stop implements Command {
		INSTANCE
	}

	private enum Tick implements Command {
		INSTANCE
	}
	private enum StartInternalClock implements Command {
		INSTANCE
	}

	private enum StopInternalClock implements Command {
		INSTANCE
	}

	private enum Start implements Command {
		INSTANCE
	}

	private record TrackDate(OffsetDateTime lastIngestionDate) implements Command {}

	private record UpdateStatus(Scheduler.SchedulerStatus status, ActorRef<Response> replyTo)
		implements Command {}

	private record UpdateScheduler(
		SchedulerDTO scheduler,
		Exception exception,
		ActorRef<Response> replyTo
	) implements Command {}

	private record DestroyQueue(Response persistStatusResponse) implements Command {}

	private record DestroyQueueResult(QueueManager.Response response) implements Command {}

	private record WorkStageResponse(WorkStage.Response response)
		implements Command {}

	private record CloseStageResponse(CloseStage.Response response) implements Command {}

	private record ClusterEvent(akka.cluster.ClusterEvent.MemberEvent event) implements Command {}

	private record PersistException(WorkStageException exception) implements Command {}

	private record TrackFailure(Response response) implements Command {}

}
