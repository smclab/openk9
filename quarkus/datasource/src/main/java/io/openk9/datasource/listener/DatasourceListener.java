package io.openk9.datasource.listener;

import io.openk9.datasource.model.Datasource;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PreUpdate;

public class DatasourceListener {

	@PreUpdate
	@PostPersist
	public void beforeAddOrUpdate(Datasource datasource) {

		SchedulerInitializer schedulerInitializer =
			CDI.current().select(SchedulerInitializer.class).get();

		try {
			schedulerInitializer.createOrUpdateScheduler(datasource);
		}
		catch (SchedulerException e) {
			_log.error(e.getMessage(), e);
		}

	}

	@PostRemove
	public void postRemove(Datasource datasource) {
		SchedulerInitializer schedulerInitializer =
			CDI.current().select(SchedulerInitializer.class).get();

		try {
			schedulerInitializer.deleteScheduler(datasource);
		}
		catch (SchedulerException e) {
			_log.error(e.getMessage(), e);
		}

	}

	private static final Logger _log = LoggerFactory.getLogger(
		DatasourceListener.class);

}
