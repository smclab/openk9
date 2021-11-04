package io.openk9.entity.manager.jet;

import com.hazelcast.map.EntryProcessor;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.model.EntityIndex;
import io.openk9.entity.manager.service.EntityService;
import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class IndexEntityEntryProcessor
	implements EntryProcessor<EntityKey, Entity, Entity>, Serializable {

	@Override
	public Entity process(Map.Entry<EntityKey, Entity> entry) {

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
		}
		catch (IOException ioe) {
			CDI.current().select(Logger.class).get().error(ioe.getMessage(), ioe);
		}

		v.setId(v.getCacheId());

		entry.setValue(v);

		return null;

	}

}
