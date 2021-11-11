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
