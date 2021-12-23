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
