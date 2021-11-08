package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;
import io.openk9.entity.manager.cache.model.IngestionEntity;
import io.openk9.entity.manager.cache.model.IngestionKey;
import io.openk9.entity.manager.service.DataService;
import io.openk9.entity.manager.util.MapUtil;
import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class AssociateEntitiesRunnable
	implements StopWatchRunnable, HazelcastInstanceAware, Serializable {

	@Override
	public void run_() {

		_log.info("start AssociateEntitiesRunnable");

		IMap<IngestionKey, IngestionEntity> ingestionMap =
			MapUtil.getIngestionMap(_hazelcastInstance);

		Set<IngestionKey> entityKeys = ingestionMap.localKeySet();

		Map<IngestionKey, IngestionEntity> localIngestionMap =
			ingestionMap.getAll(entityKeys);

		_log.info("ingestionKeys: " + entityKeys.size());

		int count = 0;

		for (Map.Entry<IngestionKey, IngestionEntity> entry : localIngestionMap.entrySet()) {

			IngestionKey k = entry.getKey();
			IngestionEntity v = entry.getValue();

			DataService dataService =
				CDI.current().select(DataService.class).get();

			try {

				boolean associated =
					dataService.associateEntity(
						v.getTenantId(),
						k.getIngestionId(),
						v
					);

				if (associated) {
					ingestionMap.delete(k);
					count++;
				}
			}
			catch (Exception ioe) {
				_log.error(ioe.getMessage());
			}

		}

		_log.info("ingestion processed: " + count);

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
