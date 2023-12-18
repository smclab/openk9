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

import io.openk9.datasource.event.util.EventType;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.util.K9Entity;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.hibernate.Hibernate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

@ApplicationScoped
public class K9EntityListener {

	@PostPersist
	public void beforeAdd(K9Entity k9Entity) {
		_handle(k9Entity, EventType.CREATE);
	}

	@PostUpdate
	public void beforeUpdate(K9Entity k9Entity) {
		_handle(k9Entity, EventType.UPDATE);
	}


	@PostRemove
	public void postRemove(K9Entity k9Entity) {
		_handle(k9Entity, EventType.DELETE);
	}

	private void _handle(K9Entity k9Entity, String event) {

		if (_isDatasource(k9Entity)) {
			Datasource datasource = (Datasource) k9Entity;
			if (EventType.DELETE.equals(event)) {
				bus.send(SchedulerInitializer.DELETE_SCHEDULER, datasource);
			}
			else {
				bus.send(SchedulerInitializer.UPDATE_SCHEDULER, datasource);
			}
		}

	}

	private boolean _isDatasource(K9Entity k9Entity) {
		return k9Entity instanceof Datasource ||
			   Hibernate.getClass(k9Entity).isAssignableFrom(Datasource.class);
	}

	@Inject
	EventBus bus;

}
