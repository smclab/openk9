package io.openk9.entity.manager.model.index;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@ToString(onlyExplicitlyIncluded = true)
public class EntityIndex {
	private final String id;
	@ToString.Include
	private final long tenantId;
	@ToString.Include
	private final String name;
	@ToString.Include
	private final String type;
	private float score;
}
