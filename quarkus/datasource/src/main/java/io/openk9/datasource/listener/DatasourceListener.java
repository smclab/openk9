package io.openk9.datasource.listener;

import io.openk9.datasource.emitter.datasource.DeleteEmitter;
import io.openk9.datasource.emitter.datasource.InsertEmitter;
import io.openk9.datasource.emitter.datasource.UpdateEmitter;
import io.openk9.datasource.model.Datasource;
import org.jboss.logging.Logger;
import org.quartz.SchedulerException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PreUpdate;

@ApplicationScoped
public class DatasourceListener {

	@PostPersist
	public void beforeAdd(Datasource datasource) {
		_createOrUpdateScheduler(datasource);
		_insertEmitter.send(datasource);
	}

	@PreUpdate
	public void beforeUpdate(Datasource datasource) {
		_createOrUpdateScheduler(datasource);
		_updateEmitter.send(datasource);
	}

	@PostRemove
	public void postRemove(Datasource datasource) {
		try {
			_schedulerInitializer.deleteScheduler(datasource);
		}
		catch (SchedulerException e) {
			_log.error(e.getMessage(), e);
		}

		_deleteEmitter.send(datasource);

	}

	private void _createOrUpdateScheduler(Datasource datasource) {

		try {
			_schedulerInitializer.createOrUpdateScheduler(datasource);
		}
		catch (SchedulerException e) {
			_log.error(e.getMessage(), e);
		}

	}

	@Inject
	DeleteEmitter _deleteEmitter;

	@Inject
	InsertEmitter _insertEmitter;

	@Inject
	UpdateEmitter _updateEmitter;

	@Inject
	SchedulerInitializer _schedulerInitializer;

	private static final Logger _log = Logger.getLogger(
		DatasourceListener.class);

}
