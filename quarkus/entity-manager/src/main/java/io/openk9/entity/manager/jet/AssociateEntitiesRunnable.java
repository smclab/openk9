package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.Pipelining;
import com.hazelcast.map.IMap;
import io.openk9.entity.manager.cache.model.IngestionEntity;
import io.openk9.entity.manager.cache.model.IngestionKey;
import io.openk9.entity.manager.service.DataService;
import io.openk9.entity.manager.util.MapUtil;
import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

		Map<String, List<IngestionEntity>> groupingByIngestionId =
			localIngestionMap
				.entrySet()
				.stream()
				.collect(
					Collectors.groupingBy(
						e -> e.getKey().getIngestionId(),
						Collectors.mapping(
							Map.Entry::getValue, Collectors.toList())));


		List<IngestionKey> entitiesToDelete = new ArrayList<>();

		for (Map.Entry<String, List<IngestionEntity>> entry : groupingByIngestionId.entrySet()) {

			String ingestionId = entry.getKey();
			List<IngestionEntity> v = entry.getValue();

			if (v.isEmpty()) {
				continue;
			}

			DataService dataService =
				CDI.current().select(DataService.class).get();

			Long tenantId =
				v
					.stream()
					.map(IngestionEntity::getTenantId)
					.findFirst()
					.get();

			try {

				boolean associated =
					dataService.associateEntities(
						tenantId,
						ingestionId,
						v
					);

				if (associated) {
					entitiesToDelete.addAll(
						v
							.stream()
							.map(ie -> IngestionKey.of(
								ie.getCacheId(), ingestionId, ie.getTenantId()))
							.collect(Collectors.toList())
					);
				}
			}
			catch (Exception ioe) {
				_log.error(ioe.getMessage());
			}

		}

		try {

			Pipelining pipelining = new Pipelining(entitiesToDelete.size());

			for (IngestionKey ingestionKey : entitiesToDelete) {
				pipelining.add(ingestionMap.removeAsync(ingestionKey));
			}

			pipelining.results();

			_log.info("ingestion processed: " + entitiesToDelete.size());

		}
		catch (Exception e) {
			_log.error(e.getMessage(), e);
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
