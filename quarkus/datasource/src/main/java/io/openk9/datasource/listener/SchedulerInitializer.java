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

import io.openk9.datasource.client.plugindriver.PluginDriverClient;
import io.openk9.datasource.client.plugindriver.dto.InvokeDataParserDTO;
import io.openk9.datasource.client.plugindriver.dto.SchedulerEnabledDTO;
import io.openk9.datasource.event.model.Event;
import io.openk9.datasource.model.Datasource;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.eclipse.microprofile.rest.client.inject.RestClient;
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
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@ApplicationScoped
public class SchedulerInitializer {

	@Inject
	EventBus eventBus;

	public void startUp(@Observes StartupEvent event) {
		eventBus.send("initialize_scheduler", "initialize_scheduler");
	}

	@ConsumeEvent(value = "initialize_scheduler", blocking = true)
	@Transactional
	public void initScheduler(String testMessage) {

		logger.info("init scheduler");

		Panache.withTransaction(() ->
			Datasource
				.<Datasource>listAll()
				.onItem()
				.invoke(list -> {
					for (Datasource datasource : list) {
						try {
							createOrUpdateScheduler(datasource);
						}
						catch (RuntimeException e) {
							throw e;
						}
						catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}))
			.await()
			.indefinitely();

	}

	public Uni<Void> triggerJob(long datasourceId, String name) {
		logger.info("datasourceId: " + datasourceId + "trigger: " + name);
		return performTask(datasourceId);
	}

	public void createOrUpdateScheduler(
		Datasource datasource) throws SchedulerException {

		if (!datasource.getActive()) {
			deleteScheduler(datasource);
			return;
		}

		String name = datasource.getName();

		JobKey jobKey = JobKey.jobKey(name);

		if (_scheduler.get().checkExists(jobKey)) {

			Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(name)
				.startNow()
				.withSchedule(
					CronScheduleBuilder.cronSchedule(
						datasource.getScheduling()))
				.build();

			TriggerKey triggerKey = TriggerKey.triggerKey(name);

			_scheduler.get().rescheduleJob(triggerKey, trigger);

		}
		else {

			JobDetail job = JobBuilder.newJob(DatasourceJob.class)
				.withIdentity(name)
				.usingJobData("datasourceId", datasource.getDatasourceId())
				.build();

			Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(name)
				.startNow()
				.withSchedule(
					CronScheduleBuilder.cronSchedule(
						datasource.getScheduling()))
				.build();

			_scheduler.get().scheduleJob(job, trigger);

		}

	}

	public void deleteScheduler(Datasource datasource)
		throws SchedulerException {
		_scheduler.get().deleteJob(JobKey.jobKey(datasource.getName()));
	}

	public Uni<Void> performTask(Long datasourceId) {

		Uni<Datasource> datasourceUni =
			Datasource.findById(datasourceId);

		return datasourceUni.flatMap(datasource -> {
			String driverServiceName = datasource.getDriverServiceName();

			Uni<SchedulerEnabledDTO> schedulerEnabledDTOUni = _pluginDriverClient
				.schedulerEnabled(driverServiceName);

			Uni<LocalDateTime> lastParsingDate =
				Event.getLastParsingDate(
					Datasource.class.getName(),
					datasource.getPrimaryKey());

			return Uni.combine()
				.all()
				.unis(lastParsingDate, schedulerEnabledDTOUni)
				.asTuple()
				.flatMap(t2 -> {

					SchedulerEnabledDTO schedulerEnabledDTO = t2.getItem2();

					if (schedulerEnabledDTO.isSchedulerEnabled()) {

						LocalDateTime parsingDate = t2.getItem1();

						return _pluginDriverClient
							.invokeDataParser(
								InvokeDataParserDTO
									.of(
										driverServiceName, datasource,
										Date.from(
											parsingDate
												.atZone(ZoneId.systemDefault())
												.toInstant()
										),
										new Date()));
				}

				logger.warn(
					"[SCHEDULER] datasourceId: " + datasourceId +
					" service: " + driverServiceName + " not found"
				);

				return Uni.createFrom().nothing();

			});

		});

	}

	@ApplicationScoped
	@DisallowConcurrentExecution
	public static class DatasourceJob implements Job {

		@Inject
		SchedulerInitializer taskBean;

		@ActivateRequestContext
		public void execute(JobExecutionContext context) {

			JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

			Panache.withTransaction(
				() -> taskBean.performTask(jobDataMap.getLong("datasourceId")))
				.await()
				.indefinitely();
		}

	}

	@Inject
	Instance<Scheduler> _scheduler;

	@Inject
	@RestClient
	PluginDriverClient _pluginDriverClient;

	@Inject
	Logger logger;

}
