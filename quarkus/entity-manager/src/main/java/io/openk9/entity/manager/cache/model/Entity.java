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
	private Long tenantId;
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
		out.writeString(id);
		out.writeString(cacheId);
		out.writeObject(tenantId);
		out.writeString(name);
		out.writeString(type);
		out.writeObject(graphId);
		out.writeString(ingestionId);
		out.writeBoolean(associated);
		out.writeBoolean(indexable);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		id = in.readString();
		cacheId = in.readString();
		tenantId = in.readObject();
		name = in.readString();
		type = in.readString();
		graphId = in.readObject();
		ingestionId = in.readString();
		associated = in.readBoolean();
		indexable = in.readBoolean();
	}
}
