package io.openk9.entity.manager.model.index;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class EntityIndex {
	@ToString.Include
	private String id;
	@ToString.Include
	private Long graphId;
	@ToString.Include
	private long tenantId;
	@ToString.Include
	private String name;
	@ToString.Include
	private String type;
	private float score;
}
