package io.openk9.entity.manager.loader;


import com.hazelcast.map.MapStoreAdapter;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.model.graph.EntityGraph;
import io.openk9.entity.manager.service.graph.EntityGraphService;
import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;

public class EntityMapStore extends MapStoreAdapter<EntityKey, Entity> {

	@Override
	public Entity load(EntityKey key) {

		try {

			EntityGraphService entityGraphService =
				CDI.current().select(EntityGraphService.class).get();

			EntityGraph entityIndex = entityGraphService.searchByNameAndType(
				key.getTenantId(), key.getName(), key.getType());

			if (entityIndex == null) {
				return null;
			}

			return new Entity(
				entityIndex.getId(),
				entityIndex.getId(),
				entityIndex.getTenantId(),
				entityIndex.getName(),
				entityIndex.getType(),
				entityIndex.getGraphId()
			);

		}
		catch (Exception e) {
			_log.error(e.getMessage());
		}

		return null;

	}

	private static final Logger _log =
		Logger.getLogger(EntityMapStore.class);

}
