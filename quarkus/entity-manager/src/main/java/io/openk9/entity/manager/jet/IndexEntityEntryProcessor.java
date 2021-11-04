package io.openk9.entity.manager.jet;

import com.hazelcast.map.EntryProcessor;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.model.EntityIndex;
import io.openk9.entity.manager.service.EntityService;
import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.Map;

public class IndexEntityEntryProcessor
	implements EntryProcessor<EntityKey, Entity, Entity>, Serializable {

	@Override
	public Entity process(Map.Entry<EntityKey, Entity> entry) {

		Entity v = entry.getValue();

		EntityService entityService = CDI.current().select(EntityService.class).get();

		if (v.getId() != null) {
			return null;
		}

		try {
			entityService.index(
				EntityIndex.of(
					v.getCacheId(),
					v.getTenantId(),
					v.getName(),
					v.getType())
			);
		}
		catch (Exception ioe) {
			_log.error(ioe.getMessage(), ioe);
		}

		v.setId(v.getCacheId());

		entry.setValue(v);

		return null;

	}

	private static final Logger _log = Logger.getLogger(IndexEntityEntryProcessor.class);

}
