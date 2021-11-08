package io.openk9.entity.manager.cache;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityCacheIdKey;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.cache.model.EntityRelation;
import io.openk9.entity.manager.cache.model.EntityRelationKey;
import io.openk9.entity.manager.cache.model.EntityWithRelation;
import io.openk9.entity.manager.cache.model.IngestionEntity;
import io.openk9.entity.manager.cache.model.IngestionKey;

public class EntityManagerDataSerializableFactory
	implements DataSerializableFactory {

	public static final int FACTORY_ID = 1;

	public static final int ENTITY_TYPE = 1;
	public static final int ENTITY_KEY_TYPE = 2;
	public static final int ENTITY_RELATION_TYPE = 3;
	public static final int ENTITY_RELATION_KEY_TYPE = 4;
	public static final int ENTITY_WITH_RELATION_TYPE = 5;
	public static final int INGESTION_KEY_TYPE = 6;
	public static final int ENTITY_CACHE_ID_KEY_TYPE = 7;
	public static final int INGESTION_ENTITY_TYPE = 8;

	@Override
	public IdentifiedDataSerializable create(int typeId) {
		switch (typeId) {
			case ENTITY_TYPE:
				return new Entity();
			case ENTITY_KEY_TYPE:
				return new EntityKey();
			case ENTITY_RELATION_TYPE:
				return new EntityRelation();
			case ENTITY_RELATION_KEY_TYPE:
				return new EntityRelationKey();
			case ENTITY_WITH_RELATION_TYPE:
				return new EntityWithRelation();
			case INGESTION_KEY_TYPE:
				return new IngestionKey();
			case ENTITY_CACHE_ID_KEY_TYPE:
				return new EntityCacheIdKey();
			case INGESTION_ENTITY_TYPE:
				return new IngestionEntity();
			default:
				return null;
		}
	}
}
