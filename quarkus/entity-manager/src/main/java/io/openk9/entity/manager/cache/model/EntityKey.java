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
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode
public class EntityKey implements IdentifiedDataSerializable {
	private long tenantId;
	private String name;
	private String type;

	@Override
	public int getFactoryId() {
		return EntityManagerDataSerializableFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return EntityManagerDataSerializableFactory.ENTITY_KEY_TYPE;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(tenantId);
		out.writeString(name);
		out.writeString(type);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		tenantId = in.readLong();
		name = in.readString();
		type = in.readString();
	}
}
