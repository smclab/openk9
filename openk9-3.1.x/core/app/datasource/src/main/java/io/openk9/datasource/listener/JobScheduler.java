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

import io.openk9.common.util.ingestion.ShardingKey;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.MessageGateway;
import io.openk9.datasource.pipeline.actor.Scheduling;
import io.openk9.datasource.util.CborSerializable;
import io.openk9.datasource.util.SchedulerUtil;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.BehaviorBuilder;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.StashBuffer;
import org.apache.pekko.actor.typed.receptionist.Receptionist;
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding;
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityRef;
import org.apache.pekko.extension.quartz.QuartzSchedulerTypedExtension;
import org.jboss.logging.Logger;
import scala.Option;

public class JobScheduler {

	public sealed interface Command extends CborSerializable {}
	private static final Logger log = Logger.getLogger(JobScheduler.class);

	public static Behavior<Command> create(
		List<ScheduleDatasource> schedulatedJobs) {

		return Behaviors.withStash(
			100,
			stash -> Behaviors.setup(ctx -> {

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
					stash,
					quartzSchedulerTypedExtension,
					new ArrayDeque<>(schedulatedJobs)
				);
			})
		);
	}

	private static Behavior<Command> busy(
		ActorContext<Command> ctx,
		CurrentBehavior currentBehavior,
		StashBuffer<Command> messageBuffer,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		ActorRef<MessageGateway.Command> messageGateway,
		List<String> jobNames) {

		return newReceiveBuilder(
			ctx,
			currentBehavior,
			messageBuffer,
			quartzSchedulerTypedExtension,
			messageGateway,
			jobNames
		)
		.onMessage(TriggerDatasource.class, jm -> onStashMessage(messageBuffer, jm))
		.build();
	}

	private static <T> boolean isLocalActorRef(ActorRef<T> actorRef) {
		return actorRef.path().address().port().isEmpty();
	}

	private static BehaviorBuilder<Command> newReceiveBuilder(
		ActorContext<Command> ctx,
		CurrentBehavior currentBehavior,
		StashBuffer<Command> messageBuffer,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		ActorRef<MessageGateway.Command> messageGateway,
		List<String> jobNames) {

		return Behaviors.receive(Command.class)
			.onMessage(
				MessageGatewaySubscription.class,
				mgs -> onInitialMessageGatewaySubscription(
					ctx,
					messageBuffer,
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
			.onMessage(TriggerDatasourcePurge.class, tdp -> onTriggerDatasourcePurge(tdp, ctx))
			.onMessage(
				ScheduleDatasourceInternal.class,
				sdi -> onScheduleDatasourceInternal(
					sdi,
					ctx,
					currentBehavior,
					messageBuffer,
					quartzSchedulerTypedExtension,
					messageGateway,
					jobNames
				)
			)
			.onMessage(
				UnScheduleJobInternal.class,
				rd -> onUnscheduleJobInternal(
					rd,
					ctx,
					messageBuffer,
					quartzSchedulerTypedExtension,
					messageGateway,
					jobNames
				)
			)
			.onMessage(
				TriggerDatasourceInternal.class,
				tdi -> onTriggerDatasourceInternal(
					tdi,
					ctx,
					messageBuffer,
					quartzSchedulerTypedExtension,
					messageGateway,
					jobNames
				)
			)
			.onMessage(
				InvokePluginDriverInternal.class,
				ipdi -> onInvokePluginDriverInternal(ipdi, ctx)
			)
			.onMessage(
				StartSchedulerInternal.class,
				ssi -> onStartScheduler(
					ssi,
					ctx,
					messageBuffer,
					quartzSchedulerTypedExtension,
					messageGateway,
					jobNames
				)
			)
			.onMessage(
				CopyIndexTemplate.class,
				cit -> onCopyIndexTemplate(ctx, cit))
			.onMessage(
				PersistSchedulerInternal.class,
				pndi -> onPersistSchedulerInternal(
					pndi,
					ctx,
					messageBuffer,
					quartzSchedulerTypedExtension,
					messageGateway,
					jobNames
				)
			)
			.onMessage(
				PersistSchedulerResponse.class,
				res -> onPersistSchedulerResponse(
					res,
					ctx,
					messageBuffer,
					quartzSchedulerTypedExtension,
					messageGateway,
					jobNames
				)
			)
			.onMessage(
				InvokePluginDriverResponse.class,
				res -> onInvokePluginDriverResponse(ctx, res)
			)
			.onMessage(HaltScheduling.class, cs -> onHaltScheduling(ctx, cs));
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
		var tenantId = cit.tenantName();

		ctx.pipeToSelf(
			JobSchedulerService.copyIndexTemplate(tenantId, scheduler),
			(ignore, throwable) -> new PersistSchedulerInternal(tenantId, scheduler, throwable)
		);

		return Behaviors.same();
	}

	private static Behavior<Command> onCreateNewScheduler(
		CreateNewScheduler createNewScheduler,
		ActorContext<Command> ctx) {

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
		scheduler.setReindex(false);

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
			scheduler.setReindex(true);

			if (oldDataIndex != null) {
				Set<DocType> docTypes = oldDataIndex.getDocTypes();
				newDataIndex.setEmbeddingDocTypeField(oldDataIndex.getEmbeddingDocTypeField());

				if (docTypes != null && !docTypes.isEmpty()) {
					ctx.getSelf().tell(
						new CopyIndexTemplate(tenantName, scheduler));

					return Behaviors.same();
				}
			}
		}

		ctx.pipeToSelf(
			JobSchedulerService.persistScheduler(tenantName, scheduler),
			(response, throwable) ->
				new PersistSchedulerResponse(tenantName, scheduler, startIngestionDate, throwable)
		);

		return Behaviors.same();
	}

	private static Behavior<Command> onHaltScheduling(
		ActorContext<Command> ctx, HaltScheduling cs) {

		Scheduler scheduler = cs.scheduler;
		var tenantId = cs.tenantName();

		var exception = cs.exception;

		String scheduleId = scheduler.getScheduleId();

		ClusterSharding clusterSharding = ClusterSharding.get(ctx.getSystem());

		EntityRef<Scheduling.Command> schedulingRef = clusterSharding.entityRefFor(
			Scheduling.ENTITY_TYPE_KEY, ShardingKey.asString(tenantId, scheduleId));

		schedulingRef.tell(
			new Scheduling.Halt(new InvokePluginDriverException(exception)));

		return Behaviors.same();
	}

	private static Behavior<Command> onInitialMessageGatewaySubscription(
		ActorContext<Command> ctx,
		StashBuffer<Command> messageBuffer,
		MessageGatewaySubscription mgs,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		List<String> jobNames) {

		Optional<ActorRef<MessageGateway.Command>> actorRefOptional = mgs.listing
			.getServiceInstances(MessageGateway.SERVICE_KEY)
			.stream()
			.filter(JobScheduler::isLocalActorRef)
			.findFirst();

		if (actorRefOptional.isPresent()) {
			return unstashAndRelease(
				ctx,
				messageBuffer,
				quartzSchedulerTypedExtension,
				actorRefOptional.get(),
				jobNames
			);
		}

		return Behaviors.same();

	}

	private static Behavior<Command> onInvokePluginDriverInternal(
		InvokePluginDriverInternal ipdi, ActorContext<Command> ctx) {

		var scheduler = ipdi.scheduler();
		var startIngestionDate = ipdi.startIngestionDate();
		var tenantId = ipdi.tenantName();

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

		var pluginDriverType = pluginDriver.getType();
		if (pluginDriverType == PluginDriver.PluginDriverType.HTTP) {
			ctx.pipeToSelf(
				JobSchedulerService.callHttpPluginDriverClient(
					tenantId, scheduler, lastIngestionDate),
				(unused, throwable) ->
					new InvokePluginDriverResponse(
						tenantId, scheduler, throwable)
			);
		}
		else {
			var exception = new IllegalArgumentException(
				String.format(
					"Unknown PluginDriverType for pluginDriver %s on tenant %s",
					pluginDriver.getName(),
					tenantId
				)
			);

			log.error(exception);

			ctx.getSelf().tell(new HaltScheduling(tenantId, scheduler, exception));
		}

		return Behaviors.same();
	}

	private static Behavior<Command> onInvokePluginDriverResponse(
		ActorContext<Command> ctx,
		InvokePluginDriverResponse res) {

		var scheduler = res.scheduler();
		var exception = res.exception();
		var tenantId = res.tenantName();


		if (exception != null) {
			ctx.getSelf().tell(new HaltScheduling(tenantId, res.scheduler, exception));
		}
		else {
			var scheduleId = scheduler.getScheduleId();

			// TODO: transform to a message
			startSchedulingActor(ctx, tenantId, scheduleId);
		}

		return Behaviors.same();
	}

	private static Behavior<Command> onPersistSchedulerInternal(
		PersistSchedulerInternal pndi,
		ActorContext<Command> ctx,
		StashBuffer<Command> messageBuffer,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		ActorRef<MessageGateway.Command> messageGateway,
		List<String> jobNames) {

		Scheduler scheduler = pndi.scheduler;
		var datasource = scheduler.getDatasource();
		var tenantName = pndi.tenantName();

		Throwable exception = pndi.throwable();

		if (exception != null) {
			log.errorf(
				exception,
				"Cannot persist the Scheduler for tenant: %s and datasource: %s",
				tenantName,
				datasource
			);

			return unstashAndRelease(
				ctx,
				messageBuffer,
				quartzSchedulerTypedExtension,
				messageGateway,
				jobNames
			);
		}

		ctx.pipeToSelf(
			JobSchedulerService.persistScheduler(tenantName, scheduler),
			(s, throwable) ->
				new PersistSchedulerResponse(tenantName, s, null, throwable)
		);

		return Behaviors.same();
	}

	private static Behavior<Command> onPersistSchedulerResponse(
		PersistSchedulerResponse rq,
		ActorContext<Command> ctx,
		StashBuffer<Command> messageBuffer,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		ActorRef<MessageGateway.Command> messageGateway,
		List<String> jobNames) {

		var scheduler = rq.scheduler();
		var tenantId = rq.tenantName();
		var startIngestionDate = rq.startIngestionDate;

		var throwable = rq.throwable();

		if (throwable != null) {

			log.error("Scheduler cannot be persisted.", throwable);

			return unstashAndRelease(
				ctx,
				messageBuffer,
				quartzSchedulerTypedExtension,
				messageGateway,
				jobNames
			);
		}

		messageGateway.tell(new MessageGateway.Register(
			ShardingKey.asString(
				tenantId, scheduler.getScheduleId())));

		ctx.getSelf()
			.tell(new InvokePluginDriverInternal(tenantId, scheduler, startIngestionDate));

		return unstashAndRelease(
			ctx,
			messageBuffer,
			quartzSchedulerTypedExtension,
			messageGateway,
			jobNames
		);
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
		CurrentBehavior currentBehavior,
		StashBuffer<Command> messageBuffer,
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

					// return the current behavior and update the actor jobNames list
					switch (currentBehavior) {
						case READY -> {
							return ready(
								ctx,
								currentBehavior,
								messageBuffer,
								quartzSchedulerTypedExtension,
								messageGatewayService,
								newJobNames
							);
						}
						case BUSY -> {
							return busy(
								ctx,
								currentBehavior,
								messageBuffer,
								quartzSchedulerTypedExtension,
								messageGatewayService,
								newJobNames
							);
						}
					}
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
		StartSchedulerInternal startSchedulerInternal,
		ActorContext<Command> ctx,
		StashBuffer<Command> messageBuffer,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		ActorRef<MessageGateway.Command> messageGateway,
		List<String> jobNames) {

		var datasource = startSchedulerInternal.datasource();
		var tenantName = startSchedulerInternal.tenantName();
		var startIngestionDate = startSchedulerInternal.startIngestionDate;
		var throwable1 = startSchedulerInternal.throwable();

		Scheduler schedulerToCancel = null;

		if (throwable1 != null) {
			log.warnf(throwable1, "Cannot start a Scheduler.");

			return unstashAndRelease(
				ctx,
				messageBuffer,
				quartzSchedulerTypedExtension,
				messageGateway,
				jobNames
			);
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

			return unstashAndRelease(
				ctx,
				messageBuffer,
				quartzSchedulerTypedExtension,
				messageGateway,
				jobNames
			);
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

	private static Behavior<Command> onStashMessage(
		StashBuffer<Command> messageBuffer,
		Command message) {

		messageBuffer.stash(message);
		log.infof("There are %d commands waiting", messageBuffer.size());
		return Behaviors.same();
	}

	private static Behavior<Command> onTriggerDatasource(
		TriggerDatasource jobMessage,
		ActorContext<Command> ctx,
		StashBuffer<Command> messageBuffer,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		ActorRef<MessageGateway.Command> messageGateway,
		List<String> jobNames) {

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

		return busy(
			ctx,
			CurrentBehavior.BUSY,
			messageBuffer,
			quartzSchedulerTypedExtension,
			messageGateway,
			jobNames
		);
	}

	private static Behavior<Command> onTriggerDatasourceInternal(
		TriggerDatasourceInternal triggerDatasourceInternal,
		ActorContext<Command> ctx,
		StashBuffer<Command> messageBuffer,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		ActorRef<MessageGateway.Command> messageGateway,
		List<String> jobNames) {

		var throwable = triggerDatasourceInternal.throwable();
		String tenantId = triggerDatasourceInternal.tenantName();

		if (throwable != null) {
			log.errorf(
				throwable,
				"error occurred when fetching datasource on tenant %s",
				tenantId
			);

			return unstashAndRelease(
				ctx,
				messageBuffer,
				quartzSchedulerTypedExtension,
				messageGateway,
				jobNames
			);
		}

		Datasource datasource = triggerDatasourceInternal.datasource();
		boolean reindex = triggerDatasourceInternal.reindex();
		OffsetDateTime offsetDateTime = triggerDatasourceInternal.startIngestionDate();

		PluginDriver pluginDriver = datasource.getPluginDriver();

		log.infof("Job executed: %s", datasource.getName());

		if (pluginDriver == null) {
			log.warnf(
				"datasource with id: %s has no pluginDriver", datasource.getId());

			return unstashAndRelease(
				ctx,
				messageBuffer,
				quartzSchedulerTypedExtension,
				messageGateway,
				jobNames
			);
		}

		ctx.pipeToSelf(
			JobSchedulerService.getTriggerType(tenantId, datasource, reindex),
			(triggerType, t) ->
				new StartSchedulerInternal(tenantId, datasource, offsetDateTime, triggerType, t)
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
		StashBuffer<Command> messageBuffer,
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

			return unstashAndRelease(
				ctx,
				messageBuffer,
				quartzSchedulerTypedExtension,
				messageGateway,
				newJobNames
			);
		}

		log.infof("Job not found: %s", jobName);

		return Behaviors.same();
	}

		private static Behavior<Command> ready(
			ActorContext<Command> ctx,
			CurrentBehavior currentBehavior,
			StashBuffer<Command> messageBuffer,
			QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
			ActorRef<MessageGateway.Command> messageGateway,
			List<String> jobNames) {

		return newReceiveBuilder(
			ctx,
			currentBehavior,
			messageBuffer,
			quartzSchedulerTypedExtension,
			messageGateway,
			jobNames
		)
		.onMessage(TriggerDatasource.class,
			jm -> onTriggerDatasource(
				jm,
				ctx,
				messageBuffer,
				quartzSchedulerTypedExtension,
				messageGateway,
				jobNames
			)
		)
		.build();
	}

	private static Behavior<Command> setup(
		ActorContext<Command> ctx,
		StashBuffer<Command> messageBuffer,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		Queue<Command> lag) {

		return Behaviors
			.receive(Command.class)
			.onMessage(MessageGatewaySubscription.class,
				mgs -> onSetupMessageGatewaySubscription(ctx, mgs))
			.onMessage(Start.class, start -> {
				// TODO: remove the lag queue and use the messageBuffer queue
				Command command = lag.poll();

				while (command != null) {
					ctx.getSelf().tell(command);
					command = lag.poll();
				}

				return unstashAndRelease(
					ctx,
					messageBuffer,
					quartzSchedulerTypedExtension,
					start.channelManagerRef,
					new ArrayList<>()
				);
			})
			.onAnyMessage(command -> {
				// TODO: remove the lag queue and use the messageBuffer queue
				ArrayDeque<Command> newLag = new ArrayDeque<>(lag);
				newLag.add(command);

				if (log.isDebugEnabled()) {
					log.debugf("there are %d commands waiting...", newLag.size());
				}

				return setup(
					ctx,
					messageBuffer,
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
			Scheduling.ENTITY_TYPE_KEY,
			shardingKey.asString()
		);

		entityRef.tell(Scheduling.WakeUp.INSTANCE);
	}

	private static Behavior<Command> unstashAndRelease(
		ActorContext<Command> ctx,
		StashBuffer<Command> messageBuffer,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		ActorRef<MessageGateway.Command> messageGateway,
		List<String> jobNames) {

		return messageBuffer.unstashAll(
			ready(
				ctx,
				CurrentBehavior.READY,
				messageBuffer,
				quartzSchedulerTypedExtension,
				messageGateway,
				jobNames
			)
		);
	}

	private enum CurrentBehavior {
		READY,
		BUSY
	}

	private enum JobType {
		TRIGGER,
		REINDEX,
		PURGE
	}

	private enum SetReady implements Command {
		INSTANCE
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

	private record CopyIndexTemplate(String tenantName, Scheduler scheduler) implements Command {}

	private record CreateNewScheduler(
		ActorContext<Command> ctx, TriggerType triggerType, Datasource datasource,
		String tenantName, OffsetDateTime startIngestionDate, Throwable throwable
	) implements Command {}

	private record HaltScheduling(
		String tenantName,
		Scheduler scheduler,
		Throwable exception
	) implements Command {}

	private record InvokePluginDriverInternal(
		String tenantName, Scheduler scheduler, OffsetDateTime startIngestionDate
	) implements Command {}

	private record InvokePluginDriverResponse(
		String tenantName, Scheduler scheduler, Throwable exception
	) implements Command {}

	private record MessageGatewaySubscription(Receptionist.Listing listing) implements Command {}

	private record PersistSchedulerInternal(
		String tenantName, Scheduler scheduler, Throwable throwable
	) implements Command {}

	private record PersistSchedulerResponse(
		String tenantName, Scheduler scheduler, OffsetDateTime startIngestionDate,
		Throwable throwable
	) implements Command {}

	private record ScheduleDatasourceInternal(
		String tenantName, long datasourceId, JobType jobType, boolean schedulable, String cron,
		String purgeMaxAge
	) implements Command {}

	private record Start(ActorRef<MessageGateway.Command> channelManagerRef) implements Command {}

	private record StartSchedulerInternal(
		String tenantName,
		Datasource datasource,
		OffsetDateTime startIngestionDate,
		TriggerType triggerType,
		Throwable throwable
	) implements Command {}

	private record TriggerDatasourceInternal(
		String tenantName, Datasource datasource, Boolean reindex,
		OffsetDateTime startIngestionDate, Throwable throwable
	) implements Command {}

	private record UnScheduleJobInternal(String jobName) implements Command {}

}
