package io.openk9.entity.manager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class EntityIndex {
	private final long id;
	private final long tenantId;
	private final String name;
	private final String type;
}
