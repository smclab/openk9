package io.openk9.datasource.listener;

import io.openk9.datasource.emitter.datasource.DeleteEmitter;
import io.openk9.datasource.emitter.datasource.InsertEmitter;
import io.openk9.datasource.emitter.datasource.UpdateEmitter;
import io.openk9.datasource.model.Datasource;
import org.quartz.SchedulerException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PreUpdate;

@ApplicationScoped
public class DatasourceListener {

	@PostPersist
	public void beforeAdd(Datasource datasource) throws SchedulerException {
		_createOrUpdateScheduler(datasource);
		_insertEmitter.send(datasource);
	}

	@PreUpdate
	public void beforeUpdate(Datasource datasource) throws SchedulerException {
		_createOrUpdateScheduler(datasource);
		_updateEmitter.send(datasource);
	}

	@PostRemove
	public void postRemove(Datasource datasource) throws SchedulerException {
		_schedulerInitializer.get().deleteScheduler(datasource);
		_deleteEmitter.send(datasource);

	}

	private void _createOrUpdateScheduler(Datasource datasource)
		throws SchedulerException {
		_schedulerInitializer.get().createOrUpdateScheduler(datasource);

	}

	@Inject
	DeleteEmitter _deleteEmitter;

	@Inject
	InsertEmitter _insertEmitter;

	@Inject
	UpdateEmitter _updateEmitter;

	@Inject
	Instance<SchedulerInitializer> _schedulerInitializer;

}
