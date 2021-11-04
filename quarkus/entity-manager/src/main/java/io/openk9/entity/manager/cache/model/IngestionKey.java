package io.openk9.entity.manager.cache.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode
public class IngestionKey implements Serializable {
	private final long entityId;
	private final String ingestionId;
	private final long tenantId;
}
