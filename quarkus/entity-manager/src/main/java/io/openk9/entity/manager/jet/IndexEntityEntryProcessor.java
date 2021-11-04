package io.openk9.entity.manager.jet;

import com.hazelcast.map.EntryProcessor;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.model.EntityIndex;
import io.openk9.entity.manager.service.EntityService;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class IndexEntityEntryProcessor
	implements EntryProcessor<EntityKey, Entity, Entity>, Serializable {

	public IndexEntityEntryProcessor(
		EntityService entityService, Logger logger) {
		_entityService = entityService;
		_logger = logger;
	}

	@Override
	public Entity process(Map.Entry<EntityKey, Entity> entry) {

		Entity v = entry.getValue();

		try {
			_entityService.index(
				EntityIndex.of(
					v.getCacheId(),
					v.getTenantId(),
					v.getName(),
					v.getType())
			);
		}
		catch (IOException ioe) {
			_logger.error(ioe.getMessage(), ioe);
		}

		v.setId(v.getCacheId());

		entry.setValue(v);

		return null;

	}

	private final EntityService _entityService;
	private final Logger _logger;

}
