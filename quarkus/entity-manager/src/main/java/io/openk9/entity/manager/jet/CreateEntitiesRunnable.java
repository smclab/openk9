package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicates;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.model.EntityIndex;
import io.openk9.entity.manager.service.EntityService;
import io.openk9.entity.manager.util.MapUtil;
import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CreateEntitiesRunnable
	implements Runnable, HazelcastInstanceAware {

	@Override
	public void run() {

		_log.info("start CreateEntitiesRunnable");

		IMap<EntityKey, Entity> entityIMap =
			MapUtil.getEntityMap(_hazelcastInstance);

		Set<EntityKey> entityKeys = entityIMap.localKeySet(
			Predicates.equal("id", null));

		Map<EntityKey, Entity> localEntityMap = entityIMap.getAll(entityKeys);

		Map<EntityKey, Entity> entityToUpdate = new HashMap<>();

		for (Map.Entry<EntityKey, Entity> entry : localEntityMap.entrySet()) {

			Entity v = entry.getValue();

			EntityService entityService = CDI.current().select(EntityService.class).get();

			try {
				entityService.index(
					EntityIndex.of(
						v.getCacheId(),
						v.getTenantId(),
						v.getName(),
						v.getType())
				);

				v.setId(v.getCacheId());

				entityToUpdate.put(entry.getKey(), v);

			}
			catch (Exception ioe) {
				_log.error(ioe.getMessage());
			}

		}

		if (!entityToUpdate.isEmpty()) {
			entityIMap.setAll(entityToUpdate);
		}

	}

	@Override
	public void setHazelcastInstance(
		HazelcastInstance hazelcastInstance) {
		_hazelcastInstance = hazelcastInstance;
	}

	private transient HazelcastInstance _hazelcastInstance;

	private static final Logger _log = Logger.getLogger(
		AssociateEntitiesRunnable.class);

}
