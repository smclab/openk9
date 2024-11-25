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

import com.typesafe.config.Config;
import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.MessageGateway;
import io.openk9.datasource.pipeline.actor.Scheduling;
import io.openk9.datasource.pipeline.base.BasePipeline;
import io.openk9.datasource.pipeline.vector.VectorPipeline;
import io.openk9.datasource.util.CborSerializable;
import org.apache.pekko.actor.typed.ActorRef;
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

	private static final Logger log = Logger.getLogger(JobScheduler.class);

	public sealed interface Command extends CborSerializable {}
	public record ScheduleDatasource(
		String tenantName, long datasourceId, boolean schedulable, String cron
	) implements Command {}
	public record UnScheduleDatasource(String tenantName, long datasourceId) implements Command {}
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
	public record TriggerDatasourcePurge(String tenantName, long datasourceId) implements Command {}
	private record ScheduleDatasourceInternal(
		String tenantName, long datasourceId, boolean schedulable, String cron
	) implements Command {}
	private record UnScheduleJobInternal(String jobName) implements Command {}
	private static Behavior<Command> onCopyIndexTemplate(
		ActorContext<Command> ctx, CopyIndexTemplate cit) {

		var scheduler = cit.scheduler();

		ctx.pipeToSelf(
			JobSchedulerService.copyIndexTemplate(scheduler),
			(ignore, throwable) -> new PersistSchedulerInternal(scheduler, throwable)
		);

		return Behaviors.same();
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
				StartSchedulingWork.class,
				rq -> onStartSchedulingWork(ctx, messageGateway, rq)
			)
			.onMessage(StartVectorPipeline.class, msg -> onStartVectorPipeline(ctx, msg))
			.onMessage(
				PersistVectorSchedulerInternal.class,
				msg -> onPersistVectorSchedulerInternal(ctx, msg)
			)
			.onMessage(
				RegisterVectorQueue.class,
				msg -> onRegisterVectorQueue(ctx, messageGateway, msg)
			)
			.onMessage(HaltScheduling.class, cs -> onHaltScheduling(ctx, cs))
			.build();

	}

	private static Behavior<Command> onPersistVectorSchedulerInternal(
		ActorContext<Command> ctx,
		PersistVectorSchedulerInternal msg) {

		Scheduler scheduler = msg.scheduler;
		var datasource = scheduler.getDatasource();
		var tenantId = datasource.getTenant();

		Throwable exception = msg.throwable();

		if (exception != null) {
			log.errorf(
				exception,
				"Cannot persist the Vector Scheduler for tenant: %s and datasource: %s",
				tenantId,
				datasource
			);

			return Behaviors.same();
		}

		ctx.pipeToSelf(
			JobSchedulerService.persistScheduler(tenantId, scheduler),
			RegisterVectorQueue::new
		);

		return Behaviors.same();
	}

	private static Behavior<Command> onRegisterVectorQueue(
		ActorContext<Command> ctx,
		ActorRef<MessageGateway.Command> messageGateway,
		RegisterVectorQueue msg) {

		var scheduler = msg.scheduler();
		var datasource = scheduler.getDatasource();
		var tenantId = datasource.getTenant();

		messageGateway.tell(new MessageGateway.Register(
			ShardingKey.asString(
				tenantId, scheduler.getScheduleId())));

		return Behaviors.same();
	}
	private record MessageGatewaySubscription(Receptionist.Listing listing) implements Command {}
	private record Start(ActorRef<MessageGateway.Command> channelManagerRef) implements Command {}

	private static Behavior<Command> onStartVectorPipeline(
		ActorContext<Command> ctx,
		StartVectorPipeline msg) {

		var scheduler = msg.scheduler();
		var datasource = scheduler.getDatasource();
		var tenantId = datasource.getTenant();

		var oldDataIndex = scheduler.getOldDataIndex();

		if (oldDataIndex == null) {
			log.infof(
				"VectorPipeline skipped because no dataIndex is associated for scheduleId %s.",
				scheduler.getScheduleId()
			);

			return Behaviors.same();
		}

		var vectorIndex = oldDataIndex.getVectorIndex();

		if (vectorIndex == null) {
			log.infof(
				"VectorPipeline skipped because no vectorIndex is configured for scheduleId %s.",
				scheduler.getScheduleId()
			);

			return Behaviors.same();
		}

		var scheduleId = scheduler.getScheduleId();
		var vScheduleId = scheduleId + VectorPipeline.VECTOR_PIPELINE_SUFFIX;

		var vScheduler = new Scheduler();
		vScheduler.setScheduleId(vScheduleId);
		vScheduler.setStatus(Scheduler.SchedulerStatus.RUNNING);
		vScheduler.setDatasource(datasource);
		vScheduler.setOldDataIndex(oldDataIndex);

		ctx.pipeToSelf(
			JobSchedulerService.fetchEmbeddingModel(tenantId),
			(ignore, throwable) ->
				new PersistVectorSchedulerInternal(vScheduler, throwable)
		);

		return Behaviors.same();
	}

	private static Behavior<Command> onStartSchedulingWork(
		ActorContext<Command> ctx,
		ActorRef<MessageGateway.Command> messageGateway,
		StartSchedulingWork rq) {

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

	private record RegisterVectorQueue(Scheduler scheduler, Throwable throwable)
		implements Command {}

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
					(unused, throwable) -> {
						if (throwable != null) {
							return new HaltScheduling(
								scheduler,
								new InvokePluginDriverException(throwable)
							);
						}
						else {
							return new StartVectorPipeline(scheduler);
						}
					}
				);

			}
		}

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
			scheduler.deleteJobSchedule(jobName + "-purge");

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

	private static Behavior<Command> onHaltScheduling(
		ActorContext<Command> ctx, HaltScheduling cs) {

		Scheduler scheduler = cs.scheduler;
		var tenantId = scheduler.getTenant();

		var exception = cs.exception;

		String scheduleId = scheduler.getScheduleId();

		ClusterSharding clusterSharding = ClusterSharding.get(ctx.getSystem());

		EntityRef<Scheduling.Command> schedulingRef = clusterSharding.entityRefFor(
			BasePipeline.ENTITY_TYPE_KEY, ShardingKey.asString(tenantId, scheduleId));

		schedulingRef.tell(new Scheduling.Halt(exception));

		return Behaviors.same();
	}

	private static Behavior<Command> onScheduleDatasourceInternal(
		ScheduleDatasourceInternal scheduleDatasourceInternal,
		ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		ActorRef<MessageGateway.Command> messageGatewayService, List<String> jobNames) {

		String tenantName = scheduleDatasourceInternal.tenantName();
		long datasourceId = scheduleDatasourceInternal.datasourceId();
		String cron = scheduleDatasourceInternal.cron();
		boolean schedulable = scheduleDatasourceInternal.schedulable();

		var defaultTimezone = QuartzSchedulerTypedExtension._typedToUntyped(
			quartzSchedulerTypedExtension).defaultTimezone();

		String jobName = tenantName + "-" + datasourceId;

		if (schedulable) {

			if (jobNames.contains(jobName)) {

				quartzSchedulerTypedExtension.updateTypedJobSchedule(
					jobName,
					ctx.getSelf(),
					new TriggerDatasource(tenantName, datasourceId, false, null),
					Option.empty(),
					cron,
					Option.empty(),
					defaultTimezone
				);

				quartzSchedulerTypedExtension.updateTypedJobSchedule(
					jobName + "-purge",
					ctx.getSelf(),
					new TriggerDatasourcePurge(tenantName, datasourceId),
					Option.empty(),
					getPurgeCron(ctx),
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
					new TriggerDatasource(tenantName, datasourceId, false, null),
					Option.empty(),
					cron,
					Option.empty(),
					defaultTimezone
				);

				quartzSchedulerTypedExtension.createTypedJobSchedule(
					jobName + "-purge",
					ctx.getSelf(),
					new TriggerDatasourcePurge(tenantName, datasourceId),
					Option.empty(),
					getPurgeCron(ctx),
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
		else if (jobNames.contains(jobName)) {
			ctx.getSelf().tell(new UnScheduleDatasource(tenantName, datasourceId));
			log.infof("job is not schedulable, removing job: %s", jobName);
			return Behaviors.same();
		}

		log.infof("Job not created: datasourceId: %s, the datasource is not schedulable", datasourceId);

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
			.map(JobScheduler.Start::new)
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

		var startIngestionDate = startSchedulerInternal.startIngestionDate;

		var throwable1 = startSchedulerInternal.throwable();

		if (throwable1 != null) {
			log.warnf(throwable1, "Cannot start a Scheduler.");

			return Behaviors.same();
		}

		var triggerType = startSchedulerInternal.triggerType();

		if (triggerType == TriggerType.IGNORE) {
			log.infof(
				"A Scheduler for datasource with id %s is already running",
				datasource.getId()
			);

			return Behaviors.same();
		}

		boolean reindex = triggerType == TriggerType.REINDEX;

		io.openk9.datasource.model.Scheduler scheduler = new io.openk9.datasource.model.Scheduler();
		scheduler.setScheduleId(UUID.randomUUID().toString());
		scheduler.setDatasource(datasource);
		scheduler.setOldDataIndex(datasource.getDataIndex());
		scheduler.setStatus(io.openk9.datasource.model.Scheduler.SchedulerStatus.RUNNING);

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
		else {

			ctx.pipeToSelf(
				JobSchedulerService.persistScheduler(tenantName, scheduler),
				(response, throwable) ->
					new StartSchedulingWork(scheduler, startIngestionDate, throwable)
			);

		}

		return Behaviors.same();
	}

	private static Behavior<Command> onTriggerDatasourcePurge(
		TriggerDatasourcePurge tdp, ActorContext<Command> ctx) {

		String tenantName = tdp.tenantName();
		long datasourceId = tdp.datasourceId;

		ctx.spawnAnonymous(DatasourcePurge.create(tenantName, datasourceId));

		return Behaviors.same();
	}

	public record TriggerDatasource(
		String tenantName, long datasourceId, boolean reindex, OffsetDateTime startIngestionDate
	) implements Command {}

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
				new StartSchedulingWork(s,null, throwable)
		);

		return Behaviors.same();
	}

	private record TriggerDatasourceInternal(
		String tenantName, Datasource datasource, Boolean reindex,
		OffsetDateTime startIngestionDate, Throwable throwable
	) implements Command {}

	private record InvokePluginDriverInternal(
		Scheduler scheduler, OffsetDateTime startIngestionDate
	) implements Command {}

	private static Behavior<Command> onRemoveDatasource(
		UnScheduleDatasource removeDatasource, ActorContext<Command> ctx) {

		long datasourceId = removeDatasource.datasourceId;
		String tenantName = removeDatasource.tenantName;

		String jobName = tenantName + "-" + datasourceId;

		ctx.getSelf().tell(new UnScheduleJobInternal(jobName));

		return Behaviors.same();

	}

	private static Behavior<Command> onAddDatasource(
		ScheduleDatasource addDatasource, ActorContext<Command> ctx) {

		long datasourceId = addDatasource.datasourceId;
		String tenantName = addDatasource.tenantName;

		ctx.getSelf().tell(
			new ScheduleDatasourceInternal(
				tenantName, datasourceId, addDatasource.schedulable,
				addDatasource.cron
			)
		);

		return Behaviors.same();

	}

	private record StartSchedulerInternal(
		Datasource datasource, OffsetDateTime startIngestionDate, TriggerType triggerType,
		Throwable throwable
	) implements Command {}

	private record CopyIndexTemplate(Scheduler scheduler) implements Command {}

	private record PersistSchedulerInternal(
		Scheduler scheduler, Throwable throwable
	) implements Command {}

	private record StartVectorPipeline(Scheduler scheduler) implements Command {}

	private record HaltScheduling(
		Scheduler scheduler,
		InvokePluginDriverException exception
	) implements Command {}

	private static <T> boolean isLocalActorRef(ActorRef<T> actorRef) {
		return actorRef.path().address().port().isEmpty();
	}

	private static String getPurgeCron(ActorContext<?> context) {
		Config config = context.getSystem().settings().config();

		String configPath = "io.openk9.scheduling.purge.cron";

		if (config.hasPathOrNull(configPath)) {
			if (config.getIsNull(configPath)) {
				return EVERY_DAY_AT_1_AM;
			} else {
				return config.getString(configPath);
			}
		} else {
			return EVERY_DAY_AT_1_AM;
		}

	}

	private final static String EVERY_DAY_AT_1_AM = "0 0 1 * * ?";

	private record StartSchedulingWork(Scheduler scheduler, OffsetDateTime startIngestionDate,
		Throwable throwable
	) implements Command {}

	private record PersistVectorSchedulerInternal(
		Scheduler scheduler, Throwable throwable
	)
		implements Command {}

}
