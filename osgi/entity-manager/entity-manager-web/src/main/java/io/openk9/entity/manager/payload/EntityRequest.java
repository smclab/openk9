package io.openk9.entity.manager.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityRequest {
	private String entityName;
	private String entityType;
	private List<RelationRequest> relations;
	private String tmpId;
}
