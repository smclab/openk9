package io.openk9.entity.manager.cache.model;


import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.partition.PartitionAware;
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
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode
public class EntityCacheIdKey
	implements IdentifiedDataSerializable, PartitionAware<String> {
	private long tenantId;
	private long cacheId;
	private String name;
	private String type;

	@Override
	public String getPartitionKey() {
		return String.join("-", Long.toString(tenantId), type, name);
	}

	@Override
	public int getFactoryId() {
		return EntityManagerDataSerializableFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return EntityManagerDataSerializableFactory.ENTITY_CACHE_ID_KEY_TYPE;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(tenantId);
		out.writeLong(cacheId);
		out.writeString(name);
		out.writeString(type);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		tenantId = in.readLong();
		cacheId = in.readLong();
		name = in.readString();
		type = in.readString();
	}

}
