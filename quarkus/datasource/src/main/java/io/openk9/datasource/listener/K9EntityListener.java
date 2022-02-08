package io.openk9.datasource.listener;

import io.openk9.datasource.emitter.datasource.K9EntityEmitter;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.K9Entity;
import org.quartz.SchedulerException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PreUpdate;

@ApplicationScoped
public class K9EntityListener {

	@PostPersist
	public void beforeAdd(K9Entity k9Entity) throws SchedulerException {
		if (k9Entity instanceof Datasource) {
			_createOrUpdateScheduler((Datasource)k9Entity);
		}
		_k9EntityEmitter.sendInsert(k9Entity);
	}

	@PreUpdate
	public void beforeUpdate(K9Entity k9Entity) throws SchedulerException {
		if (k9Entity instanceof Datasource) {
			_createOrUpdateScheduler((Datasource)k9Entity);
		}
		_k9EntityEmitter.sendUpdate(k9Entity);
	}

	@PostRemove
	public void postRemove(K9Entity k9Entity) throws SchedulerException {
		if (k9Entity instanceof Datasource) {
			_schedulerInitializer.get().deleteScheduler((Datasource)k9Entity);
		}
		_k9EntityEmitter.sendDelete(k9Entity);

	}

	private void _createOrUpdateScheduler(Datasource datasource)
		throws SchedulerException {
		_schedulerInitializer.get().createOrUpdateScheduler(datasource);

	}

	@Inject
	K9EntityEmitter _k9EntityEmitter;

	@Inject
	Instance<SchedulerInitializer> _schedulerInitializer;

}
