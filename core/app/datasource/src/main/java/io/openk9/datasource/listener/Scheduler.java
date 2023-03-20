package io.openk9.datasource.listener;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.akka.extension.quartz.QuartzSchedulerTypedExtension;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.plugindriver.HttpPluginDriverContext;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;
import io.openk9.datasource.sql.TransactionInvoker;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import scala.Option;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

public class Scheduler {

	public sealed interface Command {}
	public record ScheduleDatasource(String tenantName, long datasourceId) implements Command {}
	private record ScheduleDatasourceInternal(String tenantName, Datasource datasource) implements Command {}
	public record UnScheduleDatasource(String tenantName, long datasourceId) implements Command {}
	public record TriggerDatasource(String tenantName, long datasourceId) implements Command {}
	private record TriggerDatasourceInternal(String tenantName, Datasource datasource) implements Command {}

	public static Behavior<Command> create(
		HttpPluginDriverClient httpPluginDriverClient,
		TransactionInvoker transactionInvoker) {
		return Behaviors.setup(ctx -> {

			QuartzSchedulerTypedExtension quartzSchedulerTypedExtension =
				QuartzSchedulerTypedExtension.get(ctx.getSystem());

			return initial(
				ctx, quartzSchedulerTypedExtension, httpPluginDriverClient,
				transactionInvoker, new ArrayList<>());

		});
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		HttpPluginDriverClient httpPluginDriverClient,
		TransactionInvoker transactionInvoker,
		List<String> jobNames) {

		return Behaviors.receive(Command.class)
			.onMessage(ScheduleDatasource.class, addDatasource -> onAddDatasource(addDatasource, ctx, transactionInvoker))
			.onMessage(UnScheduleDatasource.class, removeDatasource -> onRemoveDatasource(removeDatasource, ctx, quartzSchedulerTypedExtension, httpPluginDriverClient, transactionInvoker, jobNames))
			.onMessage(TriggerDatasource.class, jobMessage -> onTriggerDatasource(jobMessage, ctx, transactionInvoker))
			.onMessage(ScheduleDatasourceInternal.class, scheduleDatasourceInternal -> onScheduleDatasourceInternal(scheduleDatasourceInternal, ctx, quartzSchedulerTypedExtension, httpPluginDriverClient, transactionInvoker, jobNames))
			.onMessage(TriggerDatasourceInternal.class, triggerDatasourceInternal -> onTriggerDatasourceInternal(triggerDatasourceInternal, ctx, httpPluginDriverClient))
			.build();

	}

	private static Behavior<Command> onTriggerDatasourceInternal(
		TriggerDatasourceInternal triggerDatasourceInternal,
		ActorContext<Command> ctx,
		HttpPluginDriverClient httpPluginDriverClient) {

		Datasource datasource = triggerDatasourceInternal.datasource;
		String tenantName = triggerDatasourceInternal.tenantName;

		PluginDriver pluginDriver = datasource.getPluginDriver();

		ctx.getLog().info("Job executed: {}", datasource.getName());

		if (pluginDriver == null) {
			ctx.getLog().warn(
				"datasource with id: {} has no pluginDriver", datasource.getId());

			return Behaviors.same();
		}

		OffsetDateTime lastIngestionDate;

		if (datasource.getLastIngestionDate() == null) {
			lastIngestionDate = OffsetDateTime.ofInstant(
				Instant.ofEpochMilli(0), ZoneId.systemDefault());
		}
		else {
			lastIngestionDate = datasource.getLastIngestionDate();
		}

		UUID uuid = UUID.randomUUID();

		String scheduleId = uuid.toString();

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
							.scheduleId(scheduleId)
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
		List<String> jobNames) {

		Datasource datasource = scheduleDatasourceInternal.datasource;
		String tenantName = scheduleDatasourceInternal.tenantName;

		String jobName = tenantName + "-" + datasource.getId() + "-" + datasource.getName();

		if (datasource.getSchedulable()) {

			if (jobNames.contains(jobName)) {

				quartzSchedulerTypedExtension.updateTypedJobSchedule(
					jobName,
					ctx.getSelf(),
					new TriggerDatasource(tenantName, datasource.getId()),
					Option.empty(),
					datasource.getScheduling(),
					Option.empty(),
					quartzSchedulerTypedExtension.defaultTimezone()
				);

				ctx.getLog().info("Job updated: {} datasourceId: {}", jobName, datasource.getId());

				return Behaviors.same();
			}
			else {

				quartzSchedulerTypedExtension.createTypedJobSchedule(
					jobName,
					ctx.getSelf(),
					new TriggerDatasource(tenantName, datasource.getId()),
					Option.empty(),
					datasource.getScheduling(),
					Option.empty(),
					quartzSchedulerTypedExtension.defaultTimezone()
				);

				ctx.getLog().info("Job created: {} datasourceId: {}", jobName, datasource.getId());

				List<String> newJobNames = new ArrayList<>(jobNames);

				newJobNames.add(jobName);

				return initial(
					ctx, quartzSchedulerTypedExtension, httpPluginDriverClient,
					transactionInvoker, newJobNames);

			}
		}
		else if (jobNames.contains(jobName)) {
			ctx.getSelf().tell(new UnScheduleDatasource(tenantName, datasource.getId()));
			ctx.getLog().info("job is not schedulable, removing job: {}", jobName);
			return Behaviors.same();
		}

		ctx.getLog().info("Job not created: datasourceId: {}, the datasource is not schedulable", datasource.getId());

		return Behaviors.same();

	}

	private static Behavior<Command> onTriggerDatasource(
		TriggerDatasource jobMessage, ActorContext<Command> ctx,
		TransactionInvoker transactionInvoker) {

		long datasourceId = jobMessage.datasourceId;
		String tenantName = jobMessage.tenantName;

		loadDatasourceAndCreateSelfMessage(
			tenantName, datasourceId, transactionInvoker, ctx,
			TriggerDatasourceInternal::new
		);

		return Behaviors.same();

	}

	private static Behavior<Command> onRemoveDatasource(
		UnScheduleDatasource removeDatasource, ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		HttpPluginDriverClient httpPluginDriverClient,
		TransactionInvoker transactionInvoker, List<String> jobNames) {

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
				transactionInvoker, newJobNames);
		}

		ctx.getLog().info("Job not found: {}", jobName);

		return Behaviors.same();

	}

	private static Behavior<Command> onAddDatasource(
		ScheduleDatasource addDatasource, ActorContext<Command> ctx,
		TransactionInvoker transactionInvoker) {

		long datasourceId = addDatasource.datasourceId;
		String tenantName = addDatasource.tenantName;

		loadDatasourceAndCreateSelfMessage(
			tenantName, datasourceId, transactionInvoker, ctx,
			ScheduleDatasourceInternal::new
		);

		return Behaviors.same();

	}

	private static void loadDatasourceAndCreateSelfMessage(
		String tenantName, long datasourceId, TransactionInvoker transactionInvoker,
		ActorContext<Command> ctx, BiFunction<String, Datasource, Command> selfMessageCreator) {
		VertxUtil.runOnContext(() ->
			transactionInvoker.withStatelessTransaction(s ->
				s.createQuery(
						"select d " +
						"from Datasource d " +
						"join fetch d.pluginDriver " +
						"where d.id = :id", Datasource.class)
					.setParameter("id", datasourceId)
					.getSingleResult()
					.invoke(d -> ctx.getSelf().tell(selfMessageCreator.apply(tenantName, d)))
			)
		);
	}


}
