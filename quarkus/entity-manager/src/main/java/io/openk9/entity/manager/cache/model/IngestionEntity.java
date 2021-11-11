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
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class IngestionEntity implements IdentifiedDataSerializable, Comparable<IngestionEntity> {
	private String id;
	@EqualsAndHashCode.Include
	private String cacheId;
	private Long tenantId;
	private String name;
	private String type;
	private List<String> context;

	@Override
	public int compareTo(IngestionEntity other) {
		IngestionEntity p1 = this;
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
		return EntityManagerDataSerializableFactory.INGESTION_ENTITY_TYPE;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeString(id);
		out.writeString(cacheId);
		out.writeObject(tenantId);
		out.writeString(name);
		out.writeString(type);
		out.writeObject(context);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		id = in.readString();
		cacheId = in.readString();
		tenantId = in.readObject();
		name = in.readString();
		type = in.readString();
		context = in.readObject();
	}

	public static IngestionEntity fromEntity(
		Entity entity, List<String> context) {
		return new IngestionEntity(
			entity.getId(),
			entity.getCacheId(),
			entity.getTenantId(),
			entity.getName(),
			entity.getType(),
			context
		);
	}

}
