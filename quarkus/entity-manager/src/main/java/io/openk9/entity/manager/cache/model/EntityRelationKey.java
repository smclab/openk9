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
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode
public class EntityRelationKey implements IdentifiedDataSerializable, PartitionAware<Integer> {

	private long entityRelationId;
	private String entityId;
	private String ingestionId;

	@Override
	public Integer getPartitionKey() {
		return Objects.hash(ingestionId);
	}

	@Override
	public int getFactoryId() {
		return EntityManagerDataSerializableFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return EntityManagerDataSerializableFactory.ENTITY_RELATION_KEY_TYPE;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(entityRelationId);
		out.writeString(entityId);
		out.writeString(ingestionId);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		entityRelationId = in.readLong();
		entityId = in.readString();
		ingestionId = in.readString();
	}
}
