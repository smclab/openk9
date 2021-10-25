package io.openk9.datasource.listener;

import io.openk9.datasource.emitter.datasource.DeleteEmitter;
import io.openk9.datasource.emitter.datasource.InsertEmitter;
import io.openk9.datasource.emitter.datasource.UpdateEmitter;
import io.openk9.datasource.model.Datasource;
import org.jboss.logging.Logger;
import org.quartz.SchedulerException;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PreUpdate;

public class DatasourceListener {

	@PostPersist
	public void beforeAdd(Datasource datasource) {
		_createOrUpdateScheduler(datasource);
		InsertEmitter insertEmitter =
			CDI.current().select(InsertEmitter.class).get();
		insertEmitter.send(datasource);
	}

	@PreUpdate
	public void beforeUpdate(Datasource datasource) {
		_createOrUpdateScheduler(datasource);
		UpdateEmitter updateEmitter =
			CDI.current().select(UpdateEmitter.class).get();
		updateEmitter.send(datasource);
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

		DeleteEmitter deleteEmitter =
			CDI.current().select(DeleteEmitter.class).get();

		deleteEmitter.send(datasource);

	}

	private void _createOrUpdateScheduler(Datasource datasource) {
		SchedulerInitializer schedulerInitializer =
			CDI.current().select(SchedulerInitializer.class).get();

		try {
			schedulerInitializer.createOrUpdateScheduler(datasource);
		}
		catch (SchedulerException e) {
			_log.error(e.getMessage(), e);
		}

	}

	private static final Logger _log = Logger.getLogger(
		DatasourceListener.class);

}
