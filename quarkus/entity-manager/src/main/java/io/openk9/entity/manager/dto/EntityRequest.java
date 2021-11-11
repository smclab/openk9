package io.openk9.entity.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EntityRequest implements Serializable {
	@EqualsAndHashCode.Include
	private String name;
	@EqualsAndHashCode.Include
	private String type;
	private List<RelationRequest> relations;
	private String tmpId;
	private List<String> context;
}
