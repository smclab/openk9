/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

		IMap<AssociableEntityKey, Entity> associableEntityMap =
			MapUtil.getAssociableEntityMap(_hazelcastInstance);

		if (associableEntityMap.isEmpty()) {
			if (_log.isDebugEnabled()) {
				_log.debug("associableEntityMap is empty. skip");
			}
			return;
		}

		_log.info("start AssociateEntitiesRunnable");

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
