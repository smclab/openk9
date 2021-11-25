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
public class AssociableEntityKey implements IdentifiedDataSerializable, PartitionAware<Integer> {

	private String cacheId;
	private String ingestionId;

	@Override
	public int getFactoryId() {
		return EntityManagerDataSerializableFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return EntityManagerDataSerializableFactory.ASSOCIABLE_ENTITY_KEY_TYPE;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeString(cacheId);
		out.writeString(ingestionId);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		cacheId = in.readString();
		ingestionId = in.readString();
	}

	@Override
	public Integer getPartitionKey() {
		return Objects.hash(ingestionId);
	}

}
