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

package io.openk9.datasource.listener;

import io.openk9.datasource.emitter.datasource.K9EntityEmitter;
import io.openk9.datasource.event.sender.EventSender;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.K9Entity;
import io.vertx.mutiny.core.eventbus.EventBus;
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

		_eventSender.sendEventAsJson(
			"ADD", k9Entity.getPrimaryKey(), k9Entity.getType().getName(),
			k9Entity);
	}

	@PreUpdate
	public void beforeUpdate(K9Entity k9Entity) throws SchedulerException {
		if (k9Entity instanceof Datasource) {
			_createOrUpdateScheduler((Datasource)k9Entity);
		}
		_k9EntityEmitter.sendUpdate(k9Entity);

		_eventSender.sendEventAsJson(
			"UPDATE", k9Entity.getPrimaryKey(), k9Entity.getType().getName(),
			k9Entity);
	}

	@PostRemove
	public void postRemove(K9Entity k9Entity) throws SchedulerException {
		if (k9Entity instanceof Datasource) {
			_schedulerInitializer.get().deleteScheduler((Datasource)k9Entity);
		}
		_k9EntityEmitter.sendDelete(k9Entity);

		_eventSender.sendEventAsJson(
			"DELETE", k9Entity.getPrimaryKey(), k9Entity.getType().getName(),
			k9Entity);
	}

	private void _createOrUpdateScheduler(Datasource datasource)
		throws SchedulerException {
		_schedulerInitializer.get().createOrUpdateScheduler(datasource);

	}

	@Inject
	K9EntityEmitter _k9EntityEmitter;

	@Inject
	Instance<SchedulerInitializer> _schedulerInitializer;

	@Inject
	EventSender _eventSender;

	@Inject
	EventBus bus;

}
