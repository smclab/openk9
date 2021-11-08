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
public class Entity implements IdentifiedDataSerializable, Comparable<Entity> {
	private Long id;
	@EqualsAndHashCode.Include
	private Long cacheId;
	private Long tenantId;
	private String name;
	private String type;

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
		return Long.compare(p1.getTenantId(), other.getTenantId());
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
		out.writeObject(id);
		out.writeObject(cacheId);
		out.writeObject(tenantId);
		out.writeString(name);
		out.writeString(type);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		id = in.readObject();
		cacheId = in.readObject();
		tenantId = in.readObject();
		name = in.readString();
		type = in.readString();
	}
}
