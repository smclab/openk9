package io.openk9.datasource.listener;

import io.openk9.datasource.client.plugindriver.PluginDriverClient;
import io.openk9.datasource.client.plugindriver.dto.InvokeDataParserDTO;
import io.openk9.datasource.client.plugindriver.dto.SchedulerEnabledDTO;
import io.openk9.datasource.model.Datasource;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Singleton
public class SchedulerInitializer {

	@PostConstruct
	public void onStart() throws SchedulerException {

		List<Datasource> datasourceList = Datasource.listAll();

		for (Datasource datasource : datasourceList) {

			createOrUpdateScheduler(datasource);

		}
	}

	public void triggerJob(long datasourceId, String name) throws SchedulerException {
		logger.info("datasourceId: " + datasourceId + "trigger: " + name);
		performTask(datasourceId);
	}

	public void createOrUpdateScheduler(
		Datasource datasource) throws SchedulerException {

		String name = datasource.getName();

		JobKey jobKey = JobKey.jobKey(name);

		if (_scheduler.checkExists(jobKey)) {

			Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(name)
				.startNow()
				.withSchedule(
					CronScheduleBuilder.cronSchedule(
						datasource.getScheduling()))
				.build();

			_scheduler.rescheduleJob(TriggerKey.triggerKey(name), trigger);

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

			_scheduler.scheduleJob(job, trigger);

		}

	}

	public void deleteScheduler(Datasource datasource)
		throws SchedulerException {
		_scheduler.deleteJob(JobKey.jobKey(datasource.getName()));
	}

	public void performTask(Long datasourceId) {

		boolean schedulerEnabled = false;

		Datasource datasource =
			Datasource.findById(datasourceId);

		String driverServiceName = datasource.getDriverServiceName();

		try {
			SchedulerEnabledDTO schedulerEnabledDTO = _pluginDriverClient
				.schedulerEnabled(driverServiceName);

			schedulerEnabled = schedulerEnabledDTO.isSchedulerEnabled();
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		if (schedulerEnabled) {

			try {

				_pluginDriverClient
					.invokeDataParser(
						InvokeDataParserDTO
							.of(
								driverServiceName, datasource,
								Date.from(datasource.getLastIngestionDate()),
								new Date()));
			}
			catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

		}
		else {
			logger.warn(
				"[SCHEDULER] datasourceId: " + datasourceId +
				" service: " + driverServiceName + " not found"
			);
		}
	}

	@DisallowConcurrentExecution
	public class DatasourceJob implements Job {

		@Transactional
		public void execute(JobExecutionContext context) throws
			JobExecutionException {

			JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

			performTask(jobDataMap.getLong("datasourceId"));
		}

	}

	@Inject
	Scheduler _scheduler;

	@Inject
	@RestClient
	PluginDriverClient _pluginDriverClient;

	@Inject
	Logger logger;

}
