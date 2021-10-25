package io.openk9.datasource.listener;

import io.openk9.datasource.client.plugindriver.PluginDriverClient;
import io.openk9.datasource.client.plugindriver.dto.InvokeDataParserDTO;
import io.openk9.datasource.client.plugindriver.dto.SchedulerEnabledDTO;
import io.openk9.datasource.model.Datasource;
import io.quarkus.runtime.StartupEvent;
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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class SchedulerInitializer {

	void onStart(@Observes StartupEvent ev) throws SchedulerException {

		List<Datasource> datasourceList = Datasource.listAll();

		for (Datasource datasource : datasourceList) {

			createOrUpdateScheduler(datasource);

		}
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

			return;

		}

		JobDetail job = JobBuilder.newJob(DatasourceJob.class)
			.withIdentity(name)
			.usingJobData("driverServiceName", datasource.getDriverServiceName())
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

	public void deleteScheduler(Datasource datasource)
		throws SchedulerException {
		_scheduler.deleteJob(JobKey.jobKey(datasource.getName()));
	}

	public void performTask(Long datasourceId, String driverServiceName) {

		boolean schedulerEnabled = false;

		try {
			SchedulerEnabledDTO schedulerEnabledDTO = _pluginDriverClient
				.schedulerEnabled(driverServiceName);

			schedulerEnabled = schedulerEnabledDTO.isSchedulerEnabled();
		}
		catch (Exception e) {
			_log.error(e.getMessage(), e);
		}

		if (schedulerEnabled) {

			Datasource datasource =
				Datasource.findById(datasourceId);

			try {

				_pluginDriverClient
					.invokeDataParser(
						InvokeDataParserDTO
							.of(
								datasource.getDriverServiceName(), datasource,
								Date.from(datasource.getLastIngestionDate()),
								new Date()));
			}
			catch (Exception e) {
				_log.error(e.getMessage(), e);
			}

		}
		else {
			_log.warn(
				"[SCHEDULER] datasourceId: " + datasourceId +
				" service: " + driverServiceName + " not found"
			);
		}
	}

	@DisallowConcurrentExecution
	@ApplicationScoped
	public static class DatasourceJob implements Job {

		@Inject
		SchedulerInitializer taskBean;

		@Transactional
		public void execute(JobExecutionContext context) throws
			JobExecutionException {

			JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

			taskBean.performTask(
				jobDataMap.getLong("datasourceId"),
				jobDataMap.getString("driverServiceName"));
		}

	}

	@Inject
	Scheduler _scheduler;

	@Inject
	@RestClient
	PluginDriverClient _pluginDriverClient;

	private static final Logger _log = Logger.getLogger(
		SchedulerInitializer.class);

}
