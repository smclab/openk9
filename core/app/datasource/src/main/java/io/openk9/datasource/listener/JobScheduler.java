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

package io.openk9.datasource.listener;

import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.MessageGateway;
import io.openk9.datasource.pipeline.actor.Scheduling;
import io.openk9.datasource.pipeline.actor.SchedulingEntityType;
import io.openk9.datasource.pipeline.base.BasePipeline;
import io.openk9.datasource.util.CborSerializable;
import io.openk9.datasource.util.SchedulerUtil;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.receptionist.Receptionist;
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding;
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityRef;
import org.apache.pekko.extension.quartz.QuartzSchedulerTypedExtension;
import org.jboss.logging.Logger;
import scala.Option;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class JobScheduler {

	public sealed interface Command extends CborSerializable {}
	private static final Logger log = Logger.getLogger(JobScheduler.class);

	public static Behavior<Command> create(
		List<ScheduleDatasource> schedulatedJobs) {

		return Behaviors.setup(ctx -> {

			log.info("setup job-scheduler");

			QuartzSchedulerTypedExtension quartzSchedulerTypedExtension =
				QuartzSchedulerTypedExtension.get(ctx.getSystem());

			ActorRef<Receptionist.Listing> listingActorRef =
				ctx.messageAdapter(
					Receptionist.Listing.class,
					MessageGatewaySubscription::new
				);

			ctx
				.getSystem()
				.receptionist()
				.tell(Receptionist.subscribe(MessageGateway.SERVICE_KEY, listingActorRef));

			return setup(
				ctx,
				quartzSchedulerTypedExtension,
				new ArrayDeque<>(schedulatedJobs)
			);


		});
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		ActorRef<MessageGateway.Command> messageGateway,
		List<String> jobNames) {

		return Behaviors.receive(Command.class)
			.onMessage(
				MessageGatewaySubscription.class,
				mgs -> onInitialMessageGatewaySubscription(ctx,
					mgs,
					quartzSchedulerTypedExtension,
					jobNames
				)
			)
			.onMessage(
				CreateNewScheduler.class,
				cns -> onCreateNewScheduler(cns, ctx) )
			.onMessage(ScheduleDatasource.class, ad -> onAddDatasource(ad, ctx))
			.onMessage(UnScheduleDatasource.class, rd -> onRemoveDatasource(rd, ctx))
			.onMessage(TriggerDatasource.class, jm -> onTriggerDatasource(jm, ctx))
			.onMessage(TriggerDatasourcePurge.class, tdp -> onTriggerDatasourcePurge(tdp, ctx))
			.onMessage(
				ScheduleDatasourceInternal.class,
				sdi -> onScheduleDatasourceInternal(sdi,
					ctx,
					quartzSchedulerTypedExtension,
					messageGateway,
					jobNames
				)
			)
			.onMessage(
				UnScheduleJobInternal.class,
				rd -> onUnscheduleJobInternal(rd,
					ctx,
					quartzSchedulerTypedExtension,
					messageGateway,
					jobNames
				)
			)
			.onMessage(
				TriggerDatasourceInternal.class,
				tdi -> onTriggerDatasourceInternal(tdi, ctx)
			)
			.onMessage(
				InvokePluginDriverInternal.class,
				ipdi -> onInvokePluginDriverInternal(ipdi, ctx)
			)
			.onMessage(StartSchedulerInternal.class, ssi -> onStartScheduler(ctx, ssi))
			.onMessage(CopyIndexTemplate.class, cit -> onCopyIndexTemplate(ctx, cit))
			.onMessage(
				PersistSchedulerInternal.class,
				pndi -> onPersistSchedulerInternal(ctx, pndi)
			)
			.onMessage(
				PersistSchedulerResponse.class,
				res -> onPersistSchedulerResponse(ctx, messageGateway, res)
			)
			.onMessage(
				InvokePluginDriverResponse.class,
				res -> onInvokePluginDriverResponse(ctx, res)
			)
			.onMessage(HaltScheduling.class, cs -> onHaltScheduling(ctx, cs))
			.build();

	}

	private static <T> boolean isLocalActorRef(ActorRef<T> actorRef) {
		return actorRef.path().address().port().isEmpty();
	}

	private static Behavior<Command> onAddDatasource(
		ScheduleDatasource addDatasource, ActorContext<Command> ctx) {

		long datasourceId = addDatasource.datasourceId;
		String tenantName = addDatasource.tenantName;

		ctx.getSelf().tell(
			new ScheduleDatasourceInternal(
				tenantName, datasourceId,
				JobType.TRIGGER,
				addDatasource.schedulable,
				addDatasource.schedulingCron,
				null
			)
		);

		ctx.getSelf().tell(
			new ScheduleDatasourceInternal(
				tenantName, datasourceId,
				JobType.REINDEX,
				addDatasource.reindexable,
				addDatasource.reindexingCron,
				null
			)
		);

		ctx.getSelf().tell(
			new ScheduleDatasourceInternal(
				tenantName, datasourceId,
				JobType.PURGE,
				addDatasource.purgeable,
				addDatasource.purgingCron,
				addDatasource.purgeMaxAge
			)
		);

		return Behaviors.same();

	}

	private static Behavior<Command> onCopyIndexTemplate(
		ActorContext<Command> ctx, CopyIndexTemplate cit) {

		var scheduler = cit.scheduler();

		ctx.pipeToSelf(
			JobSchedulerService.copyIndexTemplate(scheduler),
			(ignore, throwable) -> new PersistSchedulerInternal(scheduler, throwable)
		);

		return Behaviors.same();
	}

	private static Behavior<Command> onCreateNewScheduler(
		CreateNewScheduler createNewScheduler, ActorContext<Command> ctx) {

		var triggerType = createNewScheduler.triggerType;
		var datasource = createNewScheduler.datasource;
		var tenantName = createNewScheduler.tenantName;
		var startIngestionDate = createNewScheduler.startIngestionDate;
		var throwable1 = createNewScheduler.throwable;

		var scheduleId = UUID.randomUUID().toString();

		Scheduler scheduler = new Scheduler();
		scheduler.setScheduleId(scheduleId);
		scheduler.setDatasource(datasource);
		scheduler.setOldDataIndex(datasource.getDataIndex());
		scheduler.setStatus(Scheduler.SchedulerStatus.RUNNING);

		// The scheduler cannot start, so a scheduler in FAILURE state is created
		if (throwable1 != null) {

			log.errorf(
				throwable1,
				"The scheduler cannot start, so a scheduler with schedule-id %s " +
					"in FAILURE state is created.",
				scheduler.getScheduleId()
			);

			var errorDescription = SchedulerUtil.getErrorDescription(throwable1);

			scheduler.setStatus(Scheduler.SchedulerStatus.FAILURE);
			scheduler.setErrorDescription(errorDescription);

			JobSchedulerService.persistScheduler(tenantName, scheduler);

			return Behaviors.same();
		}

		boolean reindex = triggerType == TriggerType.SimpleTriggerType.REINDEX;

		DataIndex oldDataIndex = scheduler.getOldDataIndex();

		log.infof("A Scheduler with schedule-id %s is starting", scheduler.getScheduleId());

		if (oldDataIndex == null || reindex) {

			String newDataIndexName = datasource.getId() + "-data-" + scheduler.getScheduleId();

			DataIndex newDataIndex = new DataIndex();
			newDataIndex.setName(newDataIndexName);
			newDataIndex.setDatasource(datasource);
			scheduler.setNewDataIndex(newDataIndex);

			if (oldDataIndex != null) {
				Set<DocType> docTypes = oldDataIndex.getDocTypes();

				if (docTypes != null && !docTypes.isEmpty()) {
					ctx.getSelf().tell(
						new CopyIndexTemplate(scheduler));

					return Behaviors.same();
				}
			}
		}

		ctx.pipeToSelf(
			JobSchedulerService.persistScheduler(tenantName, scheduler),
			(response, throwable) ->
				new PersistSchedulerResponse(scheduler, startIngestionDate, throwable)
		);

		return Behaviors.same();
	}

	private static Behavior<Command> onHaltScheduling(
		ActorContext<Command> ctx, HaltScheduling cs) {

		Scheduler scheduler = cs.scheduler;
		var datasource = scheduler.getDatasource();
		var tenantId = datasource.getTenant();

		var exception = cs.exception;

		String scheduleId = scheduler.getScheduleId();

		ClusterSharding clusterSharding = ClusterSharding.get(ctx.getSystem());

		EntityRef<Scheduling.Command> schedulingRef = clusterSharding.entityRefFor(
			BasePipeline.ENTITY_TYPE_KEY, ShardingKey.asString(tenantId, scheduleId));

		schedulingRef.tell(
			new Scheduling.Halt(new InvokePluginDriverException(exception)));

		return Behaviors.same();
	}

	private static Behavior<Command> onInitialMessageGatewaySubscription(
		ActorContext<Command> ctx, MessageGatewaySubscription mgs,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		List<String> jobNames) {

		Optional<ActorRef<MessageGateway.Command>> actorRefOptional = mgs.listing
			.getServiceInstances(MessageGateway.SERVICE_KEY)
			.stream()
			.filter(JobScheduler::isLocalActorRef)
			.findFirst();

		if (actorRefOptional.isPresent()) {
			return initial(
				ctx, quartzSchedulerTypedExtension,
				actorRefOptional.get(), jobNames
			);
		}

		return Behaviors.same();

	}

	private static Behavior<Command> onInvokePluginDriverInternal(
		InvokePluginDriverInternal ipdi, ActorContext<Command> ctx) {

		var scheduler = ipdi.scheduler;
		var startIngestionDate = ipdi.startIngestionDate;

		Datasource datasource = scheduler.getDatasource();
		PluginDriver pluginDriver = datasource.getPluginDriver();

		OffsetDateTime lastIngestionDate;

		if (scheduler.getNewDataIndex() != null
			|| datasource.getLastIngestionDate() == null) {

			lastIngestionDate = OffsetDateTime.ofInstant(
				Instant.ofEpochMilli(0), ZoneId.systemDefault());
		}
		else {

			lastIngestionDate = startIngestionDate == null ?
				datasource.getLastIngestionDate() : startIngestionDate;
		}

		switch (pluginDriver.getType()) {
			case HTTP: {

				ctx.pipeToSelf(
					JobSchedulerService.callHttpPluginDriverClient(
						scheduler, lastIngestionDate),
					(unused, throwable) ->
						new InvokePluginDriverResponse(scheduler, throwable)
				);

			}
		}

		return Behaviors.same();
	}

	private static Behavior<Command> onInvokePluginDriverResponse(
		ActorContext<Command> ctx,
		InvokePluginDriverResponse res) {

		var scheduler = res.scheduler();
		var exception = res.exception();

		if (exception != null) {
			ctx.getSelf().tell(new HaltScheduling(scheduler, exception));
		}
		else {
			var datasource = scheduler.getDatasource();
			var tenantId = datasource.getTenant();
			var scheduleId = scheduler.getScheduleId();

			startSchedulingActor(ctx, tenantId, scheduleId);
		}

		return Behaviors.same();
	}

	private static Behavior<Command> onPersistSchedulerInternal(
		ActorContext<Command> ctx, PersistSchedulerInternal pndi) {

		Scheduler scheduler = pndi.scheduler;
		var datasource = scheduler.getDatasource();
		var tenantName = datasource.getTenant();

		Throwable exception = pndi.throwable();

		if (exception != null) {
			log.errorf(
				exception,
				"Cannot persist the Scheduler for tenant: %s and datasource: %s",
				tenantName,
				datasource
			);

			return Behaviors.same();
		}

		ctx.pipeToSelf(
			JobSchedulerService.persistScheduler(tenantName, scheduler),
			(s, throwable) ->
				new PersistSchedulerResponse(s, null, throwable)
		);

		return Behaviors.same();
	}

	private static Behavior<Command> onRemoveDatasource(
		UnScheduleDatasource removeDatasource, ActorContext<Command> ctx) {

		long datasourceId = removeDatasource.datasourceId;
		String tenantName = removeDatasource.tenantName;

		String jobName = tenantName + "-" + datasourceId;

		ctx.getSelf().tell(
			new UnScheduleJobInternal(jobName + "-" + JobType.TRIGGER.name().toLowerCase()));
		ctx.getSelf().tell(
			new UnScheduleJobInternal(jobName + "-" + JobType.REINDEX.name().toLowerCase()));
		ctx.getSelf().tell(
			new UnScheduleJobInternal(jobName + "-" + JobType.PURGE.name().toLowerCase()));

		return Behaviors.same();

	}

	private static Behavior<Command> onScheduleDatasourceInternal(
		ScheduleDatasourceInternal scheduleDatasourceInternal,
		ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		ActorRef<MessageGateway.Command> messageGatewayService, List<String> jobNames) {

		String tenantName = scheduleDatasourceInternal.tenantName();
		long datasourceId = scheduleDatasourceInternal.datasourceId();
		JobType jobType = scheduleDatasourceInternal.jobType();
		String cron = scheduleDatasourceInternal.cron();
		boolean schedulable = scheduleDatasourceInternal.schedulable();
		String purgeMaxAge = scheduleDatasourceInternal.purgeMaxAge();

		var defaultTimezone = QuartzSchedulerTypedExtension._typedToUntyped(
			quartzSchedulerTypedExtension).defaultTimezone();

		String jobName = tenantName + "-" + datasourceId + "-" + jobType.name().toLowerCase();

		Command command = new TriggerDatasource(tenantName, datasourceId, false, null);

		switch (jobType) {
			case JobType.TRIGGER:
				command = new TriggerDatasource(tenantName, datasourceId, false, null);
				break;

			case REINDEX:
				command = new TriggerDatasource(tenantName, datasourceId, true, null);
				break;

			case PURGE:
				command = new TriggerDatasourcePurge(tenantName, datasourceId, purgeMaxAge);
				break;
		}

		if (schedulable) {

			try {
				if (jobNames.contains(jobName)) {

					quartzSchedulerTypedExtension.updateTypedJobSchedule(
						jobName,
						ctx.getSelf(),
						command,
						Option.empty(),
						cron,
						Option.empty(),
						defaultTimezone
					);

					log.infof("Job updated: %s datasourceId: %s", jobName, datasourceId);

					return Behaviors.same();
				}
				else {

					quartzSchedulerTypedExtension.createTypedJobSchedule(
						jobName,
						ctx.getSelf(),
						command,
						Option.empty(),
						cron,
						Option.empty(),
						defaultTimezone
					);

					log.infof("Job created: %s datasourceId: %s", jobName, datasourceId);

					List<String> newJobNames = new ArrayList<>(jobNames);

					newJobNames.add(jobName);

					return initial(
						ctx, quartzSchedulerTypedExtension,
						messageGatewayService, newJobNames
					);

				}
			}
			catch (Exception e) {
				log.errorf(e, "Error creating job \"%s\"", jobName);
				return Behaviors.same();
			}
		}
		else if (jobNames.contains(jobName)) {
			ctx.getSelf().tell(new UnScheduleJobInternal(jobName));
			log.infof("job is not schedulable, removing job: %s", jobName);
			return Behaviors.same();
		}

		log.infof("Job of type %s not created: datasourceId: %s, the datasource is not schedulable",
			jobType.name(), datasourceId);

		return Behaviors.same();

	}

	private static Behavior<Command> onSetupMessageGatewaySubscription(
		ActorContext<Command> ctx, MessageGatewaySubscription mgs) {

		mgs
			.listing
			.getServiceInstances(MessageGateway.SERVICE_KEY)
			.stream()
			.filter(JobScheduler::isLocalActorRef)
			.findFirst()
			.map(Start::new)
			.ifPresentOrElse(
				cmd -> ctx.getSelf().tell(cmd),
				() -> log.error("ChannelManager not found"));

		return Behaviors.same();

	}

	private static Behavior<Command> onStartScheduler(
		ActorContext<Command> ctx,
		StartSchedulerInternal startSchedulerInternal) {

		Datasource datasource = startSchedulerInternal.datasource;
		var tenantName = datasource.getTenant();
		Scheduler schedulerToCancel = null;

		var startIngestionDate = startSchedulerInternal.startIngestionDate;

		var throwable1 = startSchedulerInternal.throwable();

		if (throwable1 != null) {
			log.warnf(throwable1, "Cannot start a Scheduler.");

			return Behaviors.same();
		}

		var triggerType = startSchedulerInternal.triggerType();

		if ( triggerType instanceof TriggerType.TriggerTypeReindex) {

			schedulerToCancel =
				((TriggerType.TriggerTypeReindex) triggerType).scheduler();

			triggerType = ((TriggerType.TriggerTypeReindex) triggerType).triggerType();
		}

		if (triggerType == TriggerType.SimpleTriggerType.IGNORE) {
			log.infof(
				"A Scheduler for datasource with id %s is already running",
				datasource.getId()
			);

			return Behaviors.same();
		}

		if (schedulerToCancel != null) {
			ctx.pipeToSelf(
				JobSchedulerService.cancelScheduler(tenantName, schedulerToCancel.getId()),
				(ignore, throwable) ->
					new CreateNewScheduler(
						ctx, TriggerType.SimpleTriggerType.REINDEX, datasource, tenantName,
						startIngestionDate, throwable)
			);
		}
		else {
			ctx.getSelf().tell(
				new CreateNewScheduler(
					ctx, triggerType, datasource, tenantName, startIngestionDate, null));
		}

		return Behaviors.same();
	}

	private static Behavior<Command> onPersistSchedulerResponse(
		ActorContext<Command> ctx,
		ActorRef<MessageGateway.Command> messageGateway,
		PersistSchedulerResponse rq) {

		var scheduler = rq.scheduler();
		var datasource = scheduler.getDatasource();
		var tenantId = datasource.getTenant();
		var startIngestionDate = rq.startIngestionDate;

		var throwable = rq.throwable();

		if (throwable != null) {

			log.error("Scheduler cannot be persisted.", throwable);

			return Behaviors.same();

		}

		messageGateway.tell(new MessageGateway.Register(
			ShardingKey.asString(
				tenantId, scheduler.getScheduleId())));

		ctx.getSelf()
			.tell(new InvokePluginDriverInternal(scheduler, startIngestionDate));

		return Behaviors.same();
	}

	private static Behavior<Command> onTriggerDatasource(
		TriggerDatasource jobMessage, ActorContext<Command> ctx) {

		long datasourceId = jobMessage.datasourceId;
		String tenantId = jobMessage.tenantName;
		boolean reindex = jobMessage.reindex;
		OffsetDateTime startIngestionDate = jobMessage.startIngestionDate();

		ctx.pipeToSelf(
			JobSchedulerService.fetchDatasourceConnection(
				tenantId, datasourceId),
			(datasource, throwable) -> new TriggerDatasourceInternal(
				tenantId, datasource, reindex, startIngestionDate, throwable)
		);

		return Behaviors.same();

	}

	private static Behavior<Command> onTriggerDatasourceInternal(
		TriggerDatasourceInternal triggerDatasourceInternal,
		ActorContext<Command> ctx) {

		var throwable = triggerDatasourceInternal.throwable();
		String tenantId = triggerDatasourceInternal.tenantName();

		if (throwable != null) {
			log.errorf(
				throwable,
				"error occurred when fetching datasource on tenant %s",
				tenantId
			);

			return Behaviors.same();
		}

		Datasource datasource = triggerDatasourceInternal.datasource();
		boolean reindex = triggerDatasourceInternal.reindex();
		OffsetDateTime offsetDateTime = triggerDatasourceInternal.startIngestionDate();

		PluginDriver pluginDriver = datasource.getPluginDriver();

		log.infof("Job executed: %s", datasource.getName());

		if (pluginDriver == null) {
			log.warnf(
				"datasource with id: %s has no pluginDriver", datasource.getId());

			return Behaviors.same();
		}

		ctx.pipeToSelf(
			JobSchedulerService.getTriggerType(datasource, reindex),
			(triggerType, t) ->
				new StartSchedulerInternal(datasource, offsetDateTime, triggerType, t)
		);

		return Behaviors.same();
	}

	private static Behavior<Command> onTriggerDatasourcePurge(
		TriggerDatasourcePurge tdp, ActorContext<Command> ctx) {

		String tenantName = tdp.tenantName();
		long datasourceId = tdp.datasourceId();
		String purgeMaxAge = tdp.purgeMaxAge();

		ctx.spawnAnonymous(DatasourcePurge.create(tenantName, datasourceId, purgeMaxAge));

		return Behaviors.same();
	}

	private static Behavior<Command> onUnscheduleJobInternal(
		UnScheduleJobInternal msg, ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		ActorRef<MessageGateway.Command> messageGateway,
		List<String> jobNames) {

		String jobName = msg.jobName;

		var scheduler = QuartzSchedulerTypedExtension._typedToUntyped(
			quartzSchedulerTypedExtension);

		if (jobNames.contains(jobName)) {
			scheduler.deleteJobSchedule(jobName);

			List<String> newJobNames = new ArrayList<>(jobNames);
			newJobNames.remove(jobName);
			log.infof("Job removed: %s", jobName);

			return initial(
				ctx,
				quartzSchedulerTypedExtension,
				messageGateway,
				newJobNames
			);
		}

		log.infof("Job not found: %s", jobName);

		return Behaviors.same();
	}

	private static Behavior<Command> setup(
		ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		Queue<Command> lag) {

		return Behaviors
			.receive(Command.class)
			.onMessage(MessageGatewaySubscription.class,
				mgs -> onSetupMessageGatewaySubscription(ctx, mgs))
			.onMessage(Start.class, start -> {
				Command command = lag.poll();

				while (command != null) {
					ctx.getSelf().tell(command);
					command = lag.poll();
				}

				return initial(
					ctx,
					quartzSchedulerTypedExtension,
					start.channelManagerRef,
					new ArrayList<>()
				);
			})
			.onAnyMessage(command -> {
				ArrayDeque<Command> newLag = new ArrayDeque<>(lag);
				newLag.add(command);

				if (log.isDebugEnabled()) {
					log.debugf("there are %d commands waiting...", newLag.size());
				}

				return setup(
					ctx,
					quartzSchedulerTypedExtension,
					newLag
				);
			})
			.build();
	}

	private static void startSchedulingActor(
			ActorContext<Command> ctx, String tenantName, String scheduleId) {

		var shardingKey = ShardingKey.fromStrings(tenantName, scheduleId);

		ActorSystem<?> actorSystem = ctx.getSystem();

		ClusterSharding clusterSharding = ClusterSharding.get(actorSystem);

		var entityRef = clusterSharding.entityRefFor(
			SchedulingEntityType.getTypeKey(shardingKey),
			shardingKey.asString()
		);

		entityRef.tell(Scheduling.WakeUp.INSTANCE);
	}

	private enum JobType {
		TRIGGER,
		REINDEX,
		PURGE
	}

	public record ScheduleDatasource(
		String tenantName, long datasourceId, boolean schedulable, String schedulingCron,
		boolean reindexable, String reindexingCron, boolean purgeable, String purgingCron,
		String purgeMaxAge
	) implements Command {}

	public record TriggerDatasource(
		String tenantName, long datasourceId, boolean reindex, OffsetDateTime startIngestionDate
	) implements Command {}

	public record TriggerDatasourcePurge(
		String tenantName, long datasourceId, String purgeMaxAge
	) implements Command {}

	public record UnScheduleDatasource(String tenantName, long datasourceId) implements Command {}

	private record CopyIndexTemplate(Scheduler scheduler) implements Command {}

	private record CreateNewScheduler(
		ActorContext<Command> ctx, TriggerType triggerType, Datasource datasource,
		String tenantName, OffsetDateTime startIngestionDate, Throwable throwable
	) implements Command {}

	private record InvokePluginDriverResponse(
		Scheduler scheduler,
		Throwable exception
	) implements Command {}

	private record HaltScheduling(
		Scheduler scheduler,
		Throwable exception
	) implements Command {}

	private record InvokePluginDriverInternal(
		Scheduler scheduler, OffsetDateTime startIngestionDate
	) implements Command {}

	private record MessageGatewaySubscription(Receptionist.Listing listing) implements Command {}

	private record PersistSchedulerInternal(
		Scheduler scheduler, Throwable throwable
	) implements Command {}

	private record ScheduleDatasourceInternal(
		String tenantName, long datasourceId, JobType jobType, boolean schedulable, String cron,
		String purgeMaxAge
	) implements Command {}

	private record Start(ActorRef<MessageGateway.Command> channelManagerRef) implements Command {}

	private record StartSchedulerInternal(
		Datasource datasource, OffsetDateTime startIngestionDate, TriggerType triggerType,
		Throwable throwable
	) implements Command {}

	private record PersistSchedulerResponse(
		Scheduler scheduler, OffsetDateTime startIngestionDate,
		Throwable throwable
	) implements Command {}

	private record TriggerDatasourceInternal(
		String tenantName, Datasource datasource, Boolean reindex,
		OffsetDateTime startIngestionDate, Throwable throwable
	) implements Command {}

	private record UnScheduleJobInternal(String jobName) implements Command {}

}
