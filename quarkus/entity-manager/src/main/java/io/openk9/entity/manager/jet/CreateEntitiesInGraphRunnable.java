package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicates;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.model.graph.EntityGraph;
import io.openk9.entity.manager.service.graph.EntityGraphService;
import io.openk9.entity.manager.util.MapUtil;
import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class CreateEntitiesInGraphRunnable
	implements StopWatchRunnable, HazelcastInstanceAware, Serializable {

	@Override
	public void run_() {

		_log.info("start CreateEntitiesInGraphRunnable");

		IMap<EntityKey, Entity> entityIMap =
			MapUtil.getEntityMap(_hazelcastInstance);

		Set<EntityKey> entityKeys = entityIMap.localKeySet(
			Predicates.and(
				Predicates.notEqual("id", null),
				Predicates.equal("graphId", null)
			)
		);

		Map<EntityKey, Entity> localEntityMap = entityIMap.getAll(entityKeys);

		_log.info("entityKeys: " + entityKeys.size());

		EntityGraphService entityGraphService = CDI.current().select(
			EntityGraphService.class).get();

		for (Map.Entry<EntityKey, Entity> entry : localEntityMap.entrySet()) {

			Entity v = entry.getValue();

			try {
				EntityGraph entityGraph =
					entityGraphService.insertEntity(
						v.getType(),
						EntityGraph.of(
							v.getId(),
							null,
							v.getTenantId(),
							v.getName(),
							v.getType()
						)
					);

				v.setGraphId(entityGraph.getGraphId());

				entityIMap.set(entry.getKey(), v);

			}
			catch (Exception e) {
				_log.error(e.getMessage());
			}

		}

	}

	@Override
	public void setHazelcastInstance(
		HazelcastInstance hazelcastInstance) {
		_hazelcastInstance = hazelcastInstance;
	}

	private transient HazelcastInstance _hazelcastInstance;

	private static final Logger _log = Logger.getLogger(
		CreateEntitiesInGraphRunnable.class);

}
