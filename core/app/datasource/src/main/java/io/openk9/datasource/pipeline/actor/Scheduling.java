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
import io.openk9.datasource.pipeline.service.SchedulingService;
import io.openk9.datasource.pipeline.service.dto.SchedulerDTO;
import io.openk9.datasource.pipeline.stages.closing.CloseStage;
import io.openk9.datasource.pipeline.stages.closing.Protocol;
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
import java.util.List;
import java.util.Set;
import java.util.function.Function;

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
	private final SchedulingKey schedulingKey;
	private final Deque<Command> lag = new ArrayDeque<>();
	private final Set<HeldMessage> heldMessages = new HashSet<>();
	private final Duration timeout;
	private final int workersPerNode;
	private final ActorRef<CloseStage.Command> closeStage;
	@Getter
	private SchedulerDTO scheduler;
	private LocalDateTime lastRequest = LocalDateTime.now();
	private boolean failureTracked = false;
	private boolean lastReceived = false;
	private int maxWorkers;
	private OffsetDateTime lastIngestionDate;

	@SafeVarargs
	public Scheduling(
		ActorContext<Command> context,
		SchedulingKey schedulingKey,
		Function<List<Protocol.Reply>, CloseStage.Aggregate> closeAggregator,
		Function<SchedulingKey, Behavior<Protocol.Command>>... closeHandlerFactories) {

		super(context);
		this.schedulingKey = schedulingKey;
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

		var replyTo = getContext().messageAdapter(
			CloseStage.Response.class,
			CloseStageResponse::new
		);

		this.closeStage = getContext().spawnAnonymous(CloseStage.create(
			getSchedulingKey(),
			replyTo,
			closeAggregator,
			closeHandlerFactories
		));

	}

	@SafeVarargs
	public static Behavior<Command> create(
		SchedulingKey schedulingKey,
		Function<List<Protocol.Reply>, CloseStage.Aggregate> closeAggregator,
		Function<SchedulingKey, Behavior<Protocol.Command>>... closeHandlerFactories) {

		return Behaviors
			.<Command>supervise(
				Behaviors.setup(ctx -> new Scheduling(
					ctx, schedulingKey, closeAggregator, closeHandlerFactories)
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
			.onMessage(PostProcess.class, this::onPostProcess)
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

	private Behavior<Command> closing() {
		logBehavior(CLOSING_BEHAVIOR);

		return Behaviors.withTimers(timers -> {

			timers.cancel(Tick.INSTANCE);

			return newReceiveBuilder()
				.onMessage(CloseStageResponse.class, this::onCloseStageResponse)
				.onMessage(GracefulEnd.class, this::onGracefulEnd)
				.onAnyMessage(this::onDiscard)
				.build();
		});
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
				schedulingKey
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

		byte[] payloadArray = ingest.payload;

		DataPayload dataPayload =
			Json.decodeValue(Buffer.buffer(payloadArray), DataPayload.class);

		if (dataPayload.getType() != null && dataPayload.getType() == PayloadType.HALT) {
			log.warnf(
				"The publisher has sent an HALT message. So %s will be cancelled.",
				schedulingKey
			);

			getContext().getSelf().tell(new GracefulEnd(Scheduler.SchedulerStatus.FAILURE));
		}

		if (dataPayload.getContentId() != null) {

			String contentId = dataPayload.getContentId();
			var parsingDateTimeStamp = dataPayload.getParsingDate();

			ActorRef<EnrichPipeline.Response> replyTo = getContext()
				.messageAdapter(EnrichPipeline.Response.class, PostProcess::new);

			ActorSystem<Void> system = getContext().getSystem();

			ClusterSharding clusterSharding = ClusterSharding.get(system);

			EntityRef<EnrichPipeline.Command> enrichPipelineRef = clusterSharding.entityRefFor(
				EnrichPipeline.ENTITY_TYPE_KEY,
				EnrichPipelineKey.of(schedulingKey, contentId, ingest.messageKey()).asString()
			);

			var heldMessage = new HeldMessage(
				getSchedulingKey(),
				contentId,
				ingest.messageKey(),
				parsingDateTimeStamp,
				ingest.replyTo()
			);

			enrichPipelineRef.tell(new EnrichPipeline.Setup(
				replyTo,
				heldMessage,
					Json.encodeToBuffer(dataPayload).getBytes(),
					scheduler
				)
			);

			heldMessages.add(heldMessage);

		}
		else if (!dataPayload.isLast()) {
			ingest.replyTo.tell(new Failure("content-id is null"));
		}
		else {
			ingest.replyTo.tell(Success.INSTANCE);
			lastReceived = true;
			log.infof("%s received last message", schedulingKey);
		}

		return heldMessages.size() < maxWorkers ? next() : busy();

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
				SchedulingKey.asString(schedulingKey.tenantId(), schedulingKey.scheduleId()),
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

	private Behavior<Command> onPostProcess(PostProcess postProcess) {
		EnrichPipeline.Response response = postProcess.response;

		var heldMessage = response.heldMessage();
		var replyTo = heldMessage.replyTo();

		heldMessages.remove(heldMessage);

		if (response instanceof EnrichPipeline.Success success) {
			log.infof(
				"enrich pipeline success for content-id %s replyTo %s",
				success.contentId(), replyTo
			);

			replyTo.tell(Success.INSTANCE);

			OffsetDateTime parsingDate =
				OffsetDateTime.ofInstant(
					Instant.ofEpochMilli(heldMessage.parsingDate()),
					ZoneOffset.UTC
				);


			getContext().getSelf().tell(new PersistLastIngestionDate(parsingDate));

			return newReceiveBuilder()
				.onMessage(
					PersistLastIngestionDate.class,
					this::isNewLastIngestionDate,
					this::onPersistLastIngestionDate
				)
				.onMessage(
					PersistLastIngestionDate.class,
					this::isSameLastIngestionDate,
					(__) -> this.next()
				)
				.onAnyMessage(this::onEnqueue)
				.build();

		}
		else if (response instanceof EnrichPipeline.Failure failure) {
			EnrichPipelineException epe = failure.exception();
			log.error("enrich pipeline failure", epe);
			this.failureTracked = true;
			replyTo.tell(new Failure(ExceptionUtil.generateStackTrace(epe)));

		}

		return next();
	}

	private boolean isNewLastIngestionDate(PersistLastIngestionDate msg) {
		return this.lastIngestionDate == null ||
			   !this.lastIngestionDate.isEqual(msg.lastIngestionDate());
	}

	private boolean isSameLastIngestionDate(PersistLastIngestionDate msg) {
		return this.lastIngestionDate != null &&
			   this.lastIngestionDate.isEqual(msg.lastIngestionDate());
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
							schedulingKey
						);
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
					log.infof("%s is done", schedulingKey);

					getContext().getSelf().tell(Close.INSTANCE);

					return Behaviors.same();
				}

				if (log.isTraceEnabled()) {
					log.tracef("check %s expiration", schedulingKey);
				}

				if (isExpired()) {
					log.infof("%s ingestion is expired", schedulingKey);

					getContext().getSelf().tell(Close.INSTANCE);

					return Behaviors.same();
				}

			case STALE:

				if (heldMessages.isEmpty()) {
					log.infof("%s is recovered", schedulingKey);
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

		// TODO: tell failure on exception
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
			SchedulingService.fetchScheduler(schedulingKey),
			(scheduler, throwable) -> new FetchedScheduler(
				scheduler, (Exception) throwable, ignoreRef)
		);

		return waitFetchedScheduler();
	}

	private Behavior<Command> onPersistLastIngestionDate(PersistLastIngestionDate persistLastIngestionDate) {

		OffsetDateTime lastIngestionDate = persistLastIngestionDate.lastIngestionDate();
		ActorRef<Response> ignoreRef = getContext().getSystem().ignoreRef();

		getContext().pipeToSelf(
			SchedulingService.persistLastIngestionDate(schedulingKey, lastIngestionDate),
			(scheduler, throwable) -> new FetchedScheduler(
				scheduler, (Exception) throwable, ignoreRef)
		);

		this.lastIngestionDate = lastIngestionDate;

		return waitFetchedScheduler();
	}

	private Behavior<Command> onStop() {
		logBehavior(STOPPED_BEHAVIOR);
		return Behaviors.stopped();
	}

	private Behavior<Command> onPostStop(PostStop postStop) {
		Set<ActorRef<Response>> released = new HashSet<>();

		for (HeldMessage heldMessage : heldMessages) {
			var replyTo = heldMessage.replyTo();

			if (!released.contains(replyTo)) {
				replyTo.tell(new Failure("stopped for unexpected reason"));
				released.add(replyTo);
			}
		}

		return Behaviors.same();
	}

	private Behavior<Command> onPersistStatus(PersistStatus persistStatus) {

		getContext().pipeToSelf(
			SchedulingService.persistStatus(schedulingKey, persistStatus.status()),
			(scheduler, throwable) -> new FetchedScheduler(
				scheduler, (Exception) throwable, persistStatus.replyTo())
		);

		return waitFetchedScheduler();
	}

	private Behavior<Command> onPersistStatusEnding(PersistStatus persistStatus) {

		getContext().pipeToSelf(
			SchedulingService.persistStatus(schedulingKey, persistStatus.status()),
			(scheduler, throwable) -> new FetchedScheduler(
				scheduler, (Exception) throwable, persistStatus.replyTo())
		);

		return Behaviors.same();
	}

	private Behavior<Command> onClose() {

		this.closeStage.tell(new CloseStage.Start(getScheduler()));

		return closing();
	}

	private Behavior<Command> onCloseStageResponse(CloseStageResponse closeStageResponse) {
		var response = closeStageResponse.response();

		if (response instanceof CloseStage.Aggregate aggregate) {

			getContext()
				.getSelf()
				.tell(new GracefulEnd(aggregate.status()));
		}
		else {
			log.warnf("Unexpected response from CloseStage for %s", getSchedulingKey());
		}

		return Behaviors.same();
	}

	private Behavior<Command> onGracefulEnd(GracefulEnd gracefulEnd) {

		var replyTo = getContext().messageAdapter(
			Response.class,
			DestroyQueue::new
		);

		getContext().getSelf().tell(new PersistStatus(gracefulEnd.status(), replyTo));

		return gracefulEnding();
	}

	private void logBehavior(String behavior) {
		log.infof("%s behavior is %s", schedulingKey, behavior);
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

	private record PostProcess(EnrichPipeline.Response response)
		implements Command {}

	private record MessageGatewaySubscription(Receptionist.Listing listing) implements Command {}

	private record FetchedScheduler(
		SchedulerDTO scheduler,
		Exception exception,
		ActorRef<Response> replyTo
	) implements Command {}

	private record DestroyQueue(Response persistStatusResponse) implements Command {}

	private record DestroyQueueResult(QueueManager.Response response) implements Command {}

	private record CloseStageResponse(CloseStage.Response response) implements Command {}

	public record HeldMessage(
		SchedulingKey schedulingKey,
		String contentId,
		String messageKey,
		long parsingDate,
		ActorRef<Scheduling.Response> replyTo
	) {}


}
