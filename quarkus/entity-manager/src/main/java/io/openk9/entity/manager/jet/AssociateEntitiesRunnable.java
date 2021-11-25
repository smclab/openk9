package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.Pipelining;
import com.hazelcast.map.IMap;
import io.openk9.entity.manager.cache.model.AssociableEntityKey;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.IngestionEntity;
import io.openk9.entity.manager.service.index.DataService;
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

		IMap<AssociableEntityKey, Entity> associableEntityMap =
			MapUtil.getAssociableEntityMap(_hazelcastInstance);

		Set<AssociableEntityKey> associableEntityKeys =
			associableEntityMap.localKeySet();

		Map<AssociableEntityKey, Entity> localEntityMap =
			associableEntityMap.getAll(associableEntityKeys);

		_log.info("ingestionKeys: " + localEntityMap.size());

		Map<String, List<Entity>> groupingByIngestionId =
			localEntityMap
				.entrySet()
				.stream()
				.collect(
					Collectors.groupingBy(
						e -> e.getKey().getIngestionId(),
						Collectors.mapping(
							Map.Entry::getValue, Collectors.toList())));

		List<AssociableEntityKey> entitiesToRemove = new ArrayList<>();

		List<String> ingestionIds = new ArrayList<>();

		for (Map.Entry<String, List<Entity>> entry : groupingByIngestionId.entrySet()) {

			String ingestionId = entry.getKey();
			List<Entity> v = entry.getValue();

			if (v.isEmpty()) {
				continue;
			}

			DataService dataService =
				CDI.current().select(DataService.class).get();

			Long tenantId =
				v
					.stream()
					.map(Entity::getTenantId)
					.findFirst()
					.get();

			try {

				boolean associated =
					dataService.associateEntities(
						tenantId,
						ingestionId,
						v
							.stream()
							.map(IngestionEntity::fromEntity)
							.collect(Collectors.toList())
					);

				if (associated) {
					for (Entity entity : v) {
						entitiesToRemove.add(
							AssociableEntityKey.of(
								entity.getCacheId(),
								entity.getIngestionId()
							)
						);
						ingestionIds.add(ingestionId);
					}
				}
			}
			catch (Exception ioe) {
				_log.error(ioe.getMessage());
			}

		}

		_log.info("entities associated: " + entitiesToRemove.size() + " ingestionIds: " + ingestionIds);

		try {

			Pipelining pipelining = new Pipelining<>(10);

			for (AssociableEntityKey associateEntityKey : entitiesToRemove) {
				pipelining.add(
					associableEntityMap.removeAsync(associateEntityKey)
				);
			}

			pipelining.results();

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
