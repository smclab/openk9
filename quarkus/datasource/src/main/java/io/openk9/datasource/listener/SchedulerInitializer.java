package io.openk9.datasource.listener;

import io.openk9.datasource.client.plugindriver.PluginDriverClient;
import io.openk9.datasource.client.plugindriver.dto.InvokeDataParserDTO;
import io.openk9.datasource.client.plugindriver.dto.SchedulerEnabledDTO;
import io.openk9.datasource.model.Datasource;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
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
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Date;

@ApplicationScoped
public class SchedulerInitializer {

	void onStart(@Observes StartupEvent ev) throws SchedulerException {

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
			.subscribe()
			.with(datasources -> logger.info("Datasources initialized"));

	}

	public Uni<Void> triggerJob(long datasourceId, String name) {
		logger.info("datasourceId: " + datasourceId + "trigger: " + name);
		return performTask(datasourceId);
	}

	public void createOrUpdateScheduler(
		Datasource datasource) throws SchedulerException {

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

			_scheduler.get().rescheduleJob(TriggerKey.triggerKey(name), trigger);

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

			return schedulerEnabledDTOUni.flatMap(schedulerEnabledDTO -> {

				if (schedulerEnabledDTO.isSchedulerEnabled()) {
					return _pluginDriverClient
						.invokeDataParser(
							InvokeDataParserDTO
								.of(
									driverServiceName, datasource,
									Date.from(datasource.getLastIngestionDate()),
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
