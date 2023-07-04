package io.openk9.datasource.listener;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.internal.receptionist.ReceptionistMessages;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;
import com.typesafe.akka.extension.quartz.QuartzSchedulerTypedExtension;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.pipeline.actor.ChannelManager;
import io.openk9.datasource.pipeline.service.ChannelManagerService;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.plugindriver.HttpPluginDriverContext;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.datasource.util.CborSerializable;
import io.vavr.Function3;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import scala.Option;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JobScheduler {

	public sealed interface Command extends CborSerializable {}
	public record ScheduleDatasource(String tenantName, long datasourceId, boolean schedulable, String cron) implements Command {}
	public record UnScheduleDatasource(String tenantName, long datasourceId) implements Command {}
	public record TriggerDatasource(
		String tenantName, long datasourceId, boolean startFromFirst) implements Command {}
	private record ScheduleDatasourceInternal(String tenantName, long datasourceId, boolean schedulable, String cron) implements Command {}
	private record TriggerDatasourceInternal(String tenantName, Datasource datasource, boolean startFromFirst) implements Command {}
	private record InvokePluginDriverInternal(
		String tenantName, io.openk9.datasource.model.Scheduler scheduler,
		boolean startFromFirst) implements Command {}
	private record ChannelManagerSubscribeResponse(Receptionist.Listing listing) implements Command {}
	private record Start(ActorRef<ChannelManager.Command> channelManagerRef) implements Command {}


	public static Behavior<Command> create(
		HttpPluginDriverClient httpPluginDriverClient,
		TransactionInvoker transactionInvoker) {

		return Behaviors.setup(ctx -> {

			QuartzSchedulerTypedExtension quartzSchedulerTypedExtension =
				QuartzSchedulerTypedExtension.get(ctx.getSystem());

			ActorRef<Receptionist.Listing> listingActorRef =
				ctx.messageAdapter(
					Receptionist.Listing.class,
					JobScheduler.ChannelManagerSubscribeResponse::new);

			ctx
				.getSystem()
				.receptionist()
				.tell(new ReceptionistMessages.Find<>(ChannelManager.SERVICE_KEY, listingActorRef));

			return start(
				httpPluginDriverClient, transactionInvoker,
				ctx, quartzSchedulerTypedExtension);


		});
	}

	private static Behavior<Command> start(
		HttpPluginDriverClient httpPluginDriverClient,
		TransactionInvoker transactionInvoker, ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension) {

		return Behaviors
			.receive(Command.class)
			.onMessage(Start.class, start ->
				initial(
					ctx, quartzSchedulerTypedExtension, httpPluginDriverClient,
					transactionInvoker,
					new ChannelManagerService(start.channelManagerRef),
					new ArrayList<>()
				)
			)
			.onMessage(
				ChannelManagerSubscribeResponse.class,
				cmsr -> onChannelManagerSubscribeResponse(ctx, cmsr))
			.build();
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		HttpPluginDriverClient httpPluginDriverClient,
		TransactionInvoker transactionInvoker,
		ChannelManagerService channelManagerService,
		List<String> jobNames) {

		return Behaviors.receive(Command.class)
			.onMessage(ScheduleDatasource.class, ad -> onAddDatasource(ad, ctx))
			.onMessage(UnScheduleDatasource.class, rd -> onRemoveDatasource(rd, ctx, quartzSchedulerTypedExtension, httpPluginDriverClient, transactionInvoker, channelManagerService, jobNames))
			.onMessage(TriggerDatasource.class, jm -> onTriggerDatasource(jm, ctx, transactionInvoker))
			.onMessage(ScheduleDatasourceInternal.class, sdi -> onScheduleDatasourceInternal(sdi, ctx, quartzSchedulerTypedExtension, httpPluginDriverClient, transactionInvoker, channelManagerService, jobNames))
			.onMessage(TriggerDatasourceInternal.class, tdi -> onTriggerDatasourceInternal(tdi, ctx, transactionInvoker, channelManagerService))
			.onMessage(InvokePluginDriverInternal.class, ipdi -> onInvokePluginDriverInternal(httpPluginDriverClient, ipdi.tenantName, ipdi.scheduler, ipdi.startFromFirst))
			.build();

	}

	private static Behavior<Command> onChannelManagerSubscribeResponse(
		ActorContext<Command> ctx, ChannelManagerSubscribeResponse cmsr) {

		cmsr
			.listing
			.getServiceInstances(ChannelManager.SERVICE_KEY)
			.stream()
			.findFirst()
			.map(JobScheduler.Start::new)
			.ifPresentOrElse(
				cmd -> ctx.getSelf().tell(cmd),
				() -> ctx.getLog().error("ChannelManager not found"));

		return Behaviors.same();

	}

	private static Behavior<Command> onTriggerDatasourceInternal(
		TriggerDatasourceInternal triggerDatasourceInternal,
		ActorContext<Command> ctx,
		TransactionInvoker transactionInvoker,
		ChannelManagerService channelManagerService) {

		Datasource datasource = triggerDatasourceInternal.datasource;
		String tenantName = triggerDatasourceInternal.tenantName;
		boolean startFromFirst = triggerDatasourceInternal.startFromFirst;

		PluginDriver pluginDriver = datasource.getPluginDriver();

		ctx.getLog().info("Job executed: {}", datasource.getName());

		if (pluginDriver == null) {
			ctx.getLog().warn(
				"datasource with id: {} has no pluginDriver", datasource.getId());

			return Behaviors.same();
		}

		startScheduler(
			ctx, datasource, startFromFirst, tenantName, transactionInvoker, channelManagerService);

		return Behaviors.same();
	}

	private static Behavior<Command> onInvokePluginDriverInternal(
		HttpPluginDriverClient httpPluginDriverClient,
		String tenantName, io.openk9.datasource.model.Scheduler scheduler,
		boolean startFromFirst) {

		Datasource datasource = scheduler.getDatasource();
		PluginDriver pluginDriver = datasource.getPluginDriver();

		OffsetDateTime lastIngestionDate;

		if (startFromFirst || datasource.getLastIngestionDate() == null) {
			lastIngestionDate = OffsetDateTime.ofInstant(
				Instant.ofEpochMilli(0), ZoneId.systemDefault());
		}
		else {
			lastIngestionDate = datasource.getLastIngestionDate();
		}

		switch (pluginDriver.getType()) {
			case HTTP: {
				VertxUtil.runOnContext(
					() -> httpPluginDriverClient.invoke(
						Json.decodeValue(
							pluginDriver.getJsonConfig(),
							HttpPluginDriverInfo.class),
						HttpPluginDriverContext
							.builder()
							.timestamp(lastIngestionDate)
							.tenantId(tenantName)
							.datasourceId(datasource.getId())
							.scheduleId(scheduler.getScheduleId())
							.datasourceConfig(new JsonObject(datasource.getJsonConfig()).getMap())
							.build()
					), (ignore) -> {}
				);
			}
		}

		return Behaviors.same();
	}

	private static Behavior<Command> onScheduleDatasourceInternal(
		ScheduleDatasourceInternal scheduleDatasourceInternal,
		ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		HttpPluginDriverClient httpPluginDriverClient,
		TransactionInvoker transactionInvoker,
		ChannelManagerService channelManagerService, List<String> jobNames) {

		String tenantName = scheduleDatasourceInternal.tenantName();
		long datasourceId = scheduleDatasourceInternal.datasourceId();
		String cron = scheduleDatasourceInternal.cron();
		boolean schedulable = scheduleDatasourceInternal.schedulable();

		String jobName = tenantName + "-" + datasourceId;

		if (schedulable) {

			if (jobNames.contains(jobName)) {

				quartzSchedulerTypedExtension.updateTypedJobSchedule(
					jobName,
					ctx.getSelf(),
					new TriggerDatasource(tenantName, datasourceId, false),
					Option.empty(),
					cron,
					Option.empty(),
					quartzSchedulerTypedExtension.defaultTimezone()
				);

				ctx.getLog().info("Job updated: {} datasourceId: {}", jobName, datasourceId);

				return Behaviors.same();
			}
			else {

				quartzSchedulerTypedExtension.createTypedJobSchedule(
					jobName,
					ctx.getSelf(),
					new TriggerDatasource(tenantName, datasourceId, false),
					Option.empty(),
					cron,
					Option.empty(),
					quartzSchedulerTypedExtension.defaultTimezone()
				);

				ctx.getLog().info("Job created: {} datasourceId: {}", jobName, datasourceId);

				List<String> newJobNames = new ArrayList<>(jobNames);

				newJobNames.add(jobName);

				return initial(
					ctx, quartzSchedulerTypedExtension, httpPluginDriverClient,
					transactionInvoker, channelManagerService, newJobNames);

			}
		}
		else if (jobNames.contains(jobName)) {
			ctx.getSelf().tell(new UnScheduleDatasource(tenantName, datasourceId));
			ctx.getLog().info("job is not schedulable, removing job: {}", jobName);
			return Behaviors.same();
		}

		ctx.getLog().info("Job not created: datasourceId: {}, the datasource is not schedulable", datasourceId);

		return Behaviors.same();

	}

	private static Behavior<Command> onTriggerDatasource(
		TriggerDatasource jobMessage, ActorContext<Command> ctx,
		TransactionInvoker transactionInvoker) {

		long datasourceId = jobMessage.datasourceId;
		String tenantName = jobMessage.tenantName;
		boolean startFromFirst = jobMessage.startFromFirst;

		loadDatasourceAndCreateSelfMessage(
			tenantName, datasourceId, startFromFirst, transactionInvoker, ctx,
			TriggerDatasourceInternal::new
		);

		return Behaviors.same();

	}

	private static Behavior<Command> onRemoveDatasource(
		UnScheduleDatasource removeDatasource, ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		HttpPluginDriverClient httpPluginDriverClient,
		TransactionInvoker transactionInvoker, ChannelManagerService channelManagerService, List<String> jobNames) {

		long datasourceId = removeDatasource.datasourceId;
		String tenantName = removeDatasource.tenantName;

		String jobName = tenantName + "-" + datasourceId;

		if (jobNames.contains(jobName)) {
			quartzSchedulerTypedExtension.deleteJobSchedule(jobName);
			List<String> newJobNames = new ArrayList<>(jobNames);
			newJobNames.remove(jobName);
			ctx.getLog().info("Job removed: {}", jobName);
			return initial(
				ctx, quartzSchedulerTypedExtension, httpPluginDriverClient,
				transactionInvoker, channelManagerService, newJobNames);
		}

		ctx.getLog().info("Job not found: {}", jobName);

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

	private static void loadDatasourceAndCreateSelfMessage(
		String tenantName, long datasourceId, boolean startFromFirst,
		TransactionInvoker transactionInvoker,
		ActorContext<Command> ctx, Function3<String, Datasource, Boolean, Command> selfMessageCreator) {
		VertxUtil.runOnContext(() ->
			transactionInvoker.withStatelessTransaction(tenantName, s ->
				s.createQuery(
						"select d " +
						"from Datasource d " +
						"join fetch d.pluginDriver " +
						"left join fetch d.dataIndex " +
						"where d.id = :id", Datasource.class)
					.setParameter("id", datasourceId)
					.getSingleResult()
					.invoke(d -> ctx.getSelf().tell(selfMessageCreator.apply(tenantName, d, startFromFirst)))
			)
		);
	}


	private static void startScheduler(
		ActorContext<Command> ctx, Datasource datasource,
		boolean startFromFirst, String tenantName, TransactionInvoker transactionInvoker,
		ChannelManagerService channelManagerService) {


		io.openk9.datasource.model.Scheduler scheduler = new io.openk9.datasource.model.Scheduler();
		scheduler.setScheduleId(UUID.randomUUID().toString());
		scheduler.setDatasource(datasource);
		scheduler.setOldDataIndex(datasource.getDataIndex());
		scheduler.setStatus(io.openk9.datasource.model.Scheduler.SchedulerStatus.STARTED);

		if (scheduler.getOldDataIndex() == null || startFromFirst) {

			DataIndex newDataIndex = new DataIndex();
			newDataIndex.setName(
				datasource.getId() + "-data-" + scheduler.getScheduleId());
			newDataIndex.setDatasource(datasource);
			scheduler.setNewDataIndex(newDataIndex);

		}

		VertxUtil.runOnContext(() ->
			transactionInvoker
				.withTransaction(tenantName, (s) ->
					s
						.persist(scheduler)
						.invoke(() -> {
							channelManagerService
								.queueSpawn(tenantName, scheduler.getScheduleId());
							ctx
								.getSelf()
								.tell(new InvokePluginDriverInternal(tenantName, scheduler, startFromFirst));
						})
				)
		);

	}

}
