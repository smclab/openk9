package io.openk9.entity.manager.loader;


import com.hazelcast.map.MapStoreAdapter;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.model.EntityIndex;
import io.openk9.entity.manager.service.EntityService;
import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import java.io.IOException;

public class EntityMapStore extends MapStoreAdapter<EntityKey, Entity> {

	@Override
	public Entity load(EntityKey key) {

		try {

			EntityService entityService =
				CDI.current().select(EntityService.class).get();

			EntityIndex entityIndex = entityService.searchByNameAndType(
				key.getTenantId(), key.getName(), key.getType());

			if (entityIndex == null) {
				return null;
			}

			return new Entity(
				entityIndex.getId(),
				entityIndex.getId(),
				entityIndex.getTenantId(),
				entityIndex.getName(),
				entityIndex.getType()
			);

		}
		catch (Exception e) {
			_log.error(e.getMessage(), e);
		}

		return null;

	}

	private static final Logger _log =
		Logger.getLogger(EntityMapStore.class);

}
