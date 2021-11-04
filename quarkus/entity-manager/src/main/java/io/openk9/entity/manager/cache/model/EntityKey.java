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
public class EntityKey implements Serializable {
	private final long tenantId;
	private final String name;
	private final String type;
}
