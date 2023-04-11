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

import io.openk9.auth.tenant.TenantResolver;
import io.openk9.datasource.event.util.EventType;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.util.K9Entity;
import oracle.jdbc.proxy.annotation.Post;
import org.hibernate.Hibernate;
import org.quartz.SchedulerException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PreUpdate;

@ApplicationScoped
public class K9EntityListener {

	@PostPersist
	public void beforeAdd(K9Entity k9Entity) throws SchedulerException {
		_handle(k9Entity, EventType.CREATE);
	}

	@PostUpdate
	public void beforeUpdate(K9Entity k9Entity) throws SchedulerException {
		_handle(k9Entity, EventType.UPDATE);
	}


	@PostRemove
	public void postRemove(K9Entity k9Entity) throws SchedulerException {
		_handle(k9Entity, EventType.DELETE);
	}

	private void _handle(K9Entity k9Entity, String create)
		throws SchedulerException {

		if (_isDatasource(k9Entity)) {
			if (EventType.DELETE.equals(create)) {
				_schedulerInitializer.get().deleteScheduler(
					(Datasource)k9Entity);
			}
			else {
				_createOrUpdateScheduler((Datasource) k9Entity);
			}
		}

	}

	private void _createOrUpdateScheduler(Datasource datasource)
		throws SchedulerException {
		_schedulerInitializer.get().createOrUpdateScheduler(
			tenantResolver.getTenantName(), datasource);
	}

	private boolean _isDatasource(K9Entity k9Entity) {
		return k9Entity instanceof Datasource ||
			   Hibernate.getClass(k9Entity).isAssignableFrom(Datasource.class);
	}

	@Inject
	Instance<SchedulerInitializer> _schedulerInitializer;

	@Inject
	TenantResolver tenantResolver;

}
