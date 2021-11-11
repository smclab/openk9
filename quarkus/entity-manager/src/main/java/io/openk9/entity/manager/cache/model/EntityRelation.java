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
