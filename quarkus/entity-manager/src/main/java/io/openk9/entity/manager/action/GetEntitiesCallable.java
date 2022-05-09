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

package io.openk9.entity.manager.action;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicates;
import com.hazelcast.scheduledexecutor.impl.HashMapAdapter;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;

public class GetEntitiesCallable
	implements Callable<Map<EntityKey, Entity>>, HazelcastInstanceAware, Serializable {

	public GetEntitiesCallable(String[] ingestionIds) {
		_ingestionIds = ingestionIds;
	}

	@Override
	public Map<EntityKey, Entity> call() throws Exception {

		IMap<EntityKey, Entity> entityMap =
			_hazelcastInstance.getMap("entityMap");

		Map<EntityKey, Entity> result = new HashMapAdapter<>();

		result.putAll(
			entityMap.getAll(
				entityMap.localKeySet(
					Predicates.in("ingestionId", _ingestionIds)))
		);

		return result;
	}

	@Override
	public void setHazelcastInstance(
		HazelcastInstance hazelcastInstance) {
		_hazelcastInstance = hazelcastInstance;
	}

	private String[] _ingestionIds;

	private transient HazelcastInstance _hazelcastInstance;

}
