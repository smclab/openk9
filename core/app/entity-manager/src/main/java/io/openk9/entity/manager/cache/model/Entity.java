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
import lombok.ToString;

import java.io.IOException;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Entity implements IdentifiedDataSerializable, Comparable<Entity> {
	@EqualsAndHashCode.Include
	private String id;
	@EqualsAndHashCode.Include
	private String cacheId;
	@ToString.Include
	private String tenantId;
	@ToString.Include
	private String name;
	@ToString.Include
	private String type;
	@ToString.Include
	private Long graphId;
	@ToString.Include
	private String ingestionId;
	private boolean associated;
	private boolean indexable;
	private List<String> context;
	private String indexName;

	@Override
	public int compareTo(Entity other) {
		Entity p1 = this;
		int res = String.CASE_INSENSITIVE_ORDER.compare(
			p1.getName(), other.getName());
		if (res != 0) {
			return res;
		}
		res = String.CASE_INSENSITIVE_ORDER.compare(
			p1.getType(), other.getType());
		if (res != 0) {
			return res;
		}
		return p1.getTenantId().compareTo(other.getTenantId());
	}

	@Override
	public int getFactoryId() {
		return EntityManagerDataSerializableFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return EntityManagerDataSerializableFactory.ENTITY_TYPE;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeString(id);
		out.writeString(cacheId);
		out.writeString(tenantId);
		out.writeString(name);
		out.writeString(type);
		out.writeObject(graphId);
		out.writeString(ingestionId);
		out.writeBoolean(associated);
		out.writeBoolean(indexable);
		out.writeObject(context);
		out.writeString(indexName);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		id = in.readString();
		cacheId = in.readString();
		tenantId = in.readString();
		name = in.readString();
		type = in.readString();
		graphId = in.readObject();
		ingestionId = in.readString();
		associated = in.readBoolean();
		indexable = in.readBoolean();
		context = in.readObject();
		indexName = in.readString();
	}
}
