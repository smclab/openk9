package io.openk9.entity.manager.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EntityRequest {
	@EqualsAndHashCode.Include
	private String name;
	@EqualsAndHashCode.Include
	private String type;
	private List<RelationRequest> relations;
	private long tmpId;
}
