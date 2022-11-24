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
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import com.hazelcast.projection.Projections;
import com.hazelcast.query.Predicates;
import io.openk9.entity.manager.cache.model.DocumentKey;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.model.graph.DocumentGraph;
import io.openk9.entity.manager.service.graph.EntityGraphService;
import io.openk9.entity.manager.util.MapUtil;
import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

public class CreateDocumentEntitiesRunnable
	implements StopWatchRunnable, HazelcastInstanceAware, Serializable {

	@Override
	public void run_() {

		MultiMap<DocumentKey, String> documentEntityMapMap =
			MapUtil.getDocumentEntityMapMap(_hazelcastInstance);

		Set<DocumentKey> documentKeys = documentEntityMapMap.localKeySet();

		if (documentKeys.isEmpty()) {
			if (_log.isDebugEnabled()) {
				_log.debug("documentKeys is empty. skip");
			}
			return;
		}

		_log.info("start CreateEntitiesRunnable");

		IMap<EntityKey, Entity> entityIMap =
			MapUtil.getEntityMap(_hazelcastInstance);

		EntityGraphService entityGraphService = CDI.current().select(
			EntityGraphService.class).get();

		for (DocumentKey documentKey : documentKeys) {

			Collection<String> entityCacheIds =
				documentEntityMapMap.get(documentKey);

			String[] arr = entityCacheIds.toArray(String[]::new);

			Collection<Object> project =
				entityIMap.project(
					Projections.singleAttribute("indexable"),
					Predicates.and(
						Predicates.in("cacheId", arr),
						Predicates.notEqual("id", null),
						Predicates.notEqual("graphId", null)
					)
				);

			if (project.size() == entityCacheIds.size()) {

				Collection<Entity> entities =
					entityIMap.values(Predicates.in("cacheId", arr));

				DocumentGraph document =
					entityGraphService.insertDocument(
						DocumentGraph.of(
							null,
							documentKey.getDatasourceId(),
							documentKey.getTenantId(),
							documentKey.getContentId())
					);

				for (Entity entity : entities) {
					entityGraphService.createDocumentRelationship(
						entity.getId(), document.getId(), "related_to");
				}

				documentEntityMapMap.remove(documentKey);

			}

		}

	}

	@Override
	public void setHazelcastInstance(
		HazelcastInstance hazelcastInstance) {
		_hazelcastInstance = hazelcastInstance;
	}

	private transient HazelcastInstance _hazelcastInstance;

	private static final Logger _log = Logger.getLogger(
		CreateDocumentEntitiesRunnable.class);

}
