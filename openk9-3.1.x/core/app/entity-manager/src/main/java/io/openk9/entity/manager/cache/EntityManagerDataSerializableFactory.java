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

package io.openk9.entity.manager.cache;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import io.openk9.entity.manager.cache.model.AssociableEntityKey;
import io.openk9.entity.manager.cache.model.DocumentKey;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.cache.model.EntityRelation;
import io.openk9.entity.manager.cache.model.EntityRelationKey;
import io.openk9.entity.manager.cache.model.IngestionEntity;
import io.openk9.entity.manager.cache.model.IngestionKey;

public class EntityManagerDataSerializableFactory
	implements DataSerializableFactory {

	public static final int FACTORY_ID = 1;

	public static final int ENTITY_TYPE = 1;
	public static final int ENTITY_KEY_TYPE = 2;
	public static final int ENTITY_RELATION_TYPE = 3;
	public static final int ENTITY_RELATION_KEY_TYPE = 4;
	public static final int INGESTION_KEY_TYPE = 6;
	public static final int INGESTION_ENTITY_TYPE = 8;
	public static final int ASSOCIABLE_ENTITY_KEY_TYPE = 9;
	public static final int DOCUMENT_KEY_TYPE = 10;

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
			case INGESTION_KEY_TYPE:
				return new IngestionKey();
			case INGESTION_ENTITY_TYPE:
				return new IngestionEntity();
			case ASSOCIABLE_ENTITY_KEY_TYPE:
				return new AssociableEntityKey();
			case DOCUMENT_KEY_TYPE:
				return new DocumentKey();
			default:
				return null;
		}
	}
}
