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
public class DocumentKey implements IdentifiedDataSerializable {

	private long datasourceId;
	private String contentId;
	private long tenantId;

	@Override
	public int getFactoryId() {
		return EntityManagerDataSerializableFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return EntityManagerDataSerializableFactory.DOCUMENT_KEY_TYPE;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(datasourceId);
		out.writeString(contentId);
		out.writeLong(tenantId);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		datasourceId = in.readLong();
		contentId = in.readString();
		tenantId = in.readLong();
	}
}
