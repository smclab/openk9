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

import com.google.protobuf.Empty;
import io.openk9.auth.tenant.TenantResolver;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.plugindriver.HttpPluginDriverContext;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.tenantmanager.grpc.TenantListResponse;
import io.openk9.tenantmanager.grpc.TenantManager;
import io.openk9.tenantmanager.grpc.TenantResponse;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.ConsumeEvent;
import io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle;
import io.smallrye.common.vertx.VertxContext;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.jboss.logging.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@ApplicationScoped
public class SchedulerInitializer {

	public void startUp(@Observes StartupEvent event) {
		eventBus.send("initialize_scheduler", "initialize_scheduler");
	}

	@GrpcClient("tenantmanager")
	TenantManager tenantManager;

	@ConsumeEvent(value = "initialize_scheduler")
	@ActivateRequestContext
	public Uni<Void> initScheduler(String testMessage) {

		Uni<TenantListResponse> tenantList =
			tenantManager.findTenantList(Empty.getDefaultInstance());

		logger.info("init scheduler");

		return tenantList
			.map(TenantListResponse::getTenantResponseList)
			.flatMap(list -> {
				List<Uni<Void>> unis = new ArrayList<>(list.size());
				for (TenantResponse tenantResponse : list) {
					String schemaName = tenantResponse.getSchemaName();
					tenantResolver.setTenant(schemaName);
					Uni<List<Datasource>> listDatasource = datasourceService.findAll();

					Uni<Void> voidUni = listDatasource
						.onItem()
						.invoke(tenantDatasourceList -> {
							for (Datasource datasource : tenantDatasourceList) {
								try {
									createOrUpdateScheduler(schemaName, datasource);
								}
								catch (RuntimeException e) {
									throw e;
								}
								catch (Exception e) {
									throw new RuntimeException(e);
								}
							}
						})
						.replaceWithVoid();

					unis.add(voidUni);

				}

				return Uni.combine()
					.all()
					.unis(unis)
					.discardItems();

			});

	}

	public Uni<List<Long>> triggerJobs(List<Long> datasourceIds) {

		List<Uni<Long>> triggers = new ArrayList<>(datasourceIds.size());

		for (long datasourceId : datasourceIds) {
			triggers.add(
				triggerJob(datasourceId, String.valueOf(datasourceId))
					.map(unused -> datasourceId)
			);
		}

		return Uni
			.combine()
			.all()
			.unis(triggers)
			.combinedWith(Long.class, Function.identity());

	}

	
	public Uni<Void> triggerJob(long datasourceId, String name) {

		return Uni.createFrom().deferred(() -> {
			logger.info("datasourceId: " + datasourceId + " trigger: " + name);
			return performTask(
				tenantResolver.getTenantName(), datasourceId);
		});

	}

	public void createOrUpdateScheduler(
			String tenantName, Datasource datasource)
		throws SchedulerException {

		if (!datasource.getSchedulable()) {
			deleteScheduler(datasource);
			return;
		}

		String name = datasource.getName();

		JobKey jobKey = JobKey.jobKey(name);

		if (scheduler.get().checkExists(jobKey)) {

			Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(name)
				.startNow()
				.withSchedule(
					CronScheduleBuilder.cronSchedule(
						datasource.getScheduling()))
				.build();

			TriggerKey triggerKey = TriggerKey.triggerKey(name);

			scheduler.get().rescheduleJob(triggerKey, trigger);

		}
		else {

			JobDetail job = JobBuilder.newJob(DatasourceJob.class)
				.withIdentity(name)
				.usingJobData("datasourceId", datasource.getId())
				.usingJobData("tenantName", tenantName)
				.build();

			Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(name)
				.startNow()
				.withSchedule(
					CronScheduleBuilder.cronSchedule(
						datasource.getScheduling()))
				.build();

			scheduler.get().scheduleJob(job, trigger);

		}

	}

	public void deleteScheduler(Datasource datasource)
		throws SchedulerException {
		scheduler.get().deleteJob(JobKey.jobKey(datasource.getName()));
	}

	public Uni<Void> performTask(String schemaName, Long datasourceId) {

		return transactionInvoker.withTransaction(
			schemaName,
			s -> datasourceService.findById(datasourceId).flatMap(
				datasource -> Mutiny2
					.fetch(s, datasource.getPluginDriver())
					.onItem()
					.call(pluginDriver -> {

						if (pluginDriver == null) {
							logger.warn(
								"datasource with id: " + datasourceId + " has no pluginDriver");
							return Uni.createFrom().voidItem();
						}

						OffsetDateTime lastIngestionDate =
							datasource.getLastIngestionDate();

						if (lastIngestionDate == null) {
							lastIngestionDate = OffsetDateTime.ofInstant(
								Instant.ofEpochMilli(0), ZoneId.systemDefault());
						}

						UUID uuid = UUID.randomUUID();

						String scheduleId = uuid.toString();

						switch (pluginDriver.getType()) {
							case HTTP: {
								return httpPluginDriverClient.invoke(
									Json.decodeValue(
										pluginDriver.getJsonConfig(),
										HttpPluginDriverInfo.class),
									HttpPluginDriverContext
										.builder()
										.timestamp(lastIngestionDate)
										.tenantId(schemaName)
										.datasourceId(datasource.getId())
										.scheduleId(scheduleId)
										.datasourceConfig(new JsonObject(datasource.getJsonConfig()).getMap())
										.build()
								);
							}
						}

						return Uni.createFrom().voidItem();

					}))
				.replaceWithVoid());

	}

	@ApplicationScoped
	@DisallowConcurrentExecution
	public static class DatasourceJob implements Job {

		@Inject
		SchedulerInitializer taskBean;

		@Inject
		Vertx vertx;

		@Inject
		Logger logger;

		public void execute(JobExecutionContext context) {

			Context vertxContext = VertxContext.getOrCreateDuplicatedContext(vertx);
			VertxContextSafetyToggle.setContextSafe(vertxContext, true);
			vertxContext.runOnContext(unused -> {

				JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

				taskBean
					.performTask(
						jobDataMap.getString("tenantName"),
						jobDataMap.getLong("datasourceId")
					)
					.subscribe().with(
						unused2 -> {},
						e -> logger.error(e.getMessage(), e)
					);
			});

		}

	}

	@Inject
	Instance<Scheduler> scheduler;

	@Inject
	Logger logger;

	@Inject
	DatasourceService datasourceService;

	@Inject
	HttpPluginDriverClient httpPluginDriverClient;

	@Inject
	TenantResolver tenantResolver;

	@Inject
	EventBus eventBus;

	@Inject
	TransactionInvoker transactionInvoker;

}