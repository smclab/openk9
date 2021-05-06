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

package io.openk9.ingestion.driver.manager.internal;

import io.openk9.datasource.repository.DatasourceRepository;
import io.openk9.model.Datasource;
import io.openk9.osgi.util.AutoCloseables;
import io.openk9.plugin.driver.manager.client.api.PluginDriverManagerClient;
import io.openk9.plugin.driver.manager.model.SchedulerEnabledDTO;
import io.openk9.sql.api.event.EntityEvent;
import io.openk9.sql.api.event.EntityEventBus;
import org.apache.karaf.scheduler.Job;
import org.apache.karaf.scheduler.ScheduleOptions;
import org.apache.karaf.scheduler.Scheduler;
import org.apache.karaf.scheduler.SchedulerError;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Map;

@Component(
	immediate = true,
	service = {
		DriverManagerActivator.class
	}
)
public class DriverManagerActivator {

	@interface Config {
		String cronExpression() default "0 */2 * ? * *";
	}

	@Activate
	public void activate(Config config) throws SchedulerError {

		Disposable disposable1 = _datasourceRepository
			.findAll(true)
			.concatMap(this::_schedule)
			.subscribe();

		Disposable disposable2 = _entityEventBus
			.stream()
			.filter(e -> e.getEntityClass() == Datasource.class)
			.concatMap(entityEvent -> {

				Datasource datasource = (Datasource) entityEvent.getValue();

				if (
					entityEvent instanceof EntityEvent.UpdateEvent
					|| entityEvent instanceof EntityEvent.InsertEvent
				) {
					return _schedule(datasource);
				}
				else {
					return Mono.fromRunnable(
						() -> _scheduler.unschedule(
							_PREFIX + datasource.getName()));
				}

			})
			.subscribe();

		_autoClosableSafe = AutoCloseables.mergeAutoCloseableToSafe(
			disposable1::dispose, disposable2::dispose
		);

	}

	@Deactivate
	public void deactivate() throws SchedulerError {

		_unschedule();

		_scheduler.unschedule(_ROOT_SCHEDULER);

		_autoClosableSafe.close();

	}

	private Mono<Void> _schedule(Datasource datasource) {
		return Mono.create(sink -> {

			try {

				Map<String, ScheduleOptions> jobs = _scheduler.getJobs();

				String jobName = _PREFIX + datasource.getName();

				ScheduleOptions scheduleOptions = jobs.get(jobName);

				if (scheduleOptions != null) {
					_scheduler.unschedule(jobName);
				}

				_scheduler.schedule(
					_createJob(
						datasource.getDriverServiceName(),
						datasource.getDatasourceId()),
					_scheduler
						.EXPR(datasource.getScheduling())
						.name(jobName)
				);

				_scheduler.trigger(jobName);

				sink.success();

			}
			catch (SchedulerError schedulerError) {
				sink.error(schedulerError);
			}

		});

	}

	protected void _unschedule() throws SchedulerError {
		Map<String, ScheduleOptions> jobs = _scheduler.getJobs();

		for (String jobName : jobs.keySet()) {

			if (jobName.equals(_ROOT_SCHEDULER)) {
				continue;
			}

			if (jobName.startsWith(_PREFIX)) {
				_scheduler.unschedule(jobName);
			}
		}
	}

	private Job _createJob(
		String serviceName, Long datasourceId) {

		return (context) -> {

			Boolean schedulerEnabled =
				_pluginDriverManagerClient
					.schedulerEnabled(serviceName)
					.map(SchedulerEnabledDTO::isSchedulerEnabled)
					.doOnError(this::_logError)
					.onErrorReturn(Boolean.FALSE)
					.block();

			if (schedulerEnabled) {

				Datasource newDatasource =
					_datasourceRepository
						.findByPrimaryKey(datasourceId)
						.block();

				_pluginDriverManagerClient
					.invokeDataParser(
						serviceName,
						newDatasource,
						Date.from(newDatasource.getLastIngestionDate()),
						new Date()
					)
					.doOnError(this::_logError)
					.onErrorContinue((throwable, o) -> _logError(throwable))
					.block();
			}
			else {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"[SCHEDULER] datasourceId: " + datasourceId +
						" service: " + serviceName + " not found"
					);
				}
			}

		};
	}

	private void _logError(Throwable throwable) {
		if (_log.isErrorEnabled()) {
			_log.error(throwable.getMessage(), throwable);
		}
	}

	private AutoCloseables.AutoCloseableSafe _autoClosableSafe;

	@Reference
	private Scheduler _scheduler;

	@Reference
	private DatasourceRepository _datasourceRepository;

	@Reference
	private PluginDriverManagerClient _pluginDriverManagerClient;

	@Reference
	private EntityEventBus _entityEventBus;

	private static final String _PREFIX =
		DriverManagerActivator.class.getName() + "-";

	private static final String _ROOT_SCHEDULER = _PREFIX + "ROOT";

	private static final Logger _log =
		LoggerFactory.getLogger(DriverManagerActivator.class);

}
