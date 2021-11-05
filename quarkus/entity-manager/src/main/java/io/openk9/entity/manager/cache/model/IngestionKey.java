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
public class IngestionKey implements Serializable, PartitionAware<String> {
	private final long entityId;
	private final String ingestionId;
	private final long tenantId;

	@Override
	public String getPartitionKey() {
		return ingestionId;
	}
}
