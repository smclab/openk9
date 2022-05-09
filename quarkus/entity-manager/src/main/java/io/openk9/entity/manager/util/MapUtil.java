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

package io.openk9.entity.manager.util;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import io.openk9.entity.manager.cache.model.AssociableEntityKey;
import io.openk9.entity.manager.cache.model.DocumentKey;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.cache.model.EntityRelation;
import io.openk9.entity.manager.cache.model.EntityRelationKey;
import io.openk9.entity.manager.cache.model.IngestionEntity;
import io.openk9.entity.manager.cache.model.IngestionKey;

public class MapUtil {

	public static IMap<AssociableEntityKey, Entity> getAssociableEntityMap(
		HazelcastInstance hazelcastInstance) {
		return hazelcastInstance.getMap("associableEntityMap");
	}

	public static IMap<EntityKey, Entity> getEntityMap(
		HazelcastInstance hazelcastInstance) {
		return hazelcastInstance.getMap("entityMap");
	}

	public static MultiMap<EntityKey, Entity> getRestEntityMultiMap(
		HazelcastInstance hazelcastInstance) {
		return hazelcastInstance.getMultiMap("restEntityMultiMap");
	}

	public static IMap<EntityRelationKey, EntityRelation> getEntityRelationMap(
		HazelcastInstance hazelcastInstance) {
		return hazelcastInstance.getMap("entityRelationMap");
	}

	public static MultiMap<IngestionKey, String> getEntityContextMultiMap(
		HazelcastInstance hazelcastInstance) {
		return hazelcastInstance.getMultiMap("entityContextMultiMap");
	}

	public static MultiMap<DocumentKey, String> getDocumentEntityMapMap(
		HazelcastInstance hazelcastInstance) {
		return hazelcastInstance.getMultiMap("documentEntityMap");
	}

	public static IMap<IngestionKey, IngestionEntity> getIngestionMap(
		HazelcastInstance hazelcastInstance) {
		return hazelcastInstance.getMap("ingestionMap");
	}

}
