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

package io.openk9.entity.manager.cache.model;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import io.openk9.entity.manager.cache.EntityManagerDataSerializableFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.IOException;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EntityRelation implements IdentifiedDataSerializable {
	@EqualsAndHashCode.Include
	private Long cacheId;
	private String entityCacheId;
	private String ingestionId;
	private String name;
	private String to;

	@Override
	public int getFactoryId() {
		return EntityManagerDataSerializableFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return EntityManagerDataSerializableFactory.ENTITY_RELATION_TYPE;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(cacheId);
		out.writeString(entityCacheId);
		out.writeString(ingestionId);
		out.writeString(name);
		out.writeString(to);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		cacheId = in.readObject();
		entityCacheId = in.readString();
		ingestionId = in.readString();
		name = in.readString();
		to = in.readString();
	}
}
