package io.openk9.entity.manager.cache.model;


import com.hazelcast.partition.PartitionAware;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode
public class EntityRelationKey implements Serializable, PartitionAware<Long> {

	private final long entityRelationId;
	private final long entityId;

	@Override
	public Long getPartitionKey() {
		return entityId;
	}
}
