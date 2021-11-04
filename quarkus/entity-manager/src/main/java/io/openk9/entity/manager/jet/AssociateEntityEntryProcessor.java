package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.multimap.MultiMap;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.IngestionKey;
import io.openk9.entity.manager.service.DataService;
import io.openk9.entity.manager.util.MapUtil;
import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.Map;

public class AssociateEntityEntryProcessor
	implements EntryProcessor<IngestionKey, Entity, Entity>, Serializable,
	HazelcastInstanceAware {

	@Override
	public Entity process(Map.Entry<IngestionKey, Entity> entry) {

		IngestionKey k = entry.getKey();
		Entity v = entry.getValue();

		if (v != null) {

			MultiMap<IngestionKey, String> entityContextMultiMap =
				MapUtil.getEntityContextMultiMap(_hazelcastInstance);

			DataService dataService =
				CDI.current().select(DataService.class).get();

			try {

				boolean associated =
					dataService.associateEntity(
						v.getTenantId(),
						k.getIngestionId(),
						v,
						entityContextMultiMap.get(k)
					);

				if (associated) {
					entry.setValue(null);
				}
			}
			catch (Exception ioe) {
				_log.error(ioe.getMessage(), ioe);
			}

		}

		return v;

	}

	private transient HazelcastInstance _hazelcastInstance;

	@Override
	public void setHazelcastInstance(
		HazelcastInstance hazelcastInstance) {
		_hazelcastInstance = hazelcastInstance;
	}

	private static final Logger _log = Logger.getLogger(AssociateEntityEntryProcessor.class);

}
