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
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import scala.Option;

import javax.enterprise.inject.spi.CDI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Scheduler {

	public sealed interface Command {}
	public record ScheduleDatasource(String tenantName, Datasource datasource) implements Command {}
	public record UnScheduleDatasource(String tenantName, Datasource datasource) implements Command {}
	public record TriggerDatasource(String tenantName, Datasource datasource) implements Command {}

	public static Behavior<Command> create() {
		return Behaviors.setup(ctx -> {

			QuartzSchedulerTypedExtension quartzSchedulerTypedExtension =
				QuartzSchedulerTypedExtension.get(ctx.getSystem());

			return initial(ctx, quartzSchedulerTypedExtension, new ArrayList<>());

		});
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		List<String> jobNames) {

		return Behaviors.receive(Command.class)
			.onMessage(ScheduleDatasource.class, addDatasource -> onAddDatasource(addDatasource, ctx, quartzSchedulerTypedExtension, jobNames))
			.onMessage(UnScheduleDatasource.class, removeDatasource -> onRemoveDatasource(removeDatasource, ctx, quartzSchedulerTypedExtension, jobNames))
			.onMessage(TriggerDatasource.class, jobMessage -> onJobMessage(jobMessage, ctx))
			.build();

	}

	private static Behavior<Command> onJobMessage(
		TriggerDatasource jobMessage, ActorContext<Command> ctx) {

		Datasource datasource = jobMessage.datasource;
		String tenantName = jobMessage.tenantName;

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

		HttpPluginDriverClient httpPluginDriverClient = findHttpPluginDriverClient();

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

	private static HttpPluginDriverClient findHttpPluginDriverClient() {
		return CDI.current().select(HttpPluginDriverClient.class).get();
	}

	private static Behavior<Command> onRemoveDatasource(
		UnScheduleDatasource removeDatasource, ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		List<String> jobNames) {

		Datasource datasource = removeDatasource.datasource;
		String tenantName = removeDatasource.tenantName;

		String jobName = tenantName + "-" + datasource.getId() + "-" + datasource.getName();

		if (jobNames.contains(jobName)) {
			quartzSchedulerTypedExtension.deleteJobSchedule(jobName);
			List<String> newJobNames = new ArrayList<>(jobNames);
			newJobNames.remove(jobName);
			ctx.getLog().info("Job removed: {}", jobName);
			return initial(ctx, quartzSchedulerTypedExtension, newJobNames);
		}

		ctx.getLog().info("Job not found: {}", jobName);

		return Behaviors.same();

	}

	private static Behavior<Command> onAddDatasource(
		ScheduleDatasource addDatasource, ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		List<String> jobNames) {

		Datasource datasource = addDatasource.datasource;
		String tenantName = addDatasource.tenantName;

		String jobName = tenantName + "-" + datasource.getId() + "-" + datasource.getName();

		if (datasource.getSchedulable()) {

			if (jobNames.contains(jobName)) {

				quartzSchedulerTypedExtension.updateTypedJobSchedule(
					jobName,
					ctx.getSelf(),
					new TriggerDatasource(tenantName, datasource),
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
					new TriggerDatasource(tenantName, datasource),
					Option.empty(),
					datasource.getScheduling(),
					Option.empty(),
					quartzSchedulerTypedExtension.defaultTimezone()
				);

				ctx.getLog().info("Job created: {} datasourceId: {}", jobName, datasource.getId());

				List<String> newJobNames = new ArrayList<>(jobNames);

				newJobNames.add(jobName);

				return initial(ctx, quartzSchedulerTypedExtension, newJobNames);

			}
		}
		else if (jobNames.contains(jobName)) {
			ctx.getSelf().tell(new UnScheduleDatasource(tenantName, datasource));
			ctx.getLog().info("job is not schedulable, removing job: {}", jobName);
			return Behaviors.same();
		}

		ctx.getLog().info("Job not created: datasourceId: {}, the datasource is not schedulable", datasource.getId());

		return Behaviors.same();

	}

}
