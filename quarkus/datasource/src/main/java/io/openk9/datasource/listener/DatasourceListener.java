package io.openk9.datasource.listener;

import io.openk9.datasource.model.Datasource;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import org.quartz.SchedulerException;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PreUpdate;

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
		SchedulerInitializer schedulerInitializer =
			CDI.current().select(SchedulerInitializer.class).get();

		try {
			schedulerInitializer.deleteScheduler(datasource);
		}
		catch (SchedulerException e) {
			_log.error(e.getMessage(), e);
		}

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

		_deleteEmitter.send(datasource);

	}

	@Inject
	@Channel("datasource-DELETE-Datasource")
	Emitter<Datasource> _deleteEmitter;

	@Inject
	@Channel("datasource-INSERT-Datasource")
	Emitter<Datasource> _insertEmitter;

	@Inject
	@Channel("datasource-UPDATE-Datasource")
	Emitter<Datasource> _updateEmitter;

	private static final Logger _log = Logger.getLogger(
		DatasourceListener.class);

}
